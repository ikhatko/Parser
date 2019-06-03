package com.ef;

import java.sql.SQLException;

public class Logger {

  public static void log(String message) {
    System.out.println(message);
  }

  public static void log(SQLException ex) {
    System.out.println("SQLException: " + ex.getMessage());
    System.out.println("SQLState: " + ex.getSQLState());
    System.out.println("VendorError: " + ex.getErrorCode());
  }
}
