/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration;

/**
 * Interface of a global context data used as logger.
 */
public interface IDataObjectMigrationLogger extends IDataObjectMigrationGlobalContextData {

  @Override
  default Class<? extends IDataObjectMigrationGlobalContextData> getIdentifierClass() {
    return IDataObjectMigrationLogger.class;
  }

  void trace(String message, Object... args);

  void debug(String message, Object... args);

  void info(String message, Object... args);

  void warn(String message, Object... args);

  void error(String message, Object... args);
}
