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
package org.eclipse.scout.service.internal;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.service";

  // The shared instance
  private static Activator plugin;

  private ServicesExtensionManager m_servicesExtensionManager;

  /**
   * The constructor
   */
  public Activator() {
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    ServiceReference ref = context.getServiceReference(IExtensionRegistry.class.getName());
    @SuppressWarnings("unchecked")
    IExtensionRegistry reg = (IExtensionRegistry) context.getService(ref);
    context.ungetService(ref);
    m_servicesExtensionManager = new ServicesExtensionManager(reg, PLUGIN_ID + ".services");
    context.addBundleListener(new SynchronousBundleListener() {
      @Override
      public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED && event.getBundle().equals(getBundle())) {
          new Job("Initialize services") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
              m_servicesExtensionManager.start();
              return Status.OK_STATUS;
            }
          }.schedule();
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    if (m_servicesExtensionManager != null) {
      m_servicesExtensionManager.stop();
      m_servicesExtensionManager = null;
    }
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

  public ServicesExtensionManager getServicesExtensionManager() {
    return m_servicesExtensionManager;
  }

}
