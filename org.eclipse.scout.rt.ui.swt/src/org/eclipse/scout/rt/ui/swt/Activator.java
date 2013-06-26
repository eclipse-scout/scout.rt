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
package org.eclipse.scout.rt.ui.swt;

import java.util.Hashtable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.rt.ui.swt.icons.SwtBundleIconLocator;
import org.eclipse.scout.rt.ui.swt.login.internal.InternalNetAuthenticator;
import org.eclipse.scout.rt.ui.swt.util.ISwtIconLocator;
import org.eclipse.scout.rt.ui.swt.util.SwtIconLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.swt";

  private static Activator m_plugin;
  private ISwtIconLocator m_iconLocator;
  private ServiceRegistration m_netAuthRegistration;

  public static Activator getDefault() {
    return m_plugin;
  }

  public Activator() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;

    // register net authenticator ui
    Hashtable<String, Object> map = new Hashtable<String, Object>();
    map.put(Constants.SERVICE_RANKING, -1);
    m_netAuthRegistration = Activator.getDefault().getBundle().getBundleContext().registerService(java.net.Authenticator.class.getName(), new InternalNetAuthenticator(), map);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_iconLocator = null;
    if (m_netAuthRegistration != null) {
      m_netAuthRegistration.unregister();
      m_netAuthRegistration = null;
    }
    m_plugin = null;
    super.stop(context);
  }

  public static Image getIcon(String name) {
    return getDefault().getIconImpl(name);
  }

  public static ImageDescriptor getImageDescriptor(String name) {
    Activator activator = getDefault();
    if (activator != null) {
      return activator.getImageDescriptorImpl(name);
    }
    return null;
  }

  private Image getIconImpl(String name) {
    if (m_iconLocator == null) {
      m_iconLocator = new SwtIconLocator(new SwtBundleIconLocator());
    }
    return m_iconLocator.getIcon(name, ISwtEnvironment.ICON_DECORATION_NONE);
  }

  private ImageDescriptor getImageDescriptorImpl(String name) {
    if (m_iconLocator == null) {
      m_iconLocator = new SwtIconLocator(new SwtBundleIconLocator());
    }
    return m_iconLocator.getImageDescriptor(name);
  }
}
