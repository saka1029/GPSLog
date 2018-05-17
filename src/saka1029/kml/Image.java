package saka1029.kml;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

/**
 * イメージクラスです。
 * JPEGの場合はEXIFのメタデータから以下の情報を取得します。
 * (1) 撮影日時
 * (2)　タイトル
 * (3) コメント
 */
public class Image implements Comparable<Image>{

	/**
	 * 出力時のイメージ最大サイズです。横長の場合は幅が、縦長の場合は高さがこのサイズになります。
	 */
	private static int maxSize = 320;
	private File file;
	private Date created = new Date();
	private String title = null;
	private String comment = null;
	private Image next = null;
	
	public static boolean isJpeg(File file) {
		String name = file.getName().toLowerCase();
		return name.endsWith(".jpeg") || name.endsWith(".jpg");
	}
	
	public Image(File file) throws JpegProcessingException, MetadataException {
		this.file = file;
		if (!isJpeg(file)) return;
		Metadata meta = JpegMetadataReader.readMetadata(file);
		Directory dir = meta.getDirectory(ExifDirectory.class);
		if (dir.containsTag(ExifDirectory.TAG_DATETIME_ORIGINAL))
			this.created = dir.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
		this.title = dir.getDescription(ExifDirectory.TAG_WIN_TITLE);
		this.comment = dir.getDescription(ExifDirectory.TAG_WIN_COMMENT);
	}

	public File getFile() {
		return file;
	}

	public Date getCreated() {
		return created;
	}

	public String getTitle() {
		return title;
	}

	public String getComment() {
		return comment;
	}

	/**
	 * java.awt.ImageをBufferedImageに変換します。
	 */
	private BufferedImage convert(java.awt.Image image) {
		BufferedImage bimg = new BufferedImage(
			image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = bimg.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimg;
	}
	
	public void writeTo(File out) throws IOException {
		//　すでにファイルがあれば何もしません。
		if (out.exists()) return;
		BufferedImage org = ImageIO.read(file);
		int max = Math.max(org.getWidth(), org.getHeight());
		double scale = (double)maxSize / max;
		BufferedImage dst;
		// ファイル名にfixがあれば縮小しません。
		if (file.getName().indexOf("fix") >= 0)
			dst = org;
		else {
			ImageFilter filter = new AreaAveragingScaleFilter(
				(int)(org.getWidth() * scale), (int)(org.getHeight() * scale));
			ImageProducer p = new FilteredImageSource(org.getSource(), filter);
			java.awt.Image dstImage = Toolkit.getDefaultToolkit().createImage(p);
			dst = convert(dstImage);
		}
		String type = isJpeg(file) ? "jpg" : "png";
		ImageIO.write(dst, type, out); 
	}
	
	public String toString() {
		return "Image[" + file.getPath()
			+ "," + Util.format(created)
			+ "," + title
			+ "," + comment
			+ "]";
	}

	public void setNext(Image next) {
		this.next = next;
	}

	public Image getNext() {
		return next;
	}

	public static int getMaxSize() {
		return maxSize;
	}

	public static void setMaxSize(int maxSize) {
		Image.maxSize = maxSize;
	}

	/**
	 * イメージの大小比較をします。
	 * 日付で比較します。
	 * 同一日付の場合はFileで比較します。
	 */
	@Override
	public int compareTo(Image o) {
		int r = created.compareTo(o.created);
		if (r != 0) return r;
		return file.compareTo(o.file);
	}
	
}
