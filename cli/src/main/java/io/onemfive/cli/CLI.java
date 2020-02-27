/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
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
