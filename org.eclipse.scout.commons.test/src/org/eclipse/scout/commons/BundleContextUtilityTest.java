package org.eclipse.scout.commons;

import java.lang.reflect.Field;

import org.easymock.EasyMock;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.internal.Activator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Tests for class {@link BundleContextUtility}.
 * 
 * @author awe
 * @since 3.10.0
 */
public class BundleContextUtilityTest {

  Activator activator = new Activator();

  @Test
  public void testGetBundleContextProperty_NoActivator() {
    Assert.assertNull(BundleContextUtility.getProperty("foo"));
  }

  @Test
  public void testGetBundleContextProperty_WithActivator() throws Exception {
    Bundle bundle = EasyMock.createMock(Bundle.class);
    BundleContext bundleContext = EasyMock.createMock(BundleContext.class);

    // The first two calls are required because of the static initializer
    // block of SwingUtility
    bundle.getBundleContext();
    EasyMock.expectLastCall().andReturn(bundleContext);
    EasyMock.expect(bundleContext.getProperty("scout.ui.layout.resetBoundsOnInvalidate")).andReturn("true");

    bundle.getBundleContext();
    EasyMock.expectLastCall().andReturn(bundleContext);
    EasyMock.expect(bundleContext.getProperty("scout.ui.verifyInputOnWindowClosed")).andReturn("false");

    bundle.getBundleContext();
    EasyMock.expectLastCall().andReturn(bundleContext);
    EasyMock.expect(bundleContext.getProperty("foo")).andReturn("bar");
    EasyMock.replay(bundleContext, bundle);

    // Prepare the Activator so it looks like it has been initialized by the
    // OSGI framework.
    setPlugin(activator);
    Field f = Plugin.class.getDeclaredField("bundle");
    f.setAccessible(true);
    f.set(activator, bundle);

    Assert.assertEquals("bar", BundleContextUtility.getProperty("foo"));
  }

  private void setPlugin(Activator activator) throws Exception {
    Field f = Activator.class.getDeclaredField("m_plugin");
    f.setAccessible(true);
    f.set(Activator.class, activator);
  }

  /**
   * Reset the Activator after each test method, so the order in which the
   * methods are executed does not matter. Otherwise the test <code>testGetBundleContextProperty_NoActivator</code>
   * could fail.
   * 
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    setPlugin(null);
  }

}
