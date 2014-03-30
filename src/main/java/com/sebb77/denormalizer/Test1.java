package com.sebb77.denormalizer;

import java.io.Serializable;
import java.util.List;

public class Test1 implements Serializable {
	private static final long serialVersionUID = -5793747568906282473L;

	private int a;
	private String b;
	private Test2 c;
	private List<String> d;
	private List<Test2> e;

	public int getA() {
		return a;
	}
	public String getB() {
		return b;
	}
	public Test2 getC() {
		return c;
	}
	public void setA(int a) {
		this.a = a;
	}
	public void setB(String b) {
		this.b = b;
	}
	public void setC(Test2 c) {
		this.c = c;
	}
	public List<String> getD() {
		return d;
	}
	public void setD(List<String> d) {
		this.d = d;
	}
	public List<Test2> getE() {
		return e;
	}
	public void setE(List<Test2> e) {
		this.e = e;
	}
	
	
}
