package de.dk.bininja.client.ui.cli;

import java.io.BufferedReader;
import java.io.IOException;

import de.dk.bininja.client.ui.UIController;
import de.dk.bininja.ui.cli.CliCommand;
import de.dk.bininja.ui.cli.CliCommandResult;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class ExitCommand extends CliCommand<UIController> {
   private static final String NAME = "exit";

   private BufferedReader in;

   public ExitCommand(BufferedReader in) {
      super(NAME);
      this.in = in;
   }

   @Override
   protected CliCommandResult execute(String input, UIController controller) throws IOException,
                                                                                    InterruptedException {
      if (controller.activeDownloadCount() > 0) {
         if (!promptReally(controller))
            return new CliCommandResult(false, "");
      }
      controller.exit();
      return new CliCommandResult(true, "Exiting BiNinjaClient");
   }

   private boolean promptReally(UIController controller) {
      System.out.println("There are unfinished downloads!");
      System.out.println("Do you want to wait for them to finish? (y/n/c) ");
      String input;
      try {
         input = in.readLine();
      } catch (IOException e) {
         input = null;
      }
      if (input != null) {
         if (input.equals("y") || input.equals("yes")) {
            try {
               controller.waitForDownloads();
            } catch (InterruptedException e) {
               System.err.println("Interrupted while waiting for the downloads to finish.");
            }
         } else if (input.equals("c") || input.equals("cancel")) {
            return false;
         }
      }
      return true;
   }

   @Override
   public void printUsage() {
      System.out.println("exit");
      System.out.println("Exit the program");
   }

}
