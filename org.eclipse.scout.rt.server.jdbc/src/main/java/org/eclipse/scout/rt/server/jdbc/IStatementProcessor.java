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
package org.eclipse.scout.rt.server.jdbc;

import java.sql.Connection;

public interface IStatementProcessor {

  Object[][] processSelect(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor);

  void processSelectInto(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor);

  void processSelectStreaming(Connection conn, IStatementCache cache, ISelectStreamHandler handler);

  int processModification(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor);

  boolean processStoredProcedure(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor);

  String createPlainText();

  /**
   * Simulate the execution of the statement and dump output to System.out
   */
  void simulate();

}
