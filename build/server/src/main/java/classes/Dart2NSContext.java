package classes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

import d3e.core.ListExt;
import d3e.core.StringExt;

public class Dart2NSContext {
	Stack<String> stack = new Stack<>();
	String basePath;
	List<String> filesLoaded = ListExt.asList();
	public String path2Content(String path) {
		System.out.println("Reading from path : " + path);
	    if (StringExt.startsWith(path, "package:", 0l)) {
	    	System.err.println("Need to load package: " + path);
	    } else if (StringExt.startsWith(path, "dart:", 0l)) {
	    	System.err.println("Need to load package: " + path);
	    } else {
	    	try {
	    		Path path2 = Path.of(basePath, path);
	    		if(filesLoaded.contains(path2.toString())) {
	    			return "";
	    		}
	    		
	    		String content = new String(Files.readAllBytes(path2), Charset.defaultCharset());
	    		filesLoaded.add(path2.toString());
	    		return content;
	    	} catch (IOException e) {
				System.err.println("Unable to read from file: " + path);
			}
	    }
	    return "";
	}
	public void push(String path) {
		Path path2 = Path.of(basePath, path);
		stack.push(basePath);
		this.basePath = path2.getParent().toString();
	}
	public void pop() {
		String pop = stack.pop();
		this.basePath = pop;
	};
}
