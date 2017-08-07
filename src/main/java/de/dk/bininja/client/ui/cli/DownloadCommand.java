package de.dk.bininja.client.ui.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Supplier;

import de.dk.bininja.client.model.DownloadMetadata;
import de.dk.bininja.client.ui.UIController;
import de.dk.bininja.net.DownloadListener;
import de.dk.bininja.ui.cli.CliCommand;
import de.dk.bininja.ui.cli.CliCommandResult;
import de.dk.util.StringUtils;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class DownloadCommand extends CliCommand<UIController> {
   private static final String NAME = "download";
   private static final String REGEX = "^" + NAME + "\\ \\S+(\\ \\S+)?$";

   private final Supplier<DownloadListener> listenerSupplier;
   private final BufferedReader in;

   public DownloadCommand(Supplier<DownloadListener> listenerSupplier, BufferedReader in) {
      super(NAME, REGEX);
      this.listenerSupplier = Objects.requireNonNull(listenerSupplier);
      this.in = in;
   }

   @Override
   protected CliCommandResult checkedExecute(String input, UIController controller) throws IOException,
                                                                                           InterruptedException {
      String[] tokens = input.split(" ");
      String urlString = tokens[1];
      URL url;
      try {
         url = new URL(urlString);
      } catch (MalformedURLException e) {
         return new CliCommandResult(false, "Invalid url: \"" + urlString + "\"\n" + e.getMessage());
      }
      DownloadMetadata metadata = new DownloadMetadata(url);
      String path;
      if (tokens.length <= 2) {
         System.out.print("Please enter a target path for the download (q to quit): ");
         path = in.readLine();
         if (path.equals("q") || path.equals("quit"))
            return new CliCommandResult(false, null);
      } else {
         path = tokens[2];
      }

      if (StringUtils.isBlank(path))
         return new CliCommandResult(false, "Invalid path: \"" + path + "\"");

      File file = new File(path);
      if (file.isDirectory()) {
         metadata.setTargetDirectory(file);
      } else {
         metadata.setTargetDirectory(file.getParentFile());
         metadata.setFileName(file.getName());
      }
      boolean worked = controller.requestDownloadFrom(metadata, listenerSupplier.get());
      return new CliCommandResult(worked, worked ? "Download initiated." : null);
   }

   @Override
   public void printUsage() {
      System.out.println(name + " <url> [<path>]");
      System.out.println("Downloads the File from the specified url to the given path.");
   }

}
