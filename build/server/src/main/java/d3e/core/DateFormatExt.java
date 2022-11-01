package d3e.core;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class DateFormatExt{

	public static DateTimeFormatter DateFormat(String pattern, String local){
    	return DateTimeFormatter.ofPattern(pattern);
    }
    public static DateTimeFormatter ofPattern(String pattern){
    	return DateTimeFormatter.ofPattern(pattern);
    }
    public static String format(LocalDateTime date) {
    	return format(date);
    }
}