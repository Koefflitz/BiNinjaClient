package de.dk.bininja.client.entrypoint;

import de.dk.bininja.client.controller.MasterControlProgram;
import de.dk.bininja.client.core.Logic;

public class Entrypoint {

   public Entrypoint() {

   }

   public static void main(String[] args) {
      MasterControlProgram mcp = new MasterControlProgram();
      Logic processor = new Logic(mcp);
      JavaFXUIAdapter.start(mcp, processor);
   }
}