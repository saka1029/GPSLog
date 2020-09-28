package saka1029.test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

class TestMySql {

    @Test
    void testSelect() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://localhost/gpslog";
//         String url = "jdbc:mysql://localhost/gpslog?serverTimezone=JST";
//         String url = "jdbc:mysql://localhost/gpslog?serverTimezone=UTC";
        try (Connection con = DriverManager.getConnection(url, "root", "")) {
            try (Statement st = con.createStatement()) {
                String sql = "SELECT * FROM sample";
                try (ResultSet result = st.executeQuery(sql)) {
                    while (result.next()) {
                        String docId = result.getString("doc_id");
                        String name = result.getString("name");
                        Date createDate = result.getDate("create_date");
                        System.out.println(docId + ", " + name + ", " + createDate);
                    }
                }
            }
            try (Statement st = con.createStatement();
                ResultSet result = st.executeQuery("select now()")) {
                while (result.next()) {
                    Timestamp now = result.getTimestamp(1);
                    System.out.println("now = " + now);
                }
            }
        }
    }
}
