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
 * Displays help for available commands. Doesn't require any arguments.
 * 
 * @author Zavar
 * @version 1.1
 */
public class HelpCommand extends Command {

	private HelpCommand() {
		super("help");
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream)
			throws CommandException {
		if (args instanceof String[] && args.length > 0 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
		} else {
			super.args = args;
			if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
				env.getCommandsMap().forEach((k, v) -> {
					((PrintStream) outStream).println(
							"<" + env.getCommandsMap().get(k).getName() + ">\n" + env.getCommandsMap().get(k).getHelp()
									+ "\n" + env.getCommandsMap().get(k).getUsage() + "\n");
				});
			}
		}
	}

	/**
	 * Uses for commands registration.
	 * 
	 * @param commandsMap Commands' map.
	 */
	public static void register(HashMap<String, Command> commandsMap) {
		HelpCommand command = new HelpCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command can give you information about other commands!";
	}

}
