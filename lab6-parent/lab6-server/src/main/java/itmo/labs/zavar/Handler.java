package itmo.labs.zavar;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Handler implements Runnable {

	private SelectionKey key;
	private ByteBuffer buffer;
	private int i;

	public Handler(SelectionKey key, int i) {
		this.key = key;
		this.buffer = ByteBuffer.allocate(256);
		this.i = i;
	}

	@Override
	public void run() {
		synchronized (key) {
			try {
				SocketChannel client = (SocketChannel) key.channel();
				String addrString = client.getLocalAddress().toString();
				// System.out.println("work" + i);
				client.read(buffer);

				// doing some stuff
				if (new String(buffer.array()).trim().contains("exit")) {
					client.close();
					System.out.println("Not accepting " + addrString + " messages anymore");
				} else {
					buffer.flip();
					client.write(buffer);
					buffer.clear();
					buffer.flip();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
