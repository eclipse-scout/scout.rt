/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration;

/**
 * Interface of a global context data used as logger.
 */
public interface IDoStructureMigrationLogger extends IDoStructureMigrationGlobalContextData {

  @Override
  default Class<? extends IDoStructureMigrationGlobalContextData> getIdentifierClass() {
    return IDoStructureMigrationLogger.class;
  }

  void trace(String message, Object... args);

  void debug(String message, Object... args);

  void info(String message, Object... args);

  void warn(String message, Object... args);

  void error(String message, Object... args);
}
