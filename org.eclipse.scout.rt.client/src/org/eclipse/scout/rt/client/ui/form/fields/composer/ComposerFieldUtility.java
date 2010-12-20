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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.client.ui.form.fields.composer.entity.IComposerEntity;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerAttributeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerEntityData;

/**
 * Transform entities and attributes from and to shared data objects and external ids (using folder and meta syntax:
 * foo/bar/name or foo/bar/name;key1=value1;key2=value2)
 * <p>
 * The external id is used to identify an entity, attribute or inner entity, attribute when using xml storages,
 * bookmarks, server calls.
 * <p>
 * see {@link AbstractComposerAttributeData}, {@link AbstractComposerEntityData},
 * {@link IComposerField#getMetaDataOfAttribute(IComposerAttribute)},
 * {@link IComposerField#getMetaDataOfAttributeData(AbstractComposerAttributeData, Object[])}
 */
public final class ComposerFieldUtility {

  private ComposerFieldUtility() {
  }

  /**
   * using group 2,3,5
   */
  private static final Pattern PAT_EXTERNAL_ID = Pattern.compile("((.+)/)?([^/;]+)(;(.+))?");
  private static final Pattern PAT_SEMI_COLON = Pattern.compile("[;]");
  private static final Pattern PAT_NVPAIR = Pattern.compile("([^=]+)=(.*)");

  /**
   * @return the external id (foo/bar/foo) for an entity using
   *         {@link IComposerField#createExternalIdPartForEntity(IComposerEntity)} and
   *         {@link IComposerField#createExternalIdPartForAttribute(IComposerAttribute)}
   */
  public static String entityToExternalId(IComposerField f, IComposerEntity e) {
    if (e.getParentEntity() != null) {
      return entityToExternalId(f, e.getParentEntity()) + "/" + e.getClass().getSimpleName();
    }
    else {
      return e.getClass().getSimpleName();
    }
  }

  /**
   * @return the external id (foo/bar/foo) for an attribute using
   *         {@link IComposerField#createExternalIdPartForEntity(IComposerEntity)} and
   *         {@link IComposerField#createExternalIdPartForAttribute(IComposerAttribute)}
   */
  public static String attributeToExternalId(IComposerField f, IComposerAttribute a) {
    if (a.getParentEntity() != null) {
      return entityToExternalId(f, a.getParentEntity()) + "/" + a.getClass().getSimpleName() + exportMetaData(f.getMetaDataOfAttribute(a));
    }
    else {
      return a.getClass().getSimpleName() + exportMetaData(f.getMetaDataOfAttribute(a));
    }
  }

  /**
   * @return the entity for an external id (foo/bar/foo) using
   *         {@link IComposerField#createExternalIdPartForEntity(IComposerEntity)} and
   *         {@link IComposerField#createExternalIdPartForAttribute(IComposerAttribute)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   */
  public static IComposerEntity externalIdToEntity(IComposerField f, String externalId, IComposerEntity parentEntity) {
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
      return findEntity(parentEntity.getComposerEntities(), elemName);
    }
    else {
      return findEntity(f.getComposerEntities(), elemName);
    }
  }

  /**
   * @return the attribute for an external id (foo/bar/foo) using
   *         {@link IComposerField#createExternalIdPartForEntity(IComposerEntity)} and
   *         {@link IComposerField#createExternalIdPartForAttribute(IComposerAttribute)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   */
  public static IComposerAttribute externalIdToAttribute(IComposerField f, String externalId, IComposerEntity parentEntity) {
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
      return findAttribute(f, parentEntity.getComposerAttributes(), elemName, meta);
    }
    else {
      return findAttribute(f, f.getComposerAttributes(), elemName, meta);
    }
  }

  /**
   * @return the entity for an external id part (no '/' characters) using
   *         {@link IComposerField#createExternalIdPartForEntity(IComposerEntity)}
   */
  public static IComposerEntity findEntity(IComposerEntity[] array, String simpleName) {
    if (array != null) {
      for (IComposerEntity e : array) {
        if (e.getClass().getSimpleName().equals(simpleName)) {
          return e;
        }
      }
    }
    return null;
  }

  /**
   * @return the attribute for an external id part (no '/' characters) using
   *         {@link IComposerField#createExternalIdPartForAttribute(IComposerAttribute)}
   */
  public static IComposerAttribute findAttribute(IComposerField f, IComposerAttribute[] array, String simpleName, Map<String, String> metaData) {
    IComposerAttribute secondaryMatch = null;
    if (array != null) {
      for (IComposerAttribute a : array) {
        if (a.getClass().getSimpleName().equals(simpleName)) {
          secondaryMatch = a;
          if (CompareUtility.equals(f.getMetaDataOfAttribute(a), metaData)) {
            return a;
          }
        }
      }
    }
    return secondaryMatch;
  }

  /**
   * @return the external id (foo/bar/foo) for an entity data using
   *         {@link IComposerField#createExternalIdPartForEntityData(AbstractComposerEntityData)} and
   *         {@link IComposerField#createExternalIdPartForAttributeData(AbstractComposerAttributeData)}
   */
  public static String entityDataToExternalId(AbstractComposerEntityData e) {
    if (e.getParentEntity() != null) {
      return entityDataToExternalId(e.getParentEntity()) + "/" + e.getClass().getSimpleName();
    }
    else {
      return e.getClass().getSimpleName();
    }
  }

  /**
   * @return the external id (foo/bar/foo) for an attribute data using
   *         {@link IComposerField#createExternalIdPartForEntityData(AbstractComposerEntityData)} and
   *         {@link IComposerField#createExternalIdPartForAttributeData(AbstractComposerAttributeData)}
   * @param values
   *          of the node containing the attribute; this may contain some meta data relevant for dynamic attributes
   */
  public static String attributeDataToExternalId(AbstractComposerAttributeData a, Map<String, String> metaData) {
    if (a.getParentEntity() != null) {
      return entityDataToExternalId(a.getParentEntity()) + "/" + a.getClass().getSimpleName() + exportMetaData(metaData);
    }
    else {
      return a.getClass().getSimpleName() + exportMetaData(metaData);
    }
  }

  /**
   * @return the entity data for an external id (foo/bar/foo) using
   *         {@link IComposerField#createExternalIdPartForEntityData(AbstractComposerEntityData)} and
   *         {@link IComposerField#createExternalIdPartForAttributeData(AbstractComposerAttributeData)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   */
  public static AbstractComposerEntityData externalIdToEntityData(AbstractComposerData formData, String externalId, AbstractComposerEntityData parentEntity) {
    if (externalId == null) return null;
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) throw new IllegalArgumentException("externalId is invalid: " + externalId);
    String folderName = m.group(2);
    String elemName = m.group(3);
    if (folderName != null) {
      parentEntity = externalIdToEntityData(formData, folderName, parentEntity);
      if (parentEntity == null) {
        return null;
      }
    }
    if (parentEntity != null) {
      return findEntityData(parentEntity.getEntities(), elemName);
    }
    else {
      return findEntityData(formData.getEntities(), elemName);
    }
  }

  /**
   * @return the attribute data for an external id (foo/bar/foo) using
   *         {@link IComposerField#createExternalIdPartForEntityData(AbstractComposerEntityData)} and
   *         {@link IComposerField#createExternalIdPartForAttributeData(AbstractComposerAttributeData)}
   * @param parentEntity
   *          is the entity on which to start resolving or null to start on top of the entity/attribute tree
   * @param values
   *          of the node containing the attribute; this may contain some meta data relevant for dynamic attributes
   */
  public static AbstractComposerAttributeData externalIdToAttributeData(AbstractComposerData formData, String externalId, AbstractComposerEntityData parentEntity) {
    if (externalId == null) return null;
    Matcher m = PAT_EXTERNAL_ID.matcher(externalId);
    if (!m.matches()) throw new IllegalArgumentException("externalId is invalid: " + externalId);
    String folderName = m.group(2);
    String elemName = m.group(3);
    Map<String, String> meta = importMetaData(m.group(5));
    if (folderName != null) {
      parentEntity = externalIdToEntityData(formData, folderName, parentEntity);
      if (parentEntity == null) {
        return null;
      }
    }
    if (parentEntity != null) {
      return findAttributeData(parentEntity.getAttributes(), elemName, meta);
    }
    else {
      return findAttributeData(formData.getAttributes(), elemName, meta);
    }
  }

  /**
   * @return the entity data for an external id part (no '/' characters) using
   *         {@link IComposerField#createExternalIdPartForEntityData(AbstractComposerEntityData)}
   */
  public static AbstractComposerEntityData findEntityData(AbstractComposerEntityData[] array, String simpleName) {
    if (array != null) {
      for (AbstractComposerEntityData e : array) {
        if (e.getClass().getSimpleName().equals(simpleName)) {
          return e;
        }
      }
    }
    return null;
  }

  /**
   * @return the attribute data for an external id part (no '/' characters) using
   *         {@link IComposerField#createExternalIdPartForAttributeData(AbstractComposerAttributeData)}
   * @param values
   *          of the node containing the attribute; this may contain some meta data relevant for dynamic attributes
   */
  public static AbstractComposerAttributeData findAttributeData(AbstractComposerAttributeData[] array, String simpleName, Map<String, String> metaData) {
    if (array != null) {
      for (AbstractComposerAttributeData a : array) {
        if (a.getClass().getSimpleName().equals(simpleName)) {
          return a;
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
