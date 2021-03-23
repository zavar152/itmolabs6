package itmo.labs.zavar.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Callable;

import itmo.labs.zavar.commands.base.CommandPackage;
import itmo.labs.zavar.commands.base.Environment;

public class ClientHandler implements Callable<String> {

	private AsynchronousSocketChannel asyncChannel;
	private Environment clientEnv;

	public ClientHandler(AsynchronousSocketChannel asyncChannel, Environment clientEnv) {
		this.asyncChannel = asyncChannel;
		this.clientEnv = clientEnv;
	}

	@Override
	public String call() throws Exception {
		String host = asyncChannel.getRemoteAddress().toString();
		System.out.println("Incoming connection from: " + host);

		final ByteBuffer buffer = ByteBuffer.wrap(new byte[4096 * 4]);

		while (asyncChannel.read(buffer).get() != -1) {
			try {

				CommandPackage per = ClientReader.read(buffer);
				System.out.println(host + ": " + per.getName());
				
				ByteBuffer outBuffer = ClientCommandExecutor.executeCommand(per, clientEnv);

				ClientWriter.write(asyncChannel, outBuffer);

				buffer.flip();
				buffer.put(new byte[buffer.remaining()]);
				buffer.clear();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		asyncChannel.close();
		System.out.println(host + " was successfully served!");
		return host;
	}
}
