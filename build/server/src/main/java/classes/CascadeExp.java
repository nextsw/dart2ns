package classes;

import java.util.List;

import d3e.core.ListExt;

public class CascadeExp extends Statement {
	Expression on;
	List<MethodCall> calls = ListExt.asList();
	
	public CascadeExp(Expression on) {
		this.on = on;
	}
}
