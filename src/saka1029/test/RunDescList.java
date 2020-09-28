package saka1029.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

class RunDescList {


    static final File BASE_DIR = new File("F:/home/records/");
    static final int BASE_DIR_SIZE = BASE_DIR.getAbsolutePath().toString().length();
    static final File OUT_FILE = new File("D:/JPGIS/GPSLog/list.txt");
    static final Charset OUT_ENC = StandardCharsets.UTF_8;
    static final Charset DESC_ENC = Charset.forName("MS932");

    static File selectKml(File dir) {
        for (File file : dir.listFiles())
            if (file.isFile() && file.getName().toLowerCase().endsWith(".kml"))
                return file;
        return null;
    }

    static void selectDesc(File file) throws IOException {
        String desc = "";
        try (BufferedReader r = Files.newBufferedReader(file.toPath(), DESC_ENC)) {
            desc = r.readLine();
        }
        File dir = file.getParentFile();
        File kml = selectKml(dir);
        String kmlName = kml != null ? kml.getName() : "";
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(OUT_FILE.toPath(), OUT_ENC))) {
            String s = String.format("%s %s %s", desc, dir.toString().substring(BASE_DIR_SIZE), kmlName);
            w.println(s);
            System.out.println(s);
        }
    }

    static void select(File dir) throws IOException {
        for (File f : dir.listFiles())
            if (f.isFile()) {
                if (f.getName().equalsIgnoreCase("desc.txt"))
                    selectDesc(f);
            } else
                select(f);
    }

    @Test
    void test() throws IOException {
        select(BASE_DIR);
    }

}
