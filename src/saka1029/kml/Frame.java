package saka1029.kml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Frame {

	private static final String HTML_NAME = "index.html";
	private static final String FRAME_TEXT =
		"<html>\n" +
		"<head>\n" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=EUC-JP\">\n" +
		"<title>GPS Log</title>\n" +
		"</head>\n" +
		"    <frameset cols=\"300,*\">\n" +
		"        <frame src=\"left.html\" name=\"left\">\n" +
		"        <frame src=\"http://maps.google.co.jp/maps?" +
			"output=embed&q=http%3a%2f%2foutdoor%2egeocities%2ejp%2fsaka1029s%2f20091223%2d175142%2ekml\" name=\"right\">\n" +
		"    </frameset>\n" +
		"</html>\n";

	public void writeTo(File dir, String baseUrl, String enc) throws UnsupportedEncodingException, FileNotFoundException {
		File out = new File(dir, HTML_NAME);
		PrintWriter writer = new PrintWriter(
			new OutputStreamWriter(new FileOutputStream(out), enc));
		try {
			writer.print(FRAME_TEXT);
		} finally {
			writer.close();
		}
	}
}
