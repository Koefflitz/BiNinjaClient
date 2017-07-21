package de.dk.bininja.client.ui.view;

import java.net.URL;

public interface DownloadViewListener {
   public boolean requestDownloadFrom(URL url, DownloadView view);
}