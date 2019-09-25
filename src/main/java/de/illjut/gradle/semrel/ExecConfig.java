package de.illjut.gradle.semrel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExecConfig {
  private Optional<String> registry = Optional.empty();
  private Optional<String> strictSsl = Optional.empty();

  public static ExecConfig instance() {
    return new ExecConfig();
  }

  private ExecConfig() {
    
  }
  
  public ExecConfig registry(String registry) {
    this.registry = Optional.ofNullable(registry);
    return this;
  }

  public ExecConfig strictSsl(Boolean strictSsl) {
    if (strictSsl == null) {
      this.strictSsl = Optional.empty();
    } else {
      this.strictSsl = Optional.of(String.valueOf(strictSsl.booleanValue()));
    }
    return this;
  }

  private void putToMap(Map<String, String> map, String key, Optional<String> value) {
    if (value.isPresent()) {
      map.put(key, value.get());
    }
  }

  public Map<String, String> buildEnvVarMap() {
    Map<String, String> envVars = new HashMap<String, String>();

    this.putToMap(envVars, "npm_config_registry", this.registry);
    this.putToMap(envVars, "npm_config_strict_ssl", this.strictSsl);

    return envVars;
  }

}