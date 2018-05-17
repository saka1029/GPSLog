package saka1029.kml;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.MetadataException;

/**
 * kmlファイルのクラスです。
 */
public abstract class Kml {

	static final String DESCRIPTION_FILE = "desc.txt";
	static final String INPUT_ENCODING = "Shift_JIS";
	
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	static {
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		// validateしないときはsetIgnoringElementContentWhitespace=trueは無効になる。
//		factory.setIgnoringElementContentWhitespace(true);
	}
	private static TransformerFactory outputFactory = TransformerFactory.newInstance();

	protected File file;
	protected String name = "";
	protected Document document;

	public static Kml create(File file)
			throws ParserConfigurationException, SAXException, IOException, ParseException
	{
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		Node root = document.getDocumentElement();
		if (!root.getNodeName().equals("kml"))
			throw new IOException("invalid kml file");
		String ns = root.getAttributes().getNamedItem("xmlns").getNodeValue();
		if (ns.contains("2.1"))
			return new Kml21(file, document);
		else
			return new Kml22(file, document);
	}
	
	protected Kml(File file, Document document) {
		this.file = file;
		this.document = document;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	protected void writeTo(File dest) throws TransformerException {
	    Transformer transformer = outputFactory.newTransformer(); 
	    transformer.transform(new DOMSource(document), new StreamResult(dest));
	}

	public abstract void writeTo(File dest, ImageMap images, String imageUrl, boolean useGoogleMapsAPI)
			throws TransformerException, IOException,
					ParserConfigurationException, SAXException,
					JpegProcessingException, MetadataException;
}
