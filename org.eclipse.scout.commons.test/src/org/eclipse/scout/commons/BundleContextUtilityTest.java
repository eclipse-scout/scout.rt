package org.eclipse.scout.commons;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for class {@link BundleContextUtility}.
 * 
 * @author awe
 * @since 3.10.0
 */
public class BundleContextUtilityTest {

  /**
   * Test for {@link BundleContextUtility#parseBooleanProperty(String, boolean)} tycho-surefire-plugin tests are always
   * executed in an OSGi environment. Therefore plain junit tests are not
   * included here.
   */
  @Test
  public void testGetBundleContextProperty_WithActivator() {
    final String testProperty = "osgi.install.area";
    String property = BundleContextUtility.getProperty(testProperty);
    Assert.assertNotNull(property);
  }
}
