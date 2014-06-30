package org.eclipse.scout.rt.client.officeonline.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

  public static String PLUGIN_ID = "org.eclipse.scout.rt.client.officeonline";

  private static BundleContext bundleContext;

  public static BundleContext getBundleContext() {
    return bundleContext;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    bundleContext = context;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    bundleContext = null;
    super.stop(context);
  }
}
