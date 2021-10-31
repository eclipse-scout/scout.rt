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

  /**
   * Sets the initial migration start time, to be called before any migration related operation is executed (e.g. data
   * objects are loaded, ...).
   */
  public void start() {
    m_startNanos.compareAndSet(0, System.nanoTime());
  }

  /**
   * Increment the number of processed data objects. To be called for each processed data object.
   */
  protected void incrementDataObjectsProcessed() {
    m_dataObjectsProcessed.increment();
  }

  /**
   * Difference in nanoseconds between calling {@link #start()} and this method.
   *
   * @return Duration in nanoseconds for the overall migration process.
   */
  public long getOverallMigrationDurationNano() {
    return System.nanoTime() - m_startNanos.get();
  }

  /**
   * @return The number of processed data objects.
   */
  public long getDataObjectsProcessedCount() {
    return m_dataObjectsProcessed.sum();
  }

  /**
   * Increment the number of changed data objects. To be called for each processed data object that has changed.
   */
  protected void incrementDataObjectsChanged() {
    m_dataObjectsChanged.increment();
  }

  /**
   * @return The number of changed data objects.
   */
  public long getDataObjectsChangedCount() {
    return m_dataObjectsChanged.sum();
  }

  /**
   * Accumulation of raw data object migration duration. To be called after migration of a single data object.
   *
   * @param startNano
   *          {@link System#nanoTime()} when migration was started
   */
  protected void addMigrationDuration(long startNano) {
    m_accumulatedMigrationDurationNano.add(System.nanoTime() - startNano);
  }

  /**
   * @return Duration in nanoseconds for the accumulated raw data object migration durations.
   */
  public long getAccumulatedMigrationDurationNano() {
    return m_accumulatedMigrationDurationNano.sum();
  }

  /**
   * @param name
   *          Name to print for entities
   * @param entityCount
   *          Number of entities processed (optional), can be different than the number of calls made to
   *          {@link DoStructureMigrator#migrateDataObject(DoStructureMigrationContext, IDataObject)}.
   */
  public void printStats(String name, Integer entityCount) {
    LOG.info("DO structure migration of {}{} entities finished in {} ms (accumulated raw data object migration took {} ms). Changed {} of {} processed data objects.",
        entityCount == null ? "" : entityCount + " ",
        name,
        m_startNanos.get() == 0 ? "?" : StringUtility.formatNanos(getOverallMigrationDurationNano()),
        StringUtility.formatNanos(getAccumulatedMigrationDurationNano()),
        getDataObjectsChangedCount(),
        getDataObjectsProcessedCount());
  }
}
