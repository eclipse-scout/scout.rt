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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.ILenientDataObjectMapper;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Main class for data object migration.
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
public class DataObjectMigrator {

  /**
   * Migrates the data object provided by string (UTF-8 encoded) and casts it to the given data object class.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DataObjectMigratorResult<T> migrateDataObject(DataObjectMigrationContext ctx, String json, Class<T> valueType) {
    assertNotNull(json, "json is required");
    return migrateDataObject(ctx, new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), valueType);
  }

  /**
   * Migrates the data object provided by input stream and casts it to the given data object class.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DataObjectMigratorResult<T> migrateDataObject(DataObjectMigrationContext ctx, InputStream inputStream, Class<T> valueType) {
    assertNotNull(inputStream, "inputStream is required");
    IDataObjectMapper dataObjectMapper = BEANS.get(IDataObjectMapper.class);
    IDataObject dataObject = dataObjectMapper.readValueRaw(inputStream);
    return migrateDataObject(ctx, dataObject, valueType);
  }

  /**
   * Migrates the data object provided as raw data object and casts it to the given data object class.
   * <p>
   * Uses latest version to migrate to. Uses no initial local context data.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DataObjectMigratorResult<T> migrateDataObject(DataObjectMigrationContext ctx, IDataObject dataObject, Class<T> valueType) {
    assertNotNull(valueType, "valueType is required");
    return migrateDataObject(ctx, dataObject, valueType, (IDataObjectMigrationLocalContextData[]) null);
  }

  /**
   * Migrates the raw data object.
   * <p>
   * Uses latest version to migrate to. Uses given initial local context data during execution.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DataObjectMigratorResult<T> migrateDataObject(DataObjectMigrationContext ctx, IDataObject dataObject, Class<T> valueType, IDataObjectMigrationLocalContextData... initialLocalContextData) {
    return migrateDataObject(ctx, dataObject, valueType, null /* latest version */, initialLocalContextData == null ? Collections.emptyList() : Arrays.asList(initialLocalContextData), Collections.emptyList());
  }

  /**
   * Migrates the raw data object.
   * <p>
   * Uses latest version to migrate to. Uses given initial local context data and given intermediate migrations during
   * execution.
   *
   * @return Result with typed data object and a flag if a migration was applied.
   */
  public <T extends IDataObject> DataObjectMigratorResult<T> migrateDataObject(
      DataObjectMigrationContext ctx,
      IDataObject dataObject,
      Class<T> valueType,
      List<IDataObjectMigrationLocalContextData> initialLocalContextData,
      List<IDataObjectIntermediateMigration<T>> localIntermediateMigrations) {
    return migrateDataObject(ctx, dataObject, valueType, null /* latest version */, initialLocalContextData, localIntermediateMigrations);
  }

  /**
   * <b>ATTENTION:</b> use {@link #migrateDataObject(DataObjectMigrationContext, IDataObject, Class)} or
   * {@link #migrateDataObject(DataObjectMigrationContext, IDataObject, Class, IDataObjectMigrationLocalContextData...)}
   * instead. Only use this for tests and very special cases.
   * <p>
   * Migrates the raw data object.
   *
   * @param dataObject
   *     Raw data object, might be a partial non-raw data object (i.e. _type info on certain entities). Only raw
   *     data object parts are migrated.
   * @param valueType
   *     The migrated object is converted to {@code valueType}. Can be {@link IDataObject} or a more specific
   *     sub-class.
   * @param toVersion
   *     Versions to migrate to, <code>null</code> if migrating to latest version.
   * @param initialLocalContextData
   *     Initial local context data to use.
   * @param localIntermediateMigrations
   *     Local intermediate migrations to use for this data object. These intermediate migrations will be applied
   *     after the intermediate migrations defined on the {@link DataObjectMigrationContext}.
   */
  public <T extends IDataObject> DataObjectMigratorResult<T> migrateDataObject(
      DataObjectMigrationContext ctx,
      IDataObject dataObject,
      Class<T> valueType,
      NamespaceVersion toVersion,
      List<IDataObjectMigrationLocalContextData> initialLocalContextData,
      List<IDataObjectIntermediateMigration<T>> localIntermediateMigrations) {
    assertNotNull(ctx, "ctx is required");
    assertNotNull(dataObject, "dataObject is required");

    // Copy context to work on own stack for local context data.
    // Local context may be initialized via initialLocalContextData.
    DataObjectMigrationContext ctxCopy = ctx.copy().withInitialLocalContext(initialLocalContextData);
    DataObjectMigrationStatsContextData stats = ctxCopy.getStats();
    IDataObjectMigrationLogger logger = ctxCopy.getLogger();

    long start = System.nanoTime();

    stats.incrementDataObjectsProcessed();
    logger.trace("Data object before migration: {}", dataObject);

    // Apply structure migration
    boolean structureChanged = applyStructureMigration(ctxCopy, dataObject, toVersion);
    if (structureChanged) {
      ctx.getLogger().trace("Data object after structure migration: {}", dataObject);
    }

    // Convert to typed object by using lenient data object mapper (value migrations might migrate additional values that cannot be put into valid typed structures yet)
    IDataObjectMapper lenientDataObjectMapper = BEANS.get(ILenientDataObjectMapper.class);
    String migratedJson = lenientDataObjectMapper.writeValue(dataObject);
    T typedDataObject = lenientDataObjectMapper.readValue(migratedJson, valueType);

    // Apply intermediate migrations on typed data object (if any), start with global (defined on context) and continue with local (provided as method parameter)
    List<IDataObjectIntermediateMigration<T>> allIntermediateMigrations = CollectionUtility.combine(ctx.getIntermediateMigrations().all(valueType), localIntermediateMigrations);
    boolean intermediateChanged = false;
    for (IDataObjectIntermediateMigration<T> intermediateMigration : allIntermediateMigrations) {
      DataObjectMigratorResult<?> result = intermediateMigration.applyMigration(ctx, typedDataObject);
      intermediateChanged |= result.isChanged();
      // Always use the (new) typed data object, even if not marked as changed.
      // There might be intermediate migrations that might do some form of normalization but don't want to mark it as changed only due to that.
      //noinspection unchecked
      typedDataObject = (T) result.getDataObject();
    }

    if (intermediateChanged) {
      logger.trace("Data object after intermediate migrations: {}", typedDataObject);
    }

    // Apply value migration
    DataObjectMigratorResult<T> result = applyValueMigration(ctxCopy, typedDataObject);
    boolean valueChanged = result.isChanged();
    T migratedDataObject = result.getDataObject();
    if (valueChanged) {
      logger.trace("Data object after value migration: {}", migratedDataObject);
    }

    // Convert to typed object by using regular data object mapper (if data object is not valid yet, it must fail, caller expects a valid data object)
    IDataObjectMapper dataObjectMapper = BEANS.get(IDataObjectMapper.class);
    String finalJson = dataObjectMapper.writeValue(migratedDataObject);
    migratedDataObject = dataObjectMapper.readValue(finalJson, valueType);

    boolean objectChanged = structureChanged || intermediateChanged || valueChanged;
    if (objectChanged) {
      stats.incrementDataObjectsChanged();
    }
    stats.addMigrationDuration(start);

    return DataObjectMigratorResult.of(migratedDataObject, objectChanged);
  }

  protected boolean applyStructureMigration(DataObjectMigrationContext ctx, IDataObject dataObject, NamespaceVersion toVersion) {
    Map<String, NamespaceVersion> typeVersions = BEANS.get(DoStructureMigrationHelper.class).collectRawDataObjectTypeVersions(dataObject);
    if (typeVersions.isEmpty()) {
      ctx.getLogger().debug("No data object entities with a type name found within {}", dataObject);
      return false;
    }

    List<NamespaceVersion> versions = BEANS.get(DataObjectMigrationInventory.class).getVersions(typeVersions, toVersion);
    if (versions.isEmpty()) {
      return false;
    }

    // Apply data object structure migrations
    boolean structureChanged = false;
    for (NamespaceVersion version : versions) {
      DoStructureMigrationDataObjectVisitor visitor = createStructureMigrationVisitor(ctx, version);
      visitor.migrate(dataObject);
      structureChanged |= visitor.isChanged();
    }
    ctx.getLogger().debug("Applied structure migrations [{} -> {}] on {}", CollectionUtility.firstElement(versions), CollectionUtility.lastElement(versions), dataObject);

    return structureChanged;
  }

  protected DoStructureMigrationDataObjectVisitor createStructureMigrationVisitor(DataObjectMigrationContext ctx, NamespaceVersion version) {
    return new DoStructureMigrationDataObjectVisitor(ctx, version);
  }

  protected <T extends IDataObject> DataObjectMigratorResult<T> applyValueMigration(DataObjectMigrationContext ctx, T dataObject) {
    // Applied value migration IDs should be explicitly set to an empty set, if all value migrations actually should be applied.
    // A null value indicates a caller which is unaware of value migrations - skip value migrations.
    if (ctx.getGlobal(DoValueMigrationIdsContextData.class).getAppliedValueMigrationIds() == null) {
      return DataObjectMigratorResult.of(dataObject, false);
    }

    DoValueMigrationDataObjectVisitor valueMigrationVisitor = createValueMigrationVisitor(ctx);
    T migratedDataObject = valueMigrationVisitor.migrate(dataObject);
    return DataObjectMigratorResult.of(migratedDataObject, valueMigrationVisitor.isChanged());
  }

  protected DoValueMigrationDataObjectVisitor createValueMigrationVisitor(DataObjectMigrationContext ctx) {
    return new DoValueMigrationDataObjectVisitor(ctx);
  }

  public static final class DataObjectMigratorResult<T extends IDataObject> {

    private T m_dataObject;
    private boolean m_changed;

    private DataObjectMigratorResult(T dataObject, boolean changed) {
      m_dataObject = dataObject;
      m_changed = changed;
    }

    public static <T extends IDataObject> DataObjectMigratorResult<T> of(T dataObject, boolean changed) {
      return new DataObjectMigratorResult<>(dataObject, changed);
    }

    public T getDataObject() {
      return m_dataObject;
    }

    public boolean isChanged() {
      return m_changed;
    }
  }
}
