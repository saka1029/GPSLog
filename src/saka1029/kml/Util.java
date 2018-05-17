package saka1029.kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Util {

	private static final String DISPLAY_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";
	private static final String DISPLAY_DATE_SHORT_FORMAT = "yyyy/MM/dd HH:mm:ss";
	private static final String KML_DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy";

	static {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        try {
            LogManager.getLogManager().readConfiguration();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
	}

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

	public static String format(Date date) {
		DateFormat df = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
		return df.format(date);
	}

	public static String formatShort(Date date) {
		DateFormat df = new SimpleDateFormat(DISPLAY_DATE_SHORT_FORMAT);
		return df.format(date);
	}

	public static Date parseKmlDate(String date) throws ParseException {
		DateFormat df = new SimpleDateFormat(KML_DATE_FORMAT, Locale.US);
		return df.parse(date);
	}

	public static String relativePath(File base, File child) throws IOException {
		String basePath = base.getCanonicalPath();
		String childPath = child.getCanonicalPath();
		if (!childPath.startsWith(basePath))
			throw new IllegalArgumentException("base");
		return childPath.substring(basePath.length() + 1);
	}

	public static File dstFile(File src, File dst, File file) throws IOException {
		String rel = relativePath(src, file);
		return new File(dst, rel);
	}

	public static PrintWriter newWriter(File file, String outputEncoding) throws IOException {
		File parent = file.getParentFile();
		if (!parent.exists()) parent.mkdirs();
		PrintWriter writer = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(file), outputEncoding));
		return writer;
	}
}
