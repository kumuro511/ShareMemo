package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	public static String getDate() {
		Date dateNow = new Date();
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd(E) HH:mm:ss", Locale.JAPAN);
		
		return format.format(dateNow);
	}
}
