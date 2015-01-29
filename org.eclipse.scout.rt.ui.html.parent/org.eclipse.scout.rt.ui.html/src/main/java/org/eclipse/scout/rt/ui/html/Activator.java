package org.eclipse.scout.rt.ui.html;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.rt.ui.html.res.IWebContentResourceLocator;
import org.eclipse.scout.rt.ui.html.res.OsgiWebContentResourceLocator;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.html";

  // The shared instance
  private static Activator plugin;

  private IWebContentResourceLocator m_webContentResourceLocator;

  public Activator() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    // Install default webcontent resource locator
    setWebContentResourceLocator(new OsgiWebContentResourceLocator());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    setWebContentResourceLocator(null);
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  public IWebContentResourceLocator getWebContentResourceLocator() {
    return m_webContentResourceLocator;
  }

  public void setWebContentResourceLocator(IWebContentResourceLocator webContentResourceLocator) {
    m_webContentResourceLocator = webContentResourceLocator;
  }
}
