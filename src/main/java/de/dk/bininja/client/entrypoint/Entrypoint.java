package de.dk.bininja.client.entrypoint;

import de.dk.bininja.client.controller.MasterControlProgram;
import de.dk.bininja.client.core.Processor;

public class Entrypoint {

   public Entrypoint() {

   }

   public static void main(String[] args) {
      MasterControlProgram mcp = new MasterControlProgram();
      Processor processor = new Processor(mcp);
      JavaFXUIAdapter.start(mcp, processor);
   }
}