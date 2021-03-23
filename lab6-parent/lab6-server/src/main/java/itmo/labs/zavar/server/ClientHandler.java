package itmo.labs.zavar.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;

import itmo.labs.zavar.commands.base.CommandPackage;
import itmo.labs.zavar.commands.base.Environment;

public class ClientHandler implements Callable<String> {

	private AsynchronousSocketChannel asyncChannel;
	private Environment clientEnv;
	private Logger logger;

	public ClientHandler(AsynchronousSocketChannel asyncChannel, Environment clientEnv, Logger logger) {
		this.asyncChannel = asyncChannel;
		this.clientEnv = clientEnv;
		this.logger = logger;
	}

	@Override
	public String call() throws Exception {
		String host = asyncChannel.getRemoteAddress().toString();
		logger.info("Incoming connection from: " + host.replace("/", ""));

		final ByteBuffer buffer = ByteBuffer.wrap(new byte[4096 * 4]);

		while (asyncChannel.read(buffer).get() != -1) {
			try {

				CommandPackage per = ClientReader.read(buffer);
				logger.info("Command from " + host.replace("/", "") + ": " + per.getName());
				
				ByteBuffer outBuffer = ClientCommandExecutor.executeCommand(per, clientEnv);

				ClientWriter.write(asyncChannel, outBuffer);
				
				logger.info("Send command's output to " + host.replace("/", ""));
				
				buffer.flip();
				buffer.put(new byte[buffer.remaining()]);
				buffer.clear();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		asyncChannel.close();
		logger.info("Client " + host.replace("/", "") + " was successfully served");
		return host;
	}
}
