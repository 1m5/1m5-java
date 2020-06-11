package io.onemfive.cli.commands;

import io.onemfive.cli.CLI;
import io.onemfive.cli.CLICommand;
import io.onemfive.cli.BaseCommand;

import java.util.List;

public class Quit extends BaseCommand implements CLICommand {

    @Override
    public String getName() {
        return "\t\tquit - Stops server and exits out of the program.";
    }

    @Override
    protected String getSynopsis() {
        return "\t\tquit [help]";
    }

    @Override
    protected String getDescription() {
        return "\t\t";
    }

    @Override
    public String execute(List<String> params) {
        if(params.size() == 1 && "help".equals(params.get(0))) {
            return help();
        }
        CLI.isRunning = false;
        return "Quit client";
    }
}
