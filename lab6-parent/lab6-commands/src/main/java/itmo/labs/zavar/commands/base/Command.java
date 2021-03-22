package itmo.labs.zavar.commands.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import itmo.labs.zavar.exception.CommandException;

/**
 * This class uses to create new commands for your system.
 * 
 * @author Zavar
 * @version 1.3
 * 
 */
public abstract class Command {
	
	private String name;
	private String[] requiredArgs;
	protected Object[] args;

	/**
	 * Creates a new command. In inheritors it recommended to make a <b>private
	 * constructor</b> and use super to invoke parent-constructor. Command shouldn't
	 * have any public constructors. Every command should have a <b>register()</b>
	 * method which adds it to commands' map.
	 * 
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * <p>
	 * In main class:
	 * </p>
	 * <p>
	 * {@code
	 * HashMap<String, Command> commandsMap = new HashMap<String, Command>();
	 * TestCommand.register(commandsMap);}
	 * </p>
	 * <p>
	 * In TestCommand class:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * public static void register(HashMap<String, Command> commandsMap)
	{
	command = new TestCommand();
	commandsMap.put(command.getName(), command);
	}}
	 * </pre>
	 * 
	 * @param name command's name
	 * @param args command's arguments (uses for showing help)
	 */
	public Command(String name, String... requiredArgs) {
		this.name = name;
		this.requiredArgs = requiredArgs;
	}

	/**
	 * 
	 * This method contains main logic of command. This method should be invoked by
	 * system to execute command. Don't forget to check invalid arguments when you
	 * overrided this method. You can use streams to read/write not only from/to
	 * console.
	 * 
	 * @param env       {@link Environment} class, which contains commands' map,
	 *                  collection etc.
	 * @param args      command's arguments.
	 * @param inStream  input stream to read from it.
	 * @param outStream output stream to write in it.
	 * @throws CommandException if something goes wrong while command is executing.
	 * 
	 * @see Environment
	 */
	public abstract void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream)
			throws CommandException;

	public final CommandPackage getPackage() {
		return new CommandPackage(getName(), args);
	}
	
	public final Object[] getArgs() {
		return args;
	}
	
	/**
	 * Returns string with helpful information about command.
	 * 
	 * @return {@link String}
	 */
	public abstract String getHelp();

	/**
	 * 
	 * Returns string with usage in format: "Usage: " + name + " " +
	 * {@link #getArgsAsString()}.
	 * 
	 * @return {@link String}
	 * 
	 * @see #getArgsAsString()
	 */
	public final String getUsage() {
		return "Usage: " + name + " " + getRequiredArgsAsString();
	}

	/**
	 * Returns array of command's arguments.
	 * 
	 * @return {@link String}
	 */
	public final String[] getRequiredArgs() {
		return requiredArgs;
	}

	/**
	 * Returns string of arguments in format: "[arg1] [arg2] ..."
	 * 
	 * @return {@link String}
	 */
	public final String getRequiredArgsAsString() {
		if (requiredArgs.length > 0) {
			return Arrays.toString(requiredArgs).replace(", ", "] [");
		} else {
			return "";
		}
	}

	/**
	 * Returns name of command.
	 * 
	 * @return {@link String}
	 */
	public final String getName() {
		return name;
	}

	/**
	 * This method should be overrided id command requires additional user input
	 * (requires {ELEMENT}). Default - false.
	 * 
	 * @return Returns true if commands requires additional user input.
	 */
	public boolean isNeedInput() {
		return false;
	}

	/**
	 * This method returns name mappings for input. Must be overrided if command has
	 * additional input.
	 * 
	 * @param type Type od order.
	 * @return Array with order.
	 */
	public String[] getInputOrder(int type) {
		return new String[] {};
	}
	
	public static enum ExecutionType
	{
		CLIENT,
		SERVER,
		SCRIPT,
		INTERNAL_CLIENT
	}
}
