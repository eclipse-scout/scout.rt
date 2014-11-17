/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import java.util.UUID;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.server.internal.ServerSessionClassFinder;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

/**
 * Service for {@link ServerJob}s that are not user specific, and run with server-subject and backend-server-session on
 * the server.
 * <p>
 * Default implementation is searching server session class
 */
@SuppressWarnings("deprecation")
public class ServerJobService extends AbstractService implements IServerJobService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerJobService.class);

  private static final String SERVER_PRINCIPAL = "anonymous";
  private final Subject m_serverSubject = createSubject(SERVER_PRINCIPAL);
  private String m_serverSessionClassName;

  @Override
  public Subject getServerSubject() {
    return m_serverSubject;
  }

  @Override
  public Subject createSubject(String principal) {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(principal));
    subject.setReadOnly();
    return subject;
  }

  public String getServerSessionClassName() {
    return m_serverSessionClassName;
  }

  public void setServerSessionClassName(String className) {
    m_serverSessionClassName = className;
  }

  @Override
  public Class<? extends IServerSession> getServerSessionClass() throws ProcessingException {
    final String sessionName = getServerSessionClassName();
    if (!StringUtility.isNullOrEmpty(sessionName)) {
      return loadSessionClass(sessionName);
    }
    LOG.error("No server session class defined: Set org.eclipse.scout.rt.server.ServerJobService#serverSessionClassName in your confit.ini ");
    //legacy support
    final String classNameByConvention = new ServerSessionClassFinder().findClassNameByConvention();
    if (classNameByConvention != null) {
      return loadSessionClass(classNameByConvention);
    }
    throw new ProcessingException("No found");
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends IServerSession> loadSessionClass(String sessionName) throws ProcessingException {
    try {
      Class clazz = SerializationUtility.getClassLoader().loadClass(sessionName);
      if (IServerSession.class.isAssignableFrom(clazz)) {
        return clazz;
      }
      throw new ProcessingException("Server session class '" + sessionName + "' could not be loaded as not of the type '" + IServerSession.class.getName() + "'");
    }
    catch (ClassNotFoundException e) {
      throw new ProcessingException("Server session class '" + sessionName + "' could not be found", e);
    }
  }

  @Override
  public IServerJobFactory createJobFactory() throws ProcessingException {
    return createJobFactory(createServerSession(), getServerSubject());
  }

  @Override
  public IServerJobFactory createJobFactory(IServerSession session, Subject subject) {
    return new ServerJobFactory(session, subject);
  }

  @Override
  public IServerSession createServerSession() throws ProcessingException {
    return createServerSession(getServerSessionClass(), getServerSubject());
  }

  @Override
  public IServerSession createServerSession(Class<? extends IServerSession> sessionClazz, Subject subject) throws ProcessingException {
    IServerSession session = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(sessionClazz, subject);
    final String id = sessionClazz.getName() + UUID.randomUUID().toString();
    session.setIdInternal(id);
    return session;
  }

}
