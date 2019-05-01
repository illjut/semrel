package de.illjut.gradle.semrel;

import java.io.IOException;
import java.io.InputStream;

import org.gradle.api.logging.Logger;

public class LogStream implements Runnable {
  protected final Logger logger;
  protected final InputStream inputStream;

  private StringBuilder logLine;

  public LogStream(Logger logger, InputStream input) {
    this.logger = logger;
    this.inputStream = input;

    this.logLine = new StringBuilder();
  }

  private void processData(byte[] data) {
    String chunk = new String(data);

    String lines[] = chunk.split("\n");
    int size = lines.length;
    boolean endsWithLineBreak = chunk.endsWith("\n");
    if (endsWithLineBreak) {
      size--;
    }
    
    for (int i = 0; i < size; i++) {
      logLine.append(lines[i]);
      this.logger.info(logLine.toString());
      logLine = new StringBuilder();
    }

    if (endsWithLineBreak) {
      this.logLine.append(lines[lines.length - 1]);
    }
  }

  @Override
  public void run() {
    while (!Thread.interrupted()) {
      try {
        this.processData(inputStream.readAllBytes());
      } catch (IOException e) {
        // TODO
        e.printStackTrace();
      }
    }
    this.logger.info(this.logLine.toString());
  }
}