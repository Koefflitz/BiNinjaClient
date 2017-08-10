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
import de.dk.bininja.ui.cli.CliCommand;
import de.dk.bininja.ui.cli.CliCommandResult;
import de.dk.util.StringUtils;
import de.dk.util.opt.ArgumentModel;
import de.dk.util.opt.ArgumentParser;
import de.dk.util.opt.ArgumentParserBuilder;
import de.dk.util.opt.ex.ArgumentParseException;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class DownloadCommand extends CliCommand<UIController> {
   private static final String NAME = "download";
   private static final String ARG_URL = "URL";
   private static final String ARG_PATH = "path";
   private static final String OPT_BLOCKING = "blocking";

   private static final ArgumentParser PARSER = buildParser();

   private final Supplier<DownloadCliView> downloadViewSupplier;
   private final BufferedReader in;

   public DownloadCommand(Supplier<DownloadCliView> downloadViewSupplier, BufferedReader in) {
      super(NAME);
      this.downloadViewSupplier = Objects.requireNonNull(downloadViewSupplier);
      this.in = in;
   }

   private static ArgumentParser buildParser() {
      return ArgumentParserBuilder.begin()
                                  .addArgument(ARG_URL, "The url to download from.")
                                  .addArgument(ARG_PATH, false, "The target path of the download.")
                                  .buildOption(OPT_BLOCKING, "blocking")
                                     .setDescription("If set to true the program will block"
                                                     + "until the operation is finished."
                                                     + "Otherwise the operation will run in background."
                                                     + "Default value is false")
                                     .setExpectsValue(true)
                                     .build()
                                  .buildAndGet();
   }

   @Override
   protected CliCommandResult execute(String input, UIController controller) throws IOException,
                                                                                    InterruptedException {
      String[] args = input.split("\\s+");
      ArgumentModel parsedArgs;
      try {
         parsedArgs = PARSER.parseArguments(1, args.length - 1, args);
      } catch (ArgumentParseException e) {
         return new CliCommandResult(false, e.getMessage());
      }

      String urlString = parsedArgs.getArgumentValue(ARG_URL);
      URL url;
      try {
         url = new URL(urlString);
      } catch (MalformedURLException e) {
         return new CliCommandResult(false, "Invalid url: \"" + urlString + "\"\n" + e.getMessage());
      }
      DownloadMetadata metadata = new DownloadMetadata(url);
      String path = parsedArgs.getArgumentValue(ARG_PATH);
      File file;
      if (path != null) {
         file = new File(path);
         if (!file.isDirectory()) {
            File parent = file.getParentFile();
            if (parent == null)
               return new CliCommandResult(false, "Invalid download target path: \"" + path + "\"");
            else if (!parent.exists())
               return new CliCommandResult(false, "Could not find the parent directory of the download target " + path);
         }
      } else {
         file = promptTarget();
      }

      if (file == null)
         return new CliCommandResult(true, null);

      if (file.isDirectory()) {
         metadata.setTargetDirectory(file);
      } else {
         metadata.setTargetDirectory(file.getParentFile());
         metadata.setFileName(file.getName());
      }

      DownloadCliView downloadView = downloadViewSupplier.get();
      boolean block;
      try {
         block = parseBlocking(parsedArgs.getOptionValue(OPT_BLOCKING));
      } catch (IllegalArgumentException e) {
         return new CliCommandResult(false, e.getMessage());
      }

      CliCommandResult result = new CliCommandResult(true, null, block);
      if (block)
         downloadView.setCommandResult(result);

      boolean success = controller.requestDownloadFrom(metadata, downloadView);
      return success ? result : new CliCommandResult(false, null);
   }

   private File promptTarget() throws IOException {
      System.out.print("Please enter a target path for the download (q to quit): ");
      String path = in.readLine();
      if (path.equals("q") || path.equals("quit"))
         return null;

      if (StringUtils.isBlank(path))
         return promptTarget();

      File file = new File(path);
      if (!file.isDirectory()) {
         File parent = file.getParentFile();
         if (parent == null)
            System.out.println("Invalid path \"" + path + "\"");
         else if (!parent.exists())
            System.out.println("Parent directory " + file.getParentFile().getAbsolutePath() + " not found.");
         else
            return file;

         return promptTarget();
      }
      return file;
   }

   private boolean parseBlocking(String input) throws IllegalArgumentException {
      if (input == null)
         return true;

      if (input.equals("true"))
         return true;
      else if (input.equals("false"))
         return false;

      String msg = String.format("Optionvalue for option \"%s\" must be one of \"%s\" or \"%s\". Was \"%s\"",
                                 OPT_BLOCKING,
                                 "true",
                                 "false");

      throw new IllegalArgumentException(msg);
   }

   @Override
   public void printUsage() {
      System.out.println("download");
      PARSER.printUsage(System.out);
   }

}
