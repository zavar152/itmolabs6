package itmo.labs.zavar.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import itmo.labs.zavar.commands.base.Command.ExecutionType;
import itmo.labs.zavar.commands.base.CommandPackage;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.exception.CommandException;

public class ClientCommandExecutor {

	public static ByteBuffer executeCommand(CommandPackage per, Environment clientEnv) throws IOException {
		ByteArrayOutputStream outCom = new ByteArrayOutputStream();
		ByteBuffer outBuffer;
		try {
			clientEnv.getCommandsMap().get(per.getName()).execute(ExecutionType.SERVER, clientEnv, per.getArgs(), System.in, new PrintStream(outCom));
			outBuffer = ByteBuffer.wrap(outCom.toByteArray());
		} catch (CommandException e) {
			outBuffer = ByteBuffer.wrap(e.getMessage().getBytes());
		}
		outCom.close();
		return outBuffer;
	}
	
}
