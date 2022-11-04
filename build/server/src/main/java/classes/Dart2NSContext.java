package classes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import d3e.core.ListExt;
import d3e.core.StringExt;
import io.jsonwebtoken.lang.Collections;

public class Dart2NSContext {
	Stack<String> stack = new Stack<>();
	static Map<String, String> packages = preparePackages();
	static String pkgBase = "/Users/rajesh/Downloads/flutter/.pub-cache/hosted/pub.dartlang.org/";
	String basePath;
	List<String> filesLoaded = ListExt.asList();
	public String path2Content(String path) {
		Path path2 = null;
	    if (StringExt.startsWith(path, "package:", 0l)) {
	    	String[] split = path.split(":");
	    	String[] pathItems = split[1].split("/");
	    	String[] pathItems2 = new String[pathItems.length - 1];
	    	System.arraycopy(pathItems, 1, pathItems2, 0, pathItems.length-1);
	    	String newBase = pkgBase + pathItems[0] + "-" + packages.get(pathItems[0]) + "/lib/";
	    	if(pathItems[0].equals("flutter")) {
	    		newBase = "/Users/rajesh/Downloads/flutter/packages/flutter/lib/";
	    	}
	    	String sub = newBase + String.join("/", pathItems2);
	    	path2 = Path.of(sub);
	    } else if (StringExt.startsWith(path, "dart:", 0l)) {
	    	String[] split = path.split(":");
	    	String base = "/Users/rajesh/Downloads/flutter/bin/cache/pkg/sky_engine/lib/";
	    	path2 = Path.of(base, split[1] + "/" + split[1] + ".dart");
	    } else {
	    	path2 = Path.of(basePath, path);
	    }
	    try {
	    	if(filesLoaded.contains(path2.toString())) {
	    		return "";
	    	}
	    	String content = new String(Files.readAllBytes(path2), Charset.defaultCharset());
	    	System.out.println("Reading from path : " + path2);
	    	filesLoaded.add(path2.toString());
	    	push(path2.getParent().toString());
	    	return content;
	    } catch (IOException e) {
	    	System.err.println("Unable to read from file: " + path);
	    }
	    return "";
	}
	private static Map<String, String> preparePackages() {
		HashMap<String,String> pkgs = new HashMap<>();
		pkgs.put("characters", "1.2.1");
		pkgs.put("collection", "1.16.0");
		  pkgs.put("material_color_utilities", "0.1.5");
		  pkgs.put("meta", "1.8.0");
		  pkgs.put("vector_math", "2.1.2");
		return pkgs;
	}
	public void push(String path) {
		stack.push(basePath);
		this.basePath = path;
	}
	public void pop() {
		String pop = stack.pop();
		this.basePath = pop;
	};
}
