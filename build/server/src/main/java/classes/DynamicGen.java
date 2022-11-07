package classes;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public class DynamicGen implements Gen{
	
	Class<Gen> genImpl;
	private Dart2NSContext context;
	private String path;

	@Override
	public void gen(Dart2NSContext context, String path) {
		this.context = context;
		this.path = path;
		try {
			genImpl = (Class<Gen>) Class.forName("classes.CppGen");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		generate();
		monitor();
	}

	private void monitor() {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path dir = Path.of("/Users/rajesh/dev/dart2ns/build/server/build/classes/java/main/classes");
			dir.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			WatchKey key;
			for(;;) {
				// wait for key to be signaled
			    key = watcher.poll(1, TimeUnit.SECONDS);
			    if(key == null) {
			    	continue;
			    }

			    for (WatchEvent<?> event: key.pollEvents()) {
			        WatchEvent.Kind<?> kind = event.kind();
			        System.out.println("File modified");
			        if (kind == StandardWatchEventKinds.OVERFLOW) {
			            continue;
			        }
			        WatchEvent<Path> ev = (WatchEvent<Path>)event;
			        Path filename = ev.context();
			        if(filename.endsWith("CppGen.class")) {
			        	loadClass(dir.getParent());
			        	generate();
			        }
			    }
			    key.reset();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		} catch (InterruptedException x) {
			return;
		}
	}

	private void loadClass(Path p) {
		try {
			URL url =p.toUri().toURL();
			System.out.println(url);
			ClassLoader current = getClass().getClassLoader();
			ClassLoader urlClassLoader = new URLClassLoader(new URL[] {p.toUri().toURL()}, new ClassLoader() {
				@Override
				public Class<?> loadClass(String name) throws ClassNotFoundException {
					if(name.equals("classes.CppGen")) {
						return null;
					}
					return super.loadClass(name);
				}
				@Override
				protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
					if(name.equals("classes.CppGen")) {
						return null;
					}
					return super.loadClass(name, resolve);
				}
			});
			genImpl = (Class<Gen>) urlClassLoader.loadClass("classes.CppGen");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generate() {
		System.out.println("Generating again");
		FileUtils.deleteFolder("/Users/rajesh/dev/ns/");
		try {
			Gen ins = genImpl.getConstructor().newInstance();
			ins.gen(context, path);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
