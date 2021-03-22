package itmo.labs.zavar.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;

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
		if (args instanceof String[] && args.length > 0 && type.equals(ExecutionType.CLIENT)) {
			throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
		} else {
			super.args = args;
			if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT)) {
				((PrintStream) outStream).println("-------");
				env.getHistory((String) args[0]).getGlobalHistory().stream().forEachOrdered(((PrintStream) outStream)::println);
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