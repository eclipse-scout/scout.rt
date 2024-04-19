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

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.namespace.INamespace;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersionedModel;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersionedModel.VersionedItems;
import org.eclipse.scout.rt.platform.namespace.Namespaces;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.Pair;

/**
 * Inventory of data object migration related classes (namespaces, versions, structure and value migration handlers,
 * context data classes).
 */
@ApplicationScoped
public class DataObjectMigrationInventory {

  protected final LinkedHashSet<String> m_namespaces = new LinkedHashSet<>();
  protected final LinkedHashSet<NamespaceVersion> m_orderedVersions = new LinkedHashSet<>(); // ordered versions according to VersionedItemInventory
  protected ByNamespaceVersionComparator m_comparator = null;
  // only one structure migration handler per type version and type name can exist
  protected final Map<NamespaceVersion, Map<String /* type name */, IDoStructureMigrationHandler>> m_structureMigrationHandlers = new HashMap<>();

  protected final Map<String /* type name */, Set<Class<? extends IDoStructureMigrationTargetContextData>>> m_doContextDataClassByTypeName = new HashMap<>();
  // Contains subclasses (replaced data objects) of classes within DoStructureMigrationContextDataTarget#doEntityClasses too.
  // This is a performance optimization so that lookup via getDoMigrationContextValues doesn't need to resolve parent classes.
  protected final Map<Class<? extends IDoEntity>, Set<Class<? extends IDoStructureMigrationTargetContextData>>> m_doContextDataClassByDoEntityClass = new HashMap<>();

  /**
   * For each type name the versions with available migration handlers.
   */
  protected final Map<String, List<NamespaceVersion>> m_typeNameVersions = new HashMap<>();

  /**
   * Based on current data from {@link DataObjectInventory}.
   */
  protected final Map<String, NamespaceVersion> m_typeNameToCurrentTypeVersion = new HashMap<>();

  /**
   * Ordered map of all value migration handlers.
   */
  protected final LinkedHashMap<DoValueMigrationId, IDoValueMigrationHandler<?>> m_valueMigrationHandlers = new LinkedHashMap<>();

  /**
   * @return All namespaces (sorted).
   */
  protected List<INamespace> getAllNamespaces() {
    return Namespaces.get().all();
  }

  /**
   * @return All type versions.
   */
  protected Collection<ITypeVersion> getAllTypeVersions() {
    return BEANS.all(ITypeVersion.class);
  }

  /**
   * @return All context data classes ({@link IDoStructureMigrationTargetContextData} annotated with
   *         {@link DoStructureMigrationContextDataTarget}).
   */
  protected Collection<Class<? extends IDoStructureMigrationTargetContextData>> getAllContextDataClasses() {
    //noinspection unchecked
    return ClassInventory.get().getKnownAnnotatedTypes(DoStructureMigrationContextDataTarget.class)
        .stream()
        .filter(IClassInfo::isInstanciable)
        .map(IClassInfo::resolveClass)
        .filter(IDoStructureMigrationTargetContextData.class::isAssignableFrom)
        .map(clazz -> (Class<? extends IDoStructureMigrationTargetContextData>) clazz)
        .collect(Collectors.toList());
  }

  /**
   * @return All structure migration handlers.
   */
  protected List<IDoStructureMigrationHandler> getAllStructureMigrationHandlers() {
    return BEANS.all(IDoStructureMigrationHandler.class);
  }

  /**
   * @return All value migration handlers.
   */
  protected List<IDoValueMigrationHandler<?>> getAllValueMigrationHandlers() {
    return BEANS.all(IDoValueMigrationHandler.class).stream()
        .map(handler -> (IDoValueMigrationHandler<?>) handler)
        .collect(Collectors.toList());
  }

  @PostConstruct
  protected void init() {
    // Collect namespaces
    getAllNamespaces().stream().map(INamespace::getId).forEach(m_namespaces::add);

    // Collect and build version model
    NamespaceVersionedModel<ITypeVersion> model = createDefaultModel();
    VersionedItems<ITypeVersion> items = model.getItems();
    assertTrue(items.isValid(), "Type version model is not valid");

    items.getItems().stream().map(ITypeVersion::getVersion).forEach(m_orderedVersions::add);

    m_comparator = new ByNamespaceVersionComparator(new ArrayList<>(m_orderedVersions));

    // Collect current type name/type versions
    DataObjectInventory dataObjectInventory = BEANS.get(DataObjectInventory.class);
    for (Entry<String, Class<? extends IDoEntity>> entry : dataObjectInventory.getTypeNameToClassMap().entrySet()) {
      String typeName = entry.getKey();
      NamespaceVersion typeVersion = dataObjectInventory.getTypeVersion(entry.getValue());
      if (typeVersion != null) {
        m_typeNameToCurrentTypeVersion.put(typeName, typeVersion);
      }
    }

    // Collect IDataObjectDoMigrationContextValue
    IClassInventory classInventory = ClassInventory.get();
    for (Class<? extends IDoStructureMigrationTargetContextData> contextValueClass : getAllContextDataClasses()) {
      DoStructureMigrationContextDataTarget annotation = contextValueClass.getAnnotation(DoStructureMigrationContextDataTarget.class);
      assertNotNull(annotation, "Annotation @{} is missing on {}", DoStructureMigrationContextDataTarget.class.getSimpleName(), contextValueClass);
      if (annotation.typeNames() != null) {
        for (String typeName : annotation.typeNames()) {
          Set<Class<? extends IDoStructureMigrationTargetContextData>> contextDataClasses = m_doContextDataClassByTypeName.computeIfAbsent(typeName, k -> new HashSet<>());
          contextDataClasses.add(contextValueClass);
        }
      }
      if (annotation.doEntityClasses() != null) {
        for (Class<? extends IDoEntity> doEntityClass : annotation.doEntityClasses()) {
          assertFalse(doEntityClass == IDoEntity.class || doEntityClass == DoEntity.class, "{}: {} and {} are invalid do entity classes for the annotation {}",
              contextValueClass, IDoEntity.class.getSimpleName(), DoEntity.class.getSimpleName(), DoStructureMigrationContextDataTarget.class.getSimpleName());

          // class itself
          m_doContextDataClassByDoEntityClass.computeIfAbsent(doEntityClass, k -> new HashSet<>()).add(contextValueClass);
          // all known subclasses
          //noinspection unchecked
          classInventory.getAllKnownSubClasses(doEntityClass).stream()
              .map(IClassInfo::resolveClass) // all subclasses of doEntityClass implement IDoEntity too, thus cast is okay
              .map(clazz -> (Class<? extends IDoEntity>) clazz)
              .map(clazz -> m_doContextDataClassByDoEntityClass.computeIfAbsent(clazz, k -> new HashSet<>()))
              .forEach(contextDataClasses -> contextDataClasses.add(contextValueClass));
        }
      }
    }

    // Collect structure migration handlers
    Map<NamespaceVersion, Map<String /* type name */, List<IDoStructureMigrationHandler>>> migrationHandlersPerVersionAndTypeName = new HashMap<>();
    Map<String, Set<NamespaceVersion>> unorderedTypeNameVersions = new HashMap<>();
    for (IDoStructureMigrationHandler migrationHandler : getAllStructureMigrationHandlers()) {
      validateStructureMigrationHandler(migrationHandler);

      // Collect structure migration handlers
      Map<String, List<IDoStructureMigrationHandler>> migrationHandlersPerTypeName = migrationHandlersPerVersionAndTypeName.computeIfAbsent(migrationHandler.toTypeVersion(), k -> new HashMap<>());
      for (String typeName : migrationHandler.getTypeNames()) {
        List<IDoStructureMigrationHandler> migrationHandlers = migrationHandlersPerTypeName.computeIfAbsent(typeName, k -> new ArrayList<>());
        migrationHandlers.add(migrationHandler);

        // there might be several migration handlers for the same type name and type version, thus use set to eliminate duplicates (sorting occurs afterwards)
        Set<NamespaceVersion> versions = unorderedTypeNameVersions.computeIfAbsent(typeName, k -> new HashSet<>());
        versions.add(migrationHandler.toTypeVersion());
      }
    }

    validateStructureMigrationHandlerUniqueness(migrationHandlersPerVersionAndTypeName);

    // validation takes care that there is only one migration handler per type version and type name
    migrationHandlersPerVersionAndTypeName.forEach((version, migrationHandlersPerTypeName) -> {
      Map<String, IDoStructureMigrationHandler> migrationHandlerPerTypeName = new HashMap<>();
      migrationHandlersPerTypeName.forEach((typeName, migrationHandlers) -> migrationHandlerPerTypeName.put(typeName, CollectionUtility.firstElement(migrationHandlers)));
      m_structureMigrationHandlers.put(version, migrationHandlerPerTypeName);
    });

    // Sort and add versions to m_typeNameVersions.
    for (Entry<String, Set<NamespaceVersion>> entry : unorderedTypeNameVersions.entrySet()) {
      List<NamespaceVersion> versions = new ArrayList<>(entry.getValue());
      versions.sort(m_comparator);
      m_typeNameVersions.put(entry.getKey(), versions);
    }

    // Collect value migration handlers
    List<IDoValueMigrationHandler<?>> allValueMigrationHandlers = getAllValueMigrationHandlers();
    Map<DoValueMigrationId, IDoValueMigrationHandler<?>> validatedValueMigrationHandlers = new HashMap<>();
    allValueMigrationHandlers.forEach(handler -> validateValueMigrationHandler(handler, validatedValueMigrationHandlers));
    // Primary sort order by type version, secondary sort order provided by bean manager (order annotation and fully qualified class name).
    allValueMigrationHandlers.stream()
        .sorted(Comparator
            .comparingDouble((IDoValueMigrationHandler<?> handler) -> handler.primarySortOrder())
            .thenComparing(IDoValueMigrationHandler::typeVersion, m_comparator))
        .forEach(handler -> m_valueMigrationHandlers.put(handler.id(), handler));
  }

  protected void validateStructureMigrationHandlerUniqueness(Map<NamespaceVersion, Map<String, List<IDoStructureMigrationHandler>>> migrationHandlersPerVersionAndTypeName) {
    // Verify that there is not more than one migration handler per type version and type name
    StringBuilder duplicateBuilder = new StringBuilder();
    for (Entry<NamespaceVersion, Map<String, List<IDoStructureMigrationHandler>>> versionEntry : migrationHandlersPerVersionAndTypeName.entrySet()) {
      NamespaceVersion version = versionEntry.getKey();
      for (Entry<String, List<IDoStructureMigrationHandler>> migrationHandlerEntry : versionEntry.getValue().entrySet()) {
        if (migrationHandlerEntry.getValue().size() > 1) {
          if (duplicateBuilder.length() > 0) {
            duplicateBuilder.append("\n");
          }
          duplicateBuilder.append(migrationHandlerEntry.getKey());
          duplicateBuilder.append("@");
          duplicateBuilder.append(version.unwrap());
          duplicateBuilder.append(": ");
          duplicateBuilder.append(migrationHandlerEntry.getValue().stream().map(Object::getClass).map(Class::getSimpleName).collect(Collectors.joining(", ")));
        }
      }
    }

    if (duplicateBuilder.length() > 0) {
      throw new PlatformException("Found multiple migration handlers for the same type version/type name:\n{}", duplicateBuilder.toString());
    }
  }

  protected NamespaceVersionedModel<ITypeVersion> createDefaultModel() {
    return NamespaceVersionedModel.<ITypeVersion> newBuilder()
        .withNames(getAllNamespaces().stream().map(INamespace::getId).collect(Collectors.toList()))
        .withItems(getAllTypeVersions())
        .build();
  }

  /**
   * Validations:
   * <ul>
   * <li>There are type names
   * <li>Verifies that the target version ({@link IDoStructureMigrationHandler#toTypeVersion()}) of the migration
   * handler are known versions
   * <li>For all type names there is a type version as least as high as the target type version of the migration handler
   * </ul>
   */
  protected <T extends IDoStructureMigrationHandler> T validateStructureMigrationHandler(T migrationHandler) {
    // Check for missing type names
    if (CollectionUtility.isEmpty(migrationHandler.getTypeNames())) {
      throw new PlatformException("Migration handler {} doesn't have any type names", migrationHandler);
    }

    // Check for invalid type names
    if (CollectionUtility.containsAny(migrationHandler.getTypeNames(), null, "")) {
      throw new PlatformException("Migration handler {} has invalid type names (empty, null)", migrationHandler);
    }

    // Check for unknown toTypeVersion
    NamespaceVersion toTypeVersion = migrationHandler.toTypeVersion();
    if (!m_orderedVersions.contains(toTypeVersion)) {
      throw new PlatformException("Unknown toTypeVersion value {}. Make sure that the type version value is registered within a {}", toTypeVersion, ITypeVersion.class.getSimpleName());
    }

    // The namespace of the previous type version of a data object must be equal to the namespace of the version of the migration handler, otherwise no migration handler
    // will be called because DoStructureMigrationHelper#isMigrationApplicable will return false on different type version namespaces.
    // This cannot be validated here because we don't have access to the previous type version.

    validateDataObjectTypeVersion(migrationHandler);

    return migrationHandler;
  }

  /**
   * Validates that if there is a migration handler for a certain type version and type names, each data object must
   * have at least this type version, otherwise the migration handler would set a higher version than the data object
   * has (assuming that {@link IDoStructureMigrationHandler#toTypeVersion()} is the target type version).
   * <p>
   * This validation prevents that a migration handler is written but the type version on the data object class was not
   * updated accordingly.
   * <p>
   * It's not an exact validation (best effort only).
   */
  protected void validateDataObjectTypeVersion(IDoStructureMigrationHandler migrationHandler) {
    NamespaceVersion typeVersionToUpdate = migrationHandler.toTypeVersion();
    DataObjectInventory inventory = BEANS.get(DataObjectInventory.class);
    for (String typeName : migrationHandler.getTypeNames()) {
      Class<? extends IDoEntity> doEntityClass = inventory.fromTypeName(typeName);
      if (doEntityClass == null) {
        // Maybe an old migration handler where the type name was changed in the meantime. Skip it.
        // When an old migration handler is involved the consistency of the type version should have been check then anyway, so it seems
        // to be fine just skipping it.
        continue;
      }

      // There might be false positives when in case of and old migration handler the type name was changed and a new data object got the same name later on.
      // We ignore this scenario currently, maybe need to handle it in a future release (e.g. by only checking the migration handlers for the newest version?).
      NamespaceVersion doEntityVersion = inventory.getTypeVersion(doEntityClass);
      if (doEntityVersion == null) {
        throw new PlatformException("Missing a type version (at least {}) for {} specified as type name in {}", typeVersionToUpdate, typeName, migrationHandler);
      }

      if (doEntityVersion.namespaceEquals(typeVersionToUpdate) && NamespaceVersion.compareVersion(doEntityVersion, typeVersionToUpdate) < 0) {
        // Only compare if namespace is equal. If the DO is replaced the namespace is not equal anymore
        throw new PlatformException("Entity do '{}' has specified a lower version than the migration handler '{}'. [entityDoVersion={}, migrationHandlerVersion={}]",
            typeName, migrationHandler.getClass().getSimpleName(), doEntityVersion, typeVersionToUpdate);
      }
    }
  }

  /**
   * Validates a {@link IDoValueMigrationHandler} for unique value migration ID and valid type version.
   *
   * @param validatedValueMigrationHandlers
   *          Map of already validated value migration handlers, used to check for unique value migration IDs
   * @param valueMigrationHandler
   *          Value migration handler to be validated
   */
  protected void validateValueMigrationHandler(IDoValueMigrationHandler valueMigrationHandler, Map<DoValueMigrationId, IDoValueMigrationHandler<?>> validatedValueMigrationHandlers) {
    // Check for duplicate value migration ID
    IDoValueMigrationHandler<?> existingHandler = validatedValueMigrationHandlers.put(valueMigrationHandler.id(), valueMigrationHandler);
    if (existingHandler != null) {
      throw new PlatformException("Multiple value migration handlers with identical ID detected. Value migration handler IDs must be unique.\n"
          + "ID: {}, value migration handlers: {}, {}", valueMigrationHandler.id(), existingHandler.getClass().getSimpleName(), valueMigrationHandler.getClass().getSimpleName());
    }

    // Check for unknown type version
    NamespaceVersion typeVersion = valueMigrationHandler.typeVersion();
    if (!m_orderedVersions.contains(typeVersion)) {
      throw new PlatformException("Unknown type version {}. Make sure that the type version value is registered within a {}", typeVersion, ITypeVersion.class.getSimpleName());
    }
  }

  /**
   * @return Unmodifiable list of all available {@link IDoValueMigrationHandler}s, ordered by type version. Migration
   *         handlers with the same type version are ordered according to sort order provided by the bean manager (order
   *         annotation and fully qualified class name).
   */
  public List<IDoValueMigrationHandler<?>> getValueMigrationHandlers() {
    return m_valueMigrationHandlers.values().stream()
        .collect(Collectors.toUnmodifiableList());
  }

  /**
   * @return {@link IDoValueMigrationHandler} for a given {@link DoValueMigrationId}
   */
  public IDoValueMigrationHandler<?> getValueMigrationHandler(DoValueMigrationId valueMigrationId) {
    return m_valueMigrationHandlers.get(valueMigrationId);
  }

  /**
   * @return Unmodifiable list of ordered versions according to VersionedItemInventory.
   */
  public List<NamespaceVersion> getAllVersionsOrdered() {
    return Collections.unmodifiableList(new ArrayList<>(m_orderedVersions));
  }

  /**
   * For each type name {@link #findNextMigrationHandlerVersion(String, NamespaceVersion)} is called. The lowest version
   * of all type names defines the starting point.
   *
   * @param typeNames
   *          Map from type name to version.
   * @param toVersion
   *          <code>null</code> to return versions up to the newest version or a known type version to only return
   *          versions up to the provided version.
   * @return List of versions for which migrations must be applied, might be an empty list (never <code>null</code>).
   */
  public List<NamespaceVersion> getVersions(Map<String, NamespaceVersion> typeNames, NamespaceVersion toVersion) {
    assertTrue(toVersion == null || m_orderedVersions.contains(toVersion), "toVersion '{}' is unknown", toVersion);

    NamespaceVersion lowestNextVersion = null;
    for (Entry<String, NamespaceVersion> entry : typeNames.entrySet()) {
      Pair<FindNextMigrationHandlerVersionStatus, NamespaceVersion> nextVersionPair = findNextMigrationHandlerVersion(entry.getKey(), entry.getValue());
      NamespaceVersion nextVersion = nextVersionPair.getRight();
      // only version is relevant, status can be ignored here
      if (nextVersion == null) {
        continue; // type name must not be considered (e.g. already current version)
      }

      if (lowestNextVersion == null || m_comparator.compare(lowestNextVersion, nextVersion) > 0) {
        lowestNextVersion = nextVersion; // found a new lowest version
      }
    }

    if (lowestNextVersion == null) {
      // lowestNextVersion is null if there are no migration handlers available for the given type name/versions.
      return Collections.emptyList();
    }

    List<NamespaceVersion> versions = new ArrayList<>(m_orderedVersions);
    int lowerIndex = versions.indexOf(lowestNextVersion);
    versions = versions.subList(lowerIndex, versions.size());

    if (toVersion != null) {
      // limit upper version if required
      int upperIndex = versions.indexOf(toVersion);
      if (upperIndex < 0) {
        // toVersion is not part of version if and only if lower bound was already higher than toVersion -> no versions
        return Collections.emptyList();
      }

      versions = versions.subList(0, upperIndex + 1); // inclusive
    }

    // remove all versions that don't have migration handlers
    versions.removeIf(version -> !m_structureMigrationHandlers.containsKey(version));

    return new ArrayList<>(versions);
  }

  /**
   * This method may be called to check whether a given type name/type version is valid, i.e. either already
   * representing an existing DO entity with the corresponding type version or there are migration handlers available
   * that would migrate that data object.
   *
   * @param typeName
   *          Current type name
   * @param version
   *          Current type version
   * @return <code>true</code> if the associated DO entity is either up-to-date or a migration is available for this
   *         type name.
   */
  public boolean isUpToDateOrMigrationAvailable(String typeName, NamespaceVersion version) {
    Pair<FindNextMigrationHandlerVersionStatus, NamespaceVersion> result = findNextMigrationHandlerVersion(typeName, version);
    return ObjectUtility.isOneOf(result.getLeft(),
        FindNextMigrationHandlerVersionStatus.UP_TO_DATE,
        FindNextMigrationHandlerVersionStatus.NO_TYPE_VERSION_YET,
        FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND);
  }

  protected enum FindNextMigrationHandlerVersionStatus {

    /**
     * Already the current type version (according to the {@link TypeVersion} annotation on the corresponding data
     * object).
     * <p>
     * No {@link NamespaceVersion} is returned.
     */
    UP_TO_DATE,

    /**
     * No migration handlers found for this type name that would migrate this data object to an up-to-date type version.
     * <p>
     * Possible reason: unknown/foreign type name
     * <p>
     * No {@link NamespaceVersion} is returned.
     */
    NO_MIGRATION_HANDLERS,

    /**
     * No type version is present yet, starts with the first migration available for this type name.
     * <p>
     * The {@link NamespaceVersion} of the first available migration handler for this type name is returned.
     */
    NO_TYPE_VERSION_YET,

    /**
     * The provided type version is unknown.
     * <p>
     * If nothing went wrong with the corresponding data object when it was persisted, the type version could only be a
     * newer one not yet known to this system (inserted into this system via e.g. import).
     * <p>
     * No {@link NamespaceVersion} is returned.
     */
    UNKNOWN_TYPE_VERSION,

    /**
     * The pair of type name/type version is somehow no consistent and no matching migration handler can be determined,
     * e.g. not renamed type name for this type version or a migration handler is missing for this type name because
     * type version is not up-to-date.
     * <p>
     * No {@link NamespaceVersion} is returned.
     */
    INVALID_TYPE_NAME_VERSION_PAIR,

    /**
     * Found a migration handlers that is going to be executed for this type name/version
     * <p>
     * The {@link NamespaceVersion} of the next migration handler is returned.
     */
    MIGRATION_HANDLER_FOUND
  }

  /**
   * Determines the lowest possible version for which a migration handler could be triggered for the provided type
   * name/version.
   * <p>
   * If the type name doesn't have the current version yet (based on {@link DataObjectInventory}, the returned version
   * is the first version > provided version this type name is affected by a migration handler.
   *
   * @param version
   *          Type version of given type name (might be <code>null</code> if persisted without a type version yet)
   * @return Non-<code>null</code> pair of status and lowest possible version or <code>null</code> namespace version if
   *         there will be no migration handler that migrates this data object (already the newest version or due to
   *         other reasons).
   */
  protected Pair<FindNextMigrationHandlerVersionStatus, NamespaceVersion> findNextMigrationHandlerVersion(String typeName, NamespaceVersion version) {
    // Example used within comments:
    //
    // - Type name "lorem.Migrationless" with current type version "lorem-2" (@TypeVersion)
    //   - Missing migration handler to "lorem-2"
    //
    // - Type name "lorem.MissingMigration" with current type version "lorem-3" (@TypeVersion)
    //   - Migration handler for type version "lorem-2"
    //   - Missing migration handler to "lorem-3"
    //
    // - Type name "lorem.Example" with current type version "lorem-3" (@TypeVersion)
    //   - Migration handler for type version "lorem-2"
    //   - Migration handler for type version "lorem-3"
    //
    // - Type name "lorem.Beta" (previously "lorem.Alpha" since "lorem-1") with current type version "lorem-4" (@TypeVersion)
    //   - Migration handler for type version "lorem-2" that acts on "lorem.Alpha" and renames it to "lorem.Beta"
    //   - Migration handler for type version "lorem-4" that acts on "lorem.Beta"
    //
    // - Type name "lorem.Two" (previously "lorem.One" since "lorem-1") with current type version "lorem-2" (@TypeVersion)
    //   - Migration handler for type version "lorem-2" that acts on "lorem.One" and renames it to "lorem.Two"
    //
    // - Type name "lorem.One" with current type version "lorem-6" (@TypeVersion) (introduced newly with "lorem-4", different data object, using same name as an old one until "lorem-2")
    //   - Migration handler for type version "lorem-5" that acts on "lorem.One"
    //   - Migration handler for type version "lorem-6" that acts on "lorem.One"
    //
    // - Type name "lorem.Switch" with current type version "ipsum-3" (@TypeVersion) (introduced with "lorem-5")
    //   - Migration handler for type version "lorem-6" that acts on "lorem.Switch" and uses "ipsum-2" as typeVersionToUpdate
    //   - Migration handler for type version "ipsum-3" that acts on "lorem.Switch"
    //
    // - Type name "ipsum.SwitchAndRename" with current type version "ipsum-3" (@TypeVersion) (introduced with "lorem.SwitchAndRename" in type version "lorem-5")
    //   - Migration handler for type version "lorem-6" that acts on "lorem.SwitchAndRename" and renames it to "ipsum.SwitchAndRename" and uses "ipsum-2" as typeVersionToUpdate
    //   - Migration handler for type version "ipsum-3" that acts on "ipsum.SwitchAndRename"
    //
    // - Known type versions "lorem-1", "lorem-2", "lorem-3", "lorem-4", "lorem-5", "lorem-6", "ipsum-2", "ipsum-3"

    NamespaceVersion typeVersion = m_typeNameToCurrentTypeVersion.get(typeName);
    if (typeVersion != null && typeVersion.equals(version)) {
      // Already the current type version, thus no need to check for migration handlers.
      return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.UP_TO_DATE, null); // Example: "lorem-3" (current type version)
    }

    // No type version maybe found for previously known type names due to renaming (example "lorem.Alpha").

    List<NamespaceVersion> versions = m_typeNameVersions.get(typeName);
    if (versions == null) {
      // Despite not having the current type version, no migration handlers are available that would handle this type name (rare case, unknown/foreign type name, no migration to trigger).
      return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.NO_MIGRATION_HANDLERS, null); // Example: type name "other.Example" or "lorem.Migrationless"
    }

    if (version == null) {
      // Persisted data object didn't have a type version yet -> start with first migration
      // Example: type name "lorem.Example" without a type version
      NamespaceVersion firstVersion = CollectionUtility.firstElement(versions);
      return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.NO_TYPE_VERSION_YET, firstVersion);
    }

    if (!m_orderedVersions.contains(version)) {
      // The type version for this type name has a version that is unknown.
      // If nothing went wrong with the corresponding data object when it was persisted, the type version could only be a newer one not yet known to this system (inserted into this system via e.g. import).
      // This check is required because m_comparator must only be used with known type versions.
      return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.UNKNOWN_TYPE_VERSION, null); // Example: "lorem.Example" with type version "lorem-7" (e.g. exported from a newer system)
    }

    // Search for the provided type version within the version having a migration handler for this type name.
    int retVal = Collections.binarySearch(versions, version, m_comparator);
    if (retVal >= 0) {
      // Exact match found: look for the next migration handler for this type name
      if (retVal + 1 == versions.size()) { // no more migration handlers
        // Example: "lorem.MissingMigration" with type version "lorem-2" (invalid, no migration handler present)
        // Example: "lorem.Alpha" with type version "lorem-2" (invalid, because should have been renamed to "lorem.Beta" when having "lorem-2")
        return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.INVALID_TYPE_NAME_VERSION_PAIR, null);
      }

      // The version after that one is the next version that should run a migration handler for this type name.
      return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, versions.get(retVal + 1)); // Example: "lorem.Example" with type version "lorem-2" -> "lorem-3" | "lorem.One" with type version "lorem-5" -> "lorem-6"
    }

    // No exact match found, happens for example if there was no migration handler executed yet for this type name.

    // binarySearch retVal = (-(insertion point) - 1)
    int insertionPoint = -retVal - 1;
    if (insertionPoint == versions.size()) { // all elements in the list are less than the specified key
      // There is no newer version with migration handlers available for this type name but data object still has a different @TypeVersion annotation
      // Two scenarios:
      // - 1: a renamed type name
      // - 2: an outdated @TypeVersion annotation and missing migration handler (example "lorem.Example" type version "lorem-4" but @TypeVersion is "lorem-3")

      // Returning null would be invalid because there are other migration handlers that are relevant for the new type name.
      // Thus return next from full list.
      List<NamespaceVersion> orderedVersions = new ArrayList<>(m_orderedVersions);
      int index = orderedVersions.indexOf(version);
      if (index + 1 < orderedVersions.size()) {
        // Example: "lorem.Alpha" with type version "lorem-1" -> "lorem-2" based on m_orderedVersions (first relevant migration handler for "lorem.Beta" is in "lorem-4")
        // Example: "lorem.SwitchAndRename" with type version "lorem-5" -> "lorem-6" based on m_orderedVersions (first relevant migration handler for "ipsum.SwitchAndRename" is in "ipsum-3")
        return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, orderedVersions.get(index + 1));
      }

      // If the current version is the last version in m_orderedVersions then something is wrong
      return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.INVALID_TYPE_NAME_VERSION_PAIR, null); // Example: scenario 2
    }

    // insertionPoint: the index of the first element greater than the key
    // Example: "lorem.Example" with type version "lorem-1" -> "lorem-2"
    // Example: "lorem.One" with type version "lorem-1" -> "lorem-2"
    // Example: "lorem.One" with type version "lorem-4" -> "lorem-5"
    // Example: "lorem.Switch" with type version "lorem-5" -> "lorem-6"
    // Example: "lorem.Switch" with type version "ipsum-2" -> "lorem-3"
    // Example: "ipsum.SwitchAndRename" with type version "ipsum-2" -> "lorem-3"
    return ImmutablePair.of(FindNextMigrationHandlerVersionStatus.MIGRATION_HANDLER_FOUND, versions.get(insertionPoint));
  }

  public Map<String, IDoStructureMigrationHandler> getStructureMigrationHandlers(NamespaceVersion version) {
    assertNotNull(version, "version is required");
    assertTrue(m_orderedVersions.contains(version), "version is unknown");
    return m_structureMigrationHandlers.computeIfAbsent(version, k -> Collections.emptyMap());
  }

  public Set<Class<? extends IDoStructureMigrationTargetContextData>> getStructureMigrationTargetContextDataClasses(IDoEntity doEntity) {
    assertNotNull(doEntity, "doEntity is required");

    String typeName = BEANS.get(DoStructureMigrationHelper.class).getType(doEntity);
    if (typeName != null) {
      return m_doContextDataClassByTypeName.getOrDefault(typeName, Collections.emptySet());
    }
    else { // data object
      return m_doContextDataClassByDoEntityClass.getOrDefault(doEntity.getClass(), Collections.emptySet());
    }
  }

  /**
   * Comparator for {@link NamespaceVersion} based on the total order of all versions according to the dependency model.
   * <p>
   * This comparator can only be used if the versions to compare are known, i.e. part of the ones provided in the
   * constructor.
   */
  protected static class ByNamespaceVersionComparator implements Comparator<NamespaceVersion>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<NamespaceVersion, Integer> m_ordering;

    /**
     * @param versions
     *          Versions (<code>null</code> versions are not allowed).
     */
    public ByNamespaceVersionComparator(List<NamespaceVersion> versions) {
      m_ordering = createOrdering(versions);
    }

    protected static Map<NamespaceVersion, Integer> createOrdering(List<NamespaceVersion> versions) {
      Map<NamespaceVersion, Integer> nameOrder = new HashMap<>();
      for (int i = 0; i < versions.size(); i++) {
        nameOrder.put(assertNotNull(versions.get(i)), i); // version must never be null
      }
      return nameOrder;
    }

    @Override
    public int compare(NamespaceVersion o1, NamespaceVersion o2) {
      // comparison only allowed for known versions, thus no versions is missing in ordering - no <null> handling
      Integer s1 = assertNotNull(m_ordering.get(o1), "order for o1 ({}) is missing", o1.unwrap());
      Integer s2 = assertNotNull(m_ordering.get(o2), "order for o2 ({}) is missing", o2.unwrap());
      return s1.compareTo(s2);
    }
  }
}
