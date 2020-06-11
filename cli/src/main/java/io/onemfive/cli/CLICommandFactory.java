package io.onemfive.cli;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CLICommandFactory {

    private static Map<String, CLICommand> commands = new HashMap<>();

    static {
        Class[] commandClasses = new Class[0];
        try {
            commandClasses = getClasses("io.onemfive.cli.commands");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Class c : commandClasses) {
            try {
                Object obj = c.getConstructor().newInstance();
                CLICommand command;
                if(obj instanceof CLICommand) {
                    command = (CLICommand)obj;
                    commands.put(command.getClass().getSimpleName(), command);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public static CLICommand getCommand(String name) throws CommandException {
        if(name==null) throw new CommandException("Command name is required.");
        if(name.length() < 2) throw new CommandException("Command unsupported: "+name);
        String cmdName = name.substring(0,1).toUpperCase() + name.substring(1);
        if(commands.containsKey(cmdName)) {
            return commands.get(cmdName);
        } else {
            throw new CommandException(name+" is unknown command.");
        }
    }

    public static List<CLICommand> getCommands() {
        List<CLICommand> cmds = commands.values().stream().collect(Collectors.toList());
        Collections.sort(cmds, Comparator.comparing(c -> c.getClass().getSimpleName()));
        return cmds;
    }

    public static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    public static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
