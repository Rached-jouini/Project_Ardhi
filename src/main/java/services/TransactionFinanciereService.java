package services;

import models.TransactionFinanciere;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionFinanciereService implements ardhi<TransactionFinanciere> {
    Connection connection;
    public  TransactionFinanciereService(){
        connection= MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(TransactionFinanciere tf) throws SQLException {
        String sql = "INSERT INTO transaction_financiere (type, montant, date_operation, source_type_id, source_id, description, wallet_id, mode_paiement) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, tf.getType());
        ps.setDouble(2, tf.getMontant());
        
        // Date par defaut : aujourd'hui
        LocalDate date = tf.getDate_operation() != null ? tf.getDate_operation() : LocalDate.now();
        ps.setDate(3, Date.valueOf(date));
        
        // Gestion des cles etrangeres (NULL si pas de valeur)
        if (tf.getSource_type_id() > 0) ps.setInt(4, tf.getSource_type_id());
        else ps.setNull(4, Types.INTEGER);
        
        if (tf.getSource_id() > 0) ps.setInt(5, tf.getSource_id());
        else ps.setNull(5, Types.INTEGER);
        
        ps.setString(6, tf.getDescription());
        
        if (tf.getWalletId() != null && tf.getWalletId() > 0) ps.setInt(7, tf.getWalletId());
        else ps.setNull(7, Types.INTEGER);

        ps.setString(8, tf.getModePaiement());
        
        ps.executeUpdate();
    }

    @Override
    public void update(TransactionFinanciere tf)throws SQLException {
        String sql ="update transaction_financiere set type=?,montant=?,date_operation=?,source_type_id=?,source_id=?,description=? where id=? ";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setString(1, tf.getType());
        preparedStatement.setDouble(2, tf.getMontant());
        preparedStatement.setDate(3, Date.valueOf(tf.getDate_operation()));
        preparedStatement.setInt(4, tf.getSource_type_id());
        preparedStatement.setInt(5, tf.getSource_id());
        preparedStatement.setString(6, tf.getDescription());
        preparedStatement.setInt(7, tf.getId());



        preparedStatement.executeUpdate();
    }

    @Override
    public void delete(int id)throws SQLException {
        String sql ="delete from transaction_financiere where id=? ";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<TransactionFinanciere> select() throws SQLException{
        List<TransactionFinanciere>transactions=new ArrayList<>();
        String sql="select * from transaction_financiere";
        Statement statement=connection.createStatement();
        ResultSet resultSet= statement.executeQuery(sql);
        while (resultSet.next()){
            TransactionFinanciere tf =new TransactionFinanciere();
            tf.setId(resultSet.getInt("id"));
            tf.setType(resultSet.getString("type"));
            tf.setMontant(resultSet.getDouble("montant"));
            tf.setDate_operation(resultSet.getDate("date_operation").toLocalDate());
            tf.setSource_type_id(resultSet.getInt("source_type_id"));
            tf.setSource_id(resultSet.getInt("source_id"));
            tf.setDescription(resultSet.getString("description"));
            
            int wId = resultSet.getInt("wallet_id");
            if (!resultSet.wasNull()) {
                tf.setWalletId(wId);
            }
            tf.setModePaiement(resultSet.getString("mode_paiement"));
            transactions.add(tf);
        }
        return transactions;
    }

    public double getMaintenanceCosts(String equipementNom) throws SQLException {
        String sql = "SELECT SUM(montant) FROM transaction_financiere WHERE type = 'Depense' AND description LIKE ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%Maintenance% " + equipementNom + "%");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 0.0;
    }
}
