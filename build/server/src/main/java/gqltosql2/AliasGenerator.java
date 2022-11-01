package gqltosql2;

import java.util.HashSet;
import java.util.Set;

public class AliasGenerator {

	private static char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	private static Set<String> sql_key_words = new HashSet<>();
	static {
		sql_key_words.add("as");
		sql_key_words.add("on");
	}
	
	private int i;

	private String prefix = "";

	private AliasGenerator pre;

	public AliasGenerator() {
	}

	public String next() {
		if (i == chars.length) {
			i = 0;
			if (pre == null) {
				pre = new AliasGenerator();
			}
			prefix = pre.next();
		}
		String n = prefix + chars[i++];
		if(sql_key_words.contains(n)) {
			return next();
		} else {
			return n;
		}
	}
}
