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
package org.eclipse.scout.jaxws.session;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.Activator;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServerSessionClassFinder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerSessionClassFinder.class);

  /**
   * Tries to find the server session class based on several approaches:
   * <ol>
   * <li>based on config.ini parameters {@link PROP_QN_SESSION} and {@link PROP_SN_BUNDLE}</li>
   * <li>based on default-naming-convention in bundle of servlet contributor for {@link ServiceTunnelServlet}</li>
   * </ol>
   * 
   * @return the found server session class or null if not found
   */
  public static Class<? extends IServerSession> find() {
    // try to resolve session based on JAX-WS specific properties in config.ini
    Class<? extends IServerSession> serverSessionClass = resolveJaxWsServerSession();
    if (serverSessionClass != null) {
      return serverSessionClass;
    }

    // try by-convention-approach
    Bundle bundle = findServletContributorBundle();
    if (bundle == null) {
      LOG.error("Failed to find server session class. Specify the property '" + DefaultServerSessionFactory.PROP_QN_SESSION + "' in config.ini accordingly.");
      return null;
    }

    String fqn = bundle.getSymbolicName() + ".ServerSession";
    serverSessionClass = loadServerSessionSafe(bundle, fqn);
    if (serverSessionClass == null) {
      LOG.error("Failed to load server session class '" + fqn + "' from bundle '" + bundle.getSymbolicName() + "'. Specify the property '" + DefaultServerSessionFactory.PROP_QN_SESSION + "' in config.ini accordingly.");
      return null;
    }
    return serverSessionClass;
  }

  private static Bundle findServletContributorBundle() {
    try {
      BundleContext context = Activator.getDefault().getBundle().getBundleContext();
      ServiceReference ref = context.getServiceReference(IExtensionRegistry.class.getName());
      if (ref == null) {
        return null;
      }
      IExtensionRegistry reg = (IExtensionRegistry) context.getService(ref);
      if (reg == null) {
        return null;
      }
      IExtensionPoint xpServlet = reg.getExtensionPoint("org.eclipse.equinox.http.registry.servlets");
      if (xpServlet == null) {
        return null;
      }
      Set<Bundle> serviceTunnelContributorBundles = new HashSet<Bundle>();
      for (IExtension xServlet : xpServlet.getExtensions()) {
        for (IConfigurationElement cServlet : xServlet.getConfigurationElements()) {
          if (cServlet.getName().equals("servlet")) {
            String servletFqn = cServlet.getAttribute("class");
            Bundle bundle = Platform.getBundle(cServlet.getContributor().getName());
            try {
              Class servletClass = bundle.loadClass(servletFqn);
              if (ServiceTunnelServlet.class.isAssignableFrom(servletClass)) {
                serviceTunnelContributorBundles.add(bundle);
              }
            }
            catch (ClassNotFoundException e) {
              // nop
            }
          }
        }
      }

      int count = serviceTunnelContributorBundles.size();
      if (count == 1) {
        return serviceTunnelContributorBundles.iterator().next();
      }
      else if (count == 0) {
        LOG.warn("No contribution bundle found for servlet '" + ServiceTunnelServlet.class.getSimpleName() + "'");
      }
      else {
        LOG.warn("Multiple contribution bundles found for servlet '" + ServiceTunnelServlet.class.getSimpleName() + "'.");
      }
    }
    catch (Throwable e) {
      LOG.error("failed to find servlet contributor", e);
    }
    return null;
  }

  private static Class<? extends IServerSession> resolveJaxWsServerSession() {
    String qnSession = Activator.getDefault().getBundle().getBundleContext().getProperty(DefaultServerSessionFactory.PROP_QN_SESSION);
    String snBundle = Activator.getDefault().getBundle().getBundleContext().getProperty(DefaultServerSessionFactory.PROP_SN_BUNDLE);

    if (!StringUtility.hasText(qnSession)) {
      return null;
    }

    if (!StringUtility.hasText(snBundle) && qnSession.split("\\.").length == 0) {
      LOG.error("Session class '" + qnSession + "' configured in config.ini '" + DefaultServerSessionFactory.PROP_QN_SESSION + "' must be fully qualified if not used in conjunction with belonging bundle '" + snBundle + "'.");
      return null;
    }

    Bundle bundle;
    if (StringUtility.hasText(snBundle)) {
      bundle = Platform.getBundle(snBundle);
      if (bundle == null) {
        LOG.error("Bundle with the symbolic name '" + snBundle + "' configured in config.ini '" + DefaultServerSessionFactory.PROP_SN_BUNDLE + "' could not be resolved. Please ensure to have typed the symbolic name correctly and that the bundle is resolved without errors.");
        return null;
      }
    }
    else {
      String symbolicName = qnSession.substring(0, qnSession.lastIndexOf('.'));
      bundle = Platform.getBundle(symbolicName);
      if (bundle == null) {
        LOG.error("Bundle with the symbolic name '" + symbolicName + "' configured in config.ini could not be found. The attempt to derive the symbolic name from within the configured session '" + DefaultServerSessionFactory.PROP_QN_SESSION + "' failed. If the package name of the session does not correspond to the symbolic name of the bundle, please specify '" + DefaultServerSessionFactory.PROP_SN_BUNDLE + "' accordingly.");
        return null;
      }
    }

    return loadServerSessionSafe(bundle, qnSession);
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends IServerSession> loadServerSessionSafe(Bundle bundle, String serverSessionFqn) {
    try {
      Class clazz = bundle.loadClass(serverSessionFqn);
      if (IServerSession.class.isAssignableFrom(clazz)) {
        return clazz;
      }
      LOG.error("Server session class '" + serverSessionFqn + "' could not be loaded as not of the type '" + IServerSession.class.getName() + "'");
    }
    catch (ClassNotFoundException e) {
      LOG.error("Server session class '" + serverSessionFqn + "' could not be found");
    }
    return null;
  }
}
