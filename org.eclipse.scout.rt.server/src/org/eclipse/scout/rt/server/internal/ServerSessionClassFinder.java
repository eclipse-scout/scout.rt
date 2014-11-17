package org.eclipse.scout.rt.server.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJobService;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.osgi.framework.Bundle;

/**
 * @deprecated use {@link ServerJobService} instead. Will be removed in N-release.
 *             Searches bundles to find the ServerSession by naming convention.
 */
@Deprecated
public class ServerSessionClassFinder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerSessionClassFinder.class);

  /**
   * Tries to find the server session class based on default-naming-convention:
   * in bundle of servlet contributor for {@link ServiceTunnelServlet}</li>
   *
   * @return the found server session class name or null if not found
   */
  public String findClassNameByConvention() {
    Bundle bundle = findServletContributorBundle();
    if (bundle == null) {
      return null;
    }
    return getServerSessionName(bundle);
  }

  public String findClassNameByConvention(String alias) {
    Bundle bundle = findServletContributorBundle(alias);
    if (bundle == null) {
      return null;
    }
    return getServerSessionName(bundle);
  }

  @SuppressWarnings("unchecked")
  public Class<? extends IServerSession> findClassByConvention(String alias) {
    Bundle bundle = findServletContributorBundle(alias);
    if (bundle == null) {
      return null;
    }
    final String serverSessionName = getServerSessionName(bundle);
    try {
      return (Class<? extends IServerSession>) bundle.loadClass(serverSessionName);
    }
    catch (ClassNotFoundException e) {
      LOG.error("Failed to load server session class '" + serverSessionName + "' from bundle '" + bundle.getSymbolicName());
    }
    return null;
  }

  private String getServerSessionName(Bundle bundle) {
    return bundle.getSymbolicName() + ".ServerSession";
  }

  private Bundle findServletContributorBundle() {
    return findServletContributorBundle(null);
  }

  public Bundle findServletContributorBundle(String alias) {
    IExtensionPoint xpServlet = getServletExtensionPoint();
    if (xpServlet != null) {
      Set<Bundle> serviceTunnelContributorBundles = new HashSet<Bundle>();
      for (IExtension xServlet : xpServlet.getExtensions()) {
        for (IConfigurationElement cServlet : xServlet.getConfigurationElements()) {
          if ("servlet".equals(cServlet.getName())) {
            final String servletClassName = cServlet.getAttribute("class");
            Bundle bundle = Platform.getBundle(cServlet.getContributor().getName());
            if (isServiceTunnelServlet(servletClassName, bundle) && isMatchingAlias(alias, cServlet)) {
              serviceTunnelContributorBundles.add(bundle);
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
    return null;
  }

  private boolean isMatchingAlias(String alias, IConfigurationElement cServlet) {
    if (alias != null) {
      return alias.equals(cServlet.getAttribute("alias"));
    }
    else {
      return true;
    }
  }

  private boolean isServiceTunnelServlet(String className, Bundle bundle) {
    if (!ServiceTunnelServlet.class.getName().equals(className)) {
      return false;
    }
    if (bundle == null) {
      return false;
    }
    try {
      Class servletClass = bundle.loadClass(className);
      if (ServiceTunnelServlet.class.isAssignableFrom(servletClass)) {
        return true;
      }
    }
    catch (ClassNotFoundException e) {
      LOG.debug("Registered Servlet Class Not found ", e);
    }
    return false;
  }

  protected IExtensionPoint getServletExtensionPoint() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    if (reg != null) {
      return reg.getExtensionPoint("org.eclipse.equinox.http.registry.servlets");
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Class<? extends IServerSession> loadServerSessionSafe(Bundle bundle, String serverSessionFqn) {
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
