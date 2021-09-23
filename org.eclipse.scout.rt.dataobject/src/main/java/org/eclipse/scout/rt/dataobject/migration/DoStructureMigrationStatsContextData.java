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

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

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

  protected final AtomicLong m_startNanos = new AtomicLong();
  protected final LongAdder m_dataObjectsProcessed = new LongAdder();
  protected final LongAdder m_dataObjectsChanged = new LongAdder();
  protected final LongAdder m_accumulatedMigrationDurationNano = new LongAdder(); // nanoseconds

  public void start() {
    m_startNanos.compareAndSet(0, System.nanoTime());
  }

  protected void incrementDataObjectsProcessed() {
    m_dataObjectsProcessed.increment();
  }

  protected void incrementDataObjectsChanged() {
    m_dataObjectsChanged.increment();
  }

  /**
   * @param startNano
   *          {@link System#nanoTime()} when migration was started
   */
  protected void addMigrationDuration(long startNano) {
    m_accumulatedMigrationDurationNano.add(System.nanoTime() - startNano);
  }

  /**
   * @param name
   *          Name to print for entities
   * @param entityCount
   *          Number of entities processed (optional), can be different then the number of calls made to
   *          {@link DoStructureMigrator#migrateDataObject(DoStructureMigrationContext, IDataObject)}.
   */
  public void printStats(String name, Integer entityCount) {
    LOG.info("Migration of {}{} entities finished in {} ms (accumulated raw data object migration took {} ms). Changed {} of {} processed data objects.",
        entityCount == null ? "" : entityCount + " ",
        name,
        m_startNanos.get() == 0 ? "?" : StringUtility.formatNanos(System.nanoTime() - m_startNanos.get()),
        StringUtility.formatNanos(m_accumulatedMigrationDurationNano.sum()),
        m_dataObjectsChanged.sum(),
        m_dataObjectsProcessed.sum());
  }
}
