package com.ef;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ArgsParser {
  //parsed params
  private LocalDateTime startDate; //yyyy-MM-dd.HH:mm:ss
  private String duration; //"hourly | daily"
  private Integer threshold;
  private String accessLog;

  //input params
  private static final String START_DATE = "startDate";
  private static final String DURATION = "duration";
  private static final String THRESHOLD = "threshold";
  private static final String ACCESSLOG = "accesslog";

  private static Map<String, String> paramMap = new HashMap<>();
  private static final String DATE_INPUT_PATTERN = "yyyy-MM-dd.HH:mm:ss";
  private static final String PARAM_DELIMITER = "=";
  private static final DateTimeFormatter FORMATTER_INPUT = DateTimeFormatter.ofPattern(DATE_INPUT_PATTERN);

  public void parseArgs(String[] args) throws Exception {
    for (String arg : args) {
      if (arg.startsWith("--")) {
        String substring = arg.substring(2);
        String[] split = substring.split(PARAM_DELIMITER);
        paramMap.put(split[0], split[1]);
      }
    }

    this.accessLog = paramMap.get(ACCESSLOG);

    this.duration = paramMap.get(DURATION);
    if (!this.duration.equals("hourly") && !this.duration.equals("daily")) {
      throw new Exception(getErrorMessage(DURATION, "`hourly` or `daily`"));
    }

    try {
      this.threshold = Integer.valueOf(paramMap.get(THRESHOLD));
    } catch (NumberFormatException e) {
      throw new Exception(getErrorMessage(THRESHOLD, " must be an integer"));
    }

    try {
      this.startDate = LocalDateTime.parse(paramMap.get(START_DATE), FORMATTER_INPUT);
    } catch (DateTimeParseException e) {
      throw new Exception(getErrorMessage(START_DATE ,DATE_INPUT_PATTERN + " format"));
    }

    Logger.log("Input parameters -> " + paramMap);
  }

  private String getErrorMessage(String param, String message) {
    return "Parameter " + param + " must be " + message;
  }

  public LocalDateTime getStartDate() {
    return startDate;
  }

  public String getDuration() {
    return duration;
  }

  public Integer getThreshold() {
    return threshold;
  }

  public String getAccessLog() {
    return accessLog;
  }
}
