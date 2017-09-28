/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public interface IDataModelAttribute extends IPropertyObserver, DataModelConstants, IOrdered, IVisibleDimension, ITypeWithClassId {

  void initAttribute();

  /**
   * For {@link #TYPE_CODE_LIST}, {@link #TYPE_CODE_TREE}, {@link #TYPE_NUMBER_LIST}, {@link #TYPE_NUMBER_TREE} and
   * {@link #TYPE_SMART} only. Delegate of the callback {@link AbstractListBox#execPrepareLookup(LookupCall)} and
   * {@link AbstractTreeBox#execPrepareLookup(LookupCall, org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode)}
   */
  void prepareLookup(ILookupCall<?> call);

  String getText();

  void setText(String s);

  /**
   * @return the type of field to display to select a value for this attribute see the TYPE_* values
   */
  int getType();

  void setType(int type);

  List<IDataModelAttributeOp> getOperators();

  void setOperators(List<? extends IDataModelAttributeOp> ops);

  /**
   * @return array of {@link DataModelConstants#AGGREGATION_*}
   */
  int[] getAggregationTypes();

  /**
   * @param aggregationTypes
   *          array of {@link DataModelConstants#AGGREGATION_*}
   */
  void setAggregationTypes(int[] aggregationTypes);

  boolean containsAggregationType(int agType);

  String getIconId();

  void setIconId(String s);

  boolean isNullOperatorEnabled();

  void setNullOperatorEnabled(boolean b);

  boolean isNotOperatorEnabled();

  void setNotOperatorEnabled(boolean b);

  boolean isAggregationEnabled();

  void setAggregationEnabled(boolean aggregationEnabled);

  Class<? extends ICodeType<?, ?>> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType<?, ?>> codeTypeClass);

  ILookupCall<Object> getLookupCall();

  void setLookupCall(ILookupCall<?> call);

  Permission getVisiblePermission();

  void setVisiblePermission(Permission p);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  boolean isVisible();

  void setVisible(boolean b);

  boolean isActiveFilterEnabled();

  void setActiveFilterEnabled(boolean active);

  IDataModelEntity getParentEntity();

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
  Map<String, String> getMetaDataOfAttribute();

  /**
   * Describes whether this attribute holds a multi-value content. The default implementation derives the result from
   * {@link #getType()}. The following types are considered multi-valued:
   * <ul>
   * <li>{@link DataModelConstants#TYPE_CODE_LIST}</li>
   * <li>{@link DataModelConstants#TYPE_CODE_TREE}</li>
   * <li>{@link DataModelConstants#TYPE_NUMBER_LIST}</li>
   * <li>{@link DataModelConstants#TYPE_NUMBER_TREE}</li>
   * </ul>
   * <p>
   * A multi valued attribute behaves same as {@link IDataModelEntity#isOneToMany()}
   *
   * @return Returns <code>true</code> if this attribute holds multiple values. <code>false</code> otherwise.
   * @since 3.8.0
   */
  boolean isMultiValued();

  /**
   * Formats the provided raw value according to the defined attribute type.
   *
   * @param rawValue
   *          Raw value to format.
   * @return Formatted value
   */
  String formatValue(Object rawValue);
}
