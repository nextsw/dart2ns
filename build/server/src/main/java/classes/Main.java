package classes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
	public static void main(String[] arg) throws IOException {
		Dart2NSContext context = new Dart2NSContext();
		context.basePath = "/Users/rajesh/Downloads/flutter/packages/flutter/lib/";
		List<TopDecl> parse = TypeParser.parse(context, "widgets.dart");
		System.out.println(parse.size());
	}
	
}
