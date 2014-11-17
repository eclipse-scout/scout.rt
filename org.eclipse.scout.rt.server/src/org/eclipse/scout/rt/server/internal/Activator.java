/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.internal;

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
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

public class Activator extends Plugin {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Activator.class);

  public static final String PLUGIN_ID = "org.eclipse.scout.rt.server";
  private static Activator plugin;

  public static Activator getDefault() {
    return plugin;
  }

  private ProcessInspector m_processInspector;

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    m_processInspector = new ProcessInspector();
    // workaround for bug in serverside equinox implementation with servletbridge
    // wait until done and launch product if one exists
    if (Platform.getBundle("org.eclipse.scout.sdk") == null) {
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
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    m_processInspector = null;
    plugin = null;
    super.stop(context);
  }

  public ProcessInspector getProcessInspector() {
    return m_processInspector;
  }

  private void runProduct() {
    IProduct product = Platform.getProduct();
    try {
      Object app = findApplicationClass(product);
      if (app instanceof IApplication) {
        ((IApplication) app).start(new ServerApplicationContext());
      }
    }
    catch (Throwable t) {
      LOG.error("Error starting application", t);
    }
  }

  private Object findApplicationClass(IProduct product) throws CoreException {
    if (product != null) {
      IExtensionRegistry reg = SERVICES.getService(IExtensionRegistry.class);
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
    return null;
  }
}
