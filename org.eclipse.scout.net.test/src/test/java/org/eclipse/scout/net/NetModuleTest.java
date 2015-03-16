package org.eclipse.scout.net;

import java.io.IOException;
import java.net.URL;

import org.eclipse.scout.net.internal.NetModule;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link NetModule}
 */
@RunWith(PlatformTestRunner.class)
public class NetModuleTest {
  private static final String TEST_URL = "http://www.eclipse.org";

  /**
   * Test connecting to an url using {@link NetModule}
   */
  @Test
  public void testNetActivator() throws IOException {
    URL url = new URL(TEST_URL);
    url.openConnection().getInputStream();
  }
}
