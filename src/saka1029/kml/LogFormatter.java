package saka1029.kml;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		Date now = new Date(record.getMillis());
		String message = String.format(
//				"%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS.%1$tL %2$-4.4s: %3$s%n",
				"%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS %2$-4.4s: %3$s%n",
				now, record.getLevel().toString(), record.getMessage());
		return message;
	}

}
