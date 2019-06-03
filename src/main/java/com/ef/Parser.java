package com.ef;

import java.util.Collection;
import java.util.List;

public class Parser {
  private static ArgsParser argsParser = new ArgsParser();
  private static LogFileParser fileParser;
  private static DBConnector dbConnector;

  public static void main(String[] args) throws Exception {
    argsParser.parseArgs(args);
    dbConnector = new DBConnector("test", "test"); //change depends on db user credentials
    if (argsParser.getAccessLog() != null) {
      fileParser = new LogFileParser();
      final List<LogEntity> logEntities = fileParser.parseLogFile(argsParser.getAccessLog());
      dbConnector.insertLogs(logEntities);
    }
    Collection<String> result = dbConnector.selectIps(argsParser.getStartDate(), argsParser.getDuration(), argsParser.getThreshold());
    result.forEach(Logger::log);
  }
}
