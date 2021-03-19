package itmo.labs.zavar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public class Connect {
	private static final int PORT = 1111;

	public static void main(String args[]) throws IOException, InterruptedException {

		Socket socket = new Socket("localhost", PORT);
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		Writer writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
		PrintWriter out = new PrintWriter(writer, true);
		Scanner scan = new Scanner(System.in);
		Person person = new Person("Chel", 69);
		String to = "";
		while (!to.equals("exit")) {
			to = scan.nextLine();
			if (to.equals("s")) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				ObjectOutputStream ser = new ObjectOutputStream(stream);
				ser.writeObject(person);
				String str = Base64.getEncoder().encodeToString(stream.toByteArray());
				System.out.println(str);
				out.println(str);
			} else {
				out.println(to);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
			String line;
			System.out.println(": " + in.readLine());

			System.out.println("next");
		}
		socket.close();
		System.out.println("Closed");
	}
}