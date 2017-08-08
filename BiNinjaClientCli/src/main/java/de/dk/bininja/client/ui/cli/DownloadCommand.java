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
      if (path == null) {
         System.out.print("Please enter a target path for the download (q to quit): ");
         path = in.readLine();
         if (path.equals("q") || path.equals("quit"))
            return new CliCommandResult(true, null);
      }

      if (StringUtils.isBlank(path))
         return new CliCommandResult(true, "Invalid path: \"" + path + "\"");

      File file = new File(path);
      if (file.isDirectory()) {
         metadata.setTargetDirectory(file);
      } else {
         metadata.setTargetDirectory(file.getParentFile());
         metadata.setFileName(file.getName());
      }

      DownloadCliView downloadView = downloadViewSupplier.get();
      controller.requestDownloadFrom(metadata, downloadView);
      boolean block = parsedArgs.getOptionalValue(OPT_BLOCKING)
                            .map(Boolean::parseBoolean)
                            .orElse(true);

      CliCommandResult result = new CliCommandResult(true, null, block);
      if (block)
         downloadView.setCommandResult(result);

      return result;
   }

   @Override
   public void printUsage() {
      System.out.println("download");
      PARSER.printUsage(System.out);
   }

}
