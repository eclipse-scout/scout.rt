package org.eclipse.scout.net;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

/**
 * Test for {@link NetActivator}
 */
public class NetActivatorTest {

  private static final String TEST_URL = "http://www.google.ch";

  /**
   * Test connecting to an url using {@link NetActivator}
   * 
   * @throws IOException
   */
  @Test
  public void testNetActivator() throws IOException {
    NetActivator.install();
    URL url = new URL(TEST_URL);
    url.openConnection().getInputStream();
  }

}
