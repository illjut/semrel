package de.illjut.gradle.semrel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.gradle.api.logging.Logger;


public class NodeExec {
  private final String distBase;
  private final String distHrefTemplate = "{0}/v{1}/node-v{1}-{2}-{3}.{4}";
  private File nodePath = null;

  private final Logger logger;

  public NodeExec(Logger logger) {
    this.distBase = "https://nodejs.org/dist";
    this.logger = logger;
  }

  public NodeExec(String distBase, Logger logger) {
    this.distBase = distBase;
    this.logger = logger;
  }

  String getDownloadHref(String version) {
    String platform = PlatformHelper.getPlatform();
    String archiveType = "tar.gz";
    if (PlatformHelper.isWindows()) {
      archiveType = "zip";
    }
    
    return MessageFormat.format(this.distHrefTemplate, this.distBase, version, platform, PlatformHelper.getArch(), archiveType);
  }

  File download(String version, File locationDir) throws IOException {
    URL url = new URL(getDownloadHref(version));
    locationDir.mkdirs();
    File distFile = new File(locationDir, url.getFile().substring(url.getFile().lastIndexOf('/')+1, url.getFile().length()));

    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
    FileOutputStream fileOutputStream = new FileOutputStream(distFile);
    FileChannel fileChannel = fileOutputStream.getChannel();

    fileOutputStream.getChannel()
      .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

    fileOutputStream.close();
    fileChannel.close();

    return distFile;
  }

  File unpackDist(File distFile, File targetDir) throws ArchiveException, IOException {
    File nodePath = null;

    ArchiveInputStream i;

    if (distFile.getName().endsWith(".tar.gz")) {
      i = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(distFile)));
    } else {
      // try autodetecting
      i = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(new FileInputStream(distFile)));
    }

    ArchiveEntry entry =   null;
    while (( entry = i.getNextEntry()) != null) {
      if (!i.canReadEntryData(entry)) {
        continue;
      }

      String name = entry.getName();
      name = name.replaceFirst("node-v[^-]+-\\w+-\\w+\\/", "node/");
      System.out.println(name);
      File f = new File(targetDir, name);

      if (entry.isDirectory()) {
        if (!f.isDirectory() && !f.mkdirs()) {
          throw new IOException("failed to create directory " + f);
        }
      } else {
        File parent = f.getParentFile();

        if(name.endsWith("/node")) {
          nodePath = parent;
        }

        if(!parent.isDirectory() && !parent.mkdirs()) {
          throw new IOException("failed to create directory " + f);
        }
        try (OutputStream o = Files.newOutputStream(f.toPath())) {
          IOUtils.copy(i, o);
        }
      }
    }

    return nodePath;
  }

  public void setupNode(String nodeVersion, File location) {
    try {
      String downloadUrl = this.getDownloadHref(nodeVersion);
      this.logger.info("downloading nodejs dist from {}", downloadUrl);
      File distFile = this.download(nodeVersion, location);
      this.logger.info("downloaded nodejs dist to {}", distFile);
      
      this.nodePath = this.unpackDist(distFile, location);
      this.logger.info("unpacked nodejs dist. Node path is {}", nodePath);
    } catch (IOException | ArchiveException e) {
      throw new RuntimeException("Failed to setup Node", e);
    }
  }

  private ProcessResult exec(List<String> command, File workDir) throws IOException {
    String pathVar = null;
    String pathValue = null;

    if(this.nodePath != null) {
      this.logger.info("using custom node path {}", this.nodePath);
      if (System.getenv("Path") != null) {
        pathVar = "Path";
      } else {
        pathVar = "PATH";
      }
      pathValue = this.nodePath + File.pathSeparator + System.getenv(pathVar);
    }

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command)
        .directory(workDir)
        .redirectErrorStream(true);
      
      if (nodePath != null) {
        processBuilder.environment().put(pathVar, pathValue);
      }

      Process proc = processBuilder.start();

      Thread logStream = new Thread(new LogStream(this.logger, proc.getInputStream()));
      logStream.start();

      try {
        proc.waitFor();
        logStream.interrupt();
      }
      catch(InterruptedException e) {
        throw new RuntimeException("NodeJS child process was interrupted", e);
      }

      return new ProcessResult(
        proc.exitValue(),
        proc.getErrorStream(),
        proc.getInputStream()
      );
    }
    catch (IOException e) {
      throw e;
    }
  }

  public ProcessResult executeNode(List<String> args, File workDir) throws IOException {
    final String nodeExecutable = "node";

    ArrayList<String> command = new ArrayList<>();
    command.add(nodeExecutable);
    command.addAll(args);

    try {
      return this.exec(command, workDir);
    }
    catch (IOException e) {
      throw e;
    }
  } 

  public ProcessResult executeNpx(List<String> args, List<String> extraPackages, File workDir) throws IOException {
    final String npmExecutable = "npx";

    ArrayList<String> command = new ArrayList<>();
    command.add(npmExecutable);

    if(extraPackages != null) {
      extraPackages.stream().forEach(p -> {
        command.add("--package");
        command.add(p);
      });
    }

    command.addAll(args);
    
    logger.info("executing {}", String.join(" ", command));
    logger.info("using working directory {}", workDir);

    try {
      return this.exec(command, workDir);
    }
    catch (IOException e) {
      throw e;
    }
  }

  public ProcessResult executeNpm(List<String> args, File workDir) throws IOException {
    final String npmExecutable = "npm";

    ArrayList<String> command = new ArrayList<>();
    command.add(npmExecutable);
    command.addAll(args);

    try {
      return this.exec(command, workDir);
    }
    catch (IOException e) {
      throw e;
    }
  }

}