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
import org.eclipse.scout.rt.ui.swt.login.internal.InternalNetAuthenticator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.swt";

  public static final String ICON_ARROW_DOWN = "ArrowDown.gif";

  // The shared instance
  private static Activator plugin;

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  private ServiceRegistration m_netAuthRegistration;

  /**
   * The constructor
   */
  public Activator() {
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
   * )
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    // register net authenticator ui
    Hashtable<String, Object> map = new Hashtable<String, Object>();
    map.put(Constants.SERVICE_RANKING, -1);
    m_netAuthRegistration = Activator.getDefault().getBundle().getBundleContext().registerService(java.net.Authenticator.class.getName(), new InternalNetAuthenticator(), map);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
   * )
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    if (m_netAuthRegistration != null) {
      m_netAuthRegistration.unregister();
      m_netAuthRegistration = null;
    }
    plugin = null;
    super.stop(context);
  }

  public static ImageDescriptor imageDescriptorFromLocalPlugin(String iconName) {
    return imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(), "resources/icons/" + iconName);
  }

}
