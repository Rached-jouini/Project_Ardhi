package test;

import utils.MyDataBase;
import java.sql.*;

public class CheckSchema {
    public static void main(String[] args) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            if (conn == null) {
                System.out.println("Connection failed!");
                return;
            }
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("Tables in database:");
            while (rs.next()) {
                System.out.println("- " + rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
