package de.dk.bininja.client.entrypoint;

import de.dk.bininja.client.controller.MasterControlProgram;
import de.dk.bininja.client.core.Logic;
import de.dk.bininja.client.ui.view.ClientView;
import de.dk.bininja.net.Base64Connection;
import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXUIAdapter extends Application {
   private static MasterControlProgram mcp;
   private static Logic processor;

   public JavaFXUIAdapter() {

   }

   public static void start(MasterControlProgram mcp, Logic processor) {
      JavaFXUIAdapter.mcp = mcp;
      JavaFXUIAdapter.processor = processor;
      launch(new String[0]);
   }

   @Override
   public void start(Stage window) {
      ClientView view = new ClientView(window, mcp, Base64Connection.PORT);
      mcp.start(processor, view);
   }

   @Override
   public void stop() {
      mcp.exit();
   }

}