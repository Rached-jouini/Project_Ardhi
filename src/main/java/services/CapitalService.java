package services;

import models.Capitale;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CapitalService implements ardhi<Capitale> {
    Connection connection;
    public  CapitalService(){
        connection= MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(Capitale capitale)throws SQLException {
        String sql="insert into capital (montant_initial,montant_actuel,devise,date_creation,description) values("+capitale.getMontant_initial()+","+capitale.getMontant_actuel()+",'"+capitale.getDevise()+"','"+Date.valueOf(capitale.getDate_creation())+"','"+capitale.getDescription()+"')";
        Statement statement =connection.createStatement();
        statement.executeUpdate(sql);
    }

    @Override
    public void update(Capitale capitale)throws SQLException {
        String sql ="update capital set montant_initial=?,montant_actuel=?,devise=?,date_creation=?,description=? where id=? ";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setDouble(1, capitale.getMontant_initial());
        preparedStatement.setDouble(2, capitale.getMontant_actuel());
        preparedStatement.setString(3, capitale.getDevise());
        preparedStatement.setDate(4, Date.valueOf(capitale.getDate_creation()));
        preparedStatement.setString(5, capitale.getDescription());
        preparedStatement.setInt(6, capitale.getId());



        preparedStatement.executeUpdate();
    }

    @Override
    public void delete(int id)throws SQLException {
        String sql ="delete from capital where id=? ";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<Capitale> select() throws SQLException{
        List<Capitale>capitales=new ArrayList<>();
        String sql="select * from capital";
        Statement statement=connection.createStatement();
        ResultSet resultSet= statement.executeQuery(sql);
        while (resultSet.next()){
            Capitale capitale =new Capitale();
            capitale.setId(resultSet.getInt("id"));
            capitale.setMontant_initial(resultSet.getDouble("montant_initial"));
            capitale.setMontant_actuel(resultSet.getDouble("montant_actuel"));
            capitale.setDevise(resultSet.getString("devise"));
            capitale.setDate_creation(resultSet.getDate("date_creation").toLocalDate());
            capitale.setDescription(resultSet.getString("description"));
            capitales.add(capitale);
        }
        return capitales;
    }
}
