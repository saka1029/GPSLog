package saka1029.mymap;

import java.io.IOException;
import java.net.URI;

public class HttpException extends IOException {

    public HttpException(URI uri, int httpStatus) {
        super(String.format("URI=%s http status=%s", uri, httpStatus));
    }

}
