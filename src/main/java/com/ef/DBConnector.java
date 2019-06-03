package com.ef;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ef.Logger.log;

public class DBConnector {
  private final static String CONNECTION_URL = "jdbc:mysql://localhost:3306/parser?user=%s&password=%s&serverTimezone=CET&rewriteBatchedStatements=true";
  private final static String INSERT_LOG_QUERY = "INSERT INTO logs VALUES (?,?,?,?,?)";
  private final static String INSERT_BLOCKED_IPS_QUERY = "INSERT INTO blocked (ip, reason) VALUES (?,?)";
  private final static String SELECT_IP_QUERY = "SELECT over.ip FROM " +
      "(SELECT ip, COUNT(ip) as ip_count FROM parser.logs WHERE log_date BETWEEN ? AND ? GROUP BY ip) AS over " +
      "WHERE over.ip_count > ?;";

  private final String dbUser;
  private final String dbPassword;

  private final int BATCH_SIZE = 2000;
  private int count = 0;

  public DBConnector(String dbUser, String dbPassword) {
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
  }

  public void insertLogs(List<LogEntity> logs) {
    long start = System.currentTimeMillis();
    Connection connection = getDbConnection();

    try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_LOG_QUERY)){
      log("Found " + logs.size() + " log entities.");
      log("Inserting logs to database...");

      logs.forEach(logEntity -> {
        try {
          preparedStatement.setTimestamp(1, Timestamp.valueOf(logEntity.getDate()));
          preparedStatement.setString(2, logEntity.getIp());
          preparedStatement.setString(3, logEntity.getRequest());
          preparedStatement.setInt(4, logEntity.getStatus());
          preparedStatement.setString(5, logEntity.getUserAgent());
          preparedStatement.addBatch();

          if (++count % BATCH_SIZE == 0) {
            preparedStatement.executeBatch();
          }
        } catch (SQLException e) {
          log(e);
        }
      });

      preparedStatement.executeBatch();

    } catch (SQLException e) {
      log(e);
    } finally {
      count = 0;
      try {
        connection.close();
      } catch (SQLException e) {
        log(e);
      }
    }

    log("Completed in " + (System.currentTimeMillis() - start) + " ms");
  }

  public Collection<String> selectIps(LocalDateTime startDate, String duration, Integer threshold) {
    Map<String, String> result = new HashMap<>();
    Connection connection = getDbConnection();

    try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_IP_QUERY)) {

      Timestamp from = Timestamp.valueOf(startDate);
      Timestamp to = duration.equals("hourly") ? Timestamp.valueOf(startDate.plusHours(1).minusSeconds(1))
          : Timestamp.valueOf(startDate.plusDays(1).minusSeconds(1));

      preparedStatement.setTimestamp(1, from);
      preparedStatement.setTimestamp(2, to);
      preparedStatement.setInt(3, threshold);

      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        String ip = resultSet.getString("ip");
        result.put(ip, String.format("%s blocked because it's reached threshold = %s queries %s", ip, threshold, duration));
      }

      this.saveResults(result);

    } catch (SQLException e) {
      log(e);
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        log(e);
      }
    }

    return result.values();
  }

  private void saveResults(Map<String, String> result) {
    Connection connection = getDbConnection();

    try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_BLOCKED_IPS_QUERY)) {

      result.forEach((ip, reason) -> {
        try {
          preparedStatement.setString(1, ip);
          preparedStatement.setString(2, reason);
          preparedStatement.executeUpdate();

        } catch (SQLException e) {
          log(e);
        }
      });

    } catch (SQLException e) {
      log(e);
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        log(e);
      }
    }
  }

  private Connection getDbConnection() {
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(String.format(CONNECTION_URL, dbUser, dbPassword));
    } catch (SQLException e) {
      log(e);
    }
    return connection;
  }
}
