package itmo.labs.zavar.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

import itmo.labs.zavar.commands.base.CommandPackage;

public class ClientReader {
	
	public static CommandPackage read(ByteBuffer buffer) throws IOException, ClassNotFoundException {
		ByteArrayInputStream stream2 = new ByteArrayInputStream(Base64.getMimeDecoder().decode(buffer.array()));
		ObjectInputStream obj = new ObjectInputStream(stream2);
		CommandPackage comPack = (CommandPackage) obj.readObject();
		obj.close();
		stream2.close();
		return comPack;
	}
}
