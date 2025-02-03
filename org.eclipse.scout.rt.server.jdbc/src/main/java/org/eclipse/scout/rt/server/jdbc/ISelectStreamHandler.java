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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public interface ISelectStreamHandler {

  void handleRow(Connection con, PreparedStatement stm, ResultSet rs, int rowIndex, List<SqlBind> values);

  void finished(Connection con, PreparedStatement stm, ResultSet rs, int rowCount);
}
