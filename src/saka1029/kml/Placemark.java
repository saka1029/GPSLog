package saka1029.kml;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

public class Placemark {

	public static final String STYLE_START = "#styleMap_TLP_Start";
	public static final String STYLE_NORMAL = "#styleMap_TLP_Normal";
	public static final String STYLE_FINISH = "#styleMap_TLP_Finish";
	public static final String STYLE_PHOTO = "#styleMap_TLP_Photo";
	public static final String STYLE_NONE = "#styleMap_TLP_None";
	
	private Node node;
	private Date date;
	private String name;
	private Node nameNode;
	private Node description;
	private Node styleUrl;
	private Set<Image> images;
	private double speed = Double.NaN;

	private double latitude;
	private double longtitude = Double.NaN;
	private double distance = Double.NaN;
	private double height = Double.NaN;

	public Placemark(Node node) throws ParseException {
		this.node = node;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeName().equals("name")) {
				nameNode = child;
				name = child.getTextContent().trim();
				String t = name.replaceAll("\\s+S=.*$", "");
				date = Util.parseKmlDate(t);
				if (name.contains("S=")) {
					String d = name.replaceFirst("^.*S=\\s*", "");
					d = d.replaceFirst("[^0-9.]*$", "");
					speed = Double.parseDouble(d);
				}
			}
			else if (child.getNodeName().equals("description"))
				description = child;
			else if (child.getNodeName().equals("styleUrl"))
				styleUrl = child;
			else if (child.getNodeName().equals("Point")) {
				for (Node gc = child.getFirstChild(); gc != null; gc = gc.getNextSibling())
					if (gc.getNodeName().equals("coordinates")) {
						String coord = child.getTextContent().trim();
						String c[] = coord.split(",");
						longtitude = Double.parseDouble(c[0]);
						latitude = Double.parseDouble(c[1]);
						height = Double.parseDouble(c[2]);
					}
			}
		}
	}

	public void remove() {
		Node parent = node.getParentNode();
		parent.removeChild(node);
	}
	
	public Date getDate() {
		return date;
	}
	
	public Node getNode() {
		return node;
	}
	
	public Set<Image> getImages() {
		return images;
	}
	
	private String makeHtml(Set<Image> images, String imageUrl) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (Image image : images) {
			if (image.getTitle() != null)
				pw.printf("<b>%s</b><br>\n", image.getTitle());
			if (image.getComment() != null)
				pw.printf("%s<br>\n", image.getComment());
			pw.printf("<img src=\"%s/%s\"></img><br>\n",
				imageUrl, image.getFile().getName());
		}
		pw.flush();
		return sw.toString();
	}
	
	public void setImages(Set<Image> images, String imageUrl) {
		this.images = images;
		if (description == null) {
			description = node.getOwnerDocument().createElement("description");
			node.appendChild(description);
		} else {
			while (description.getChildNodes().getLength() > 0)
				description.removeChild(description.getFirstChild());
		}
		CDATASection cdata = node.getOwnerDocument().createCDATASection(makeHtml(images, imageUrl));
		description.appendChild(cdata);
	}
	
	public String getStyle() {
		return styleUrl.getTextContent().trim();
	}
	
	public void setStyle(String style) {
		styleUrl.setTextContent(style);
	}
	
	/**
	 * 速度、位置、高さからDOMのテキストを更新します。
	 */
	public void resetNode() {
		name = Util.formatShort(date);
		if (!Double.isNaN(speed))
			name += String.format(" %.2fKm/hr", speed);
		if (!Double.isNaN(distance))
			name += String.format(" %.2fKm", distance);
		if (!Double.isNaN(height))
			name += String.format(" %.0fm", height);
		nameNode.setTextContent(name);
	}
	
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public String toString() {
		return "Placemark[" + name
			+ "," + Util.formatShort(date)
			+ "," + getStyle()
			+ "," + (description == null ? "" : description.getTextContent().trim())
			+ "," + "(" + longtitude + "," + latitude + "," + height + ")"
			+ "," + speed
			+ ", distance=" + String.format("%7.2f", distance).trim()
			+ "," + images
			+ "]";
	}

	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
