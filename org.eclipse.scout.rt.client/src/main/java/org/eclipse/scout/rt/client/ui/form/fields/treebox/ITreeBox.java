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
package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * A treebox represents a list of values which correspond to checked keys in the tree. When setting
 * loadIncremental=false just one LookupCall by <b>all</b> is sent to load all tree nodes. It is expected that the
 * returned {@link ILookupRow}s contains their corresponding parentKey or null for root nodes. When setting
 * loadIncremental=true a {@link ILookupCall} by <b>rec</b> is sent whenever a node's children are loaded and contains
 * the parentKey.
 * <p>
 * The listbox value is a List<T> where the {@link List<T>} is the set of checked keys of the listbox<br>
 * the inner table shows those rows as checked which have the key value as a part of the listbox value (List<T>)
 * <p>
 * Note, that the listbox might not necessarily show all checked rows since the value of the listbox might contain
 * inactive keys that are not reflected in the listbox<br>
 * Therefore an empty listbox table is not the same as a listbox with an empty value (null)
 */
public interface ITreeBox<T> extends IValueField<Set<T>>, ICompositeField {

  /**
   * {@link boolean}
   */
  String PROP_FILTER_ACTIVE_NODES = "filterActiveNodes";
  /**
   * {@link boolean}
   */
  String PROP_FILTER_CHECKED_NODES = "filterCheckedNodes";
  /**
   * {@link boolean}
   */
  String PROP_FILTER_CHECKED_NODES_VALUE = "filterCheckedNodesValue";
  /**
   * {@link TriState}
   */
  String PROP_FILTER_ACTIVE_NODES_VALUE = "filterActiveNodesValue";

  ITree getTree();

  /**
   * true: a filter is added to the treebox tree that only accepts nodes that are active or checked. Affects
   * {@link ITreeNode#getFilteredChildNodes()}
   */
  boolean isFilterActiveNodes();

  /**
   * see {@link #isFilterActiveNodes()}
   */
  void setFilterActiveNodes(boolean b);

  TriState getFilterActiveNodesValue();

  void setFilterActiveNodesValue(TriState t);

  /**
   * true: a filter is added to the treebox tree that only accepts checked nodes Affects
   * {@link ITreeNode#getFilteredChildNodes()}
   */
  boolean isFilterCheckedNodes();

  /**
   * see {@link #isFilterCheckedRows()}
   */
  void setFilterCheckedNodes(boolean b);

  boolean getFilterCheckedNodesValue();

  void setFilterCheckedNodesValue(boolean b);

  /**
   * Populate tree with data from service all existing data in the tree is discarded
   *
   * @see execFilterTreeNode
   */
  void loadRootNode();

  void loadChildNodes(ITreeNode parentNode);

  List<ITreeNode> callChildLookup(ITreeNode parentNode);

  List<ITreeNode> callCompleteTreeLookup();

  ILookupCall<T> getLookupCall();

  void setLookupCall(ILookupCall<T> call);

  Class<? extends ICodeType<?, T>> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType<?, T>> codeTypeClass);

  boolean isAutoExpandAll();

  void setAutoExpandAll(boolean b);

  boolean isAutoCheckChildNodes();

  void setAutoCheckChildNodes(boolean b);

  boolean isLoadIncremental();

  void setLoadIncremental(boolean b);

  boolean isNodeActive(ITreeNode node);

  @Override
  Set<T> getValue();

  @Override
  Set<T> getInitValue();

  /**
   * @return the first selected/checked value if any
   *         <p>
   *         By default a treebox is checkable, so its value is the array of all checked keys
   *         <p>
   *         When it is made non-checkable, its value is the array of all selected keys
   */
  T getSingleValue();

  /**
   * Convenience for setting a single value with {@link #setValue(Object)}
   */
  void setSingleValue(T value);

  int getCheckedKeyCount();

  T getCheckedKey();

  Set<T> getCheckedKeys();

  void checkKey(T key);

  void checkKeys(Set<T> keys);

  void uncheckAllKeys();

  Set<T> getUncheckedKeys();

  void checkAllKeys();

}
