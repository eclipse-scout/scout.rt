/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
