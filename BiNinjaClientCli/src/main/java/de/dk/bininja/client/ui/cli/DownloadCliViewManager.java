package de.dk.bininja.client.ui.cli;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class DownloadCliViewManager {
   private final Collection<DownloadCliView> views = new LinkedList<>();

   public DownloadCliViewManager() {

   }

   public void waitFor() throws InterruptedException {
      synchronized (views) {
         for (DownloadCliView view : views)
            view.waitFor();
      }
   }

   public boolean add(DownloadCliView view) {
      view.setManager(this);
      synchronized (views) {
         return views.add(view);
      }
   }

   public void remove(DownloadCliView view) {
      synchronized (views) {
         views.remove(view);
      }
   }

   public int size() {
      synchronized (views) {
         return views.size();
      }
   }

   public boolean isEmpty() {
      synchronized (views) {
         return views.isEmpty();
      }
   }

}
