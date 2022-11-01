package d3e.core;

public class CriteriaException extends RuntimeException {
	public CriteriaException(Throwable t) {
		super(t.getMessage());
	}
}
