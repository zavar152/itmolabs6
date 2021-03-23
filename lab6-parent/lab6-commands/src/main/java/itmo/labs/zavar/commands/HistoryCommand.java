package itmo.labs.zavar.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandRunningException;

/**
 * Ouputs history of commands.
 * 
 * @author Zavar
 *
 */
public class HistoryCommand extends Command {

	private HistoryCommand() {
		super("history", "count");
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream) throws CommandException {
		if(type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT)) {
			if (args instanceof String[] && (args.length > 2 || args.length < 0) && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT)) ) {
				throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
			} else if (args.length == 0) {
				super.args = args;
				((PrintStream) outStream).println("-------");
				env.getHistory().getGlobalHistory().stream().forEachOrdered(((PrintStream) outStream)::println);
			} else {
				super.args = args;
				if (Integer.parseInt((String) args[0]) <= 0) {
					throw new CommandArgumentException("Argument should be greater than 0!");
				}
				if (env.getHistory().getGlobalHistory().size() - Integer.parseInt((String) args[0]) < 0) {
					throw new CommandRunningException("There are only " + env.getHistory().getGlobalHistory().size() + " commands in history!");
				} else {
					((PrintStream) outStream).println("-------");
					env.getHistory().getGlobalHistory().stream().skip(env.getHistory().getGlobalHistory().size() - Integer.parseInt((String) args[0])).forEachOrdered(((PrintStream) outStream)::println);
				}
			}
		}
	}

	/**
	 * Uses for commands registration.
	 * 
	 * @param commandsMap Commands' map.
	 */
	public static void register(HashMap<String, Command> commandsMap) {
		HistoryCommand command = new HistoryCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command shows history of commands!";
	}
}
