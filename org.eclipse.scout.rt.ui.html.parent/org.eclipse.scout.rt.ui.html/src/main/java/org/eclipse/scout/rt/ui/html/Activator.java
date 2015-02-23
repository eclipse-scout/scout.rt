package org.eclipse.scout.rt.ui.html;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.res.IWebContentResourceLocator;
import org.eclipse.scout.rt.ui.html.res.OsgiWebContentResourceLocator;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

public class Activator extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.html";

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Activator.class);
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
    // workaround for bug in serverside equinox implementation with servletbridge
    // wait until done and launch product if one exists
    context.addBundleListener(new SynchronousBundleListener() {
      @Override
      public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED && event.getBundle().equals(getBundle())) {
          new Job("Product launcher") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
              if (Platform.getBundle("org.eclipse.equinox.http.servletbridge") != null) {
                runProduct();
              }
              return Status.OK_STATUS;
            }
          }.schedule();
        }
      }
    });
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

  private void runProduct() {
    IProduct product = Platform.getProduct();
    try {
      Object app = findApplicationClass(product);
      if (app instanceof IApplication) {
        ((IApplication) app).start(new P_EmtpyApplicationContext());
      }
    }
    catch (Throwable t) {
      LOG.error("Error starting application", t);
    }
  }

  private Object findApplicationClass(IProduct product) throws CoreException {
    if (product != null) {
      ServiceReference serviceRef = getBundle().getBundleContext().getServiceReference(IExtensionRegistry.class.getName());
      try {
        IExtensionRegistry reg = (IExtensionRegistry) getBundle().getBundleContext().getService(serviceRef);
        if (reg != null) {
          IExtensionPoint xpProd = reg.getExtensionPoint("org.eclipse.core.runtime.products");
          if (xpProd != null) {
            IExtension xProd = xpProd.getExtension(product.getId());
            if (xProd != null) {
              for (IConfigurationElement cProd : xProd.getConfigurationElements()) {
                if (cProd.getName().equals("product")) {
                  String appId = cProd.getAttribute("application");
                  IExtensionPoint xpApp = reg.getExtensionPoint("org.eclipse.core.runtime.applications");
                  if (xpApp != null) {
                    IExtension xApp = xpApp.getExtension(appId);
                    if (xApp != null) {
                      for (IConfigurationElement cApp : xApp.getConfigurationElements()) {
                        if (cApp.getName().equals("application")) {
                          for (IConfigurationElement cRun : cApp.getChildren("run")) {
                            return cRun.createExecutableExtension("class");
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      finally {
        getBundle().getBundleContext().ungetService(serviceRef);
      }
    }
    return null;
  }

  private class P_EmtpyApplicationContext implements IApplicationContext {

    @Override
    public Map getArguments() {
      return null;
    }

    @Override
    public void applicationRunning() {
    }

    @Override
    public String getBrandingApplication() {
      return null;
    }

    @Override
    public String getBrandingName() {
      return null;
    }

    @Override
    public String getBrandingDescription() {
      return null;
    }

    @Override
    public String getBrandingId() {
      return null;
    }

    @Override
    public String getBrandingProperty(String key) {
      return null;
    }

    @Override
    public Bundle getBrandingBundle() {
      return null;
    }

    @Override
    public void setResult(Object result, IApplication application) {
    }

  }

}
