package classes;

public class Main {
	public static void main(String[] arg) {
		Dart2NSContext context = new Dart2NSContext();
		context.start("/Users/rajesh/Downloads/flutter/packages/flutter/lib/", "widgets.dart");
		new DynamicGen().gen(context, "/Users/rajesh/dev/ns/");
		//FileUtils.deleteFolder("/Users/rajesh/dev/ns/");
		System.out.println();
	}
	
}
