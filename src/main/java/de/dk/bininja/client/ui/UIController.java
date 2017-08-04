package de.dk.bininja.client.ui;

import de.dk.bininja.client.model.DownloadMetadata;
import de.dk.bininja.net.DownloadListener;
import de.dk.bininja.ui.cli.CliController;

public interface UIController extends CliController {
   public boolean requestDownloadFrom(DownloadMetadata metadata, DownloadListener listener);
   public int activeDownloadCount();
   public String getConnectionAsString();
}