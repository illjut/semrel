package de.illjut.gradle.semrel;

import java.util.List;

public class ProcessResult {
  List<String> log;
  int exitCode;

  public ProcessResult(int exitCode, List<String> log) {
    this.exitCode = exitCode;
    this.log = log;
  }

  public boolean isSuccess() {
    return exitCode == 0;
  }
}