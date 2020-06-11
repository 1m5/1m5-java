package io.onemfive.cli;

import io.onemfive.util.Config;

import java.util.*;
import java.util.logging.Logger;

/**
 * 1M5 Command Line Interface
 */
public class CLI {

    private static Logger LOG = Logger.getLogger(CLI.class.getName());

    private static CLI instance;
    public static boolean isRunning = true;

    public static  Properties config;
    private String host;
    private int port;

    public static void main(String[] args) throws Exception {
        config = new Properties();
        try {
            config.putAll(Config.loadFromClasspath("1m5-cli.config", null, false));
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
        }
        instance = new CLI();
        instance.run(args);
    }

    private CLI() {

    }

    private void run(String[] args) {
        System.out.println("Welcome to 1M5 CLI v"+config.getProperty("1m5.version"));
        try (Scanner scanner = new Scanner(System.in);) {
            while (isRunning) {
                String[] tokens = scanner.nextLine().split(" ");
                if (tokens.length == 0) {
                    return;
                }
                long start = System.currentTimeMillis();
                String command = tokens[0];
                List<String> params = new ArrayList<>();
                if (tokens.length > 1) {
                    params.addAll(Arrays.asList(tokens));
                    params.remove(0);
                }
                String result = "";
                CLICommand cmd = null;
                try {
                    cmd = CLICommandFactory.getCommand(command);
                    result = cmd.execute(params);
                } catch (CommandException e) {
                    result = e.getLocalizedMessage();
                }
                LOG.info("Request took " + (System.currentTimeMillis() - start)+ " ms");
                System.out.println(result);
            }
        }
    }
}
