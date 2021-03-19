package itmo.labs.zavar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server2 {
	public static void main(String[] args) {
		ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());// from w w w . j
																										// a v a2 s .com
		try (AsynchronousServerSocketChannel asyncServerChannel = AsynchronousServerSocketChannel.open()) {
			if (asyncServerChannel.isOpen()) {
				asyncServerChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
				asyncServerChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				asyncServerChannel.bind(new InetSocketAddress("127.0.0.1", 1111));
				System.out.println("Waiting for connections ...");

				while (true) {
					Future<AsynchronousSocketChannel> asynchFuture = asyncServerChannel.accept();

					try {
						final AsynchronousSocketChannel asyncChannel = asynchFuture.get();
						Callable<String> worker = new Callable<String>() {

							@Override
							public String call() throws Exception {

								String host = asyncChannel.getRemoteAddress().toString();
								System.out.println("Incoming connection from: " + host);

								final ByteBuffer buffer = ByteBuffer.wrap(new byte[1024]);

								while (asyncChannel.read(buffer).get() != -1) {
									try {
										buffer.flip();

										asyncChannel.write(buffer).get();

										buffer.flip();
										String from = new String(buffer.array());
										if (!from.equals("exit")) {
											System.out.println();
											ByteArrayInputStream stream2 = new ByteArrayInputStream(Base64.getMimeDecoder().decode(buffer.array()));
											ObjectInputStream obj = new ObjectInputStream(stream2);
											Person per = (Person) obj.readObject();
											System.out.println(per.getName());
											obj.close();
											stream2.close();
										}
										if (buffer.hasRemaining()) {
											// buffer.compact();
										} else {
											buffer.clear();
										}
									} catch (Exception e) {
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
				}
			} else {
				System.out.println("The asynchronous server-socket channel cannot be opened!");
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
}