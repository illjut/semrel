package de.illjut.gradle.semrel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;


public class NodeExec {
  private static final String NODE_EXECUTABLE = "node";
  private static final String NPX_EXECUTABLE = "npx";
  private static final String NPM_EXECUTABLE = "npm";

  private File nodePath = null;

  private final Logger logger;

  public NodeExec(Project project, File nodePath) {
    this.nodePath = nodePath;
    this.logger = project.getLogger();
  }

  public void setNodePath(File path) {
    this.nodePath = path;
  }

  boolean isNodeAvailable() {
    ArrayList<String> command = new ArrayList<>();
    command.add("node");
    command.add("-v");

    try {
      ProcessResult result = this.exec(command, new File("."), null);
      return result.exitCode == 0;
    }
    catch (IOException e) {
      this.logger.warn("Error while poking NodeJS. Assuming it is not available.");
      return false;
    }
  }

  private ProcessResult exec(List<String> command, File workDir) throws IOException {
    return this.exec(command, workDir, this.nodePath);
  }

  private ProcessResult exec(List<String> command, File workDir, File nodePathOverride) throws IOException {
    String pathVar = null, pathValue = null;

    if (nodePathOverride != null) {
      command.set(0, this.nodePath.getAbsolutePath() + "/" + command.get(0));
      this.logger.info("using executable {}", command.get(0));

      this.logger.info("using custom node path {}", nodePathOverride);
      if (System.getenv("Path") != null) {
        pathVar = "Path";
      } else {
        pathVar = "PATH";
      }
      pathValue = nodePathOverride + File.pathSeparator + System.getenv(pathVar);
    }

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command)
        .directory(workDir)
        .redirectErrorStream(true);

      if (pathVar != null) processBuilder.environment().put(pathVar, pathValue);

      Process proc = processBuilder.start();

      LogStream logStream = new LogStream(this.logger, proc.getInputStream());
      
      ExecutorService logExecutor = Executors.newSingleThreadExecutor();
      Future<List<String>> logs = logExecutor.submit(logStream);
      
      try {
        proc.waitFor();
        proc.getInputStream().close();
        logExecutor.shutdown();

        return new ProcessResult(
          proc.exitValue(),
          logs.get()
        );
      }
      catch(ExecutionException | InterruptedException e) {
        throw new RuntimeException("NodeJS child process was interrupted", e);
      }

    }
    catch (IOException e) {
      throw e;
    }
  }

  public ProcessResult executeNode(List<String> args, File workDir) throws IOException {
    ArrayList<String> command = new ArrayList<>();
    command.add(NODE_EXECUTABLE);
    command.addAll(args);

    try {
      return this.exec(command, workDir);
    }
    catch (IOException e) {
      throw e;
    }
  } 

  public ProcessResult executeNpx(List<String> args, List<String> extraPackages, File workDir) throws IOException {
    ArrayList<String> command = new ArrayList<>();
    if (PlatformHelper.isWindows()) {
      command.add(NPX_EXECUTABLE + ".cmd");
    } else {
      command.add(NPX_EXECUTABLE);
    }

    if(extraPackages != null) {
      extraPackages.stream().forEach(p -> {
        command.add("--package");
        command.add(p);
      });
    }

    command.addAll(args);
    
    logger.info("using working directory {}", workDir);
    logger.info("executing {}", String.join(" ", command));

    try {
      return this.exec(command, workDir);
    }
    catch (IOException e) {
      throw e;
    }
  }

  public ProcessResult executeNpm(List<String> args, File workDir) throws IOException {
    ArrayList<String> command = new ArrayList<>();
    if (PlatformHelper.isWindows()) {
      command.add(NPM_EXECUTABLE + ".cmd");
    } else {
      command.add(NPM_EXECUTABLE);
    }
    command.addAll(args);

    try {
      return this.exec(command, workDir);
    }
    catch (IOException e) {
      throw e;
    }
  }


}