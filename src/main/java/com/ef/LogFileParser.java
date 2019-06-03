package com.ef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogFileParser {
  private static final String DATE_OUTPUT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern(DATE_OUTPUT_PATTERN);

  public List<LogEntity> parseLogFile(String pathToFile) {
    List<LogEntity> result = new ArrayList<>();
    try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

      result = stream.map(s -> {
        final String[] split = s.split("\\|");
        final LocalDateTime date = LocalDateTime.parse(split[0], formatterOutput);
        final String ip = split[1];
        final String request = split[2];
        final Integer status = Integer.valueOf(split[3]);
        final String userAgent = split[4];
        return new LogEntity(date, ip, request, status, userAgent);
      }).collect(Collectors.toList());

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }

    return result;
  }
}
