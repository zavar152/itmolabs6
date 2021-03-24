package itmo.labs.zavar.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import itmo.labs.zavar.commands.SaveCommand;
import itmo.labs.zavar.commands.ShowCommand;
import itmo.labs.zavar.commands.ShuffleCommand;
import itmo.labs.zavar.commands.UpdateCommand;
import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Command.ExecutionType;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.csv.CSVManager;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.studygroup.StudyGroup;

public class Server {
	
	private static Stack<StudyGroup> stack = new Stack<StudyGroup>();
	private static HashMap<String, Command> clientsCommandsMap = new HashMap<String, Command>();
	private static HashMap<String, Command> internalCommandsMap = new HashMap<String, Command>();
	
	private static final Logger rootLogger = LogManager.getRootLogger();
	
	public static void main(String[] args) {
		
		if(args.length != 2)
		{
			rootLogger.error("You should enter a path to .csv file and server port!");
			System.exit(0);
		}
		
		Environment[] envs = prepareEnvironments(args);
		Environment clientEnv = envs[0];
		Environment internalEnv = envs[1];
		
		ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
		
		try (AsynchronousServerSocketChannel asyncServerChannel = AsynchronousServerSocketChannel.open()) {
			if (asyncServerChannel.isOpen()) {
				asyncServerChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4096*4);
				asyncServerChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				asyncServerChannel.bind(new InetSocketAddress(Integer.parseInt(args[1])));
				rootLogger.info("Waiting for connections ...");
				
				taskExecutor.submit(() -> {
					Scanner scan = new Scanner(System.in);
					Logger internalClientLogger = LogManager.getLogger("internal");
					while (true) {
						try {
							String input = scan.nextLine();
							input = input.replaceAll(" +", " ").trim();
							internalClientLogger.info(input);
							String command[] = input.split(" ");

							if (command[0].equals("exit")) {
								internalEnv.getCommandsMap().get(command[0]).execute(ExecutionType.INTERNAL_CLIENT, internalEnv, Arrays.copyOfRange(command, 1, command.length), System.in, System.out);
								if(args.length != 0)
									internalEnv.getCommandsMap().get("save").execute(ExecutionType.INTERNAL_CLIENT, internalEnv, new Object[] { args[0] }, System.in, System.out);
								taskExecutor.shutdownNow();
								System.exit(0);
							}
							
							if (internalEnv.getCommandsMap().containsKey(command[0])) {
								try {
									internalEnv.getHistory().addToGlobal(input);
									internalEnv.getCommandsMap().get(command[0]).execute(ExecutionType.INTERNAL_CLIENT, internalEnv, Arrays.copyOfRange(command, 1, command.length), System.in, System.out);
									internalEnv.getHistory().clearTempHistory();
								} catch (CommandException e) {
									rootLogger.error(e.getMessage());
									internalEnv.getHistory().clearTempHistory();
								}
							} else {
								rootLogger.error("Unknown command! Use help.");
							}
						} catch (Exception e) {
							if (!scan.hasNextLine()) {
								rootLogger.warn("Inputing is closed! Server is closing...");
								taskExecutor.shutdownNow();
								scan.close();
								System.exit(0);
							} else {
								rootLogger.error("Unexcepted error!");
							}
						}
					}
				});
				
				while (true) {

					Future<AsynchronousSocketChannel> asynchFuture = asyncServerChannel.accept();

					try {
						final AsynchronousSocketChannel asyncChannel = asynchFuture.get();
						ClientHandler worker = new ClientHandler(asyncChannel, clientEnv, rootLogger);
						taskExecutor.submit(worker);
					} catch (InterruptedException | ExecutionException ex) {
						rootLogger.error(ex);
						rootLogger.error("\n Server is shutting down ...");
						taskExecutor.shutdown();
						while (!taskExecutor.isTerminated());
						break;
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			} else {
				rootLogger.error("The asynchronous server-socket channel cannot be opened!");
			}
		} catch (IOException ex) {
			rootLogger.error(ex);
		}
	}
	
	private static Environment[] prepareEnvironments(String[] args) {

		File file = new File(args[0]);

		try {
			rootLogger.info("Reading file...");
			if (CSVManager.read(file.toPath().toString(), stack, System.out)) {
				rootLogger.info("Collection loaded!");
			}
		} catch (Exception e) {
			rootLogger.error("Unexcepted error during initialization. Server will be closed...");
			System.exit(-1);
		}
		
		HelpCommand.register(clientsCommandsMap);
		ShowCommand.register(clientsCommandsMap);
		ExecuteScriptCommand.register(clientsCommandsMap);
		ClearCommand.register(clientsCommandsMap);
		InfoCommand.register(clientsCommandsMap);
		AddCommand.register(clientsCommandsMap);
		RemoveByIDCommand.register(clientsCommandsMap);
		ShuffleCommand.register(clientsCommandsMap);
		HistoryCommand.register(clientsCommandsMap);
		RemoveAnyBySCCommand.register(clientsCommandsMap);
		AverageOfTSCommand.register(clientsCommandsMap);
		CountGreaterThanTSCommand.register(clientsCommandsMap);
		AddIfMaxCommand.register(clientsCommandsMap);
		AddIfMinCommand.register(clientsCommandsMap);
		UpdateCommand.register(clientsCommandsMap);
		
		internalCommandsMap.putAll(clientsCommandsMap);
		ExitCommand.register(internalCommandsMap);
		SaveCommand.register(internalCommandsMap);
		
		return new Environment[] { new Environment(file, clientsCommandsMap, stack),  new Environment(file, internalCommandsMap, stack)};
	}
}