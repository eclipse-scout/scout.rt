/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.jaxws.internal.servlet.JaxWsServletContextListener;
import org.osgi.framework.BundleContext;

import com.sun.xml.internal.ws.fault.SOAPFaultBuilder;

public class Activator extends Plugin {

  public static final String PROP_STACKTRACE = "org.eclipse.scout.jaxws.stacktrace";
  public static final String PROP_DEFAULT_PRINCIPAL = "org.eclipse.scout.jaxws.txhandler.sessionfactory.principal";
  public static final String PROP_PUBLISH_STATUS_PAGE = "org.eclipse.scout.jaxws.publish_status_page";

  public static final String PLUGIN_ID = "org.eclipse.scout.jaxws";
  private static Activator m_plugin;

  private JaxWsServletContextListener m_servletContextListener;

  public Activator() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
    // install context listener
    m_servletContextListener = new JaxWsServletContextListener();

    // apply properties
    BundleContext bundleContext = getBundle().getBundleContext();
    boolean stacktraceEnabled = TypeCastUtility.castValue(bundleContext.getProperty(PROP_STACKTRACE), boolean.class);
    if (!stacktraceEnabled) {
      System.setProperty(SOAPFaultBuilder.class.getName() + ".disableCaptureStackTrace", "false");
    }
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_servletContextListener = null;
    m_plugin = null;
    super.stop(context);
  }

  public static Activator getDefault() {
    return m_plugin;
  }

  public JaxWsServletContextListener getServletContextListener() {
    return m_servletContextListener;
  }

}
