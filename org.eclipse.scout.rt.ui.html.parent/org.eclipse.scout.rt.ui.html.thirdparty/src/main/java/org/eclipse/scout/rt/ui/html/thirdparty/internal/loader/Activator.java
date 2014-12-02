package org.eclipse.scout.rt.ui.html.thirdparty.internal.loader;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.html.thirdparty";

  private static BundleContext context;

  public Activator() {
  }

  @Override
  public void start(BundleContext c) throws Exception {
    Activator.context = c;
  }

  @Override
  public void stop(BundleContext c) throws Exception {
    Activator.context = null;
  }

  public static BundleContext getContext() {
    return context;
  }
}
