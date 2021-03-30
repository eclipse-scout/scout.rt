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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DataObjectVisitors;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.namespace.Namespaces;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Helper for data object structure migration.
 */
@ApplicationScoped
public class DoStructureMigrationHelper {

  /**
   * Name of type attribute used for serialization.
   */
  public static final String TYPE_ATTRIBUTE_NAME = "_type";

  /**
   * Name of type version attribute used for serialization.
   */
  public static final String TYPE_VERSION_ATTRIBUTE_NAME = "_typeVersion";

  /**
   * @param doEntity
   *          Raw data object (typed data objects don't have a type attribute).
   */
  public String getType(IDoEntity doEntity) {
    assertNotNull(doEntity, "doEntity is required");
    return doEntity.getString(TYPE_ATTRIBUTE_NAME);
  }

  /**
   * Set the {@link #TYPE_ATTRIBUTE_NAME} to the provided data object with the provided type name
   *
   * @param doEntity
   *          Add type attribute to this data object.
   * @param typeName
   *          The typeName of the type attribute.
   */
  public void setType(IDoEntity doEntity, String typeName) {
    assertNotNull(doEntity, "doEntity is required");
    doEntity.put(TYPE_ATTRIBUTE_NAME, typeName);
  }

  /**
   * @param doEntity
   *          Raw data object (typed data objects don't have a type version attribute).
   */
  public NamespaceVersion getTypeVersion(IDoEntity doEntity) {
    assertNotNull(doEntity, "doEntity is required");
    return NamespaceVersion.of(doEntity.getString(TYPE_VERSION_ATTRIBUTE_NAME));
  }

  /**
   * Set the {@link #TYPE_VERSION_ATTRIBUTE_NAME} to the provided data object with the provided value.
   *
   * @param doEntity
   *          Add type version attribute to this raw data object.
   * @param version
   *          The value of the type version attribute.
   */
  public void setTypeVersion(IDoEntity doEntity, NamespaceVersion version) {
    assertNotNull(doEntity, "doEntity is required");
    doEntity.put(TYPE_VERSION_ATTRIBUTE_NAME, version == null ? null : version.unwrap());
  }

  /**
   * Updates the type version of the data object.
   *
   * @return <code>true</code> if the new version was set, <code>false</code> if the type version was already up-to-date
   *         (equal).
   */
  public boolean updateTypeVersion(IDoEntity doEntity, NamespaceVersion newVersion) {
    assertNotNull(doEntity, "doEntity is required");
    NamespaceVersion currentVersion = getTypeVersion(doEntity);
    if (ObjectUtility.equals(currentVersion, newVersion)) {
      return false; // type version already up-to-date
    }

    setTypeVersion(doEntity, newVersion);
    return true;
  }

  /**
   * Each {@link IDoEntity} within the given data object is present in the returned map, the map value (version) might
   * be <code>null</code> though.
   * <p>
   * If the map is empty this means that no raw {@link IDoEntity} (i.e. one with a type name) was found.
   *
   * @param rawDataObject
   *          Raw data object. If a typed data object is provided, no versions will be returned.
   * @return Map of type name to type version
   */
  public Map<String, NamespaceVersion> collectRawDataObjectTypeVersions(IDataObject rawDataObject) {
    Map<String, NamespaceVersion> typeVersionByTypeName = new HashMap<>();

    DataObjectVisitors.forEachRec(rawDataObject, IDoEntity.class, doEntity -> {
      NamespaceVersion typeVersion = getTypeVersion(doEntity);
      String typeName = getType(doEntity);
      if (typeName != null) {
        typeVersionByTypeName.put(typeName, typeVersion); // version might be null if a data object was persisted without a version yet
      }
    });

    return typeVersionByTypeName;
  }

  /**
   * A migration of a given version is applicable for the given data object if
   * <ul>
   * <li>it's a raw data object (has a type name in `_type')
   * <li>and the type version</li>
   * <ul>
   * <li>is missing</li>
   * <li>or is available and the namespaces are equal (given vs. type version of data object) and the data object's type
   * version is smaller than the given one
   * </ul>
   * </ul>
   */
  public boolean isMigrationApplicable(IDoEntity doEntity, NamespaceVersion version) {
    if (getType(doEntity) == null) {
      return false; // no raw data object
    }

    NamespaceVersion doEntityTypeVersionValue = getTypeVersion(doEntity);
    if (doEntityTypeVersionValue == null) {
      return true; // the initial persisted data object didn't have a type version so migrations should be applied (data object should never be persisted without a type version).
    }

    if (!doEntityTypeVersionValue.namespaceEquals(version)) {
      // The prefix of the type names is not relevant for this comparison and must not be used because when a data object is replaced, the type names will not
      // be changed, only the type version will be (resulting in a difference between type name prefix and type version name).
      // Example: a migration handler for version charlie-1.1.0 might migrate a data object with the type name bravo.Example.
      // A migration handler for version bravo-1.2.0 and a type name bravo.Example will not migrate a replaced data object anymore.
      // Any migrations must be manually adapter after changing a type version of a replaced data object.
      return false; // different namespace in type version, no migration
    }

    int compare = NamespaceVersion.compareVersion(doEntityTypeVersionValue, version);
    if (compare < 0) {
      return true; // type version is smaller then current version
    }

    return false; // type version of data object might be equal or even higher already than given version
  }

  /**
   * Quick check to verify if any migration might be required for the given type name/version pairs.
   * <p>
   * There is no guarantee that even if this method returns <code>true</code>, there will be applicable migration
   * handlers. But if the method returns <code>false</code>, there is no need to apply migrations (e.g. all type
   * versions are already up-to-date or unknown namespaces are present).
   *
   * @return <code>true</code> if for at least one type name {@link #isMigrationRequired(String, NamespaceVersion)}
   *         returns <code>true</code>.
   */
  public boolean isAnyMigrationRequired(Map<String, String> typeVersionsByTypeName) {
    if (CollectionUtility.isEmpty(typeVersionsByTypeName)) {
      return false; // no types present
    }

    for (Entry<String, String> entry : typeVersionsByTypeName.entrySet()) {
      String typeName = entry.getKey();
      String typeVersion = entry.getValue();
      NamespaceVersion typeVersionValue = NamespaceVersion.of(typeVersion);
      if (isMigrationRequired(typeName, typeVersionValue)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks whether it's possible that a migration might be required for this type name with the given type version.
   * <p>
   * Behaves similar as {@link #isMigrationApplicable(IDoEntity, NamespaceVersion)}.
   */
  protected boolean isMigrationRequired(String typeName, NamespaceVersion typeVersion) {
    if (typeVersion == null) {
      // Invalid because any persisted data object must have a type version.
      // Assume that migration is required and that a migration handler will add a type version.
      return true;
    }

    if (Namespaces.get().byId(typeVersion.getNamespace()) == null) {
      // If the type version has an unknown namespace, ignore it (shouldn't happen, e.g. foreign data objects).
      // A migration might still be required, but because the namespace is unknown, there won't be any migration handler for this one.
      return false;
    }

    DataObjectInventory inventory = BEANS.get(DataObjectInventory.class);
    Class<? extends IDoEntity> doEntityClass = inventory.fromTypeName(typeName);
    if (doEntityClass == null) {
      return true; // type name is unknown, probably renamed, assume outdated
    }

    NamespaceVersion currentTypeVersion = inventory.getTypeVersion(doEntityClass);
    if (!typeVersion.namespaceEquals(currentTypeVersion)) {
      return true; // namespace is different, thus might have been changed in a newer version, assume outdated
    }

    int compare = NamespaceVersion.compareVersion(typeVersion, currentTypeVersion);
    if (compare < 0) {
      return true; // previous < current -> outdated
    }

    // Type version is equal or higher than the one from the Java class (inventory).
    // There shouldn't exist persisted data object with type versions higher than the one from the corresponding Java class.
    // May be a result of an imported configuration from a newer system.
    return false;
  }

  /**
   * Renames the type name of the data object.
   *
   * @return <code>true</code> if type name was not already the new type name, <code>false</code> otherwise.
   */
  public boolean renameTypeName(IDoEntity doEntity, String newTypeName) {
    if (ObjectUtility.equals(getType(doEntity), newTypeName)) {
      return false;
    }

    setType(doEntity, newTypeName);
    return true;
  }

  /**
   * Renames an attribute on the data object.
   *
   * @return <code>true</code> if the attribute node with 'attributeName' exists and was renamed, <code>false</code>
   *         otherwise.
   */
  public boolean renameAttribute(IDoEntity doEntity, String attributeName, String newAttributeName) {
    if (!doEntity.has(attributeName)) {
      return false;
    }

    DoNode<?> node = doEntity.getNode(attributeName);
    doEntity.remove(attributeName);
    doEntity.putNode(newAttributeName, node);
    return true;
  }
}
