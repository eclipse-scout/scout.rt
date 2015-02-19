package org.eclipse.scout.rt.platform.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

  private static BundleContext bundleContext;

  @Override
  public void start(BundleContext context) throws Exception {
    bundleContext = context;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    bundleContext = null;
  }

  public static BundleContext getBundleContext() {
    return bundleContext;
  }
}
