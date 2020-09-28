package saka1029.kml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Main {

	static final String LEFT_HEADER =
		"<html>\n" +
		"<head>\n" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=%1$s\">\n" +
		"<title>%2$s</title>\n" +
		"<script>\n" +
		"  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n" +
		"  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n" +
		"  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n" +
		"  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');\n" +
		"\n" +
		"  ga('create', 'UA-37161278-2', 'fc2.com');\n" +
		"  ga('send', 'pageview');\n" +
		"\n" +
		"</script>\n" +
		"</head>\n" +
		"<body>\n" +
		"<h3>%2$s</h3>\n";

	static final String LEFT_ENTRY =
		"<a href=\"http://tokyoheight.html.xdomain.jp/?k=%1$s%2$s\" target=\"right\">%3$s</a>" +
		"<br>\n";

	static final String LEFT_FOOTER =
		"<br>" +
		"<a href=\"../left.html\">ホームに戻る</a>" +
		"</body>\n" +
		"</html>\n";

	static final String SITEMAP_HEADER =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
		"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"%n" +
        "    xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\"%n" +
        "    xmlns:video=\"http://www.google.com/schemas/sitemap-video/1.1\">%n";

	static final String SITEMAP_ENTRY =
		"        <url><loc>%1$s</loc></url>%n";

	static final String SITEMAP_FOOTER =
		"</urlset>%n";

	static boolean isKml(File file) {
		if (!file.isFile()) return false;
		String name = file.getName().toLowerCase();
		return name.endsWith(".kml");
	}

	static void kmls(File dir, List<Kml> list)
			throws ParserConfigurationException, SAXException, IOException, ParseException {
		for (File child : dir.listFiles())
			if (child.isDirectory())
				kmls(child, list);
			else if (isKml(child))
				list.add(Kml.create(child));
	}

	static List<Kml> kmls(File srcDir)
			throws ParserConfigurationException, SAXException, IOException, ParseException {
		List <Kml> list = new ArrayList<Kml>();
		kmls(srcDir, list);
		return list;
	}

	void writeLeft(List<Kml> kmls, File inputDir, String baseUrl,
			File file, String outputEncoding)
			throws IOException {
		PrintWriter writer = Util.newWriter(file, outputEncoding);
		try {
			writer.printf(LEFT_HEADER, outputEncoding, file.getParentFile().getName());
			for (Kml kml : kmls) {
				String relUrl = Util.relativePath(inputDir, kml.getFile()).replace('\\', '/');
				writer.printf(LEFT_ENTRY, baseUrl,  relUrl, kml.getName());
			}
			writer.printf(LEFT_FOOTER);
		} finally {
			writer.close();
		}
	}

	void removeKml(File file) {
		if (file.isDirectory())
			for (File child : file.listFiles())
				removeKml(child);
		else if (file.getName().toLowerCase().endsWith(".kml"))
			file.delete();
	}

	static Logger logger = Util.getLogger(Main.class);
	String inputEncoding = "Shift_JIS";
	String outputEncoding = "UTF-8";
	String baseUrl = "http://gpslog.html.xdomain.jp/";
	File inputDir = new File("L:/home/records");
	File outputDir = new File("L:/home/web");
	boolean useGoogleMpasAPI = false;

	public Main inputEncoding(String inputEncoding) { this.inputEncoding = inputEncoding; return this; }
	public Main outputEncoding(String outputEncoding) { this.outputEncoding = outputEncoding; return this; }
	public Main baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
	public Main inputDir(File inputDir) { this.inputDir = inputDir; return this; }
	public Main outputDir(File outputDir) { this.outputDir = outputDir; return this; }
	public Main useGoogleMapsAPI(boolean useGoogleMapsAPI) { this.useGoogleMpasAPI = useGoogleMapsAPI; return this; }

	void run(File inputDir, File srcDir, File dstDir) throws Exception {
//		// 出力先にあるすべてのKMLファイルを削除します。（開発時テスト用）
//		removeKml(outputDir);
		// Kmlファイルをすべて読み込みます。
		List<Kml> kmls = kmls(srcDir);
		// left.htmlを作成します。
		writeLeft(kmls, inputDir, baseUrl, new File(dstDir, "left.html"), outputEncoding);
		for (Kml kml : kmls) {
			File parent = kml.getFile().getParentFile();
			// 出力先の確認
			File dst = Util.dstFile(srcDir, dstDir, kml.getFile());
			// すでに出力先にファイルがあればスキップ
			if (dst.exists()) continue;
			// イメージのマップ
			ImageMap images = new ImageMap(parent);
			String imageUrl = baseUrl + Util.relativePath(inputDir, parent).replace('\\', '/');
			// 出力先ディレクトリがなければ作成
			if (!dst.getParentFile().exists()) dst.getParentFile().mkdirs();
			kml.writeTo(dst, images, imageUrl, useGoogleMpasAPI);
		}
	}

	public void run() throws Exception {
		try (PrintWriter writer = Util.newWriter(new File(outputDir, "sitemap.xml"), outputEncoding)) {
			writer.printf(SITEMAP_HEADER);
			for (File srcDir : inputDir.listFiles()) {
				if (srcDir.isDirectory()) {
					run(inputDir, srcDir, new File(outputDir, srcDir.getName()));
					String url = String.format("%s%s/left.html", baseUrl, srcDir.getName());
					writer.printf(SITEMAP_ENTRY, url);
				}
			}
			writer.printf(SITEMAP_FOOTER);
		}
	}

	private static void usage() {
		System.out.println("Usage: java saka1029.kml.Main" +
			" -i 入力ディレクトリ" +
			" -o 出力ディレクトリ" +
			" -u ベースURL" +
			" [-g]");
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		logger.info("開始");
		Main main = new Main();
		try {
			for (int i = 0, size = args.length; i < size; ++i)
				if (args[i].equals("-i"))
					main.inputDir(new File(args[++i]));
				else if (args[i].equals("-o"))
					main.outputDir(new File(args[++i]));
				else if (args[i].equals("-u"))
					main.baseUrl(args[++i]);
				else if (args[i].equals("-g"))
					main.useGoogleMapsAPI(true);
				else
					usage();
			if (main.inputDir == null) usage();
			if (main.outputDir == null) usage();
			if (main.baseUrl == null) usage();
			main.run();
		} finally {
			logger.info("終了");
		}
	}
}
