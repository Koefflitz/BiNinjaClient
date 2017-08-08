package de.dk.bininja.client.entrypoint;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dk.bininja.InvalidArgumentException;
import de.dk.bininja.client.controller.MasterControlProgram;
import de.dk.bininja.client.core.Logic;
import de.dk.bininja.client.ui.cli.ClientCli;
import de.dk.util.opt.ArgumentModel;
import de.dk.util.opt.ArgumentParser;
import de.dk.util.opt.ArgumentParserBuilder;
import de.dk.util.opt.ex.ArgumentParseException;

/**
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 */
public class Entrypoint {
   private static final Logger LOGGER = LoggerFactory.getLogger(Entrypoint.class);

   public Entrypoint() {

   }

   public static void main(String... args) {
      ParsedArguments parsedArgs;
      try {
         parsedArgs = parseArgs(args);
      } catch (ArgumentParseException e) {
         LOGGER.error("Error parsing args.", e);
         System.exit(1);
         return;
      }
      if (parsedArgs == null)
         return;

      MasterControlProgram mcp = new MasterControlProgram();
      Logic processor = new Logic(mcp);
      if (parsedArgs.isCli())
         mcp.start(processor, new ClientCli(mcp), parsedArgs);
      else
         JavaFXUIAdapter.start(mcp, processor, parsedArgs);
   }

   private static ParsedArguments parseArgs(String... args) throws ArgumentParseException {
      LOGGER.debug("Parsing args");
      ArgumentParserBuilder builder = ArgumentParserBuilder.begin();
      for (Option opt : Option.values())
         opt.build(builder);

      ArgumentParser parser = builder.buildAndGet();
      ArgumentModel result;
      if (parser.isHelp(args)) {
         parser.printUsage(System.out);
         return null;
      }
      try {
         result = parser.parseArguments(args);
      } catch (ArgumentParseException e) {
         System.out.println(e.getMessage());
         parser.printUsage(System.out);
         throw e;
      }
      ParsedArguments parsedArgs = new ParsedArguments();
      result.getOptionalValue(Option.HOST.getKey())
            .ifPresent(parsedArgs::setHost);

      String portOption = result.getOptionValue(Option.PORT.getKey());
      if (portOption != null) {
         int port;
         try {
            port = Integer.parseInt(portOption);
         } catch (NumberFormatException e) {
            throw new InvalidArgumentException("Invalid port: " + portOption);
         }
         parsedArgs.setPort(port);
      }

      parsedArgs.setCli(result.isOptionPresent(Option.HEADLESS.getKey()));
      if (result.isOptionPresent(Option.SCRIPT.getLongKey())) {
         File script = new File(result.getOptionValue(Option.SCRIPT.getLongKey()));
         parsedArgs.setScript(script);
      }
      parsedArgs.setCommand(result.getOptionValue(Option.COMMAND.getLongKey()));
      return parsedArgs;
   }
}