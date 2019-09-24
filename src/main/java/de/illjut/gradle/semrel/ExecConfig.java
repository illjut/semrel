package de.illjut.gradle.semrel;

import java.util.HashMap;
import java.util.Map;

public class ExecConfig {
  private String registry = null;

  public static ExecConfig instance() {
    return new ExecConfig();
  }

  private ExecConfig() {
    
  }
  
  public ExecConfig registry(String registry) {
    this.registry = registry;
    return this;
  }

  public Map<String, String> buildEnvVarMap() {
    Map<String, String> envVars = new HashMap<String, String>();

    envVars.put("npm_config_registry", this.registry);

    return envVars;
  }

}