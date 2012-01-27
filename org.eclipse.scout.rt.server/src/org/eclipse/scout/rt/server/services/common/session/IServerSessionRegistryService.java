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
package org.eclipse.scout.rt.server.services.common.session;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.service.IService;

public interface IServerSessionRegistryService extends IService {

  /**
   * @param clazz
   * @param subject
   *          if the creation of the session requires a special jaas context
   *          (not the default), it is run inside a {@link Subject#doAs(Subject, java.security.PrivilegedAction)}
   *          section
   * @return a new server session The new session is created and loaded, see
   *         {@link IServerSession#loadSession(org.osgi.framework.Bundle)}
   */
  <T extends IServerSession> T newServerSession(Class<T> clazz, Subject subject) throws ProcessingException;

}
