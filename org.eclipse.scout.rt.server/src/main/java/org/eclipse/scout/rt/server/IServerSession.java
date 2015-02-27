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
package org.eclipse.scout.rt.server;

import java.util.Locale;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.shared.ISession;
import org.osgi.framework.Bundle;

/**
 * Server-side session
 */
public interface IServerSession extends ISession {

  void loadSession(Bundle bundle) throws ProcessingException;

  /**
   * Set the session id. Should only be done during initialization.
   *
   * @param id
   */
  void setIdInternal(String id);

  /**
   * @return a unique id
   */
  String getId();

  /**
   * @return the current job's Locale or the JVM default if unknown; is never <code>null</code>.
   * @deprecated use {@link NlsLocale#get()}; will be removed in release 5.2<br/>
   *             reason: on server-side, the Locale is bound very tight to the current executing job: if triggered by a
   *             client-request, the Locale is included in every request and set accordingly on the job. Otherwise, the
   *             submitter of the job decides which Locale to use.
   */
  @Deprecated
  Locale getLocale();
}
