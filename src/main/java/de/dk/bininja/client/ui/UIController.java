package de.dk.bininja.client.ui;

import de.dk.bininja.client.model.DownloadMetadata;
import de.dk.bininja.net.DownloadListener;

public interface UIController {
   public boolean requestDownloadFrom(DownloadMetadata metadata, DownloadListener listener);
   public void connectTo(String host, int port);
   public int activeDownloadCount();
   public String getConnectionAsString();
}