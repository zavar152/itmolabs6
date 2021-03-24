package itmo.labs.zavar.client;

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
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import itmo.labs.zavar.commands.AddCommand;
import itmo.labs.zavar.commands.AddIfMaxCommand;
import itmo.labs.zavar.commands.AddIfMinCommand;
import itmo.labs.zavar.commands.AverageOfTSCommand;
import itmo.labs.zavar.commands.ClearCommand;
import itmo.labs.zavar.commands.CountGreaterThanTSCommand;
import itmo.labs.zavar.commands.ExecuteScriptCommand;
import itmo.labs.zavar.commands.ExitCommand;
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

public class Client {
	
	private static HashMap<String, Command> commandsMap = new HashMap<String, Command>();


	public static void main(String args[]) throws IOException, InterruptedException {
		
		if(args.length != 2) {
			System.out.println("You should enter ip and port!");
			System.exit(0);
		}
		
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
		ExitCommand.register(commandsMap);
		
		Environment env = new Environment(commandsMap);
		
		System.out.println("Connecting to the server...");
		boolean connected = false;
		Socket socket = null;
		while(!connected) {
			try {
				socket = new Socket(args[0], Integer.parseInt(args[1]));
				connected = true;
			} catch (ConnectException e1) {
				Thread.sleep(2000);
			} catch (UnknownHostException e) {
				System.out.println("Unknown host");
				System.exit(0);
			} catch (Exception e) {
				System.out.println("Error during connection");
				System.exit(0);
			}
		}
		System.out.println("Connected!");
		
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		Writer writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
		PrintWriter out = new PrintWriter(writer, true);
		Scanner in = new Scanner(System.in);
		ReadableByteChannel channel = Channels.newChannel(is);
		ByteBuffer buf = ByteBuffer.allocateDirect(4096*4);
		String input = "";
		while (true) {

			try {
				input = in.nextLine();
				input = input.replaceAll(" +", " ").trim();
				String command[] = input.split(" ");

				if (command[0].equals("exit")) {
					commandsMap.get(command[0]).execute(ExecutionType.CLIENT, env, Arrays.copyOfRange(command, 1, command.length), System.in, System.out);
					break;
				}

				if (env.getCommandsMap().containsKey(command[0])) {
					try {
						env.getHistory().addToGlobal(input);
						env.getCommandsMap().get(command[0]).execute(ExecutionType.CLIENT, env, Arrays.copyOfRange(command, 1, command.length), System.in, System.out);
						env.getHistory().clearTempHistory();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						ObjectOutputStream ser = new ObjectOutputStream(stream);
						ser.writeObject(env.getCommandsMap().get(command[0]).getPackage());
						String str = Base64.getMimeEncoder().encodeToString(stream.toByteArray());
						out.println(str);
						ser.close();
						stream.close();

						buf.rewind();
						int bytesRead = channel.read(buf);
						buf.rewind();
						byte[] b = new byte[bytesRead];
						for (int i = 0; i < bytesRead; i++) {
							b[i] = buf.get();
						}
						ByteArrayInputStream stream2 = new ByteArrayInputStream(Base64.getMimeDecoder().decode(b));
						ObjectInputStream obj = new ObjectInputStream(stream2);
						String per = (String) obj.readObject();
						System.out.println(per);
						buf.flip();
						buf.put(new byte[buf.remaining()]);
						buf.clear();
					} catch (CommandException e) {
						env.getHistory().clearTempHistory();
						System.err.println(e.getMessage());
					}
				} else {
					System.err.println("Unknown command! Use help.");
				}
			} catch (SocketException | NegativeArraySizeException e) {
				System.out.println("Server is unavailable!\nWaiting for connection...");
				connected = false;
				
				while(!connected) {
					try {
						socket = new Socket(args[0], Integer.parseInt(args[1]));
						connected = true;
					} catch (ConnectException e1) {
						Thread.sleep(2000);
					} catch (Exception e1) {
						System.out.println("Error during connection");
						System.exit(0);
					}
				}
				
				is = socket.getInputStream();
				os = socket.getOutputStream();
				writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
				out = new PrintWriter(writer, true);
				channel = Channels.newChannel(is);
				
				System.out.println("Connected!");
				
			} catch (Exception e) {
				if (!in.hasNextLine()) {
					System.out.println("Inputing is closed! Client is closing...");
					break;
				} else {
					e.printStackTrace();
					System.out.println("Unexcepted error!");
					break;
				}
			}
		}
		socket.close();
		in.close();
	}
	
}