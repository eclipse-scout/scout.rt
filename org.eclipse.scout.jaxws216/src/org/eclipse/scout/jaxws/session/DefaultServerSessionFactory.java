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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.service.SERVICES;

/**
 * <p>
 * Factory that creates a new {@link IServerSession} for each web service request.
 * </p>
 * <p>
 * The {@link IServerSession} is tried to be found based on several approaches:
 * <ol>
 * <li>based on config.ini parameters
 * <code>org.eclipse.scout.jaxws.session.DefaultServerSessionFactory#qnSession</code> and
 * <code>org.eclipse.scout.jaxws.session.DefaultServerSessionFactory#snBundle</code></li>
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
    Class<? extends IServerSession> sessionClazz = ServerSessionClassFinder.find();
    if (sessionClazz == null) {
      throw new WebServiceException("Server session class could not be found.'");
    }

    Subject subject = null;
    try {
      subject = Subject.getSubject(AccessController.getContext());
    }
    catch (Exception e) {
      LOG.error("Failed to get subject of calling acess context", e);
    }
    if (subject == null) {
      throw new WebServiceException("Unexpected: missing subject in current access context.");
    }

    try {
      return SERVICES.getService(IServerSessionRegistryService.class).newServerSession(sessionClazz, subject);
    }
    catch (Throwable e) {
      if (e.getCause() instanceof SecurityException) {
        throw new WebServiceException("Session could not be created. Access denied.", e);
      }
      throw new WebServiceException("Session could not be created.", e);
    }
  }
}
