/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.osgi;

import org.eclipse.core.runtime.Platform;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class BundleTestTest extends AbstractBundleTest {

  private static final String PLUGIN_ID_1 = "a.bundle1";
  private static final String PLUGIN_ID_2 = "a.bundle2";
  private static final String PLUGIN_ID_3 = "a.bundle3";
  private static final String PLUGIN_VERSION = "1.0.0";

  @Test
  public void testAbstractBundleTest_base() throws Exception {
    // install
    Bundle aBundle = installBundle(PLUGIN_ID_1, PLUGIN_VERSION);
    Assert.assertNotNull(aBundle);
    Assert.assertEquals(Bundle.INSTALLED, aBundle.getState());
    Assert.assertNull(Platform.getBundle(PLUGIN_ID_1));

    // resolve installed
    resolveBundles(aBundle);
    Assert.assertEquals(Bundle.RESOLVED, aBundle.getState());
    Assert.assertSame(aBundle, Platform.getBundle(PLUGIN_ID_1));

    // uninstall
    uninstallBundles(aBundle);
    Assert.assertEquals(Bundle.UNINSTALLED, aBundle.getState());
    Assert.assertNull(Platform.getBundle(PLUGIN_ID_1));

    // resolve uninstalled
    resolveBundles(aBundle);
    Assert.assertEquals(Bundle.UNINSTALLED, aBundle.getState());
    Assert.assertNull(Platform.getBundle(PLUGIN_ID_1));
  }

  @Test
  public void testAbstractBundleTest_multiDependencies() throws Exception {
    Bundle aBundle1 = installBundle(PLUGIN_ID_1, PLUGIN_VERSION);
    Bundle aBundle2 = installBundle(PLUGIN_ID_2, PLUGIN_VERSION, PLUGIN_ID_1);
    Bundle aBundle3 = installBundle(PLUGIN_ID_3, PLUGIN_VERSION, PLUGIN_ID_2, PLUGIN_ID_1);
    resolveBundles(aBundle1, aBundle2, aBundle3);
    Assert.assertSame(aBundle1, Platform.getBundle(PLUGIN_ID_1));
    Assert.assertSame(aBundle2, Platform.getBundle(PLUGIN_ID_2));
    Assert.assertSame(aBundle3, Platform.getBundle(PLUGIN_ID_3));
    uninstallBundles(aBundle1, aBundle2, aBundle3);
  }
}
