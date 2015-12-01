/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.model.fixture;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

public final class CustomDataModelExtension {

  public static boolean ENABLED = true;

  private CustomDataModelExtension() {
  }

  /**
   * Inject top level attributes into the provided collection
   *
   * @param entityInstance
   * @param attributes
   *          Attribute collection to inject attributes to
   */
  public static void injectAttributes(IDataModelEntity entityInstance, OrderedCollection<IDataModelAttribute> attributes) {
    if (!ENABLED) {
      return;
    }
    injectMetadataAttributes(new long[]{1, 2, 3}, attributes);
  }

  /**
   * Inject top level entities into the provided collection
   *
   * @param entityInstance
   * @param entities
   *          Entity collection to inject entities to
   */
  public static void injectEntities(IDataModelEntity entityInstance, OrderedCollection<IDataModelEntity> entities) {
    if (!ENABLED) {
      return;
    }
    injectMetadataEntities(new long[]{1, 2, 3}, entities);
  }

  /**
   * Inject top level attributes into the provided collection
   *
   * @param entityInstance
   * @param attributes
   *          Attribute collection to inject attributes to
   */
  private static void injectMetadataAttributes(long[] idList, OrderedCollection<IDataModelAttribute> attributes) {
    for (long id : idList) {
      ExtendedFieldAttribute a = new ExtendedFieldAttribute(id, "Field " + id, DataModelConstants.TYPE_LONG);
      attributes.addLast(a);
    }
  }

  private static void injectMetadataEntities(long[] idList, OrderedCollection<IDataModelEntity> entities) {
    for (long id : idList) {
      ExtendedFolderEntity e = new ExtendedFolderEntity(id, "Folder " + id);
      entities.addLast(e);
    }
  }

  public static class ExtendedFolderEntity extends AbstractDataModelEntity {
    private static final long serialVersionUID = 1L;

    private final Long m_id;

    public ExtendedFolderEntity(Long id, String text) {
      super(false);
      m_id = id;
      setText(text);
      //
      callInitializer();
    }

    /**
     * complete override that returns {@link #getText()}
     */
    @Override
    protected String getConfiguredText() {
      return getText();
    }

    @Override
    protected boolean getConfiguredOneToMany() {
      return false;
    }

    @Override
    protected void injectAttributesInternal(OrderedCollection<IDataModelAttribute> attributes) {
      injectMetadataAttributes(new long[]{11, 12, 13}, attributes);
    }

    public Long getId() {
      return m_id;
    }

    /**
     * Specialization for dynamic type
     */
    @Override
    public Map<String, String> getMetaDataOfEntity() {
      HashMap<String, String> map = new HashMap<String, String>(1);
      map.put("id", "" + getId());
      return map;
    }
  }

  public static class ExtendedFieldAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    private final Long m_id;

    public ExtendedFieldAttribute(Long id, String text, int type) {
      super(false);
      m_id = id;
      setText(text);
      setType(type);
      //
      callInitializer();
    }

    /**
     * complete override that returns {@link #getText()}
     */
    @Override
    protected String getConfiguredText() {
      return getText();
    }

    /**
     * complete override that returns {@link #getType()}
     */
    @Override
    protected int getConfiguredType() {
      return super.getType();
    }

    /**
     * Attributes are defined in an entity below the base entity (e.g. Company) but they belong to the base entity as
     * other attributes. Therefore they can not be aggregated.
     */
    @Override
    protected boolean getConfiguredAggregationEnabled() {
      return false;
    }

    public Long getId() {
      return m_id;
    }

    /**
     * Specialization for dynamic type
     */
    @Override
    public Map<String, String> getMetaDataOfAttribute() {
      HashMap<String, String> map = new HashMap<String, String>(1);
      map.put("id", "" + getId());
      return map;
    }
  }
}
