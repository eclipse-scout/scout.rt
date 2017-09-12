/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@FunctionalInterface
public interface IStatementProcessorMonitor {

  /**
   * Called after data has been completely fetched and before operation is going to close affected resources.
   * 
   * @param rows
   *          is the life list of rows fetched so far, changes to this list will be returned by the original method
   *          call. Used in methods
   *          {@link IStatementProcessor#processSelect(Connection, IStatementCache, IStatementProcessorMonitor)}
   *          {@link IStatementProcessor#processSelectInto(Connection, IStatementCache, IStatementProcessorMonitor)}
   */
  void postFetchData(Connection con, PreparedStatement stm, ResultSet rs, List<Object[]> rows);
}
