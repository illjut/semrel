package de.illjut.gradle.semrel;

import org.gradle.api.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class LogStreamTest {

  private LogStream uut;
  private Logger loggerMock;

  @BeforeEach
  void before() {
    this.loggerMock = Mockito.mock(Logger.class);
  }


  @Test
  public void testDataProcessor() {
    String testData = "this is a test";
    this.uut = new LogStream(this.loggerMock, null);

    this.uut.processData(testData.getBytes());
    this.uut.processData("abc \n".getBytes());
    this.uut.processData("this is the end.\n".getBytes());

    Mockito.verify(this.loggerMock, Mockito.times(1)).info(Mockito.any());
  }
}