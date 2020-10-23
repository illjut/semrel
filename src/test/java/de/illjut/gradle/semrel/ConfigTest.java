package de.illjut.gradle.semrel;

import de.illjut.gradle.semrel.*;
import groovy.util.GroovyTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;


public class ConfigTest {

  private SemanticReleaseConfig uut;

  @BeforeEach
  void before() {
    String resourceName = "releaserc-test.yml";
    ClassLoader classLoader = getClass().getClassLoader();
    java.io.File file = new java.io.File(classLoader.getResource(resourceName).getFile());
    this.uut = new SemanticReleaseConfig(file);
  }


  @Test
  public void testConfig() {
    Assertions.assertEquals(this.uut.semanticReleaseVersion, "1337");
  }
}