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
 * Outputs all elements of the collection in a string representation to the
 * standard output stream. Doesn't require any arguments.
 * 
 * @author Zavar
 * @version 1.3
 */
public class ShowCommand extends Command {

	private ShowCommand() {
		super("show");
	}
	
	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream) throws CommandException {
		PrintStream pr = ((PrintStream) outStream);
		if (args instanceof String[] && args.length > 0 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
		} else {
			super.args = args;
			if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
				if (env.getCollection().isEmpty()) {
					throw new CommandRunningException("Collection is empty!");
				}

				env.getCollection().stream().forEachOrdered((sg) -> {
					pr.println("ID: " + sg.getId());
					pr.println("Name: " + sg.getName());
					pr.println("Coordinte X: " + sg.getCoordinates().getX());
					pr.println("Coordinte Y: " + sg.getCoordinates().getY());
					pr.println("Creation date: " + sg.getCreationLocalDate());
					pr.println("Students count: " + sg.getStudentsCount());
					pr.println("Expelled students: " + sg.getExpelledStudents());
					pr.println("Transferred students: " + sg.getTransferredStudents());
					pr.println("Form of Education: " + sg.getFormOfEducation());
					if (sg.getGroupAdmin() != null) {
						pr.println("Admin's name: " + sg.getGroupAdmin().getName());
						pr.println("Admin's passport ID: " + sg.getGroupAdmin().getPassportID());
						pr.println("Admin's eye color: " + sg.getGroupAdmin().getEyeColor());
						pr.println("Admin's hair color: " + sg.getGroupAdmin().getHairColor());
						if (sg.getGroupAdmin().getNationality() != null) {
							pr.println("Admin's nationality: " + sg.getGroupAdmin().getNationality());
						}
						pr.println("Admin's location X: " + sg.getGroupAdmin().getLocation().getX());
						pr.println("Admin's location Y: " + sg.getGroupAdmin().getLocation().getY());
						pr.println("Admin's location Z: " + sg.getGroupAdmin().getLocation().getZ());
						pr.println("Admin's location name: " + sg.getGroupAdmin().getLocation().getName());
						pr.println();
					} else {
						pr.println();
					}
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
		ShowCommand command = new ShowCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command shows all elements of collection!";
	}
}
