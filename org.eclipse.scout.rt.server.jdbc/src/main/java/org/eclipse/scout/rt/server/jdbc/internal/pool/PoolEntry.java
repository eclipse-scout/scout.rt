/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.internal.pool;

import java.sql.Connection;

@SuppressWarnings({"squid:S00116", "squid:ClassVariableVisibilityCheck"})
class PoolEntry {
  public Connection conn;
  public long createTime;
  public long leaseBegin;
  public int leaseCount;
}
