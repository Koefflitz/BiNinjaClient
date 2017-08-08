package de.dk.bininja.client.ui.view;

import de.dk.bininja.client.model.DownloadMetadata;
import de.dk.bininja.client.ui.UI;
import de.dk.bininja.client.ui.UIController;
import de.dk.bininja.net.Base64Connection;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class GUI extends Application implements UI {
   private final UIController controller;
   private UI view;

   public GUI(UIController controller) {
      this.controller = controller;
   }

   @Override
   public void start(Stage window) {
      this.view = new ClientView(window, controller, Base64Connection.PORT);
      view.start();
   }

   @Override
   public void start() {
      launch(new String[0]);
   }

   @Override
   public void show(String format, Object... args) {
      view.show(format, args);
   }

   @Override
   public void showError(String errorMsg, Object... args) {
      view.showError(errorMsg, args);
   }

   @Override
   public void alert(String format, Object... args) {
      view.alert(format, args);
   }

   @Override
   public void alertError(String errorMsg, Object... args) {
      view.alertError(errorMsg, args);
   }

   @Override
   public void setConnected(boolean connected) {
      view.setConnected(connected);
   }

   @Override
   public void prepareDownload(DownloadMetadata metadata) throws IllegalStateException {
      view.prepareDownload(metadata);
   }

   @Override
   public void setDownloadTargetTo(DownloadMetadata metadata) {
      view.setDownloadTargetTo(metadata);
   }

   @Override
   public void close() {
      view.close();
   }

}
