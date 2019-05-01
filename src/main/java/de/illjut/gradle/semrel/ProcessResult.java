package de.illjut.gradle.semrel;

import java.io.InputStream;

public class ProcessResult {
  InputStream stdOut;
  InputStream stdErr;
  int exitCode;

  public ProcessResult(int exitCode, InputStream stdOut, InputStream stdErr) {
    this.exitCode = exitCode;
    this.stdOut = stdOut;
    this.stdErr = stdErr;
  }
}