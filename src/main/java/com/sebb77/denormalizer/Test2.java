package com.sebb77.denormalizer;

import java.io.Serializable;
import java.util.List;

public class Test2 implements Serializable {
	private static final long serialVersionUID = -5339393425301464301L;

	public Test2() {}
	public Test2(int x, String y) {
		this.x = x;
		this.y = y;
		this.z = null;
	}
	public Test2(int x, String y, List<String> z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	private int x;
	private String y;
	private List<String> z;
	
	public int getX() {
		return x;
	}
	public String getY() {
		return y;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(String y) {
		this.y = y;
	}
	public List<String> getZ() {
		return z;
	}
	public void setZ(List<String> z) {
		this.z = z;
	}
}
