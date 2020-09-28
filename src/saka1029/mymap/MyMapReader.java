package saka1029.mymap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import saka1029.kml.Util;

public class MyMapReader {

    static final Logger logger = Util.getLogger(MyMapReader.class);

    static final Pattern GMAP_DOC_ID = Pattern.compile("\"doc_id\"\\s*:\\s*\"([^\"]+)\"");

    public static String docId(File gmapFile) throws IOException {
        if (!gmapFile.getName().toString().endsWith(".gmap"))
            throw new IOException("Not GMAP file: " + gmapFile);
        try (Reader r = new FileReader(gmapFile, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(r)) {
            String line = br.readLine();
            if (line == null)
                throw new IOException("Empty GMAP file: " + gmapFile);
            Matcher m = GMAP_DOC_ID.matcher(line);
            if (m.find())
                return m.group(1);
            else
                throw new IOException("Invalid GMAP format: " + gmapFile);
        }
    }

    static HttpClient client = HttpClient.newBuilder().build();

    public static InputStream openKmzStream(String docId) throws IOException, InterruptedException {
        URI uri = URI.create("https://www.google.com/maps/d/kml?mid=" + docId);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .build();
        HttpResponse<InputStream> res = client.send(req, BodyHandlers.ofInputStream());
//        logger.info("doc_id=" + docId + " uri=" + res.uri() + " status=" + res.statusCode());
        int status = res.statusCode();
        if (status != 200)
            throw new HttpException(uri, status);
        return res.body();
    }

    public static InputStream openKmlStream(String docId) throws IOException, InterruptedException {
        InputStream kmz = openKmzStream(docId);
        ZipInputStream in = new ZipInputStream(kmz);
        while (true) {
            ZipEntry e = in.getNextEntry();
            if (e == null)
                throw new IOException("'doc.kml' not found in doc_id: " + docId);
            if (e.getName().equals("doc.kml"))
                return in;
        }
    }

    public static void parseKml(InputStream kmlInputStream, DefaultHandler handler)
        throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(kmlInputStream, handler);
    }

    public static MyMap parseKml(File gmapFile) throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String title = gmapFile.getName().replaceFirst("\\.gmap$", "");
        String id = docId(gmapFile);
        MyMap myMap = new MyMap(id, title);
        try (InputStream kmlInputStream = openKmlStream(myMap.id)) {
            parseKml(kmlInputStream, new Handler(myMap));
        }
        return myMap;
    }

    static Pattern YOUTUBE_PAT = Pattern.compile("https?://(youtu\\.be|www\\.youtube\\.com)/(embed/|watch)?(\\?v=)?(?<ID>[A-zA-z0-9-_]*)");
    static Pattern HEIGHT_PAT = Pattern.compile("\\d+/\\d+/\\d+ \\d+:\\d+:\\d+ \\d+\\.\\d+Km/hr \\d+\\.\\d+Km -?\\d+m");

    static class Handler extends DefaultHandler {
        final MyMap myMap;
        final Deque<String> tags = new LinkedList<>();
        final StringBuilder sb = new StringBuilder();

        Handler(MyMap myMap) {
            this.myMap = myMap;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            tags.push(qName);
            sb.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            sb.append(ch, start, length);
        }

        static void updateLngLon(MyMap myMap, String text) {
            String[] coordinates = text.trim().split("\\s+");
            for (String c : coordinates) {
                String[] s = c.split(",");
                double lon = Double.parseDouble(s[0]);
                double lat = Double.parseDouble(s[1]);
                myMap.minLon = Math.min(myMap.minLon, lon);
                myMap.maxLon = Math.max(myMap.maxLon, lon);
                myMap.minLat = Math.min(myMap.minLat, lat);
                myMap.maxLat = Math.max(myMap.maxLat, lat);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String text = sb.toString().trim();
            if (text.length() > 0) {
                switch (qName) {
                case "coordinates":
                    updateLngLon(myMap, text);
                    break;
                case "name": case "description":
                    String s = text.replaceAll("<[^>]*>", "").trim();
                    if (isDescription(s))
                        myMap.description.add(s);
                    /* through */
                default:
                    Matcher m = YOUTUBE_PAT.matcher(text);
                    while (m.find()) {
//                            logger.info("youtube:" + m.group("ID"));
                        myMap.youTube.add(m.group("ID"));
                    }
                }
            }
            sb.setLength(0);
            tags.pop();
        }
    };

    static boolean isDescription(String text) {
        switch (text) {
        case "":
        case "waypoints": case "Route": case "無題のレイヤ":
        case "スタート地点": case "ゴール地点": case "経路":
        case "スタート": case "ゴール":
            return false;
        default:
            if (HEIGHT_PAT.matcher(text).matches())
                return false;
            return true;
        }
    }

}
