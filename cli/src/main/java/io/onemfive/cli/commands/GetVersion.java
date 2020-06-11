package io.onemfive.cli.commands;

import io.onemfive.cli.CLI;
import io.onemfive.cli.CLICommand;
import io.onemfive.cli.BaseCommand;

import java.util.List;

public class GetVersion extends BaseCommand implements CLICommand {

    @Override
    public String getName() {
        return "\t\tgetVersion - Returns current version of CLI.";
    }

    @Override
    protected String getSynopsis() {
        return "\t\tgetVersion [help]";
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
        return CLI.config.getProperty("1m5.version");
    }
}
