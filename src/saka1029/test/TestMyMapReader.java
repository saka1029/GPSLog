package saka1029.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import saka1029.kml.Util;
import saka1029.mymap.HttpException;
import saka1029.mymap.MyMap;
import saka1029.mymap.MyMapReader;

public class TestMyMapReader {

    static Logger logger = Util.getLogger(TestMyMapReader.class);

    @Test
    public void testWriteGmapJs() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        File inDirectory = new File("D:/GoogleDrive/");
        try (PrintWriter w = new PrintWriter(new FileWriter("gpslog/gmap.js", StandardCharsets.UTF_8))) {
            w.printf("const ALL_DATA = [%n");
            for (File gmapFile : inDirectory.listFiles()) {
                if (!gmapFile.getName().toLowerCase().endsWith(".gmap"))
                    continue;
                logger.info("#### " + gmapFile.getName());
                try {
                    MyMap myMap = MyMapReader.parseKml(gmapFile);
                    w.printf("\t%s,%n", myMap.toJson());
                    logger.info("     " + myMap);
                } catch (HttpException h) {
                    logger.severe(h.toString());
                }
            }
            w.printf("];%n");
        }
    }

    @Test
    public void testDownloadAllKMZ() throws IOException, InterruptedException {
        File inDirectory = new File("D:/GoogleDrive/");
        File outDirectory = new File("D:/JPGIS/MyMap/");
        if (!outDirectory.exists()) outDirectory.mkdirs();
        for (File gmapFile : inDirectory.listFiles()) {
            String fileName = gmapFile.getName().toLowerCase();
            if (!fileName.endsWith(".gmap"))
                continue;
            logger.info("#### " + gmapFile.getName());
            String docId = MyMapReader.docId(gmapFile);
            File outFile = new File(outDirectory, fileName.replaceFirst("\\.gmap$", ".kmz"));
            try (InputStream in = MyMapReader.openKmzStream(docId);
                OutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
            } catch (HttpException e) {
                logger.severe(e.toString());
            }
        }
    }

    @Test
    public void testDownloadAllKML() throws IOException, InterruptedException {
        File inDirectory = new File("D:/GoogleDrive/");
        File outDirectory = new File("D:/JPGIS/MyMap/");
        if (!outDirectory.exists()) outDirectory.mkdirs();
        for (File gmapFile : inDirectory.listFiles()) {
            String fileName = gmapFile.getName().toLowerCase();
            if (!fileName.endsWith(".gmap"))
                continue;
            logger.info("#### " + gmapFile.getName());
            String docId = MyMapReader.docId(gmapFile);
            File outFile = new File(outDirectory, fileName.replaceFirst("\\.gmap$", ".kml"));
            try (InputStream in = MyMapReader.openKmlStream(docId);
                OutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
            } catch (HttpException e) {
                logger.severe(e.toString());
            }
        }
    }

}
