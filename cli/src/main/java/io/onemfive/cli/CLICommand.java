package io.onemfive.cli;

import java.util.List;

public interface CLICommand {
    String execute(List<String> params) throws CommandException;
}
