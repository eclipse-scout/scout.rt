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

import java.security.Permission;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

public interface IDataModelEntity extends IPropertyObserver, IOrdered, ITypeWithClassId {

  String PROP_VISIBLE = "visible";

  /**
   * @return meta data for the attribute, default returns null
   *         <p>
   *         This method is useful and should be overridden when dynamic attributes are used, where multiple attributes
   *         of the same type (Class) occur in the same {@link IDataModel}. This meta map contains the distinguishing
   *         map of these multiple instances.
   *         <p>
   *         If the map is not filled or null, the comparison is only based on the type ({@link #getClass()})
   *         <p>
   *         see {@link DataModelUtility}
   */
  Map<String, String> getMetaDataOfEntity();

  /**
   * Initialize this entity.
   */
  void initEntity();

  String getIconId();

  void setIconId(String s);

  String getText();

  void setText(String s);

  List<IDataModelAttribute> getAttributes();

  IDataModelAttribute getAttribute(Class<? extends IDataModelAttribute> attributeClazz);

  List<IDataModelEntity> getEntities();

  IDataModelEntity getEntity(Class<? extends IDataModelEntity> entityClazz);

  IDataModelEntity getParentEntity();

  /**
   * In order to avoid loop cycles, this initializer is called by the composer field to load the child entity graph,
   * that may have cycles
   *
   * @param instanceMap
   *          map containing all previously created entities, there should be only one entity per type
   */
  void initializeChildEntities(Map<Class<? extends IDataModelEntity>, IDataModelEntity> instanceMap);

  Permission getVisiblePermission();

  void setVisiblePermission(Permission p);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  boolean isVisible();

  void setVisible(boolean b);

  /**
   * Controls the multiplicity of this entity in the context of its parent entity. The result is <code>true</code>, if
   * this entity can occur multiple times within its parent's context. Default is <code>true</code>.
   * <p/>
   * <b>Example:</b>A person has a mother, a father and an arbitrary number of friends. Let's assume the
   * <em>PersonEntity</em> contains a <em>PersonMotherEntity</em>, a <em>PersonFatherEntity</em> and a
   * <em>PersonFriendEntity</em>. Both, the mother and father entities' {@link IDataModelEntity#isOneToMany()} return
   * <code>false</code>. However the one of the friend entity return <code>true</code>.
   *
   * @return Returns <code>true</code> (default) if this entity can occur multiple times in the context of its parent
   *         entity. Otherwise the entity is considered many-to-one, i.e. this entity can occur at most one time within
   *         the context of its parent entity.
   * @since 3.8.0
   */
  boolean isOneToMany();

  /**
   * Controls the multiplicity of this entity in the context of its parent entity.
   *
   * @param b
   *          <code>true</code> if this entity is referenced at most one time by its parent entity.
   * @see #isOneToMany()
   * @since 3.8.0
   */
  void setOneToMany(boolean b);
}
