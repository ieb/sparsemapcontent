package org.sakaiproject.nakamura.lite.jdbc.derby;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

public class KeyValueRowsMain {

    private Connection connection;
    private String[] dictionary;

    public KeyValueRowsMain() {
    }

    public void deleteDb(String file) {
        FileUtils.deleteQuietly(new File(file));
    }

    public void open(String file) throws SQLException {
        connection = DriverManager
                .getConnection("jdbc:derby:" + file + "/db;create=true", "sa", "");
    }

    public void createTables(int columns) throws SQLException {
        Statement s = connection.createStatement();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE cn_css_kv (");
        sql.append("id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),");
        sql.append("rid varchar(32) NOT NULL,");
        sql.append("cid varchar(64) NOT NULL,");
        sql.append("v varchar(740),");
        sql.append("primary key(id))");
        s.execute(sql.toString());
        s.execute("CREATE UNIQUE INDEX cn_css_kv_rc ON cn_css_kv (rid,cid)");
        s.execute("CREATE INDEX cn_css_kv_cv ON cn_css_kv (cid,v)");
        s.close();
    }

    public void populateDictionary(int size) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        dictionary = new String[size];
        MessageDigest md = MessageDigest.getInstance("SHA1");
        for (int i = 0; i < size; i++) {
            dictionary[i] = Base64.encodeBase64URLSafeString(md.digest(String.valueOf("Dict" + i)
                    .getBytes("UTF-8")));
        }
    }

    public void loadTable(int columns, int records) throws SQLException,
            UnsupportedEncodingException, NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into cn_css_kv (rid, cid, v) values ( ?, ?, ?)");
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = connection.prepareStatement(sb.toString());
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            SecureRandom sr = new SecureRandom();
            long cs = System.currentTimeMillis();
            rs = connection.createStatement().executeQuery(
                    "select count(*) from cn_css_kv");
            rs.next();
            int nrows = rs.getInt(1);
            for (int i = 0 + nrows; i < records + nrows; i++) {
                String rid = Base64.encodeBase64URLSafeString(sha1.digest(String
                        .valueOf("TEST" + i).getBytes("UTF-8")));
                for (int j = 0; j < columns; j++) {
                    if (sr.nextBoolean()) {
                        p.clearParameters();
                        p.setString(1, rid);
                        p.setString(2, "v" + j);
                        p.setString(3, dictionary[sr.nextInt(dictionary.length)]);
                        p.execute();
                    }
                }

                if (i % 100 == 0) {
                    long ct = System.currentTimeMillis();
                    System.err.println("Commit " + i + " " + (ct - cs) + " ms/100");
                    cs = ct;
                    connection.commit();
                }
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e ) {
                    
                }
            }
            if (p != null) {
                try {
                    p.close();
                } catch (SQLException e ) {
                    
                }
            }
        }
    }

    private void close() throws SQLException {
        connection.close();
    }

    public void testSelect(int ncols, int sorts, int columns, long timeToLive) throws SQLException {
        StringBuilder sb = new StringBuilder();
        SecureRandom sr = new SecureRandom();
        Set<Integer> used = new LinkedHashSet<Integer>();
        while (used.size() < ncols) {
            int c = sr.nextInt(columns);
            if (!used.contains(c)) {
                used.add(c);
            }
        }
        Integer[] cnums = used.toArray(new Integer[ncols]);
        sb.append("select distinct a.rid  ");
        if (sorts > 0) {
            for (int i = 0; i < sorts; i++) {
                sb.append(", s").append(i).append(".v ");
            }
        }
        sb.append(" from cn_css_kv a ");
        for (int i = 0; i < ncols; i++) {
            sb.append(" , cn_css_kv a").append(i);
        }
        if (sorts > 0) {
            for (int i = 0; i < sorts; i++) {
                sb.append(" , cn_css_kv s").append(i);
            }
        }
        sb.append(" where  ");
        for (int i = 0; i < ncols; i++) {
            sb.append(" a").append(i).append(".cid = ? AND a").append(i).append(".v = ? AND a")
                    .append(i).append(".rid = a.rid AND ");
        }
        if (sorts > 0) {
            for (int i = 0; i < sorts; i++) {
                sb.append("s").append(i).append(".cid = ? AND a.rid = s").append(i)
                        .append(".rid AND ");
            }
        }
        sb.append(" 1 = 1 ");
        Integer[] snums = null;
        if (sorts > 0) {
            sb.append(" order by ");
            used.clear();
            while (used.size() < sorts) {
                int c = sr.nextInt(columns);
                if (!used.contains(c)) {
                    used.add(c);
                }
            }
            snums = used.toArray(new Integer[ncols]);
            for (int i = 0; i < sorts - 1; i++) {
                sb.append("s").append(i).append(".v ,");
            }
            sb.append("s").append(sorts - 1).append(".v ");
        }
        System.err.println(sb.toString());
        PreparedStatement p = null;
        ResultSet rs = null;
        long atstart = System.currentTimeMillis();
        int arows = 0;
        int nq = 0;
        try {
            p = connection.prepareStatement(sb.toString());
            long endTestTime = atstart + timeToLive;
            while (System.currentTimeMillis() < endTestTime) {
                p.clearParameters();
                for (int i = 0; i < ncols; i++) {
                    p.setString(i * 2 + 1, "v" + cnums[i]);
                    p.setString(i * 2 + 2, dictionary[sr.nextInt(dictionary.length)]);
                }
                if (sorts > 0) {
                    for (int i = 0; i < sorts; i++) {
                        p.setString(i + 1 + ncols * 2, "s" + snums[i]);
                    }
                }
                rs = p.executeQuery();
                int rows = 0;
                while (rs.next()) {
                    rows++;
                }
                arows += rows;
                nq++;
                rs.close();
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {

                }
            }
            if (p != null) {
                try {
                    p.close();
                } catch (SQLException e) {

                }
            }
        }
        double t = System.currentTimeMillis() - atstart;
        double a = t / nq;
        System.err.println("Found " + arows + " in " + t + "ms executed " + nq + " queries");
        System.err.println("Average " + (arows / nq) + " in " + a + "ms");

    }

    public static void main(String[] argv) throws SQLException, NoSuchAlgorithmException,
            UnsupportedEncodingException {
        KeyValueRowsMain tmr = new KeyValueRowsMain();
        String db = "target/testkv";
        tmr.deleteDb(db);
        boolean exists = new File(db).exists();
        tmr.open(db);
        if (!exists) {
            tmr.createTables(30);
        }
        tmr.populateDictionary(1000);
        tmr.loadTable(30, 10000);
        tmr.testSelect(1, 0, 30, 5000);
        tmr.testSelect(2, 0, 30, 5000);
        tmr.testSelect(3, 0, 30, 5000);
        tmr.testSelect(4, 0, 30, 5000);
        tmr.testSelect(5, 0, 30, 5000);
        tmr.testSelect(1, 1, 30, 5000);
        tmr.testSelect(2, 1, 30, 5000);
        tmr.testSelect(3, 1, 30, 5000);
        tmr.testSelect(4, 1, 30, 5000);
        tmr.testSelect(5, 1, 30, 5000);
        tmr.testSelect(1, 2, 30, 5000);
        tmr.testSelect(2, 2, 30, 5000);
        tmr.testSelect(3, 2, 30, 5000);
        tmr.testSelect(4, 2, 30, 5000);
        tmr.testSelect(5, 2, 30, 5000);
        tmr.close();
    }

}
