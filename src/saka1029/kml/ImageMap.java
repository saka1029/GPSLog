package saka1029.kml;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.MetadataException;

public class ImageMap {

	private TreeMap<Date, Image> map = new TreeMap<Date, Image>();
	
	private void fetch(File file) throws JpegProcessingException, MetadataException {
		if (file.isDirectory())
			for (File child : file.listFiles())
				fetch(child);
		else if (Image.isJpeg(file)) {
			Image image = new Image(file);
			Date key = image.getCreated();
			Image prev = map.get(key);
			if (prev != null)
				image.setNext(prev);
			map.put(key, image);
		}
	}
	
	public ImageMap(File root) throws JpegProcessingException, MetadataException {
		fetch(root);
	}

	public Set<Image> get(Date from, Date to) {
		Set<Image> ret = new TreeSet<Image>();
		for (Entry<Date, Image> e : map.subMap(from, to).entrySet())
			for (Image image = e.getValue(); image != null; image = image.getNext())
				ret.add(image);
		return ret;
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.printf("ImageMetaMap[%n");
		for (Entry<Date, Image> e : map.entrySet()) {
			pw.printf("\t%s:%n", Util.format(e.getKey()));
			for (Image image = e.getValue(); image != null; image = image.getNext())
				pw.printf("\t\t%s%n", image);
		}
		pw.printf("]%n");
		pw.flush();
		return sw.toString();
	}
}
