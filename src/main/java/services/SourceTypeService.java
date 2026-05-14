package services;

import models.SourceType;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SourceTypeService implements ardhi<SourceType> {
    Connection connection;
    public  SourceTypeService(){
        connection= MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(SourceType sourceType)throws SQLException {
        String sql="insert into source_type (nom) values('"+sourceType.getNom()+"')";
        Statement statement =connection.createStatement();
        statement.executeUpdate(sql);
    }

    @Override
    public void update(SourceType sourceType)throws SQLException {
        String sql ="update source_type set nom=? where id=? ";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setString(1, sourceType.getNom());
        preparedStatement.setInt(2, sourceType.getId());



        preparedStatement.executeUpdate();
    }

    @Override
    public void delete(int id)throws SQLException {
        String sql ="delete from source_type where id=? ";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<SourceType> select() throws SQLException{
        List<SourceType>sourceTypes=new ArrayList<>();
        String sql="select * from source_type";
        Statement statement=connection.createStatement();
        ResultSet resultSet= statement.executeQuery(sql);
        while (resultSet.next()){
            SourceType st =new SourceType();
            st.setId(resultSet.getInt("id"));
            st.setNom(resultSet.getString("nom"));
            sourceTypes.add(st);
        }
        return sourceTypes;
    }
}
