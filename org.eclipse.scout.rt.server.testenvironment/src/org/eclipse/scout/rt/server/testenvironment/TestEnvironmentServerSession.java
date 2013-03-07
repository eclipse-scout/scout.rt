/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.testenvironment;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.ThreadContext;

/**
 * {@link org.eclipse.scout.rt.server.IServerSession} for Server Test Environment
 * 
 * @author jbr
 */
public class TestEnvironmentServerSession extends AbstractServerSession {
  private static IScoutLogger logger = ScoutLogManager
      .getLogger(TestEnvironmentServerSession.class);

  public TestEnvironmentServerSession() {
    super(true);
  }

  /**
   * @return session in current ThreadContext
   */
  public static TestEnvironmentServerSession get() {
    return (TestEnvironmentServerSession) ThreadContext.getServerSession();
  }

  @Override
  protected void execLoadSession() throws ProcessingException {
    logger.info("created a new session for " + getUserId());
  }
}
