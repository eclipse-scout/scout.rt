/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.extension.client.internal.AbstractExtensionManager;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuExtensionManager;
import org.eclipse.scout.rt.extension.client.ui.desktop.internal.DesktopExtensionManager;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageExtensionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

/**
 * @since 3.9.0
 */
public class Activator extends Plugin {
  public static final String PLUGIN_ID = "org.eclipse.scout.rt.extension.client";
  private static Activator plugin;

  private final Map<Class<? extends AbstractExtensionManager>, AbstractExtensionManager> m_extensionManagers;

  public static Activator getDefault() {
    return plugin;
  }

  public Activator() {
    m_extensionManagers = new HashMap<Class<? extends AbstractExtensionManager>, AbstractExtensionManager>();
  }

  private void addExtensionManager(AbstractExtensionManager em) {
    m_extensionManagers.put(em.getClass(), em);
  }

  private <T> T getExtensionManager(Class<T> type) {
    return type.cast(m_extensionManagers.get(type));
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;

    IExtensionRegistry extensionRegistry = getExtensionRegistry(context);
    addExtensionManager(new PageExtensionManager(extensionRegistry));
    addExtensionManager(new MenuExtensionManager(extensionRegistry));
    addExtensionManager(new DesktopExtensionManager(extensionRegistry));

    context.addBundleListener(new SynchronousBundleListener() {
      @Override
      public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED && event.getBundle().equals(getBundle())) {
          new Job("Initialize Scout client extensions") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
              for (AbstractExtensionManager em : m_extensionManagers.values()) {
                em.start();
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
    for (Iterator<AbstractExtensionManager> it = m_extensionManagers.values().iterator(); it.hasNext();) {
      AbstractExtensionManager em = it.next();
      it.remove();
      em.stop();
    }
    plugin = null;
    super.stop(context);
  }

  public PageExtensionManager getPagesExtensionManager() {
    return getExtensionManager(PageExtensionManager.class);
  }

  public MenuExtensionManager getMenuExtensionManager() {
    return getExtensionManager(MenuExtensionManager.class);
  }

  public DesktopExtensionManager getDesktopExtensionManager() {
    return getExtensionManager(DesktopExtensionManager.class);
  }

  private IExtensionRegistry getExtensionRegistry(BundleContext context) {
    ServiceReference ref = context.getServiceReference(IExtensionRegistry.class.getName());
    IExtensionRegistry reg = (IExtensionRegistry) context.getService(ref);
    context.ungetService(ref);
    return reg;
  }
}
