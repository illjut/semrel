package de.illjut.gradle.semrel;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.logging.Logger;

public class LogStream implements Callable<List<String>> {
  protected final Logger logger;
  protected final InputStream inputStream;

  private LinkedList<String> log;
  private StringBuilder logLine;

  public LogStream(Logger logger, InputStream input) {
    this.logger = logger;
    this.inputStream = input;

    this.logLine = new StringBuilder();
    this.log = new LinkedList<String>();
  }

  private void addLogLine(String line) {
    if (line.trim().length() > 0) {
      this.log.add(line);
      this.logger.info(line);
    }
  }

  protected void processData(byte[] data) {
    String chunk = new String(data);

    String lines[] = chunk.split("\n");
    int size = lines.length;
    boolean endsWithLineBreak = chunk.endsWith("\n");
    if (endsWithLineBreak) {
      size--;
    }
    
    for (int i = 0; i < size; i++) {
      logLine.append(lines[i]);
      this.addLogLine(logLine.toString());
      logLine = new StringBuilder();
    }

    if (endsWithLineBreak) {
      this.logLine.append(lines[lines.length - 1]);
    }
  }

  @Override
  public List<String> call() {
    try {
      while(inputStream.available() >= 0) {
        this.processData(inputStream.readAllBytes());
      }
    } catch (IOException e) {
      logger.debug("input stream reached end", e);
    }
    this.addLogLine(this.logLine.toString());

    return this.log;
  }
}