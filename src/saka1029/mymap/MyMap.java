package saka1029.mymap;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MyMap {

    public final String id;
    public final String title;
    public final Set<String> description = new HashSet<>();
    public final Set<String> youTube = new HashSet<>();
    public double minLon = 180.0, maxLon = -180.0;
    public double minLat = 90.0, maxLat = -90.0;

    public MyMap(String id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return String.format("MyMap(title=%s id=%s description=%s lonlat=(%f,%f)-(%f,%f) youTube=%s)",
            title, id, description,
            minLon, minLat, maxLon, maxLat,
            youTube);
    }

    static String escape(int codePoint) {
        switch (codePoint) {
        case '\\': return "\\\\";
        case '\"': return "\\\"";
        default: return Character.toString(codePoint);
        }
    }

    static String quote(String s) {
        return "\"" + s.codePoints()
            .mapToObj(MyMap::escape)
            .collect(Collectors.joining()) + "\"";
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("id:").append(quote(id)).append(",");
        sb.append("title:").append(quote(title)).append(",");
        sb.append("description:").append(quote(String.join(" ", description))).append(",");
        sb.append("minLon:").append(minLon).append(",");
        sb.append("maxLon:").append(maxLon).append(",");
        sb.append("minLat:").append(minLat).append(",");
        sb.append("maxLat:").append(maxLat).append(",");
        sb.append("youTube:[");
        boolean first = true;
        for (String s : youTube) {
            if (!first) sb.append(",");
            sb.append(quote(s));
            first = false;
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

}
