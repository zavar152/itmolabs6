package itmo.labs.zavar.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import itmo.labs.zavar.commands.AddCommand;
import itmo.labs.zavar.commands.AddIfMaxCommand;
import itmo.labs.zavar.commands.AddIfMinCommand;
import itmo.labs.zavar.commands.AverageOfTSCommand;
import itmo.labs.zavar.commands.ClearCommand;
import itmo.labs.zavar.commands.CountGreaterThanTSCommand;
import itmo.labs.zavar.commands.ExecuteScriptCommand;
import itmo.labs.zavar.commands.HelpCommand;
import itmo.labs.zavar.commands.HistoryCommand;
import itmo.labs.zavar.commands.InfoCommand;
import itmo.labs.zavar.commands.RemoveAnyBySCCommand;
import itmo.labs.zavar.commands.RemoveByIDCommand;
import itmo.labs.zavar.commands.ShowCommand;
import itmo.labs.zavar.commands.ShuffleCommand;
import itmo.labs.zavar.commands.UpdateCommand;
import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Command.ExecutionType;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.studygroup.StudyGroup;

public class Connect {
	private static final int PORT = 1111;
	
	private static Stack<StudyGroup> stack = new Stack<StudyGroup>();
	private static HashMap<String, Command> commandsMap = new HashMap<String, Command>();


	public static void main(String args[]) throws IOException, InterruptedException {
		
		HelpCommand.register(commandsMap);
		ShowCommand.register(commandsMap);
		ExecuteScriptCommand.register(commandsMap);
		ClearCommand.register(commandsMap);
		InfoCommand.register(commandsMap);
		AddCommand.register(commandsMap);
		RemoveByIDCommand.register(commandsMap);
		ShuffleCommand.register(commandsMap);
		HistoryCommand.register(commandsMap);
		RemoveAnyBySCCommand.register(commandsMap);
		AverageOfTSCommand.register(commandsMap);
		CountGreaterThanTSCommand.register(commandsMap);
		AddIfMaxCommand.register(commandsMap);
		AddIfMinCommand.register(commandsMap);
		UpdateCommand.register(commandsMap);
		
		Environment env = new Environment(commandsMap);
		
		Socket socket = new Socket("localhost", PORT);
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		Writer writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
		PrintWriter out = new PrintWriter(writer, true);
		Scanner in = new Scanner(System.in);
		ReadableByteChannel channel = Channels.newChannel(is);
		ByteBuffer buf = ByteBuffer.allocateDirect(Integer.MAX_VALUE);
		String input = "";
		while (true) {

			try {
				input = in.nextLine();
				input = input.replaceAll(" +", " ").trim();
				String command[] = input.split(" ");

				if (command[0].equals("exit")) {
					break;
				}

				if (env.getCommandsMap().containsKey(command[0])) {
					try {
						env.getCommandsMap().get(command[0]).execute(ExecutionType.CLIENT, env, Arrays.copyOfRange(command, 1, command.length), System.in, System.out);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						ObjectOutputStream ser = new ObjectOutputStream(stream);
						ser.writeObject(env.getCommandsMap().get(command[0]).getPackage());
						String str = Base64.getMimeEncoder().encodeToString(stream.toByteArray());
						System.out.println(str);
						out.println(str);
						ser.close();
						stream.close();

					} catch (CommandException e) {
						System.err.println(e.getMessage());
					}

					buf.rewind();
					int bytesRead = channel.read(buf);
					//System.out.println(bytesRead);
					buf.rewind();
					byte[] b = new byte[bytesRead];
					for (int i = 0; i < bytesRead; i++) {
						b[i] = buf.get();
						//System.out.println(i + " Byte read: " + b);
					}
					ByteArrayInputStream stream2 = new ByteArrayInputStream(Base64.getMimeDecoder().decode(b));
					ObjectInputStream obj = new ObjectInputStream(stream2);
					String per = (String) obj.readObject();
					System.out.println(per);
					//System.out.println("next");
					buf.flip();
					buf.put(new byte[buf.remaining()]);
					buf.clear();
				} else {
					System.err.println("Unknown command! Use help.");
				}
			} catch (Exception e) {
				if (!in.hasNextLine()) {
					System.out.println("Inputing is closed! Program is closing...");
					System.exit(0);
				} else {
					e.printStackTrace();
					System.out.println("Unexcepted error!");
				}
			}
		}
		socket.close();
		in.close();
		System.out.println("Closed");
	}
	
}