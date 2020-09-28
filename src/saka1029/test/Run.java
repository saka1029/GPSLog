package saka1029.test;

import java.io.File;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import saka1029.kml.Main;
import saka1029.kml.Util;


class Run {

    static final Logger logger = Util.getLogger(Run.class);

    @Test
    void run() throws Exception {
        logger.info("開始");
        Main main = new Main();
        main.inputDir(new File("F:/home/records"));
        main.outputDir(new File("D:/JPGIS/GPSLog"));
        main.baseUrl("http://gpslog.html.xdomain.jp/");
        main.run();
        logger.info("終了");
    }

//    @Test
//    void run() throws Exception {
//        logger.info("開始");
//        Main main = new Main();
//        main.inputDir(new File("L:/home/records"));
//        main.outputDir(new File("L:/home/web"));
//        main.baseUrl("http://gpslog.html.xdomain.jp/");
//        main.run();
//        logger.info("終了");
//    }

}
