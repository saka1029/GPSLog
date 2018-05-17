package saka1029.kml;

/**
 * 地球上の位置を緯度と経度で示すクラスです。
 */
public class GPSLocation {

	private double latitude;
	private double longtitude;
	
	/**
	 * インスタンスを作成します。
	 * @param latitude 緯度を指定します。
	 * @param longtitude 経度を指定します。
	 */
	public GPSLocation(double latitude, double longtitude) {
		this.latitude = latitude;
		this.longtitude = longtitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}
}
