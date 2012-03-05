/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;

/**
 * Transform entities and attributes from and to shared data objects and external ids (using folder and meta syntax:
 * foo/bar/name or foo/bar/name;key1=value1;key2=value2)
 * <p>
 * The external id is used to identify an entity, attribute or inner entity, attribute when using xml storages,
 * bookmarks, server calls.
 * <p>
 * see {@link AbstractDataModelAttributeData}, {@link AbstractDataModelEntityData},
 * {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)},
 * {@link IDataModel#getMetaDataOfAttributeData(AbstractDataModelAttributeData, Object[])}
 */
public final class DataModelUtility {

  private DataModelUtility() {
  }

  /**
   * using group 2,3,5
   */
  private static final Pattern PAT_EXTERNAL_ID = Pattern.compile("((.+)/)?([^/;]+)(;(.+))?");
  private static final Pattern PAT_SEMI_COLON = Pattern.compile("[;]");
  private static final Pattern PAT_NVPAIR = Pattern.compile("([^=]+)=(.*)");

  /**
   * @return the external id (foo/bar/foo) for an entity using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   */
  public static String entityToExternalId(IDataModelEntity e) {
    if (e.getParentEntity() != null) {
      return entityToExternalId(e.getParentEntity()) + "/" + e.getClass().getSimpleName() + exportMetaData(e.getMetaDataOfEntity());
    }
    else {
      return e.getClass().getSimpleName() + exportMetaData(e.getMetaDataOfEntity());
    }
  }

  /**
   * @return the external id (foo/bar/foo) for an attribute using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   */
  public static String attributeToExternalId(IDataModelAttribute a) {
    if (a.getParentEntity() != null) {
      return entityToExternalId(a.getParentEntity()) + "/" + a.getClass().getSimpleName() + exportMetaData(a.getMetaDataOfAttribute());
    }
    else {
      return a.getClass().getSimpleName() + exportMetaData(a.getMetaDataOfAttribute());
    }
  }

  /**
   * Computes the given attribute's external id along the given path of entities. In general, this method does not
   * return the same external id as {@link #attributeToExternalId(IDataModelAttribute)} because the {@link IDataModel}
   * creates only one instance for every entity. Hence an entity's {@link IDataModelEntity#getParentEntity()} references
   * always the parent entity it was initialized for.
   * 
   * @return Returns the external id for an attribute using the given attribute path.
   * @since 3.8.0
   */
  public static String attributeToExternalId(IDataModelAttribute a, IDataModelEntity... entityPath) {
    if (!isEntityPathValid(a, entityPath)) {
      // fallback
      return attributeToExternalId(a);
    }

    String id = a.getClass().getSimpleName() + exportMetaData(a.getMetaDataOfAttribute());
    for (int i = entityPath.length - 1; i >= 0; i--) {
      IDataModelEntity e = entityPath[i];
      id = e.getClass().getSimpleName() + "/" + id;
    }

    return id;
  }

  /**
   * Checks whether the given path of entities is valid for the given attribute (i.e. it is possible to navigate through
   * the list of entities and to ending up with the given attribute).
   * 
   * @return Returns <code>true</code> if the given path leads to the given attribute.
   * @since 3.8.0
   */
  private static boolean isEntityPathValid(IDataModelAttribute a, IDataModelEntity... entityPath) {
    if (a == null || entityPath == null || entityPath.length == 0) {
      return false;
    }

    IDataModelEntity tail = entityPath[entityPath.length - 1];
    if (tail == null || a != tail.getAttribute(a.getClass())) {
      return false;
    }

    for (int i = entityPath.length - 2; i >= 0; i--) {
      if (entityPath[i] == null || tail != entityPath[i].getEntity(tail.getClass())) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return the entity for an external id (foo/bar/foo) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   */
  public static IDataModelEntity externalIdToEntity(IDataModel f, String externalId, IDataModelEntity parentEntity) {
    if (externalId == null) {
      return null;
    }
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) {
      throw new IllegalArgumentException("externalId is invalid: " + externalId);
    }
    String folderName = m.group(2);
    String elemName = m.group(3);
    Map<String, String> meta = importMetaData(m.group(5));
    if (folderName != null) {
      parentEntity = externalIdToEntity(f, folderName, parentEntity);
      if (parentEntity == null) {
        return null;
      }
    }
    if (parentEntity != null) {
      return findEntity(parentEntity.getEntities(), elemName, meta);
    }
    else {
      return findEntity(f.getEntities(), elemName, meta);
    }
  }

  /**
   * Returns the path of entities starting by the root, which is described by the external ID.
   * 
   * @param f
   *          the data model
   * @param externalId
   *          string representation of an entity or attribute (the search stops with the last entity, if the id points
   *          to an attribute).
   * @param parentEntity
   *          optional parent entity the externalId is relative to. If <code>null</code>, the entities are resolved
   *          starting from the data model root entities.
   * @return
   * @since 3.8.0
   */
  public static IDataModelEntity[] externalIdToEntityPath(IDataModel f, String externalId, IDataModelEntity parentEntity) {
    List<IDataModelEntity> entityPath = new ArrayList<IDataModelEntity>();
    resolveEntityPath(f, externalId, parentEntity, entityPath);
    return entityPath.toArray(new IDataModelEntity[entityPath.size()]);

  }

  /**
   * Recursively resolves the path of entities described by the given external Id.
   * 
   * @return the list of all entities starting from the root entity.
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   * @since 3.8.0
   */
  private static IDataModelEntity resolveEntityPath(IDataModel f, String externalId, IDataModelEntity parentEntity, List<IDataModelEntity> entityPath) {
    if (externalId == null) {
      return null;
    }
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) {
      throw new IllegalArgumentException("externalId is invalid: " + externalId);
    }
    String folderName = m.group(2);
    String elemName = m.group(3);
    Map<String, String> meta = importMetaData(m.group(5));
    if (folderName != null) {
      parentEntity = resolveEntityPath(f, folderName, parentEntity, entityPath);
      if (parentEntity == null) {
        return null;
      }
    }
    IDataModelEntity result;
    if (parentEntity != null) {
      result = findEntity(parentEntity.getEntities(), elemName, meta);
    }
    else {
      result = findEntity(f.getEntities(), elemName, meta);
    }
    if (result != null) {
      entityPath.add(result);
    }
    return result;
  }

  /**
   * @return the attribute for an external id (foo/bar/foo) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   */
  public static IDataModelAttribute externalIdToAttribute(IDataModel f, String externalId, IDataModelEntity parentEntity) {
    if (externalId == null) {
      return null;
    }
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) {
      throw new IllegalArgumentException("externalId is invalid: " + externalId);
    }
    String folderName = m.group(2);
    String elemName = m.group(3);
    Map<String, String> meta = importMetaData(m.group(5));
    if (folderName != null) {
      parentEntity = externalIdToEntity(f, folderName, parentEntity);
      if (parentEntity == null) {
        return null;
      }
    }
    if (parentEntity != null) {
      return findAttribute(parentEntity.getAttributes(), elemName, meta);
    }
    else {
      return findAttribute(f.getAttributes(), elemName, meta);
    }
  }

  /**
   * @deprecated use {@link #findEntity(IDataModelEntity[], String, Map)} instead
   */
  @Deprecated
  public static IDataModelEntity findEntity(IDataModelEntity[] array, String simpleName) {
    return findEntity(array, simpleName, null);
  }

  /**
   * @return the entity for an external id part (no '/' characters) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   */
  public static IDataModelEntity findEntity(IDataModelEntity[] array, String simpleName, Map<String, String> metaData) {
    if (array != null) {
      for (IDataModelEntity e : array) {
        if (e.getClass().getSimpleName().equals(simpleName)) {
          if (CompareUtility.equals(e.getMetaDataOfEntity(), metaData)) {
            return e;
          }
        }
      }
    }
    return null;
  }

  /**
   * @return the attribute for an external id part (no '/' characters) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   */
  public static IDataModelAttribute findAttribute(IDataModelAttribute[] array, String simpleName, Map<String, String> metaData) {
    if (array != null) {
      for (IDataModelAttribute a : array) {
        if (a.getClass().getSimpleName().equals(simpleName)) {
          if (CompareUtility.equals(a.getMetaDataOfAttribute(), metaData)) {
            return a;
          }
        }
      }
    }
    return null;
  }

  /**
   * import a string of the form ;key=value;key=value;... to a map
   * <p>
   * when no name/value pairs or null string is imported as null
   * <p>
   * empty values are imported as null
   */
  public static Map<String, String> importMetaData(String s) {
    if (s == null) {
      return null;
    }
    Map<String, String> map = new HashMap<String, String>(1);
    for (String e : PAT_SEMI_COLON.split(s)) {
      Matcher m = PAT_NVPAIR.matcher(e);
      if (m.matches()) {
        map.put(m.group(1), m.group(2));
      }
    }
    return map.size() > 0 ? map : null;
  }

  /**
   * export a map to a string of the form ;key=value;key=value;...
   * <p>
   * a null or empty map is exported as empty string ""
   * <p>
   * null values are exported as empty strings
   */
  public static String exportMetaData(Map<String, String> map) {
    if (map == null || map.size() == 0) {
      return "";
    }
    StringBuffer buf = new StringBuffer(16);
    for (Map.Entry<String, String> e : map.entrySet()) {
      buf.append(";");
      buf.append(e.getKey());
      buf.append('=');
      if (e.getValue() != null) {
        buf.append(e.getValue());
      }
    }
    return buf.toString();
  }

  /**
   * Sorts the given array of data model entities by their display names.
   * 
   * @param array
   * @return Returns the the sorted array of entities.
   * @since 3.8.0
   */
  public static IDataModelEntity[] sortEntities(IDataModelEntity[] array) {
    if (array == null) {
      return null;
    }
    Arrays.sort(array, new Comparator<IDataModelEntity>() {
      @Override
      public int compare(IDataModelEntity o1, IDataModelEntity o2) {
        if (o1 == null && o2 == null) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o2 == null) {
          return 1;
        }
        return StringUtility.compareIgnoreCase(o1.getText(), o2.getText());
      }
    });
    return array;
  }

  /**
   * Sorts the given array of data model attributes by their display name. Those having
   * {@link IDataModelAttribute#getType()} == {@link DataModelConstants#TYPE_AGGREGATE_COUNT} are moved to the head.
   * 
   * @param array
   * @return Returns the sorted array of attributes.
   * @since 3.8.0
   */
  public static IDataModelAttribute[] sortAttributes(IDataModelAttribute[] array) {
    if (array == null) {
      return null;
    }
    Arrays.sort(array, new Comparator<IDataModelAttribute>() {
      @Override
      public int compare(IDataModelAttribute o1, IDataModelAttribute o2) {
        if (o1 == null && o2 == null) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o2 == null) {
          return 1;
        }
        if (o1.getType() == DataModelConstants.TYPE_AGGREGATE_COUNT && o2.getType() != DataModelConstants.TYPE_AGGREGATE_COUNT) {
          return -1;
        }
        if (o2.getType() == DataModelConstants.TYPE_AGGREGATE_COUNT && o1.getType() != DataModelConstants.TYPE_AGGREGATE_COUNT) {
          return 1;
        }
        return StringUtility.compareIgnoreCase(o1.getText(), o2.getText());
      }
    });
    return array;
  }
}
