/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration;

import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe implementation of a data object migration logger that additionally counts the log items per log level.
 */
// TODO 23.1 [data object migration] rename to DataObjectMigrationCountingPassThroughLogger
public class DoStructureMigrationCountingPassThroughLogger extends DoStructureMigrationPassThroughLogger {

  private static final Logger LOG = LoggerFactory.getLogger(DoStructureMigrationCountingPassThroughLogger.class);

  protected final LongAdder m_traceCount = new LongAdder();
  protected final LongAdder m_debugCount = new LongAdder();
  protected final LongAdder m_infoCount = new LongAdder();
  protected final LongAdder m_warnCount = new LongAdder();
  protected final LongAdder m_errorCount = new LongAdder();

  @Override
  public void trace(String message, Object... args) {
    super.trace(message, args);
    m_traceCount.increment();
  }

  @Override
  public void debug(String message, Object... args) {
    super.debug(message, args);
    m_debugCount.increment();
  }

  @Override
  public void info(String message, Object... args) {
    super.info(message, args);
    m_infoCount.increment();
  }

  @Override
  public void warn(String message, Object... args) {
    super.warn(message, args);
    m_warnCount.increment();
  }

  @Override
  public void error(String message, Object... args) {
    super.error(message, args);
    m_errorCount.increment();
  }

  public long getTraceCount() {
    return m_traceCount.sum();
  }

  public long getDebugCount() {
    return m_debugCount.sum();
  }

  public long getInfoCount() {
    return m_infoCount.sum();
  }

  public long getWarnCount() {
    return m_warnCount.sum();
  }

  public long getErrorCount() {
    return m_errorCount.sum();
  }

  public void printSummary() {
    long total = m_traceCount.sum() + m_debugCount.sum() + m_infoCount.sum() + m_warnCount.sum() + m_errorCount.sum();
    if (total == 0) {
      LOG.info("No data object migration log entries were made");
    }
    else {
      LOG.info("{} data object migration log entries were made: {} trace, {} debug, {} info, {} warn, {} error", total, m_traceCount.sum(), m_debugCount.sum(), m_infoCount.sum(), m_warnCount.sum(), m_errorCount.sum());
    }
  }
}
