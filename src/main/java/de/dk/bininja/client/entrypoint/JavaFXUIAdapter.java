package de.dk.bininja.client.entrypoint;

import de.dk.bininja.client.controller.MasterControlProgram;
import de.dk.bininja.client.core.Logic;
import de.dk.bininja.client.ui.view.ClientView;
import de.dk.bininja.net.Base64Connection;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class JavaFXUIAdapter extends Application {
   private static MasterControlProgram mcp;
   private static Logic processor;
   private static ParsedArguments args;

   public JavaFXUIAdapter() {

   }

   public static void start(MasterControlProgram mcp, Logic processor, ParsedArguments args) {
      JavaFXUIAdapter.mcp = mcp;
      JavaFXUIAdapter.processor = processor;
      JavaFXUIAdapter.args = args;
      launch(new String[0]);
   }

   @Override
   public void start(Stage window) {
      ClientView view = new ClientView(window, mcp, Base64Connection.PORT);
      mcp.start(processor, view, args);
   }

   @Override
   public void stop() {
      mcp.exit();
   }

}
