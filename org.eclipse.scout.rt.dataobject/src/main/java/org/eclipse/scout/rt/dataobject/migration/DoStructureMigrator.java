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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Main class for data object structure migration.
 * <p>
 * Example usage:
 *
 * <pre>
 * DoStructureMigrationContext ctx = BEANS.get(DoStructureMigrationContext.class);
 * DoStructureMigratorResult<ExampleDo> result = BEANS.get(DoStructureMigrator.class).migrateDataObject(ctx, rawContent, ExampleDo.class);
 * ctx.getStats().printStats("example", 1);
 * </pre>
 */
@ApplicationScoped
public class DoStructureMigrator {

  /**
   * Migrates the data object provided by string (UTF-8 encoded) and casts it to the given data object class.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DoStructureMigratorResult<T> migrateDataObject(DoStructureMigrationContext ctx, String json, Class<T> valueType) {
    assertNotNull(json, "json is required");
    return migrateDataObject(ctx, new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), valueType);
  }

  /**
   * Migrates the data object provided by input stream and casts it to the given data object class.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DoStructureMigratorResult<T> migrateDataObject(DoStructureMigrationContext ctx, InputStream inputStream, Class<T> valueType) {
    assertNotNull(inputStream, "inputStream is required");
    IDataObjectMapper dataObjectMapper = BEANS.get(IDataObjectMapper.class);
    IDataObject dataObject = dataObjectMapper.readValueRaw(inputStream);
    return migrateDataObject(ctx, dataObject, valueType);
  }

  /**
   * Migrates the data object provided as raw data object and casts it to the given data object class.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DoStructureMigratorResult<T> migrateDataObject(DoStructureMigrationContext ctx, IDataObject dataObject, Class<T> valueType) {
    assertNotNull(valueType, "valueType is required");
    IDataObjectMapper dataObjectMapper = BEANS.get(IDataObjectMapper.class);
    boolean changed = migrateDataObject(ctx, dataObject);
    String json = dataObjectMapper.writeValue(dataObject);
    T typedDataObject = dataObjectMapper.readValue(json, valueType);
    return new DoStructureMigratorResult<>(typedDataObject, changed);
  }

  /**
   * Migrates the raw data object.
   * <p>
   * Uses latest version to migrate too.
   */
  public boolean migrateDataObject(DoStructureMigrationContext ctx, IDataObject dataObject) {
    return migrateDataObject(ctx, dataObject, (NamespaceVersion) null /* latest version */);
  }

  /**
   * <b>ATTENTION:</b> use {@link #migrateDataObject(DoStructureMigrationContext, IDataObject)} instead. Only use this
   * for tests and very special cases.
   * <p>
   * Migrates the raw data object.
   *
   * @param dataObject
   *          Raw data object, might be a partial non-raw data object (i.e. _type info on certain entities). Only raw
   *          data object parts are migrated.
   * @param toVersion
   *          Versions to migrate to, <code>null</code> if migrating to latest version.
   */
  public boolean migrateDataObject(DoStructureMigrationContext ctx, IDataObject dataObject, NamespaceVersion toVersion) {
    assertNotNull(ctx, "ctx is required");
    assertNotNull(dataObject, "dataObject is required");

    DoStructureMigrationContext ctxCopy = ctx.copy(); // copy context to work on own stack for local context data.
    DoStructureMigrationStatsContextData stats = ctxCopy.getStats();
    IDoStructureMigrationLogger logger = ctxCopy.getLogger();

    long start = System.nanoTime();

    stats.incrementDataObjectsProcessed();
    logger.trace("Data object before migration: {}", dataObject);

    Map<String, NamespaceVersion> typeVersions = BEANS.get(DoStructureMigrationHelper.class).collectRawDataObjectTypeVersions(dataObject);
    if (typeVersions.isEmpty()) {
      logger.debug("No data object entities with a type name found within {}", dataObject);
      stats.addMigrationDuration(start);
      return false;
    }

    List<NamespaceVersion> versions = BEANS.get(DoStructureMigrationInventory.class).getVersions(typeVersions, toVersion);
    if (versions.isEmpty()) {
      stats.addMigrationDuration(start);
      return false;
    }

    boolean changed = false;
    for (NamespaceVersion version : versions) {
      changed |= migrateDataObject(ctxCopy, version, dataObject);
    }

    if (changed) {
      stats.incrementDataObjectsChanged();
      logger.trace("Data object after migration: {}", dataObject);
    }

    logger.debug("Applied migrations [{} -> {}] on {}", CollectionUtility.firstElement(versions), CollectionUtility.lastElement(versions), dataObject);

    stats.addMigrationDuration(start);
    return changed;
  }

  protected boolean migrateDataObject(DoStructureMigrationContext ctx, NamespaceVersion version, IDataObject dataObject) {
    MigrationDataObjectVisitor visitor = new MigrationDataObjectVisitor(ctx, version);
    visitor.migrate(dataObject);
    return visitor.isChanged();
  }

  public static class DoStructureMigratorResult<T extends IDataObject> {

    private T m_dataObject;
    private boolean m_changed;

    public DoStructureMigratorResult(T dataObject, boolean changed) {
      m_dataObject = dataObject;
      m_changed = changed;
    }

    public T getDataObject() {
      return m_dataObject;
    }

    public boolean isChanged() {
      return m_changed;
    }
  }
}
