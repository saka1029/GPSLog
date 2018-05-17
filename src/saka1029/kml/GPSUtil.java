package saka1029.kml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * 緯度、経度から以下を求めます。
 * (1) ２点間の距離
 * (2) 高度
 */
public class GPSUtil {

	public static final double BESSEL_A = 6377397.155;
	public static final double BESSEL_E2 = 0.00667436061028297;
	public static final double BESSEL_MNUM = 6334832.10663254;

	public static final double GRS80_A = 6378137.000;
	public static final double GRS80_E2 = 0.00669438002301188;
	public static final double GRS80_MNUM = 6335439.32708317;

	public static final double WGS84_A = 6378137.000;
	public static final double WGS84_E2 = 0.00669437999019758;
	public static final double WGS84_MNUM = 6335439.32729246;

	public static final int BESSEL = 0;
	public static final int GRS80 = 1;
	public static final int WGS84 = 2;

	public static double deg2rad(double deg) {
		return deg * Math.PI / 180.0;
	}

	public static double calcDistHubeny(
			double lat1, double lng1, double lat2, double lng2,
			double a, double e2, double mnum) {
		double my = deg2rad((lat1 + lat2) / 2.0);
		double dy = deg2rad(lat1 - lat2);
		double dx = deg2rad(lng1 - lng2);
		double sin = Math.sin(my);
		double w = Math.sqrt(1.0 - e2 * sin * sin);
		double m = mnum / (w * w * w);
		double n = a / w;
		double dym = dy * m;
		double dxncos = dx * n * Math.cos(my);
		return Math.sqrt(dym * dym + dxncos * dxncos);
	}

	/**
	 * 緯度および経度の対から距離を計算します。
	 * 単位はメートルです。
	 * 
	 * 実行例。Java 版は緯度、経度の順になっています。
	 *
	 * $ java GPSUtil 36.10056 140.09111 35.65500 139.74472
	 * GPSUtil Test Program
	 * Distance = 58502.45893124115 m
	 * 
	 * http://yamadarake.web.fc2.com/trdi/2009/report000001.html
	 * 
	 * @param lat1 地点１の緯度
	 * @param lng1 地点１の経度
	 * @param lat2 地点２の緯度
	 * @param lng2 地点２の経度
	 * @return
	 */
	public static double calcDistHubeny(
			double lat1, double lng1, double lat2, double lng2) {
		return calcDistHubeny(lat1, lng1, lat2, lng2, GRS80_A, GRS80_E2, GRS80_MNUM);
	}

	public static double calcDistHubery(
			double lat1, double lng1, double lat2, double lng2, int type) {
		switch (type) {
		case BESSEL:
			return calcDistHubeny(lat1, lng1, lat2, lng2, BESSEL_A, BESSEL_E2, BESSEL_MNUM);
		case WGS84:
			return calcDistHubeny(lat1, lng1, lat2, lng2, WGS84_A, WGS84_E2, WGS84_MNUM);
		default:
			return calcDistHubeny(lat1, lng1, lat2, lng2, GRS80_A, GRS80_E2, GRS80_MNUM);
		}
	}

	static final String GOOGLE_ELEVATION_API_URL = "http://maps.google.com/maps/api/elevation/xml";
	// 一度の問い合わせで高度を求められる場所の最大数です。
	// Google maps APIのドキュメントでは512となっていますが、実際は37でもエラーとなります。
	static final int MAX_LOCATIONS = 20;

	private static void getElevationInternal(Node node, List<Double> elevations) throws IOException {
		if (node == null) return;
		if (node.getNodeName().equals("status"))
			if (!node.getTextContent().trim().equals("OK"))
				throw new IOException("ElevationAPIの呼び出しに失敗しました");
		if (node.getNodeName().equals("elevation"))
				elevations.add(Double.parseDouble(node.getTextContent().trim()));
		if (!node.hasChildNodes()) return;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			getElevationInternal(child, elevations);
	}
	
	private static List<Double> getElevationInternal(List<GPSLocation> locations)
			throws IOException, ParserConfigurationException, SAXException {
		StringBuilder sb = new StringBuilder();
		for (GPSLocation location : locations)
			sb.append(sb.length() <= 0 ? "" : "|")
				.append(location.getLatitude())
				.append(",").append(location.getLongtitude());
		URL url = new URL(GOOGLE_ELEVATION_API_URL + "?sensor=false&locations=" + sb.toString());
		URLConnection conn = url.openConnection();
		InputStream is = conn.getInputStream();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(is);
		List<Double> elevations = new ArrayList<Double>();
		getElevationInternal(document.getDocumentElement(), elevations);
		return elevations;
	}
	
	private static void getElevation(List<GPSLocation> locations, List<Double> elevations)
			throws IOException, ParserConfigurationException, SAXException {
		List<Double> res = getElevationInternal(locations);
		for (double d : res)
			elevations.add(d);
		locations.clear();
	}
	
	/**
	 * Google maps APIを使って緯度、経度からその場所の高度を求めます。
	 * 
	 * @param locations　緯度、経度のリストを指定します。
	 * @return 指定された高度(m)のリストを返します。
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static List<Double> getElevation(List<GPSLocation> locations)
			throws IOException, ParserConfigurationException, SAXException {
		List<GPSLocation> locs = new ArrayList<GPSLocation>();
		List<Double> elevations = new ArrayList<Double>();
		for (GPSLocation loc : locations) {
			locs.add(loc);
			if (locs.size() >= MAX_LOCATIONS)
				getElevation(locs, elevations);
		}
		if (locs.size() > 0)
			getElevation(locs, elevations);
		return elevations;
	}
	
}
