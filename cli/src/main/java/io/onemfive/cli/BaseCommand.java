package io.onemfive.cli;

public abstract class BaseCommand implements CLICommand {

    // TODO: Support wrapping
    protected String help() {
        StringBuilder sb = new StringBuilder();
        sb.append("MAN(1)\t\t\t\tManual pager utils\t\t\t\tMAN(1)");
        sb.append("NAME\n");
        sb.append(getName()+"\n");
        sb.append("SYNOPSIS\n");
        sb.append(getSynopsis()+"\n");
        sb.append("DESCRIPTION\n");
        sb.append(getDescription()+"\n");
        return sb.toString();
    }

    public abstract String getName();
    protected abstract String getSynopsis();
    protected abstract String getDescription();
}
