package de.dk.bininja.client.ui.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import de.dk.bininja.client.model.DownloadMetadata;
import de.dk.bininja.client.ui.UI;
import de.dk.bininja.client.ui.UIController;
import de.dk.bininja.ui.cli.Cli;
import de.dk.bininja.ui.cli.CliCommand;
import de.dk.bininja.ui.cli.ConnectCommand;
import de.dk.util.StringUtils;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class ClientCli extends Cli<UIController> implements UI {
   private static final String PROMPT_NOT_CONNECTED = "BiNinja (n.c.)>";
   private static final String PROMPT_CONNECTED = "BiNinjaClient>";

   private final Collection<? extends CliCommand<? super UIController>> commands_connected
      = Arrays.asList(new DownloadCommand(this::newDownload, in));

   private final Collection<? extends CliCommand<? super UIController>> commands_offline
      = Arrays.asList(new ConnectCommand());

   private DownloadCliViewManager downloads = new DownloadCliViewManager();

   {
      commands.add(new ExitCommand(in));
      commands.addAll(commands_offline);
   }

   public ClientCli(UIController controller, BufferedReader in) {
      super(controller, getCommands(), in, PROMPT_CONNECTED, PROMPT_NOT_CONNECTED);
   }

   public ClientCli(UIController controller) {
      super(controller, getCommands(), PROMPT_CONNECTED, PROMPT_NOT_CONNECTED);
   }

   private static Collection<CliCommand<? super UIController>> getCommands() {
      return new LinkedList<>();
   }

   @Override
   public void prepareDownload(DownloadMetadata metadata) throws IllegalStateException {

   }

   private DownloadCliView newDownload() {
      DownloadCliView listener = new DownloadCliView();
      downloads.add(listener);
      return listener;
   }

   @Override
   public void setDownloadTargetTo(DownloadMetadata metadata) {
      if (metadata.getFileName() == null) {
         System.out.println("The url doesn't provide information about the filename.");
         if (metadata.getTargetDirectory() != null) {
            String prompt = String.format("Please enter a filename for the download file to place at \"%s\": ",
                                          metadata.getTargetDirectory());
            String input = prompt(prompt, true);
            if (StringUtils.isBlank(input))
               return;

            metadata.setFileName(input);
         } else {
            String input = prompt("Please enter a path for the download file", true);
            if (StringUtils.isBlank(input))
               return;

            File file = new File(input);
            metadata.setTargetDirectory(file.getParentFile());
            metadata.setFileName(file.getName());
         }
      } else {
         String prompt = "Please enter the target directory to place the download file " + metadata.getFileName();
         String input = prompt(prompt, true);
         if (StringUtils.isBlank(input))
            return;

         File file = new File(input);
         if (!file.isDirectory()) {
            System.out.println("The file " + file.getAbsolutePath() + " is not a directory.");
            input = prompt("Use file " + file.getAbsolutePath() + " as download target file? (y/n): ", false);
            if (input == null)
               return;

            if (input.equals("y") || input.equals("yes")) {
               metadata.setTargetDirectory(file.getParentFile());
               metadata.setFileName(file.getName());
            } else {
               setDownloadTargetTo(metadata);
            }
         }
      }
   }

   @Override
   public String prompt(String msg, boolean qToQuit) {
      String input;
      try {
         input = super.prompt(msg, qToQuit);
         return input;
      } catch (IOException | InterruptedException e) {
         return null;
      }
   }

   @Override
   public void connected() {
      System.err.println("connected");
      commands.removeAll(commands_offline);
      commands.addAll(commands_connected);
      super.connected();
   }

   @Override
   protected void disconnected() {
      System.err.println("disconnected");
      commands.removeAll(commands_connected);
      commands.addAll(commands_offline);
      super.disconnected();
   }

   @Override
   public void alert(String format, Object... args) {
      show(format, args);
   }

   @Override
   public void alertError(String errorMsg, Object... args) {
      showError(errorMsg, args);
   }
}
