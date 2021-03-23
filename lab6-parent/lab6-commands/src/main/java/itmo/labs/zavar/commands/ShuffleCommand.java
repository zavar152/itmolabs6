package itmo.labs.zavar.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandRunningException;

/**
 * Shuffles the elements of the collection in a random order. Doesn't require
 * any arguments.
 * 
 * @author Zavar
 * @version 1.1
 */
public class ShuffleCommand extends Command {

	private ShuffleCommand() {
		super("shuffle");
	}
	
	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream) throws CommandException {
		if (args instanceof String[] && args.length > 0 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
		} else {
			super.args = args;
			if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
				if (env.getCollection().isEmpty()) {
					throw new CommandRunningException("Collection is empty!");
				}

				Collections.shuffle(env.getCollection());
				((PrintStream) outStream).println("Collection mixed up!");
			}
		}
	}

	/**
	 * Uses for commands registration.
	 * 
	 * @param commandsMap Commands' map.
	 */
	public static void register(HashMap<String, Command> commandsMap) {
		ShuffleCommand command = new ShuffleCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command shuffles the elements in collection!";
	}

}
