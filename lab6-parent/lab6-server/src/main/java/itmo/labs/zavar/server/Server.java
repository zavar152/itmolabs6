package itmo.labs.zavar.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();

		ServerSocketChannel serverSocket = ServerSocketChannel.open();

		serverSocket.bind(new InetSocketAddress("localhost", 1111));

		serverSocket.configureBlocking(false);
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		ByteBuffer buffer = ByteBuffer.allocate(256);
		ExecutorService executor;
        executor = Executors.newFixedThreadPool(1);
		while (true) {

			// log("i'm a server and i'm waiting for new connection and buffer select...");
			selector.select();

			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();

			int i = 0;

			while (iter.hasNext()) {
				selectedKeys.forEach(c -> {
					System.out.println(c.selector().toString());
				});
				SelectionKey myKey = iter.next();

				if (myKey.isAcceptable()) {
					register(selector, serverSocket);
				}
				if (myKey.isReadable()) {
					System.out.println("readable");
					/*
					 * Thread cli = new Thread(new Handler(myKey, i)); cli.start();
					 */
					//executor.execute(new Handler(myKey, i));
					answerWithEcho(buffer, myKey);
					break;

				}
				iter.remove();
				i++;
			}

		}
	}

	private static void answerWithEcho(ByteBuffer buffer, SelectionKey key) throws IOException {

		new Thread() {
			public void run() {
				synchronized (key) {

					SocketChannel client = (SocketChannel) key.channel();
					try {
						client.read(buffer);
						if (new String(buffer.array()).trim().equals("exit")) {
							client.close();
							System.out.println("Not accepting client messages anymore");
						} else {
							buffer.flip();
							client.write(buffer);
							buffer.clear();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
		SocketChannel client = serverSocket.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);
		log("Connection Accepted: " + client.getLocalAddress() + "\n");
	}

	private static void log(String str) {
		System.out.println(str);
	}

}
