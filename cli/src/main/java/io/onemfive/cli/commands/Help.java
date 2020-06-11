package io.onemfive.cli.commands;

import io.onemfive.cli.CLICommand;
import io.onemfive.cli.CLICommandFactory;
import io.onemfive.cli.CommandException;
import io.onemfive.cli.BaseCommand;

import java.util.List;

public class Help implements CLICommand {

    @Override
    public String execute(List<String> params) throws CommandException {
        StringBuilder sb = new StringBuilder();
        sb.append("Following commands are available:");
        String h;
        for(CLICommand c : CLICommandFactory.getCommands()) {
            if(c instanceof BaseCommand) {
                h = ((BaseCommand)c).getName();
                if (h != null)
                    sb.append("\n\t"+h);
            }
        }
        return sb.toString();
    }

}
