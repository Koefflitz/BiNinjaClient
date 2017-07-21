package de.dk.bininja.client.ui.view;

public interface ClientViewListener extends DownloadViewListener {
   public void connectTo(String host, int port);
}