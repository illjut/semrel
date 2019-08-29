package de.illjut.gradle.semrel

import org.gradle.api.*
import de.illjut.gradle.semrel.*
import java.text.MessageFormat
import java.util.*
import java.io.*

class NodeSetup {
  private final String distBase;
  private final String distHrefTemplate = "{0}/v{1}/node-v{1}-{2}-{3}.{4}";

  def project;
  def nodeBinPath;

  NodeSetup(project, distBase = "https://nodejs.org/dist") {
    this.project = project;
    this.distBase = distBase;
  }

  String buildDownloadUrl(version) {
    String platform = PlatformHelper.getPlatform();
    String archiveType = "tar.gz";
    if (PlatformHelper.isWindows()) {
      archiveType = "zip";
    }
    
    return MessageFormat.format(this.distHrefTemplate, this.distBase, version, platform, PlatformHelper.getArch(), archiveType);
  }

  void setupNode(String version, File dest) {
    def distFile = this.downloadNode(version, dest);
    this.unpack(distFile, dest);
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
      for (String executable : [ "node", "npm", "npx" ]) {
        new File(this.nodeBinPath, executable).setExecutable(true);
      }
    }

    return this.nodeBinPath;
  }
}