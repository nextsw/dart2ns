package d3e.core;

public class FieldValidation {
	public static boolean isEmail(String email) {
		String p = "^(([^<>\\(\\)\\[\\]\\\\.,;:\\s@\\\"]+(\\.[^<>\\(\\)\\[\\]\\\\.,;:\\s@\\\"]+)*)|(\\\".+\\\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		
		RegExp regExp = new RegExp(p, true, false);

		return regExp.hasMatch(email);
	}
}
