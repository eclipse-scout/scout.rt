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

import org.eclipse.scout.rt.platform.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe logger that directly outputs to {@link Logger}.
 */
@Bean
public class DataObjectMigrationPassThroughLogger implements IDataObjectMigrationLogger {

  private static final Logger LOG = LoggerFactory.getLogger(DataObjectMigrationPassThroughLogger.class);

  @Override
  public void trace(String message, Object... args) {
    LOG.trace(message, args);
  }

  @Override
  public void debug(String message, Object... args) {
    LOG.debug(message, args);
  }

  @Override
  public void info(String message, Object... args) {
    LOG.info(message, args);
  }

  @Override
  public void warn(String message, Object... args) {
    LOG.warn(message, args);
  }

  @Override
  public void error(String message, Object... args) {
    LOG.error(message, args);
  }
}
