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

import java.security.Permission;
import java.util.Map;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public interface IDataModelAttribute extends IPropertyObserver, DataModelConstants {

  void initAttribute() throws ProcessingException;

  /**
   * For {@link #TYPE_CODE_LIST}, {@link #TYPE_CODE_TREE}, {@link #TYPE_NUMBER_LIST}, {@link #TYPE_NUMBER_TREE} and
   * {@link #TYPE_SMART} only. Delegate of the callback {@link AbstractListBox#execPrepareLookup(LookupCall)} and
   * {@link AbstractTreeBox#execPrepareLookup(LookupCall, org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode)}
   */
  void prepareLookup(LookupCall call) throws ProcessingException;

  String getText();

  void setText(String s);

  /**
   * @return the type of field to display to select a value for this attribute
   *         see the TYPE_* values
   */
  int getType();

  void setType(int type);

  IDataModelAttributeOp[] getOperators();

  void setOperators(IDataModelAttributeOp[] ops);

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

  Class<? extends ICodeType> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType> codeTypeClass);

  LookupCall getLookupCall();

  void setLookupCall(LookupCall call);

  Permission getVisiblePermission();

  void setVisiblePermission(Permission p);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  boolean isVisible();

  void setVisible(boolean b);

  IDataModelEntity getParentEntity();

  void setParentEntity(IDataModelEntity parent);

  /**
   * @return meta data for the attribute, default returns null
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
   * 
   * @return Returns <code>true</code> if this attribute holds multiple values. <code>false</code> otherwise.
   * @since 3.8.0
   */
  boolean isMultiValued();
}
