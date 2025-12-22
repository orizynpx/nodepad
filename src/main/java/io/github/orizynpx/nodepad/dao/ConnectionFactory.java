package io.github.orizynpx.nodepad.dao;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionFactory {
    Connection connect() throws SQLException;
}