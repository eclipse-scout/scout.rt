/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(DataModelUtility.class);

  private DataModelUtility() {
  }

  /**
   * using group 2,3,5
   */
  private static final Pattern PAT_EXTERNAL_ID = Pattern.compile("((.+)/)?([^/;]+)(;(.+))?");
  private static final Pattern PAT_SEMI_COLON = Pattern.compile("[;]");
  private static final Pattern PAT_NVPAIR = Pattern.compile("([^=]+)=(.*)");

  /**
   * @return the external id (foo/bar/foo) for an entity using {@link Class#getSimpleName()} and
   *         {@link IDataModelEntity#getMetaDataOfEntity()}
   * @since 3.8.0
   */
  public static String entityPathToExternalId(IDataModel f, EntityPath entityPath) {
    if (entityPath == null || entityPath.size() == 0) {
      return "";
    }
    StringBuilder buf = new StringBuilder();
    for (IDataModelEntity e : entityPath.elements()) {
      if (buf.length() > 0) {
        buf.append("/");
      }
      buf.append(e.getClass().getSimpleName());
      buf.append(exportMetaData(e.getMetaDataOfEntity()));
    }
    String externalId = buf.toString();
    if (LOG.isInfoEnabled()) {
      EntityPath verify = externalIdToEntityPath(f, externalId);
      if (verify == null) {
        LOG.info("entity externalId " + externalId + " resolves to null");
      }
      else if (!verify.equals(entityPath)) {
        LOG.info("entity externalId " + externalId + " is not valid for " + entityPath);
      }
    }
    return externalId;
  }

  /**
   * Computes the given attribute's external id along the given path of entities.
   * 
   * @return the external id (foo/bar/foo) for an attribute using {@link Class#getSimpleName()} and
   *         {@link IDataModelEntity#getMetaDataOfEntity()}, {@link IDataModelAttribute#getMetaDataOfAttribute()}
   * @since 3.8.0
   */
  public static String attributePathToExternalId(IDataModel f, AttributePath attributePath) {
    if (attributePath == null) {
      return "";
    }
    StringBuilder buf = new StringBuilder();
    buf.append(entityPathToExternalId(f, attributePath.getEntityPath()));
    if (buf.length() > 0) {
      buf.append("/");
    }
    IDataModelAttribute a = attributePath.getAttribute();
    buf.append(a.getClass().getSimpleName());
    buf.append(exportMetaData(a.getMetaDataOfAttribute()));
    String externalId = buf.toString();
    if (LOG.isInfoEnabled()) {
      AttributePath verify = externalIdToAttributePath(f, externalId);
      if (verify == null) {
        LOG.info("attribute externalId " + externalId + " resolves to null");
      }
      else if (!verify.equals(attributePath)) {
        LOG.info("attribute externalId " + externalId + " is not valid for " + attributePath);
      }
    }
    return externalId;
  }

  /**
   * Returns the path of entities starting by the root, which is described by the external ID.
   * 
   * @param f
   *          the data model
   * @param externalId
   *          string representation of an entity
   * @return
   * @since 3.8.0
   */
  public static EntityPath externalIdToEntityPath(IDataModel f, String externalId) {
    if (externalId == null || externalId.length() == 0) {
      return EntityPath.EMPTY;
    }
    return resolveEntityPathRec(f, externalId, EntityPath.EMPTY);
  }

  /**
   * @return the attribute for an external id (foo/bar/foo) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   * @since 3.8.0
   */
  public static AttributePath externalIdToAttributePath(IDataModel f, String externalId) {
    if (externalId == null || externalId.length() == 0) {
      return null;
    }
    return resolveAttributePath(f, externalId);
  }

  /**
   * Recursively resolves the path of entities described by the given external Id.
   * 
   * @return the list of all entities starting from the root entity.
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   * @since 3.8.0
   */
  private static EntityPath resolveEntityPathRec(IDataModel f, String externalId, EntityPath inputPath) {
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) {
      throw new IllegalArgumentException("externalId is invalid: " + externalId);
    }
    String folderName = m.group(2);
    String elemName = m.group(3);
    Map<String, String> meta = importMetaData(m.group(5));
    EntityPath resolvedPath;
    if (folderName != null) {
      resolvedPath = resolveEntityPathRec(f, folderName, inputPath);
      if (resolvedPath == null) {
        return null;
      }
    }
    else {
      resolvedPath = inputPath;
    }
    IDataModelEntity parentEntity = resolvedPath.lastElement();
    IDataModelEntity e;
    if (parentEntity != null) {
      e = findEntity(parentEntity.getEntities(), elemName, meta);
    }
    else {
      e = findEntity(f.getEntities(), elemName, meta);
    }
    if (e == null) {
      if (LOG.isInfoEnabled()) {
        LOG.info("entity externalId " + externalId + " resolves to null");
      }
      return null;
    }
    return resolvedPath.addToEnd(e);
  }

  private static AttributePath resolveAttributePath(IDataModel f, String externalId) {
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) {
      throw new IllegalArgumentException("externalId is invalid: " + externalId);
    }
    String folderName = m.group(2);
    String elemName = m.group(3);
    Map<String, String> meta = importMetaData(m.group(5));
    EntityPath entityPath;
    if (folderName != null) {
      entityPath = resolveEntityPathRec(f, folderName, EntityPath.EMPTY);
      if (entityPath == null) {
        return null;
      }
    }
    else {
      entityPath = EntityPath.EMPTY;
    }
    IDataModelEntity parentEntity = entityPath.lastElement();
    IDataModelAttribute a;
    if (parentEntity != null) {
      a = findAttribute(parentEntity.getAttributes(), elemName, meta);
    }
    else {
      a = findAttribute(f.getAttributes(), elemName, meta);
    }
    if (a == null) {
      if (LOG.isInfoEnabled()) {
        LOG.info("attribute externalId " + externalId + " resolves to null");
      }
      return null;
    }
    return entityPath.addToEnd(a);
  }

  /**
   * @return the entity for an external id part (no '/' characters) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   */
  public static IDataModelEntity findEntity(List<? extends IDataModelEntity> entities, String simpleName, Map<String, String> metaData) {
    if (entities != null) {
      for (IDataModelEntity e : entities) {
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
  public static IDataModelAttribute findAttribute(List<? extends IDataModelAttribute> attributes, String simpleName, Map<String, String> metaData) {
    if (attributes != null) {
      for (IDataModelAttribute a : attributes) {
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
  public static List<? extends IDataModelEntity> sortEntities(List<? extends IDataModelEntity> entities) {
    if (CollectionUtility.isEmpty(entities)) {
      return CollectionUtility.emptyArrayList();
    }
    entities = new ArrayList<IDataModelEntity>(entities);
    Collections.sort(entities, new Comparator<IDataModelEntity>() {
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
    return entities;
  }

  /**
   * Sorts the given array of data model attributes by their display name. Those having
   * {@link IDataModelAttribute#getType()} == {@link DataModelConstants#TYPE_AGGREGATE_COUNT} are moved to the head.
   * 
   * @param array
   * @return Returns the sorted array of attributes.
   * @since 3.8.0
   */
  public static List<? extends IDataModelAttribute> sortAttributes(List<? extends IDataModelAttribute> attributes) {
    if (CollectionUtility.isEmpty(attributes)) {
      return CollectionUtility.emptyArrayList();
    }
    attributes = new ArrayList<IDataModelAttribute>(attributes);
    Collections.sort(attributes, new Comparator<IDataModelAttribute>() {
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
    return attributes;
  }
}
