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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ISession;
import org.osgi.framework.Bundle;

/**
 * The base implementation {@link org.eclipse.scout.rt.server.servlet.DeploymentServiceConfig} uses the
 * scout.xml file for setting deployment specific service properties
 */
public interface IServerSession extends ISession {

  void loadSession(Bundle bundle) throws ProcessingException;

  /**
   * @deprecated Use {@link #getData(String)} instead.<br/>
   *             Will be removed in 3.9.0
   */
  @Deprecated
  Object getAttribute(String key);

  /**
   * @deprecated Use {@link #setData(String, Object)} isntead.<br/>
   *             Will be removed in 3.9.0
   */
  @Deprecated
  void setAttribute(String key, Object value);

  /**
   * @deprecated Use {@link #isSingleThreadSession()} instead.<br/>
   *             Will be removed in 3.9.0
   */
  @Deprecated
  boolean isWebSession();
}
