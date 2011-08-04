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
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

public class MultiRowsMain {

    
    private Connection connection;
    private String[] dictionary;

    public MultiRowsMain() {
    }
    
    public void deleteDb(String file) {
        FileUtils.deleteQuietly(new File(file));
    }
    
    public void open(String file) throws SQLException {
        connection = DriverManager.getConnection("jdbc:derby:"+file+"/db;create=true", "sa", "");        
    }
    
    public void createTables(int columns) throws SQLException {
        Statement s = connection.createStatement();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE cn_css_index (");
        sql.append("id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),");
        sql.append("rid varchar(32) NOT NULL,");
        for ( int i = 0; i < columns; i++ ) {
            sql.append("v").append(i).append(" varchar(780),");
        }
        sql.append("primary key(id))");
        s.execute(sql.toString());
        s.execute("CREATE UNIQUE INDEX cn_css_index_i ON cn_css_index (rid)");
        for ( int i = 0; i < columns; i++) {
            s.execute("CREATE INDEX cn_css_index_v"+i+" ON cn_css_index (v"+i+")");
        }
        s.close();
    }
    

    
    public void populateDictionary(int size) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        dictionary = new String[size];
        MessageDigest md = MessageDigest.getInstance("SHA1");
        for ( int i = 0; i < size; i++) {
            dictionary[i] = Base64.encodeBase64URLSafeString(md.digest(String.valueOf("Dict"+i).getBytes("UTF-8")));
        }
    }
    
    public void loadTable(int columns, int records) throws SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into cn_css_index (rid");
        for ( int i = 0; i < columns; i++ ) {
            sb.append(",v").append(i);
        }
        sb.append(") values ( ?");
        for ( int i = 0; i < columns; i++ ) {
            sb.append(",?");
        }
        sb.append(")");
        PreparedStatement p = connection.prepareStatement(sb.toString());
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        SecureRandom sr = new SecureRandom();
        long cs = System.currentTimeMillis();
        ResultSet rs = connection.createStatement().executeQuery("select count(*) from cn_css_index");
        rs.next();
        int nrows = rs.getInt(1);
        for ( int i = 0+nrows; i < records+nrows; i++) {
            String rid = Base64.encodeBase64URLSafeString(sha1.digest(String.valueOf("TEST"+i).getBytes("UTF-8")));
            p.clearParameters();
            p.setString(1, rid);
            for ( int j = 2; j <= columns+1; j++) {
                if ( sr.nextBoolean() ) {
                    p.setString(j,  dictionary[sr.nextInt(dictionary.length)]);
                } else {
                    p.setNull(j, Types.VARCHAR);
                }
            }
            p.execute();
            
            if ( i%100 == 0) {
                long ct = System.currentTimeMillis();
                System.err.println("Commit "+i+" "+(ct-cs)+" ms/100");
                cs = ct;
                connection.commit();
            }
        }
        p.close();   
    }
    private void close() throws SQLException {
        connection.close();
    }

    public void testSelect(int ncols, int sorts, int columns, long timeToLive) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("select rid from cn_css_index where ");
        SecureRandom sr = new SecureRandom();
        Set<Integer> used = new LinkedHashSet<Integer>();
        while(used.size() < ncols ) {
            int c = sr.nextInt(columns);
            if ( !used.contains(c) ) {
                used.add(c);
            }
        }
        Integer[] cnums = used.toArray(new Integer[ncols]);
        for ( int i = 0; i < ncols-1; i++ ) {
            
            sb.append("v").append(cnums[i]).append(" = ? AND ");
        }
        sb.append("v").append(cnums[ncols-1]).append(" = ? ");
        if ( sorts > 0 ) {
            sb.append(" order by ");
            used.clear();
            while(used.size() < sorts ) {
                int c = sr.nextInt(columns);
                if ( !used.contains(c) ) {
                    used.add(c);
                }
            }
            cnums = used.toArray(new Integer[ncols]);
            for ( int i = 0; i < sorts-1; i++ ) {
                sb.append("v").append(cnums[i]).append(",");
            }
            sb.append("v").append(cnums[sorts-1]);
        }
        System.err.println(sb.toString());
        PreparedStatement p = connection.prepareStatement(sb.toString());
        long atstart = System.currentTimeMillis();
        long endTestTime = atstart+timeToLive;
        int nq = 0;
        int arows = 0;
        while(System.currentTimeMillis() < endTestTime) {
            p.clearParameters();
            for ( int i = 1; i <= ncols; i++ ) {
                p.setString(i, dictionary[sr.nextInt(dictionary.length)]);
            }
            ResultSet rs = p.executeQuery();
            int rows = 0;
            while(rs.next()) {
                rows++;
            }
            arows += rows;
            nq++;
            rs.close();
        }
        double t = System.currentTimeMillis()-atstart;
        double a = t/nq;
        System.err.println("Found "+arows+" in "+t+"ms executed "+nq+" queries");
        System.err.println("Average "+(arows/nq)+" in "+a+"ms");
        
    }
    
    public static void main(String[] argv) throws SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
        MultiRowsMain tmr = new MultiRowsMain();
        String db = "target/testwide";
        boolean exists = new File("target/testwide").exists();
        tmr.open(db);
        if ( ! exists ) {
            tmr.createTables(30);
        }
        tmr.populateDictionary(1000);
        tmr.loadTable(30, 200000);
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
