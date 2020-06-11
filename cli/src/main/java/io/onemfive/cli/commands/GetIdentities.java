package io.onemfive.cli.commands;

import io.onemfive.cli.CommandException;
import io.onemfive.cli.BaseCommand;

import java.util.List;

public class GetIdentities extends BaseCommand {

    @Override
    public String getName() {
        return "\t\tgetIdentities - Returns list of end users DIDs.";
    }

    @Override
    protected String getSynopsis() {
        return "\t\tgetIdentities [help]";
    }

    @Override
    protected String getDescription() {
        return "\t\t";
    }

    @Override
    public String execute(List<String> params) throws CommandException {
        return null;
    }
}
