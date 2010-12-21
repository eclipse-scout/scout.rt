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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CompareUtility;

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
      return entityToExternalId(e.getParentEntity()) + "/" + e.getClass().getSimpleName();
    }
    else {
      return e.getClass().getSimpleName();
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
   * @return the entity for an external id (foo/bar/foo) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   */
  public static IDataModelEntity externalIdToEntity(IDataModel f, String externalId, IDataModelEntity parentEntity) {
    if (externalId == null) return null;
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) throw new IllegalArgumentException("externalId is invalid: " + externalId);
    String folderName = m.group(2);
    String elemName = m.group(3);
    if (folderName != null) {
      parentEntity = externalIdToEntity(f, folderName, parentEntity);
      if (parentEntity == null) {
        return null;
      }
    }
    if (parentEntity != null) {
      return findEntity(parentEntity.getEntities(), elemName);
    }
    else {
      return findEntity(f.getEntities(), elemName);
    }
  }

  /**
   * @return the attribute for an external id (foo/bar/foo) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   */
  public static IDataModelAttribute externalIdToAttribute(IDataModel f, String externalId, IDataModelEntity parentEntity) {
    if (externalId == null) return null;
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) throw new IllegalArgumentException("externalId is invalid: " + externalId);
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
   * @return the entity for an external id part (no '/' characters) using
   *         {@link IDataModel#getMetaDataOfAttribute(IDataModelAttribute)}
   */
  public static IDataModelEntity findEntity(IDataModelEntity[] array, String simpleName) {
    if (array != null) {
      for (IDataModelEntity e : array) {
        if (e.getClass().getSimpleName().equals(simpleName)) {
          return e;
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
    IDataModelAttribute secondaryMatch = null;
    if (array != null) {
      for (IDataModelAttribute a : array) {
        if (a.getClass().getSimpleName().equals(simpleName)) {
          secondaryMatch = a;
          if (CompareUtility.equals(a.getMetaDataOfAttribute(), metaData)) {
            return a;
          }
        }
      }
    }
    return secondaryMatch;
  }

  /**
   * import a string of the form ;key=value;key=value;... to a map
   * <p>
   * when no name/value pairs or null string is imported as null
   * <p>
   * empty values are imported as null
   */
  public static Map<String, String> importMetaData(String s) {
    if (s == null) return null;
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
    if (map == null || map.size() == 0) return "";
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
}
