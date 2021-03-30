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

import org.eclipse.scout.rt.platform.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe logger that directly outputs to {@link Logger}.
 */
@Bean
public class DoStructureMigrationPassThroughLogger implements IDoStructureMigrationLogger {

  private static final Logger LOG = LoggerFactory.getLogger(DoStructureMigrationPassThroughLogger.class);

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
