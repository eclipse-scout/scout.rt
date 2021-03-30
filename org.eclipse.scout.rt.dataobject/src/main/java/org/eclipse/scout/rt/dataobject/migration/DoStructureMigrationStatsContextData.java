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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe implementation of stats for data object structure migration.
 */
@Bean
public class DoStructureMigrationStatsContextData implements IDoStructureMigrationGlobalContextData {

  private static final Logger LOG = LoggerFactory.getLogger(DoStructureMigrationStatsContextData.class);

  protected final AtomicInteger m_dataObjectsProcessed = new AtomicInteger();
  protected final AtomicInteger m_dataObjectsChanged = new AtomicInteger();
  protected final AtomicLong m_accumulatedMigrationDurationNano = new AtomicLong(); // nanoseconds

  protected void incrementDataObjectsProcessed() {
    m_dataObjectsProcessed.incrementAndGet();
  }

  protected void incrementDataObjectsChanged() {
    m_dataObjectsChanged.incrementAndGet();
  }

  /**
   * @param startNano
   *          {@link System#nanoTime()} when migration was started
   */
  protected void addMigrationDuration(long startNano) {
    m_accumulatedMigrationDurationNano.addAndGet(System.nanoTime() - startNano);
  }

  /**
   * @param name
   *          Name to print for entities
   * @param entityCount
   *          Number of entities processed (optional), can be different then the number of calls made to
   *          {@link DoStructureMigrator#migrateDataObject(DoStructureMigrationContext, IDataObject)}.
   */
  public void printStats(String name, Integer entityCount) {
    LOG.info("Migration of {}{} entities finished in {} milliseconds accumulated migration time. Changed {} of {} processed data objects.",
        entityCount == null ? "" : entityCount + " ", name, StringUtility.formatNanos(m_accumulatedMigrationDurationNano.get()), m_dataObjectsChanged.get(), m_dataObjectsProcessed.get());
  }
}
