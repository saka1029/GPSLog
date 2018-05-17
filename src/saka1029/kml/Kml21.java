package saka1029.kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.MetadataException;

/**
 * HOLUX M-241が生成するkmlファイルです。
 * (1)Placemarkの時刻に近いタイムスタンプを持つイメージへのリンクを生成します。
 * (2)Google Elevation APIにより高度を求めます。
 * (3)イメージ内に格納されたEXIFのタイトルおよびコメントを抽出し、
 *    PlacemarkごとのHTMLコメントとして生成します。
 */
public class Kml21 extends Kml {

	static Logger log = Util.getLogger(Kml21.class);

	private static final long epsilon = 180000;

	private List<Placemark> placemarks = new ArrayList<Placemark>();

	public Kml21(File file, Document document) throws ParseException, IOException  {
		super(file, document);
		// 同一ディレクトリにあるdesc.txtからnameを設定します。
		name = getName(file);
		// kmlのDOMからPlacemarkを抽出します。
		addPlacemarks(document.getDocumentElement());
		// Placemarkごとに開始地点からの距離を求めます。
		calculateDistance();
		// それぞれのPlacemarkにイメージを割り当てます。
		// イメージのタイトルとコメントをPlacemarkにHTMLで埋め込みます。
		// Placemarkごとの速度を求めます。
		calculateSpeed();
	}

	private static String getName(File file) throws IOException {
		File desc = new File(file.getParentFile(), DESCRIPTION_FILE);
		if (!desc.isFile()) return "";
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(desc), INPUT_ENCODING));
		try {
			String line = reader.readLine();
			if (line == null) return "";
			return line;
		} finally {
			reader.close();
		}
	}

	private void addPlacemarks(Node node) throws ParseException {
		if (node == null) return;
		if (node.getNodeName().equals("name")
			&& node.getParentNode().getNodeName().equals("Document"))
			node.setTextContent(name);
		else if (node.getNodeName().equals("Placemark")
			&& node.getParentNode().getNodeName().equals("Folder"))
			placemarks.add(new Placemark(node));
		if (!node.hasChildNodes()) return;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			addPlacemarks(child);
	}

	private void calculateDistance() {
		double distance = 0.0;
		Placemark prev = null;
		for (Placemark pm : placemarks) {
			if (prev != null)
				distance += GPSUtil.calcDistHubeny(
						prev.getLatitude(), prev.getLongtitude(),
						pm.getLatitude(), pm.getLongtitude()) / 1000.0;
			pm.setDistance(distance);
			prev = pm;
		}
	}

	private void calculateSpeed() {
		for (int i = 0, size = placemarks.size(); i < size; ++i) {
			Placemark pm = placemarks.get(i);
			long startTime = pm.getDate().getTime();
			double startPos = pm.getDistance();
			long endTime = startTime;
			double endPos = startPos;
			if (i > 0) {
				Placemark prev = placemarks.get(i - 1);
				startTime = prev.getDate().getTime();
				startPos = prev.getDistance();
			}
			if (i < size - 1) {
				Placemark next = placemarks.get(i + 1);
				endTime = next.getDate().getTime();
				endPos = next.getDistance();
			}
			// Km/hrで時速を求めます。
			double time = (double)(endTime - startTime);
			double speed = (endPos - startPos) / (time / 3600000.0);
			pm.setSpeed(speed);
		}
	}

	private int calculateHeight(int step)
			throws IOException, ParserConfigurationException, SAXException {
		int count = 0;
		List<GPSLocation> locs = new ArrayList<GPSLocation>();
		int i = 0;
		for (Placemark pm : placemarks) {
			// 最終的に間引かれないPlacemarkは必ず高度を取得します。
			if (!pm.getStyle().equals(Placemark.STYLE_NONE) || i % step == 0) {
				++count;
				locs.add(new GPSLocation(pm.getLatitude(), pm.getLongtitude()));
			}
			++i;
		}
		List<Double> els = GPSUtil.getElevation(locs);
		i = 0;
		int j = 0;
		for (Placemark pm : placemarks) {
			if (!pm.getStyle().equals(Placemark.STYLE_NONE) || i % step == 0)
				pm.setHeight(els.get(j++));
			++i;
		}
		return count;
	}

	private File createSpeedGraph(int step) throws IOException {
		XYSeriesCollection ds = new XYSeriesCollection();
		XYSeries speeds = new XYSeries("height");
		int i = 0;
		for (Placemark pm : placemarks) {
			if (!pm.getStyle().equals(Placemark.STYLE_NONE) || i % step == 0)
				speeds.add(pm.getDistance(), pm.getSpeed());
			++i;
		}
		ds.addSeries(speeds);
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		JFreeChart chart = ChartFactory.createXYLineChart(
				"速度", // タイトル
                "移動距離(Km)", // categoryAxisLabel （カテゴリ軸、横軸、X軸のラベル）
                "Km/hr", // valueAxisLabel（ヴァリュー軸、縦軸、Y軸のラベル）
                ds, // dataset
                PlotOrientation.VERTICAL,
                false, // legend
                false, // tooltips
                false); // URLs
		File png = new File(file.getParentFile(), "speed_fix.png");
        ChartUtilities.saveChartAsPNG(png, chart, 320, 240);
        return png;
	}

	private File createHeightGraph(int step) throws IOException {
		XYSeriesCollection ds = new XYSeriesCollection();
		XYSeries heights = new XYSeries("height");
		int i = 0;
		for (Placemark pm : placemarks) {
			if (!pm.getStyle().equals(Placemark.STYLE_NONE) || i % step == 0)
				heights.add(pm.getDistance(), pm.getHeight());
			++i;
		}
		ds.addSeries(heights);
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		JFreeChart chart = ChartFactory.createXYLineChart(
				"高度", // タイトル
                "移動距離(Km)", // categoryAxisLabel （カテゴリ軸、横軸、X軸のラベル）
                "m", // valueAxisLabel（ヴァリュー軸、縦軸、Y軸のラベル）
                ds, // dataset
                PlotOrientation.VERTICAL,
                false, // legend
                false, // tooltips
                false); // URLs
		File png = new File(file.getParentFile(), "height_fix.png");
        ChartUtilities.saveChartAsPNG(png, chart, 320, 240);
        return png;
	}

	private void setImages(ImageMap images, String imageUrl) {
		for (int i = 0, size = placemarks.size(); i < size; ++i) {
			Placemark pm = placemarks.get(i);
			long from;
			if ((i - 1) < 0)
				from = pm.getDate().getTime() - epsilon * 2;
			else
				from = placemarks.get(i - 1).getDate().getTime();
			long to;
			if ((i + 1) >= size)
				to = pm.getDate().getTime() + epsilon * 2;
			else
				to = placemarks.get(i + 1).getDate().getTime();
			pm.setImages(images.get(
				new Date((from +  pm.getDate().getTime()) / 2),
				new Date((pm.getDate().getTime() + to) / 2)),
				imageUrl);
		}
	}

	private void setStyle() {
		for (int i = 0, size = placemarks.size(); i < size; ++i) {
			Placemark pm = placemarks.get(i);
			if (i <= 0)							// 先頭のPlacemark
				pm.setStyle(Placemark.STYLE_START);
			else if (i >= (size - 1))			// 最後のPlacemark
				pm.setStyle(Placemark.STYLE_FINISH);
			else if (pm.getImages().size() > 0)	// イメージありのPlacemark
				pm.setStyle(Placemark.STYLE_PHOTO);
			else if (i % 20 == 0)				// 20個ごとのPlacemark
				pm.setStyle(Placemark.STYLE_NORMAL);
			else								// その他のPlacemark
				pm.setStyle(Placemark.STYLE_NONE);
		}
	}

	private void removeSpaces() {
		// Placemarkを間引きます。
		List<Placemark> backup = placemarks;
		placemarks = new ArrayList<Placemark>();
		for (Placemark pm : backup)
			if (pm.getStyle().equals(Placemark.STYLE_NONE))
				pm.remove();	// DOMから削除します。
			else
				placemarks.add(pm);
		// Placemark間の無効なスペースをDOMから削除します。
		if (placemarks.size() <= 0) return;
		Node parent = placemarks.get(0).getNode().getParentNode();
		for (Node child = parent.getFirstChild(); child != null;) {
			Node self = child;
			child = child.getNextSibling();	// 削除する前に弟を取得する。
			if (self.getNodeType() == Node.TEXT_NODE)
				parent.removeChild(self);
		}
	}

	public void writeTo(File file, ImageMap images, String imageUrl, boolean useGoogleMapsAPI)
			throws TransformerException,
					JpegProcessingException, MetadataException,
					IOException,
					ParserConfigurationException,
					SAXException {
		// イメージをPlacemarkに割り当てます。
		setImages(images, imageUrl);
		// Placemarkのアイコンを設定します。
		setStyle();
		// 高度、グラフのサンプリングレイトを指定します。
		int step = 2;
		// Google maps APIを使ってPlacemarkの高度を求めます。
		if (useGoogleMapsAPI) calculateHeight(step);
		// 速度のグラフを作成します。
		File speedImage = createSpeedGraph(step);
		// 高度のグラフを作成します。
		File heightImage = createHeightGraph(step);
		// 速度、高度のグラフを先頭のPlacemarkに追加します。
		if (placemarks.size() > 0) {
			Set<Image> imgs = placemarks.get(0).getImages();
			imgs.add(new Image(speedImage));
			imgs.add(new Image(heightImage));
			placemarks.get(0).setImages(imgs, imageUrl);
		}
		int heightCount = 0;
		int i = 0;
		for (Placemark pm : placemarks) {
			if (!pm.getStyle().equals(Placemark.STYLE_NONE) || i % step == 0)
				++heightCount;
			++i;
		}
		int pmCount = placemarks.size();
		int imageCount = 0;
		// Placemarkを間引きます。
		removeSpaces();
		// Placemarkノードのテキストを再設定します。
	    for (Placemark pm : placemarks)
	    	pm.resetNode();
	    // DOMツリーをファイルに出力します。
	    writeTo(file);
	    File parent = file.getParentFile();
	    for (Placemark pm : placemarks) {
	    	imageCount += pm.getImages().size();
	    	for (Image image : pm.getImages())
	    		image.writeTo(new File(parent, image.getFile().getName()));
	    }
	    int pegCount = placemarks.size();
	    log.info(String.format("%s placemarks=%d pegs=%d images=%d heights=%d",
	    		file.getName(), pmCount, pegCount, imageCount, heightCount));
	}

	public File getFile() {
		return file;
	}

	public String getShortName() {
		String name = file.getName();
		return name.substring(name.length() - 19);
	}

	public List<Placemark> getPlacemarks() {
		return placemarks;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.printf("Kml[%n");
		pw.printf("\t%s%n", file);
		for (Placemark pm : placemarks)
			pw.printf("\t%s%n", pm);
		pw.printf("]%n");
		pw.flush();
		return sw.toString();
	}
}
