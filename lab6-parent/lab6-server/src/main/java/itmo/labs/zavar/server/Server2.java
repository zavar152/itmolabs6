package itmo.labs.zavar.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.ArrayUtils;

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
import itmo.labs.zavar.commands.base.CommandPackage;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.csv.CSVManager;
import itmo.labs.zavar.io.ByteBufferBackedOutputStream;
import itmo.labs.zavar.studygroup.StudyGroup;

public class Server2 {
	
	private static Stack<StudyGroup> stack = new Stack<StudyGroup>();
	private static HashMap<String, Command> commandsMap = new HashMap<String, Command>();
	
	public static void main(String[] args) {
		
		if (args.length != 1) {
			args = new String[] { "" };
			System.out.println("You should enter a path to .csv file! Collection will be empty!");
		}

		File file = new File(args[0]);

		try {
			System.out.println("Reading file...");
			if (CSVManager.read(file.toPath().toString(), stack, System.out)) {
				System.out.println("Collection loaded!");
			}
		} catch (Exception e) {
			System.out.println("Unexcepted error during initialization. Program will be closed...");
			System.exit(-1);
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
		
		Environment env = new Environment(file, commandsMap, stack);
		
		ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());// from w w w . j
																										// a v a2 s .com
		try (AsynchronousServerSocketChannel asyncServerChannel = AsynchronousServerSocketChannel.open()) {
			if (asyncServerChannel.isOpen()) {
				asyncServerChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4096*4);
				asyncServerChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				asyncServerChannel.bind(new InetSocketAddress("127.0.0.1", 1111));
				System.out.println("Waiting for connections ...");
				
				taskExecutor.submit(() -> {
					Scanner scan = new Scanner(System.in);
					while (scan.hasNextLine()) {
						if (scan.hasNext()) {
							System.out.println(scan.nextLine());
						}
					}
					if (!scan.hasNextLine()) {
						System.err.println("Server is shutting down ...");
						taskExecutor.shutdownNow();
						while (!taskExecutor.isTerminated()) {

						}
						System.exit(-1);
					}
				});
				
				while (true) {
					
					Future<AsynchronousSocketChannel> asynchFuture = asyncServerChannel.accept();

					try {
						final AsynchronousSocketChannel asyncChannel = asynchFuture.get();
						Callable<String> worker = new Callable<String>() {

							@Override
							public String call() throws Exception {

								String host = asyncChannel.getRemoteAddress().toString();
								System.out.println("Incoming connection from: " + host);

								final ByteBuffer buffer = ByteBuffer.wrap(new byte[4096*4]);
								final ByteBuffer outBuffer = ByteBuffer.wrap(new byte[4096*4]);
								
								while (asyncChannel.read(buffer).get() != -1) {
									try {
										//buffer.flip();
										//String from = new String(buffer.array());
										//System.out.println(from);
										ByteArrayInputStream stream2 = new ByteArrayInputStream(Base64.getMimeDecoder().decode(buffer.array()));
										ObjectInputStream obj = new ObjectInputStream(stream2);
										CommandPackage per = (CommandPackage) obj.readObject();
										System.out.println(host + ": " + per.getName());
										//System.out.println(per.getArgs());
										env.getHistory(host).addToGlobal(per.getName() + " " + Arrays.toString(per.getArgs()));
										commandsMap.get(per.getName()).execute(ExecutionType.SERVER, env, ArrayUtils.addAll(per.getArgs(), new Object[]{host}), System.in, new PrintStream(new ByteBufferBackedOutputStream(outBuffer)));
										env.getHistory(host).clearTempHistory();
										//System.out.println(new String(outBuffer.array()));
										obj.close();
										stream2.close();
										ByteArrayOutputStream stream = new ByteArrayOutputStream();
										ObjectOutputStream ser = new ObjectOutputStream(stream);
										ser.writeObject(new String(outBuffer.array()));
										String str = Base64.getMimeEncoder().encodeToString(stream.toByteArray());
										//System.out.println(str);
										ser.close();
										stream.close();
										
										asyncChannel.write(ByteBuffer.wrap(str.getBytes())).get();
										
										outBuffer.flip();
										outBuffer.put(new byte[outBuffer.remaining()]);
										outBuffer.clear();
										buffer.flip();
										buffer.put(new byte[buffer.remaining()]);
										buffer.clear();
										
									} catch (Exception e) {
										env.getHistory(host).clearTempHistory();
										e.printStackTrace();
									}
								}

								asyncChannel.close();
								System.out.println(host + " was successfully served!");
								return host;
							}
						};
						taskExecutor.submit(worker);
					} catch (InterruptedException | ExecutionException ex) {
						System.err.println(ex);
						System.err.println("\n Server is shutting down ...");
						taskExecutor.shutdown();
						while (!taskExecutor.isTerminated()) {

						}
						break;
					}
					catch (Exception e)
					{
						e.printStackTrace();
						System.exit(-1);
					}
				}
			} else {
				System.out.println("The asynchronous server-socket channel cannot be opened!");
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
}