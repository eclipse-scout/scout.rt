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

import java.security.AccessController;

import javax.security.auth.Subject;
import javax.xml.ws.WebServiceException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.Activator;
import org.eclipse.scout.rt.server.IServerJobService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Factory that creates a new {@link IServerSession} for each web service request.
 * </p>
 * <p>
 * The {@link IServerSession} is tried to be found based on several approaches:
 * <ol>
 * <li>based on config.ini parameters <code>org.eclipse.scout.jaxws.session.DefaultServerSessionFactory#qnSession</code>
 * and <code>org.eclipse.scout.jaxws.session.DefaultServerSessionFactory#snBundle</code></li>
 * <li>based on default-naming-convention in bundle of servlet contributor for {@link ServiceTunnelServlet}</li>
 * </ol>
 * </p>
 * <p>
 * <b>Configuration in config.ini for creation of DefaultServerSession</b>
 * </p>
 * <table border="1">
 * <tr>
 * <td width="250px"><b>Property</b></td>
 * <td><b>Mandatory</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * </tr>
 * <tr>
 * <td>org.eclipse.scout.jaxws.session.DefaultServerSessionFactory#qnSession</td>
 * <td>yes</td>
 * <td>Fully qualified name of session class</td>
 * </tr>
 * <tr>
 * <td>org.eclipse.scout.jaxws.session.DefaultServerSessionFactory#snBundle</td>
 * <td>no</td>
 * <td>Symbolic name of bundle whose classloader is to be used to load the session class. If not specified, the bundle
 * to load the session is assumed to be the qualifier of the session's qualified name.</td>
 * </tr>
 * </table>
 */
public class DefaultServerSessionFactory implements IServerSessionFactory {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultServerSessionFactory.class);

  /**
   * Symbolic name of the bundle that holds the {@link IServerSession}
   */
  public static final String PROP_SN_BUNDLE = DefaultServerSessionFactory.class.getName() + "#snBundle";
  /**
   * Fully qualified name of {@link IServerSession}
   */
  public static final String PROP_QN_SESSION = DefaultServerSessionFactory.class.getName() + "#qnSession";

  @Override
  public IServerSession create() {
    String qnSession = Activator.getDefault().getBundle().getBundleContext().getProperty(DefaultServerSessionFactory.PROP_QN_SESSION);
    String snBundle = Activator.getDefault().getBundle().getBundleContext().getProperty(DefaultServerSessionFactory.PROP_SN_BUNDLE);

    try {
      final Class<? extends IServerSession> sessionClazz = getSessionClass(qnSession, snBundle, PROP_QN_SESSION, PROP_SN_BUNDLE);
      Subject subject = safeGetSubject();
      return SERVICES.getService(IServerJobService.class).createServerSession(sessionClazz, subject);
    }
    catch (ProcessingException e) {
      throw new WebServiceException("Session could not be created.", e);
    }
  }

  private Class<? extends IServerSession> getSessionClass(String sessionClass, String bundleName, String sessionClassProperty, String bundleNameProperty) throws ProcessingException {
    final Class<? extends IServerSession> classByProperty = findByProperties(sessionClass, bundleName, sessionClassProperty, bundleNameProperty);
    if (classByProperty != null) {
      return classByProperty;
    }
    return SERVICES.getService(IServerJobService.class).getServerSessionClass();
  }

  private Class<? extends IServerSession> findByProperties(String session, String bundleName, String sessionProperty, String bundleProperty) {
    if (!StringUtility.hasText(session)) {
      return null;
    }

    if (!StringUtility.hasText(bundleName) && session.split("\\.").length == 0) {
      LOG.error("Session class '" + session + "' configured in config.ini '" + sessionProperty + "' must be fully qualified if not used in conjunction with belonging bundle '" + bundleName + "'.");
      return null;
    }

    Bundle bundle;
    if (StringUtility.hasText(bundleName)) {
      bundle = Platform.getBundle(bundleName);
      if (bundle == null) {
        LOG.error("Bundle with the symbolic name '" + bundleName + "' configured in config.ini '" + bundleProperty + "' could not be resolved. Please ensure to have typed the symbolic name correctly and that the bundle is resolved without errors.");
        return null;
      }
    }
    else {
      String symbolicName = session.substring(0, session.lastIndexOf('.'));
      bundle = Platform.getBundle(symbolicName);
      if (bundle == null) {
        LOG.error("Bundle with the symbolic name '" + symbolicName + "' configured in config.ini could not be found. The attempt to derive the symbolic name from within the configured session '" + sessionProperty + "' failed. If the package name of the session does not correspond to the symbolic name of the bundle, please specify '" + bundleProperty + "' accordingly.");
        return null;
      }
    }

    return loadServerSessionSafe(bundle, session);
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

  private Subject safeGetSubject() {
    try {
      return Subject.getSubject(AccessController.getContext());
    }
    catch (Exception e) {
      LOG.error("Failed to get subject of calling acess context", e);
    }
    throw new WebServiceException("Unexpected: missing subject in current access context.");
  }
}
