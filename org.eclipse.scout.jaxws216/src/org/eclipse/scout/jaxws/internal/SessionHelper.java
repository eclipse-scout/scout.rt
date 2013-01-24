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
package org.eclipse.scout.jaxws.internal;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.jaxws.Activator;
import org.eclipse.scout.jaxws.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;

/**
 * Helper to create a session
 */
public final class SessionHelper {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SessionHelper.class);

  private SessionHelper() {
  }

  /**
   * To create a new session
   * 
   * @param factory
   *          the factory to be used to create the session
   * @return
   */
  public static IServerSession createNewServerSession(final IServerSessionFactory factory) {
    if (factory == null) {
      return null;
    }

    Subject subject = null;
    try {
      subject = Subject.getSubject(AccessController.getContext());
    }
    catch (Exception e) {
      LOG.error("Failed to get subject of calling access context", e);
    }
    // As long as the request is not authenticated the subject is null or empty. If using container authentication, the subject is always available.
    if (subject == null || subject.getPrincipals().size() == 0) {
      // create subject with anonymous principal
      String principalName = Activator.getDefault().getBundle().getBundleContext().getProperty(Activator.PROP_DEFAULT_PRINCIPAL);
      if (!StringUtility.hasText(principalName)) {
        LOG.warn("No subject found in calling AccessContext. That is why the principal 'anonymous' is registered with the subject. This subject is used to create sessions for transactional handlers as long as the request is not authenticated. The default principal can be changed by configuring the prinicipal in '" + Activator.PROP_DEFAULT_PRINCIPAL + "' in config.ini.");
        principalName = "anonymous";
      }
      subject = new Subject();
      subject.getPrincipals().add(new SimplePrincipal(principalName));
      subject.setReadOnly();
    }

    // create the new session on behalf of the subject
    IServerSession serverSession = null;
    try {
      serverSession = Subject.doAs(subject, new PrivilegedExceptionAction<IServerSession>() {
        @Override
        public IServerSession run() throws Exception {
          return factory.create();
        }
      });
    }
    catch (Exception e) {
      LOG.error("Failed to create server session.", e);
      return null;
    }
    if (serverSession == null) {
      LOG.error("Session created by factory '" + factory.getClass().getName() + "' must not be null.");
    }
    return serverSession;
  }
}
