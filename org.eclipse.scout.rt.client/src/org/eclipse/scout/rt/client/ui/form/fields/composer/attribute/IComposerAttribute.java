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
package org.eclipse.scout.rt.client.ui.form.fields.composer.attribute;

import java.security.Permission;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.entity.IComposerEntity;
import org.eclipse.scout.rt.client.ui.form.fields.composer.operator.IComposerOp;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Composer search attribute
 */
public interface IComposerAttribute extends IPropertyObserver {

  /**
   * internal type for operators that inherit type of attribute (default)
   */
  int TYPE_INHERITED = -1;

  /**
   * type for operators that need no value, such as IS NULL or IS NOT NULL
   */
  int TYPE_NONE = 0;

  int TYPE_CODE_LIST = 1;
  int TYPE_CODE_TREE = 2;
  int TYPE_NUMBER_LIST = 3;
  int TYPE_NUMBER_TREE = 4;
  int TYPE_DATE = 5;
  int TYPE_TIME = 6;
  int TYPE_DATE_TIME = 7;
  int TYPE_INTEGER = 8;
  int TYPE_LONG = 9;
  int TYPE_DOUBLE = 10;
  int TYPE_PLAIN_INTEGER = 11;
  int TYPE_PLAIN_LONG = 12;
  int TYPE_PLAIN_DOUBLE = 13;
  int TYPE_PERCENT = 14;
  int TYPE_STRING = 15;
  int TYPE_SMART = 16;
  /**
   * Attribute used to create a count(Entity) on the enclosing entity.
   */
  int TYPE_AGGREGATE_COUNT = 17;
  /**
   * Attribute used for full text searches
   */
  int TYPE_FULL_TEXT = 18;

  void initAttribute() throws ProcessingException;

  /**
   * For {@link #TYPE_CODE_LIST}, {@link #TYPE_CODE_TREE}, {@link #TYPE_NUMBER_LIST}, {@link #TYPE_NUMBER_TREE} and
   * {@link #TYPE_SMART} only. Delegate of the callback {@link AbstractListBox#execPrepareLookup(LookupCall)} and
   * {@link AbstractTreeBox#execPrepareLookup(LookupCall, org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode)}
   */
  void prepareLookup(LookupCall call) throws ProcessingException;

  /**
   * @deprecated the id must always by the class simple name
   *             for dynamic attributes use {@link IComposerField#getMetaDataOfAttribute(IComposerAttribute)}
   */
  @Deprecated
  String getId();

  /**
   * @deprecated the id must always by the class simple name
   *             for dynamic attributes use {@link IComposerField#getMetaDataOfAttribute(IComposerAttribute)}
   */
  @Deprecated
  void setId(String s);

  String getText();

  void setText(String s);

  /**
   * @return the type of field to display to select a value for this attribute
   *         see the TYPE_* values
   */
  int getType();

  void setType(int type);

  IComposerOp[] getOperators();

  void setOperators(IComposerOp[] ops);

  /**
   * @return array of {@link ComposerConstants#AGGREGATION_*}
   */
  int[] getAggregationTypes();

  /**
   * @param aggregationTypes
   *          array of {@link ComposerConstants#AGGREGATION_*}
   */
  void setAggregationTypes(int[] aggregationTypes);

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

  IComposerEntity getParentEntity();

  void setParentEntity(IComposerEntity parent);
}
