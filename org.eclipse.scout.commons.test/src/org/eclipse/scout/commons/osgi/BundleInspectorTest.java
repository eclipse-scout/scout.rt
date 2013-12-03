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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

@Ignore
public class BundleInspectorTest extends AbstractBundleTest {

  @Test
  public void testGetOrderedBundleDependencyList_oneBundle() throws Exception {
    Bundle aBundle = installBundle("a.bundle", "1.0.0");
    try {
      resolveBundles(aBundle);
      Bundle[] bundles = BundleInspector.getOrderedBundleList("a");
      assertNotNull(bundles);
      assertEquals(getBundleContext().getBundles().length, bundles.length);
      assertSame(aBundle, bundles[0]);
    }
    finally {
      uninstallBundles(aBundle);
    }
  }

  @Test
  public void testGetOrderedBundleDependencyList_twoDependentBundles() throws Exception {
    Bundle aBundle2 = installBundle("a.bundle2", "1.0.0", "a.bundle1");
    Bundle aBundle1 = installBundle("a.bundle1", "1.0.0");
    try {
      resolveBundles(aBundle2, aBundle1);
      Bundle[] bundles = BundleInspector.getOrderedBundleList("a");
      assertNotNull(bundles);
      assertEquals(getBundleContext().getBundles().length, bundles.length);
      assertSame(aBundle2, bundles[0]);
      ScoutAssert.assertOrder(new Bundle[]{aBundle2, aBundle1}, bundles);
    }
    finally {
      uninstallBundles(aBundle1, aBundle2);
    }
  }

  @Test
  public void testGetOrderedBundleDependencyList_indirectDependencies() throws Exception {
    Bundle aBundle1 = installBundle("a.bundle1", "1.0.0");
    Bundle aBundle2 = installBundle("a.bundle2", "1.0.0", "a.bundle1");
    Bundle aBundle3 = installBundle("a.bundle3", "1.0.0", "a.bundle2", "a.bundle1");
    try {
      resolveBundles(aBundle1, aBundle2, aBundle3);
      Bundle[] bundles = BundleInspector.getOrderedBundleList("a");
      assertNotNull(bundles);
      assertEquals(getBundleContext().getBundles().length, bundles.length);
      assertSame(aBundle3, bundles[0]);
      ScoutAssert.assertOrder(new Bundle[]{aBundle3, aBundle2, aBundle1}, bundles);
    }
    finally {
      uninstallBundles(aBundle1, aBundle2, aBundle3);
    }
  }

  @Test
  public void testGetOrderedBundleDependencyList_twoDependencyGraphs() throws Exception {
    Bundle aBundle1 = installBundle("a.bundle1", "1.0.0");
    Bundle aBundle2 = installBundle("a.bundle2", "1.0.0", "a.bundle1");
    Bundle aBundle3 = installBundle("a.bundle3", "1.0.0", "a.bundle2");
    Bundle aBundle4 = installBundle("a.bundle4", "1.0.0", "a.bundle2");
    Bundle aBundle5 = installBundle("a.bundle5", "1.0.0", "a.bundle2");

    Bundle bBundle1 = installBundle("b.bundle1", "1.0.0");
    Bundle bBundle2 = installBundle("b.bundle2", "1.0.0", "b.bundle1");
    Bundle bBundle3 = installBundle("b.bundle3", "1.0.0", "b.bundle2", "b.bundle1");

    try {
      resolveBundles(aBundle1, aBundle2, aBundle3, aBundle4, aBundle5, bBundle1, bBundle2, bBundle3);
      Bundle[] bundles = BundleInspector.getOrderedBundleList("a");
      assertNotNull(bundles);
      ScoutAssert.assertOrder(new Bundle[]{aBundle3, aBundle2, aBundle1, bBundle3, bBundle2, bBundle1}, bundles);
      ScoutAssert.assertOrder(new Bundle[]{aBundle4, aBundle2, aBundle1, bBundle3, bBundle2, bBundle1}, bundles);
      ScoutAssert.assertOrder(new Bundle[]{aBundle5, aBundle2, aBundle1, bBundle3, bBundle2, bBundle1}, bundles);

      bundles = BundleInspector.getOrderedBundleList("b");
      assertNotNull(bundles);
      assertEquals(getBundleContext().getBundles().length, bundles.length);
      assertSame(bBundle3, bundles[0]);
      ScoutAssert.assertOrder(new Bundle[]{bBundle3, bBundle2, bBundle1, aBundle3, aBundle2, aBundle1}, bundles);
      ScoutAssert.assertOrder(new Bundle[]{bBundle3, bBundle2, bBundle1, aBundle4, aBundle2, aBundle1}, bundles);
      ScoutAssert.assertOrder(new Bundle[]{bBundle3, bBundle2, bBundle1, aBundle5, aBundle2, aBundle1}, bundles);
    }
    finally {
      uninstallBundles(aBundle1, aBundle2, aBundle3, aBundle4, aBundle5, bBundle1, bBundle2, bBundle3);
    }
  }

  @Test
  public void testGetOrderedBundleDependencyList_multiPrifixList() throws Exception {
    Bundle aBundle1 = installBundle("a.bundle1", "1.0.0");
    Bundle aBundle2 = installBundle("a.bundle2", "1.0.0", "a.bundle1");
    Bundle aBundle3 = installBundle("a.bundle3", "1.0.0", "a.bundle2");
    Bundle aBundle4 = installBundle("a.bundle4", "1.0.0", "a.bundle2");
    Bundle aBundle5 = installBundle("a.bundle5", "1.0.0", "a.bundle2");

    Bundle bBundle1 = installBundle("b.bundle1", "1.0.0");
    Bundle bBundle2 = installBundle("b.bundle2", "1.0.0", "b.bundle1");
    Bundle bBundle3 = installBundle("b.bundle3", "1.0.0", "b.bundle2", "b.bundle1");

    Bundle cBundle1 = installBundle("c.bundle1", "1.0.0");
    Bundle cBundle2 = installBundle("c.bundle2", "1.0.0", "c.bundle1");
    Bundle cBundle3 = installBundle("c.bundle3", "1.0.0", "c.bundle2");

    try {
      resolveBundles(aBundle1, aBundle2, aBundle3, aBundle4, aBundle5, bBundle1, bBundle2, bBundle3, cBundle1, cBundle2, cBundle3);
      Bundle[] bundles = BundleInspector.getOrderedBundleList("c", "b");
      assertNotNull(bundles);
      assertEquals(getBundleContext().getBundles().length, bundles.length);
      ScoutAssert.assertOrder(new Bundle[]{cBundle3, cBundle2, cBundle1, bBundle3, bBundle2, bBundle1, aBundle3, aBundle2, aBundle1}, bundles);
      ScoutAssert.assertOrder(new Bundle[]{cBundle3, cBundle2, cBundle1, bBundle3, bBundle2, bBundle1, aBundle4, aBundle2, aBundle1}, bundles);
      ScoutAssert.assertOrder(new Bundle[]{cBundle3, cBundle2, cBundle1, bBundle3, bBundle2, bBundle1, aBundle5, aBundle2, aBundle1}, bundles);
    }
    finally {
      uninstallBundles(aBundle1, aBundle2, aBundle3, aBundle4, aBundle5, bBundle1, bBundle2, bBundle3, cBundle1, cBundle2, cBundle3);
    }
  }

  @Test
  public void testGetOrderedBundleDependencyList_multiDependenciesAndMultiPrifixList() throws Exception {
    Bundle aBundle1 = installBundle("a.bundle1", "1.0.0");
    Bundle aBundle2 = installBundle("a.bundle2", "1.0.0", "a.bundle1");
    Bundle aBundle3 = installBundle("a.bundle3", "1.0.0", "a.bundle2");
    Bundle aBundle4 = installBundle("a.bundle4", "1.0.0", "a.bundle2", "a.bundle1");

    Bundle bBundle1 = installBundle("b.bundle1", "1.0.0", "a.bundle2");
    Bundle bBundle2 = installBundle("b.bundle2", "1.0.0", "b.bundle1");

    Bundle cBundle1 = installBundle("c.bundle1", "1.0.0", "a.bundle2");
    Bundle cBundle2 = installBundle("c.bundle2", "1.0.0", "c.bundle1");

    try {
      resolveBundles(aBundle1, aBundle2, aBundle3, aBundle4, bBundle1, bBundle2, cBundle1, cBundle2);
      Bundle[] bundles = BundleInspector.getOrderedBundleList("c", "b");
      assertNotNull(bundles);
      assertEquals(getBundleContext().getBundles().length, bundles.length);
      ScoutAssert.assertOrder(new Bundle[]{cBundle2, cBundle1, bBundle2, bBundle1, aBundle3, aBundle2, aBundle1}, bundles);
      ScoutAssert.assertOrder(new Bundle[]{cBundle2, cBundle1, bBundle2, bBundle1, aBundle4, aBundle2, aBundle1}, bundles);
    }
    finally {
      uninstallBundles(aBundle1, aBundle2, aBundle3, aBundle4, bBundle1, bBundle2, cBundle1, cBundle2);
    }
  }

  @Test
  public void testGetHostBundle() throws Exception {
    assertNull(BundleInspector.getHostBundle(null));
    Bundle bundle = installBundle("a.bundle", "1.0.0");
    Bundle fragment = null;
    try {
      assertSame(bundle, BundleInspector.getHostBundle(bundle));
      fragment = installFragment("a.fragment", "1.0.0", "a.bundle");
      resolveBundles(bundle, fragment);
      assertSame(bundle, BundleInspector.getHostBundle(fragment));
    }
    finally {
      uninstallBundles(bundle, fragment);
    }
  }

  @Test
  public void testIsFragment() throws Exception {
    Bundle bundle = null;
    Bundle fragment = null;
    try {
      assertNull(BundleInspector.getHostBundle(null));
      bundle = installBundle("a.bundle", "1.0.0");
      assertFalse(BundleInspector.isFragment(bundle));
      fragment = installFragment("a.fragment", "1.0.0", "a.bundle");
      assertTrue(BundleInspector.isFragment(fragment));
    }
    finally {
      uninstallBundles(bundle, fragment);
    }
  }
}
