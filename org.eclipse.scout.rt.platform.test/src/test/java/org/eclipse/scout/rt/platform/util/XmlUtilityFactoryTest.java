package org.eclipse.scout.rt.platform.util;

import java.io.IOException;

import org.eclipse.scout.rt.testing.platform.util.XmlFactoriesTestSupport;
import org.junit.Test;

/**
 * <h3>{@link XmlUtilityFactoryTest}</h3>
 */
public class XmlUtilityFactoryTest {
  @Test
  public void testNoFactoriesInCode() throws IOException {
    XmlFactoriesTestSupport test = new XmlFactoriesTestSupport();
    test.doTest();
    test.failOnError();
  }
}
