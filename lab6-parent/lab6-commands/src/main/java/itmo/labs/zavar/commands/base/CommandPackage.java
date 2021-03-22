package itmo.labs.zavar.commands.base;

import java.io.Serializable;

public class CommandPackage implements Serializable
{
	private static final long serialVersionUID = -7071028630270434499L;
	
	private String name;
	private Object[] args;
	
	public CommandPackage(String name, Object[] args) {
		this.name = name;
		this.args = args;
	}

	public String getName() {
		return name;
	}

	public Object[] getArgs() {
		return args;
	}
}
