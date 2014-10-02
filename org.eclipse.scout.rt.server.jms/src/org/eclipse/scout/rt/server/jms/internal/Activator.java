package org.eclipse.scout.rt.server.jms.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Activator.class);

  public static final String PLUGIN_ID = "org.eclipse.scout.rt.server.jms";
  private static Activator plugin;

  public static Activator getDefault() {
    return plugin;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }
}
