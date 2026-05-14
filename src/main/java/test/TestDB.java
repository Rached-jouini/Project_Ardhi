package test;

import utils.MyDataBase;
import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            if (conn == null) {
                System.out.println("Connection is NULL!");
                return;
            }
            System.out.println("Connection successful!");
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM evenement");
            if (rs.next()) {
                System.out.println("Number of records in 'evenement': " + rs.getInt(1));
            }
            
            rs = st.executeQuery("SELECT * FROM evenement");
            while (rs.next()) {
                System.out.println("Record: " + rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Other Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
