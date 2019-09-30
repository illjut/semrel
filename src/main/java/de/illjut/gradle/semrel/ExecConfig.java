package de.illjut.gradle.semrel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExecConfig {
  private Optional<String> registry = Optional.empty();
  private Optional<String> strictSsl = Optional.empty();
  private Optional<Map<String, Object>> envVars = Optional.empty();

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

  public ExecConfig envVars(Map<String, Object> envVars) {
    this.envVars = Optional.ofNullable(envVars);

    return this;
  }

  private void putToMap(Map<String, String> map, String key, Optional<String> value) {
    if (value.isPresent()) {
      map.put(key, value.get());
    }
  }

  public Map<String, String> buildEnvVarMap() {
    Map<String, String> result = new HashMap<String, String>();

    this.putToMap(result, "npm_config_registry", this.registry);
    this.putToMap(result, "npm_config_strict_ssl", this.strictSsl);

    if (this.envVars.isPresent()) {
      this.envVars.get().forEach((key, value) -> {
        result.put(key, String.valueOf(value));
      });
    }

    return result;
  }

}