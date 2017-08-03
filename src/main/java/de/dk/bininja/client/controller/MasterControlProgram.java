package de.dk.bininja.client.controller;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dk.bininja.client.core.Logic;
import de.dk.bininja.client.model.DownloadMetadata;
import de.dk.bininja.client.net.ClientDownload;
import de.dk.bininja.client.ui.UI;
import de.dk.bininja.client.ui.UIController;
import de.dk.bininja.net.Base64Connection;
import de.dk.bininja.net.ConnectionType;
import de.dk.bininja.net.DownloadListener;
import de.dk.bininja.net.packet.download.DownloadPacket;
import de.dk.util.channel.Channel;
import de.dk.util.channel.ChannelDeclinedException;
import de.dk.util.channel.ChannelManager;
import de.dk.util.net.ConnectionListener;
import de.dk.util.net.ReadingException;

public class MasterControlProgram implements ProcessorController,
                                             UIController,
                                             ConnectionListener {
   private static final Logger LOGGER = LoggerFactory.getLogger(MasterControlProgram.class);

   private static final long CONNECTION_CLOSE_TIMEOUT = 8000;

   private Logic processor;
   private UI ui;

   private Base64Connection connection;
   private ChannelManager channelManager;

   private boolean stopping = false;

   public MasterControlProgram() {

   }

   public void start(Logic processor, UI ui) {
      this.processor = processor;
      this.ui = ui;
      ui.start();
   }

   @Override
   public void connectTo(String host, int port) {
      if (connection != null && connection.isRunning()) {
         processor.cancelDownloads();
         try {
            connection.close(CONNECTION_CLOSE_TIMEOUT);
         } catch (IOException | InterruptedException e) {
            LOGGER.warn("Error closing the connection to " + connection.getInetAddress(), e);
         }
      }

      LOGGER.info("Establishing connection to \"" + host + "\".");
      try {
         this.connection = new Base64Connection(host, port);
         LOGGER.debug("Sending initial message, to tell the server, that I am a download client.");
         connection.sendRaw(ConnectionType.CLIENT.getString());
         connection.addListener(this);
         connection.start();
         this.channelManager = connection.attachChannelManager();
         LOGGER.info("Connection with " + host + " established");
         ui.connected();
         ui.show("Verbindung zu " + host + " hergestellt");
      } catch (IOException e) {
         LOGGER.error("Connecting to \"" + host + "\" failed", e);
         ui.showError("Verbinden fehlgeschlagen.\n" + e.getMessage());
      }
   }

   @Override
   public boolean requestDownloadFrom(DownloadMetadata metadata, DownloadListener listener) {
      Channel<DownloadPacket> downloadChannel = null;
      ClientDownload download;
      LOGGER.debug("Establishing new channel for download: " + metadata);
      try {
         downloadChannel = channelManager.establishNewChannel(DownloadPacket.class, CONNECTION_CLOSE_TIMEOUT);
         LOGGER.debug("Download channel with id " + downloadChannel.getId() + " established.");
         download = processor.requestDownloadFrom(metadata, downloadChannel);
      } catch (IOException | ChannelDeclinedException | InterruptedException | TimeoutException e) {
         String errorMsg = "Error initializing download: " + metadata;
         LOGGER.error(errorMsg, e);
         ui.showError("Downloadanfrage fehlgeschlagen.\n%s", e.getMessage());
         if (downloadChannel != null)
            close(downloadChannel);
         return false;
      }
      if (download == null)
         return false;

      ui.prepareDownload(metadata);
      download.addListener(listener);
      try {
         processor.startDownload(downloadChannel, download);
         return true;
      } catch (IOException e) {
         String msg = "Error starting the download " + metadata;
         LOGGER.debug(msg, e);
         ui.showError("Fehler beim Starten des Downloads vom Server");
         close(downloadChannel);
         return false;
      }
   }

   @Override
   public int activeDownloadCount() {
      return processor.activeDownloadCount();
   }

   @Override
   public String getConnectionAsString() {
      return connection.getInetAddress()
                       .toString();
   }

   @Override
   public void setDownloadTargetTo(DownloadMetadata metadata) {
      ui.setDownloadTargetTo(metadata);
   }

   @Override
   public void readingError(ReadingException e) {
      LOGGER.warn(e.getMessage(), e);
      ui.alert("Hackerwarnung!"
               + "Eine unbekannte Nachricht ist soeben eingegangen!"
               + "Versucht da jemand Nachrichten in diese Anwendung einzuschleusen?");
   }

   private void close(Channel<?> channel) {
      try {
         channel.close();
      } catch (IOException e) {
         LOGGER.warn("Error closing the channel: " + channel, e);
      }
   }

   @Override
   public void closed() {
      if (stopping)
         return;

      LOGGER.debug("Connection to server " + connection.getInetAddress() + " closed.");
      ui.alert("Verbindung zum Server verloren.");
      ui.disconnected();
   }

   public void stop() {
      LOGGER.info("Freeing resources before terminating");
      stopping  = true;

      processor.close();
      ui.close();

      boolean closeNecessary = connection != null
                               && connection.getSocket().isConnected()
                               && !connection.getSocket().isClosed();
      if (closeNecessary) {
         try {
            LOGGER.debug("Closing connection to " + connection.getInetAddress());
            connection.close(CONNECTION_CLOSE_TIMEOUT);
         } catch (IOException | InterruptedException e) {
            LOGGER.warn("Error closing the connection " + connection.getInetAddress(), e);
         }
      }

      LOGGER.info("BiNinjaClient out.");
   }

}