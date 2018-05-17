package saka1029.kml;

import java.io.File;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * HOLUX M-241以外のkmlファイルです。
 * 具体的にはGoogle Mapのマイプレイスで編集したkmlを想定しています。
 * このタイプのkmlファイルは何も加工せずに単純にコピーします。
 * nameはkmlファイルのnameタグから取得します。
 */
public class Kml22 extends Kml {

	public Kml22(File file, Document document) {
		super(file, document);
		name = getName(document.getDocumentElement()) + "[動画]";
	}
	
	/**
	 * 以下の名前を抽出します。
	 * <?xml ... ?><kml><Document><name>名前</name>...
	 */
	private static String getName(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE)
			return "";
		if (node.getLocalName().equals("name"))
			return node.getTextContent();
		for (int i = 0, size = node.getChildNodes().getLength(); i < size; ++i) {
			String v = getName(node.getChildNodes().item(i));
			if (!v.equals("")) return v;
		}
		return "";
	}
	
	public void writeTo(File dest, ImageMap images, String imageUrl, boolean useGoogle)
			throws TransformerException
	{
		writeTo(dest);
	}
}
