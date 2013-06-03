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

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.internal.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

public abstract class AbstractBundleTest {

  protected static Bundle installBundle(String bundleId, String version, String... dependencies) throws Exception {
    return installBundle(bundleId, version, null, dependencies);
  }

  protected static Bundle installFragment(String fragmentId, String version, String hostBundle, String... dependencies) throws Exception {
    return installBundle(fragmentId, version, hostBundle, dependencies);
  }

  private static Bundle installBundle(String bundleId, String version, String hostBundle, String... dependencies) throws Exception {
    P_AwaitingBundleListener listener = new P_AwaitingBundleListener(BundleEvent.INSTALLED, bundleId);
    BundleContext bundleContext = getBundleContext();
    Bundle bundle;
    try {
      bundleContext.addBundleListener(listener);
      ByteArrayInputStream in = new ByteArrayInputStream(createBundle(bundleId, version, hostBundle, dependencies));
      bundle = bundleContext.installBundle(bundleId, in);
      listener.await(1000);
    }
    finally {
      bundleContext.removeBundleListener(listener);
    }
    return bundle;
  }

  protected static void uninstallBundles(Bundle... bundles) throws Exception {
    if (bundles != null) {
      for (Bundle bundle : bundles) {
        if (bundle != null) {
          bundle.uninstall();
        }
      }
    }
  }

  protected static void resolveBundles(Bundle... bundles) {
    assertNotNull(bundles);
    BundleContext context = getBundleContext();
    ServiceReference packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
    PackageAdmin packageAdmin = null;
    if (packageAdminRef != null) {
      packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
    }
    if (packageAdmin == null) {
      return;
    }
    String[] bundleIds = new String[bundles.length];
    for (int i = 0; i < bundles.length; i++) {
      bundleIds[i] = bundles[i].getSymbolicName();
    }
    P_AwaitingBundleListener listener = new P_AwaitingBundleListener(BundleEvent.RESOLVED, bundleIds);
    try {
      context.addBundleListener(listener);
      packageAdmin.refreshPackages(bundles);
      listener.await(1000);
    }
    finally {
      if (listener != null) {
        context.removeBundleListener(listener);
      }
    }
  }

  protected static BundleContext getBundleContext() {
    return Activator.getDefault().getBundle().getBundleContext();
  }

  private static byte[] createBundle(String bundleId, String version, String hostBundle, String... dependencies) throws Exception {
    Manifest manifest = new Manifest(new ByteArrayInputStream(createManifest(bundleId, version, hostBundle, dependencies).getBytes()));
    ByteArrayOutputStream bundle = new ByteArrayOutputStream();
    JarOutputStream zOut = new JarOutputStream(bundle, manifest);
    zOut.close();
    return bundle.toByteArray();
  }

  private static String createManifest(String symbolicName, String bundleVersion, String hostBundle, String... bundleDependencies) {
    StringBuilder builder = new StringBuilder();
    builder.append("Manifest-Version: 1.0\n");
    builder.append("Bundle-SymbolicName: ");
    builder.append(symbolicName);
    builder.append("\n");
    builder.append("Bundle-ManifestVersion: 2\n");
    builder.append("Bundle-Name: ");
    builder.append(symbolicName);
    builder.append(" Test\n");
    builder.append("Bundle-Version: ");
    builder.append(bundleVersion);
    builder.append("\n");
    if (StringUtility.hasText(hostBundle)) {
      builder.append("Fragment-Host: ");
      builder.append(hostBundle);
      builder.append("\n");
    }
    builder.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\n");
    if (bundleDependencies != null && bundleDependencies.length > 0) {
      builder.append("Require-Bundle: ");
      boolean firstDependency = true;
      for (String dependency : bundleDependencies) {
        if (firstDependency) {
          firstDependency = false;
        }
        else {
          builder.append(",\n ");
        }
        builder.append(dependency);
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  protected static class P_AwaitingBundleListener implements BundleListener {
    private final String[] m_bundleIds;
    private final int m_bundleEvent;
    private final CountDownLatch countDownLatch;
    private final Set<String> m_remainingBundleIds;

    public P_AwaitingBundleListener(int bundleEvent, String... bundleIds) {
      m_bundleIds = bundleIds;
      m_bundleEvent = bundleEvent;
      countDownLatch = new CountDownLatch(1);
      m_remainingBundleIds = Collections.synchronizedSet(new HashSet<String>());
      for (String bundleId : bundleIds) {
        m_remainingBundleIds.add(bundleId);
      }
    }

    @Override
    public void bundleChanged(BundleEvent event) {
      if (event.getType() == m_bundleEvent && m_remainingBundleIds.contains(event.getBundle().getSymbolicName())) {
        m_remainingBundleIds.remove(event.getBundle().getSymbolicName());
        if (m_remainingBundleIds.isEmpty()) {
          countDownLatch.countDown();
        }
      }
    }

    public void await(long millis) {
      try {
        countDownLatch.await(millis, TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException e) {
      }
    }
  }
}
