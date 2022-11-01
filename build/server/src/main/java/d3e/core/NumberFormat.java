package d3e.core;

import java.text.DecimalFormat;

public class NumberFormat extends DecimalFormat {

	private static final long serialVersionUID = 1L;

	public NumberFormat() {
	}

	public NumberFormat(String pattern, String local) {
		super(pattern);
	}
}
