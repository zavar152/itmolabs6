package itmo.labs.zavar.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.NoSuchElementException;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandRunningException;

/**
 * Deletes an item from the collection by its id. Requires ID.
 * 
 * @author Zavar
 * @version 1.3
 */
public class RemoveByIDCommand extends Command {

	private RemoveByIDCommand() {
		super("remove_by_id", "id");
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream)
			throws CommandException {
		if (args instanceof String[] && args.length != 1 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command requires id of element only!\n" + getUsage());
		} else {
			super.args = args;
			int id;
			try {
				id = Integer.parseInt((String) args[0]);
			} catch (NumberFormatException e) {
				throw new CommandArgumentException("ID must be a number!\n" + getUsage());
			} catch (Exception e) {
				throw new CommandRunningException("Unexcepted error! " + e.getMessage());
			}

			if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
				if (env.getCollection().isEmpty()) {
					throw new CommandRunningException("Collection is empty!");
				}
				try {
					env.getCollection().remove(env.getCollection().stream().filter((p) -> p.getId() == id).findFirst().orElseThrow(NoSuchElementException::new));
					((PrintStream) outStream).println("Element deleted!");
				} catch (NoSuchElementException e) {
					((PrintStream) outStream).println("No such element!");
				} catch (Exception e) {
					throw new CommandRunningException("Unexcepted error! " + e.getMessage());
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
		RemoveByIDCommand command = new RemoveByIDCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command removes one element from collection by ID!";
	}

}
