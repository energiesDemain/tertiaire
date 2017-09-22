package com.ed.cgdd.test.model;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.springframework.jdbc.core.RowMapper;


public class TestMapper implements RowMapper<LigneBat> {
    public LigneBat mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        LigneBat batiment = new LigneBat();
        batiment.setId(resultSet.getString("ID"));
        batiment.setAge(resultSet.getInt("AGE"));
        batiment.setType(resultSet.getString("TYPE"));
        batiment.setValeur(resultSet.getFloat("VALEUR"));
        return batiment;
    }

}
