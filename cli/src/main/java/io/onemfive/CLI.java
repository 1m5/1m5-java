/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.onemfive;

import onemfive.api.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * gRPC client.
 *
 * FIXME We get warning 'DEBUG io.grpc.netty.shaded.io.netty.util.internal.PlatformDependent0 - direct buffer constructor: unavailable
 * java.lang.UnsupportedOperationException: Reflective setAccessible(true) disabled' which is
 * related to Java 10 changes. Requests are working but we should find out why we get that warning
 */
public class CLI {

    private static CLI instance;

    private String host;
    private int port;

    public static void main(String[] args) throws Exception {
        instance = new CLI("localhost", 2017);
        instance.run();
    }

    private CLI(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void run() {
        // TODO validate input
        try (Scanner scanner = new Scanner(System.in);) {
            p("Welcome to 1M5.\n");
            while (true) {
                String[] tokens = scanner.nextLine().split(" ");
                if (tokens.length == 0) {
                    return;
                }
                String command = tokens[0];
                List<String> params = new ArrayList<>();
                if (tokens.length > 1) {
                    params.addAll(Arrays.asList(tokens));
                    params.remove(0);
                }
                String result = "";

                switch (command) {
                    case "start": {
                        pl("Starting...");

                        pl("Running.");
                        break;
                    }
                    case "stop": {
                        pl("Stopping...");

                        break;
                    }
                    default: {
                        p(help());
                    }
                }

                p(result);
            }
        }
    }

    private String help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Start: starts router.\n");
        sb.append("Stop: stops router.\n");
        return sb.toString();
    }

    private void pl(String out) {
        System.out.println(out);
    }

    private void p(String out) {
        System.out.print(out);
    }

    private void shutdown() {
        System.exit(0);
    }
}
