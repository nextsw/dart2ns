package classes;

public class Main {
	public static void main(String[] args) {
		String flutterHome = args[0];
		String outDir = args[1];
		String monitorDir = null;
		if(args.length > 2) {
			monitorDir = args[2];
		}
		Dart2NSContext context = new Dart2NSContext(flutterHome);
		context.start("packages/flutter/lib/widgets");
		Gen gen = new CppGen();
//		Gen gen = new DynamicGen(monitorDir);
		gen.gen(context, outDir);
		System.out.println();
	}
	
}
