package itmo.labs.zavar;

import java.io.Serializable;

public class Person implements Serializable {

	private static final long serialVersionUID = -5721614165432423622L;
	private String name;
	private int age;

	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public int getAge() {
		return age;
	}

	public String getName() {
		return name;
	}

}
