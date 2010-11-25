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
package org.eclipse.scout.rt.server.services.common.jdbc.internal.pool;

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

class ConnectionCloseThread extends Thread {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SqlConnectionPool.class);

  private Connection m_conn;

  public ConnectionCloseThread(String name, Connection conn) {
    super(name);
    m_conn = conn;
    setDaemon(true);
  }

  @Override
  public void run() {
    if (LOG.isInfoEnabled()) LOG.info("close connection " + m_conn);
    try {
      m_conn.close();
    }
    catch (SQLException e) {
      LOG.error("connection: " + m_conn, e);
    }
  }
}
