package de.illjut.gradle.semrel

import org.gradle.api.*
import de.illjut.gradle.semrel.*
import java.text.MessageFormat
import java.util.*
import java.io.*
import java.nio.file.*

class NodeSetup {
  private final String distBase;
  private final String distHrefTemplate = "{0}/v{1}/node-v{1}-{2}-{3}.{4}";
  private final String distFilenameTemplate = "node-v{0}-{1}-{2}"
  private final String completeMarkerName = ".complete";

  def project;
  def nodeBinPath;

  NodeSetup(project, distBase = "https://nodejs.org/dist") {
    this.project = project;
    this.distBase = distBase;
  }

  String buildFilename(version) {
    String platform = PlatformHelper.getPlatform();
    return MessageFormat.format(this.distFilenameTemplate, version, platform, PlatformHelper.getArch());
  }

  String buildDownloadUrl(version) {
    String platform = PlatformHelper.getPlatform();
    String archiveType = "tar.gz";
    if (PlatformHelper.isWindows()) {
      archiveType = "zip";
    }
    
    return MessageFormat.format(this.distHrefTemplate, this.distBase, version, platform, PlatformHelper.getArch(), archiveType);
  }

  void copyNpmRc(File dest) {
    def npmrc = new File(project.rootProject.projectDir, ".npmrc")
    def target = new File(dest, ".npmrc");
    if (npmrc.exists()) {
      Files.copy(
        npmrc.toPath(),
        target.toPath(),
        StandardCopyOption.REPLACE_EXISTING
      );
    } else if (target.exists()) {
      Files.delete(target.toPath());
    }
  }

  void setupNode(String version, File dest) {
    def completeMarker = new File(dest, this.completeMarkerName)

    if (!completeMarker.exists()) { // download node
      def distFile = this.downloadNode(version, dest)
      this.unpack(distFile, dest)
      completeMarker.createNewFile()
    } else {
      this.project.logger.info "skipped node download (completion marker found)"
      this.nodeBinPath = new File(dest, "${this.buildFilename(version)}/bin")
    }
  }

  File downloadNode(String version, File dest) {
    URL url = new URL(buildDownloadUrl(version));
    File distFile = new File(dest, url.getFile().substring(url.getFile().lastIndexOf('/')+1, url.getFile().length()));

    this.project.ant.get(
      src: url,
      dest: distFile,
      quiet: true
    )

    return distFile;
  }

  File unpack(File src, File dest) {
    String archiveName = src.getName()
      .replace(".zip", '')
      .replace(".tar.gz", '')
      .replace(".tar", '');

    if (src.name.endsWith(".tar.gz")) {
      this.project.ant.untar(
        src: src,
        dest: dest,
        compression: "gzip"
      )
    } else {
      this.project.ant.unzip(
        src: src,
        dest: dest
      )
    }

    this.nodeBinPath = new File(dest, archiveName + "/bin/");

    if (PlatformHelper.isUnix()) {
      for (String symlink : [ "npm", "npx"]) {
        Path target = (new File(this.nodeBinPath, symlink)).toPath();
        if (Files.deleteIfExists(target)) {
          Files.createSymbolicLink(
            target,
            nodeBinPath.toPath().relativize(
              new File(this.nodeBinPath.parentFile, "lib/node_modules/npm/bin/${symlink}-cli.js")
                .toPath()
            )
          )
        }
      }
      
      for (String executable : [ "node", "npm", "npx" ]) {
        new File(this.nodeBinPath, executable).setExecutable(true);
      }
    }

    return this.nodeBinPath;
  }
}