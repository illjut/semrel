package de.illjut.gradle.semrel

import java.util.Map
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import org.gradle.api.*

class SemanticReleaseConfig {
  private final String nodeVersion
  private final String branch
  private final boolean downloadNode
  private final boolean autoDetectNode
  private final List<String> packages
  private final distUrl
  private final npmConfig

  SemanticReleaseConfig(File configFile = new File(".releaserc.yml")) {
    Yaml yaml = new Yaml();

    if (!configFile.exists()) {
      throw new GradleScriptException("could not load ${configFile}",
        new Exception("file not found")
      )
    }

    Map<String, Object> config = yaml.load(new FileReader(configFile))
    this.branch = config.branch
    this.downloadNode = config.gradle?.node?.download == true
    this.autoDetectNode = config.gradle?.node?.detect ?: false
    this.nodeVersion = config.gradle?.node?.version ?: '10.16.3'
    this.npmConfig = config.gradle?.config;
    
    if (config.gradle?.node?.packages != null) {
      this.packages = config.gradle.node.packages
    }

    this.distUrl = config.gradle?.node?.distUrl ?: System.getenv("SR_NODE_DISTURL")
  }

}