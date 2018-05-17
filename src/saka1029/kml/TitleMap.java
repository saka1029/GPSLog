package saka1029.kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TitleMap {

	private static final String ENC = "MS932";
	private static final String HTML_NAME = "left.html";
	private static final String INDEX_PAGE_HEADER =
		"<html>\n" +
		"<head>\n" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=EUC-JP\">\n" +
		"<title>GPS Log</title>\n" +
		"<meta name=\"generator\" content=\" 6.0.0.49\">\n" +
		"</head>\n" +
		"<body bgcolor=\"white\" text=\"black\" link=\"blue\" vlink=\"purple\" alink=\"red\">\n" +
		"<IMG SRC=\"http://counter.outdoor.geocities.jp/ncounter.cgi?id=saka1029s\" ALT=\"Counter\">\n" +
		"<h3>GPS Logs</h3>\n" +
		"<p>\n" +
		"自転車に乗って<a target=\"_top\" href=\"http://www.holux.com/JCore/en/products/products_content.jsp?pno=389\">\n" +
		"Holux M-241c</a>で採ったGPSログです。<br/>\n" +
		"リンクをクリックするとGoogle Map上でルートが表示されます。<br/>\n" +
		"コメントを<a href=\"http://outdoor.geocities.yahoo.co.jp/gb/sign_view?member=saka1029s\" target=\"right\">ゲストブック</a>に書いてください。<br/>\n" +
		"</p>\n" +
		"<a href=\"fukuyama\" target=\"_top\">1999年の福山</a><br>\n" +
		"<a href=\"http://maps.google.co.jp/maps?q=http://www.ultraloc.org/ultraloc.kmz\" target=\"_top\">ULTRA</a><br>\n"
		;
	private static final String INDEX_PAGE_ENTRY =
		"<a href=\"http://maps.google.co.jp/maps?output=embed&q=%s/%s\" target=\"right\">%s</a><br>\n";
	private static final String INDEX_PAGE_FOOTER = "</body>\n</html>\n";

	private File file;
	private TreeMap<String, String> titles = new TreeMap<String, String>();
	
	private void read() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ENC));
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.equals("") || line.startsWith("#")) continue;
				int pos = line.indexOf('=');
				if (pos < 0) throw new IOException("セパレータ'='がありません(" + line + ")");
				String name = line.substring(0, pos).trim();
				String title = line.substring(pos + 1).trim();
				titles.put(name, title);
			}
		} finally {
			reader.close();
		}
	}
	
	public TitleMap(File file) throws IOException {
		this.file = file;
		// ファイルからkmlのタイトルを読み込みます。
		read();
	}
	
	public void writeTo(File dir, String baseUrl, String enc) throws UnsupportedEncodingException, FileNotFoundException {
		File out = new File(dir, HTML_NAME);
		PrintWriter writer = new PrintWriter(
			new OutputStreamWriter(new FileOutputStream(out), enc));
		try {
			writer.print(INDEX_PAGE_HEADER);
			for (Entry<String, String> e : titles.entrySet())
				writer.printf(INDEX_PAGE_ENTRY, baseUrl, e.getKey(), e.getValue());
			writer.print(INDEX_PAGE_FOOTER);
		} finally {
			writer.close();
		}
	}
	
	public File getFile(File dir, File source) {
		return new File(dir, getKey(source));
	}
	
	private String getKey(File file) {
		String name = file.getName();
		return name.substring(name.length() - 19).toLowerCase();
	}
	
	public String get(File file) {
		String key = getKey(file);
		return titles.get(key);
	}
	
	public String toString() {
		return titles.toString();
	}
}
