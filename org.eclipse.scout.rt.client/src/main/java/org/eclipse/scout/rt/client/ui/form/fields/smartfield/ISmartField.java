/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ColumnDescriptor;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

/**
 * Generic type V: value of the SmartField, which is also the key used in lookup-rows.
 */
public interface ISmartField<VALUE> extends IValueField<VALUE> {
  int NOT_UNIQUE_ERROR_CODE = 1;
  int NO_RESULTS_ERROR_CODE = 2;

  String PROP_DISPLAY_STYLE = "displayStyle";
  String PROP_RESULT = "result";
  String PROP_ACTIVE_FILTER_ENABLED = "activeFilterEnabled";
  String PROP_ACTIVE_FILTER = "activeFilter";
  String PROP_ACTIVE_FILTER_LABELS = "activeFilterLabels";
  String PROP_SEARCH_REQUIRED = "searchRequired";
  String PROP_STATUS = "status";
  String PROP_STATUS_VISIBLE = "statusVisible";
  String PROP_INIT_ACTIVE_FILTER = "initActiveFilter";

  /**
   * Hint to mark the {@link IFuture} used to load the field's initial lookup rows. Typically, this future must not be
   * cancelled.
   */
  String EXECUTION_HINT_INITIAL_LOOKUP = "initialLookup";

  String PROP_BROWSE_HIERARCHY = "browseHierarchy";
  String PROP_BROWSE_LOAD_INCREMENTAL = "browseLoadIncremental";
  String PROP_BROWSE_LOAD_PARENT_NODES = "browseLoadParentNodes";
  String PROP_BROWSE_AUTO_EXPAND_ALL = "browseAutoExpandAll";
  String PROP_MULTILINE_TEXT = "multilineText";
  String PROP_BROWSE_MAX_ROW_COUNT = "browseMaxRowCount";
  String PROP_COLUMN_DESCRIPTORS = "columnDescriptors";
  String PROP_LOOKUP_ROW = "lookupRow";
  String PROP_LOAD_PARENT_NODES = "loadParentNodes";
  String PROP_MAX_LENGTH = "maxLength";

  String DISPLAY_STYLE_DEFAULT = "default";
  String DISPLAY_STYLE_DROPDOWN = "dropdown";

  void lookupByAll();

  void lookupByText(String searchText);

  void lookupByRec(VALUE parentKey);

  void lookupByKey(VALUE key);

  ILookupCallResult getResult();

  /**
   * true: inactive rows are display and can be also be parsed using the UI facade according to
   * {@link #getActiveFilter()} false: inactive rows are only display when the smart field valid is set by the model.
   * The UI facade cannot choose such a value.
   */
  boolean isActiveFilterEnabled();

  /**
   * see {@link #isActiveFilterEnabled()}
   */
  void setActiveFilterEnabled(boolean enabled);

  /**
   * Changes the default-label text for the active-filter radio-button with the given state.
   */
  void setActiveFilterLabel(TriState activeFilter, String label);

  /**
   * Returns the label-texts of the active-filter radio-button in this order:
   * <ol>
   * <li>UNDEFINED</li>
   * <li>FALSE</li>
   * <li>TRUE</li>
   * </ol>
   */
  String[] getActiveFilterLabels();

  /**
   * This has only an effect if {@link #isActiveFilterEnabled()} is set to true. true: include only active values false:
   * include only inactive values undefined: include active and inactive values
   */
  TriState getActiveFilter();

  /**
   * see {@link #getActiveFilter()}
   */
  void setActiveFilter(TriState activeFilter);

  void setInitActiveFilter(TriState initActiveFilter);

  TriState getInitActiveFilter();

  int getBrowseMaxRowCount();

  void setBrowseMaxRowCount(int browseMaxRowCount);

  /**
   * @since 5.1
   */
  void setMultilineText(boolean multilineText);

  /**
   * @since 5.1
   */
  boolean isMultilineText();

  boolean isBrowseAutoExpandAll();

  void setBrowseAutoExpandAll(boolean browseAutoExpandAll);

  boolean isBrowseHierarchy();

  void setBrowseHierarchy(boolean browseHierarchy);

  boolean isBrowseLoadIncremental();

  void setBrowseLoadIncremental(boolean browseLoadIncremental);

  boolean isLoadParentNodes();

  void setLoadParentNodes(boolean loadParentNodes);

  /**
   * @param maxLength
   *     of the text in this field. Negative values are automatically converted to 0.
   */
  void setMaxLength(int maxLength);

  /**
   * @return the maximum length of text
   */
  int getMaxLength();

  /**
   * Filter selection of hierarchy browse tree. The level reported here is different than the one used in {@link AbstractTree}
   * such as this level is one smaller. This is because a tree smart field assumes its tree to have multiple roots, but the ITree
   * model is built as single-root tree with invisible root node. level=-1 is the invisible (anonymous) root level=0 are
   * the multiple roots of the smart tree ...
   */
  boolean acceptBrowseHierarchySelection(VALUE value, int level, boolean leaf);

  /**
   * variant A
   */
  Class<? extends ICodeType<?, VALUE>> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType<?, VALUE>> codeType);

  /**
   * variant B
   */
  ILookupCall<VALUE> getLookupCall();

  void setLookupCall(ILookupCall<VALUE> call);

  void prepareKeyLookup(ILookupCall<VALUE> call, VALUE key);

  void prepareTextLookup(ILookupCall<VALUE> call, String text);

  void prepareBrowseLookup(ILookupCall<VALUE> call, TriState activeState);

  void prepareRecLookup(ILookupCall<VALUE> call, VALUE parentKey, TriState activeState);

  ISmartFieldUIFacade<VALUE> getUIFacade();

  void setWildcard(String wildcard);

  String getWildcard();

  // search and update the field with the result

  void doSearch(IQueryParam<VALUE> param, boolean synchronous);

  // blocking lookups

  /**
   * Lookup rows by key using {@link ILookupCall#getDataByKey()}. Blocks until the result is available.
   *
   * @param key
   *     lookup key
   * @return rows not <code>null</code>
   */
  List<? extends ILookupRow<VALUE>> callKeyLookup(VALUE key);

  /**
   * Lookup rows by text {@link ILookupCall#getDataByText()}. Blocks until the result is available.
   *
   * @param text
   *     search text
   * @return rows not <code>null</code>
   */
  List<? extends ILookupRow<VALUE>> callTextLookup(String text, int maxRowCount);

  /**
   * Lookup all rows using {@link ILookupCall#getDataByAll()}. Blocks until the result is available.
   *
   * @return rows not <code>null</code>
   */
  List<? extends ILookupRow<VALUE>> callBrowseLookup(int maxRowCount);

  /**
   * Lookup all rows using {@link ILookupCall#getDataByAll()}. Blocks until the result is available.
   *
   * @return rows not <code>null</code>
   */
  List<? extends ILookupRow<VALUE>> callBrowseLookup(int maxRowCount, TriState activeState);

  /**
   * Lookup rows of a parent key using {@link ILookupCall#getDataByRec()}. Blocks until the result is available.
   *
   * @return rows not <code>null</code>
   */
  List<ILookupRow<VALUE>> callSubTreeLookup(VALUE parentKey);

  /**
   * Lookup rows of a parent key using {@link ILookupCall#getDataByRec()}. Blocks until the result is available.
   *
   * @return rows not <code>null</code>
   */
  List<ILookupRow<VALUE>> callSubTreeLookup(VALUE parentKey, TriState activeState);

  // non-blocking lookups

  IFuture<List<ILookupRow<VALUE>>> callKeyLookupInBackground(final VALUE key, boolean cancelRunningJobs);

  /**
   * Lookup rows asynchronously by text {@link ILookupCall#getDataByText()}.
   *
   * @param cancelRunningJobs
   *     if <code>true</code> it automatically cancels already running lookup jobs of this field, before starting
   *     the new lookup job.
   * @return {@link IFuture} to cancel data fetching.
   */
  IFuture<List<ILookupRow<VALUE>>> callTextLookupInBackground(String text, boolean cancelRunningJobs);

  /**
   * Lookup rows asynchronously by all {@link ILookupCall#getDataByAll()}. Automatically cancels already running lookup
   * jobs of this field, before starting the lookup job.
   *
   * @param cancelRunningJobs
   *     if <code>true</code> it automatically cancels already running lookup jobs of this field, before starting
   *     the new lookup job.
   * @return {@link IFuture} to cancel data fetching
   */
  IFuture<List<ILookupRow<VALUE>>> callBrowseLookupInBackground(boolean cancelRunningJobs);

  /**
   * Lookup child rows of a given parent key asynchronously using {@link ILookupCall#getDataByRec()}.
   *
   * @param cancelRunningJobs
   *     if <code>true</code> it automatically cancels already running lookup jobs of this field, before starting
   *     the new lookup job.
   * @return {@link IFuture} to cancel data fetching
   */
  IFuture<List<ILookupRow<VALUE>>> callSubTreeLookupInBackground(final VALUE parentKey, boolean cancelRunningJobs);

  /**
   * Lookup child rows of a given parent key asynchronously using {@link ILookupCall#getDataByRec()}.
   *
   * @param cancelRunningJobs
   *     if <code>true</code> it automatically cancels already running lookup jobs of this field, before starting
   *     the new lookup job.
   * @return {@link IFuture} to cancel data fetching
   */
  IFuture<List<ILookupRow<VALUE>>> callSubTreeLookupInBackground(final VALUE parentKey, final TriState activeState, boolean cancelRunningJobs);

  // non-blocking lookups using callbacks (legacy)

  /**
   * Loads lookup rows asynchronously, and notifies the specified callback upon loading completed.
   * <p>
   * The methods of {@link ILookupRowFetchedCallback} are invoked in the model thread.
   *
   * @return {@link IFuture} to cancel data fetching
   */
  IFuture<Void> callKeyLookupInBackground(VALUE key, ILookupRowFetchedCallback<VALUE> callback);

  /**
   * Loads lookup rows asynchronously, and notifies the specified callback upon loading completed.
   * <p>
   * The methods of {@link ILookupRowFetchedCallback} are invoked in the model thread.
   *
   * @return {@link IFuture} to cancel data fetching.
   */
  IFuture<Void> callTextLookupInBackground(String text, int maxRowCount, ILookupRowFetchedCallback<VALUE> callback);

  /**
   * Loads lookup rows asynchronously, and notifies the specified callback upon loading completed.
   * <p>
   * The methods of {@link ILookupRowFetchedCallback} are invoked in the model thread.
   *
   * @return {@link IFuture} to cancel data fetching
   */
  IFuture<Void> callBrowseLookupInBackground(int maxRowCount, ILookupRowFetchedCallback<VALUE> callback);

  /**
   * Loads lookup rows asynchronously, and notifies the specified callback upon loading completed.
   * <p>
   * The methods of {@link ILookupRowFetchedCallback} are invoked in the model thread.
   *
   * @return {@link IFuture} to cancel data fetching
   */
  IFuture<Void> callBrowseLookupInBackground(int maxRowCount, TriState activeState, ILookupRowFetchedCallback<VALUE> callback);

  ColumnDescriptor[] getColumnDescriptors();

  void setColumnDescriptors(ColumnDescriptor[] columnDescriptors);

  /**
   * Reloads and sets the display text for the current value by performing a key lookup.
   * <p>
   * This is useful when the data used to compute the display text changes but the key remains the same.
   */
  @Override
  void refreshDisplayText();

  String getDisplayStyle();

  void setDisplayStyle(String displayStyle);

  ILookupRow<VALUE> getLookupRow();

  void setLookupRow(ILookupRow<VALUE> lookupRow);

  /**
   * Sets the value by using the key of the given lookup row. The property <code>lookupRow</code> will be set too.
   *
   * @param lookupRow
   *     Lookup row to resolve the value to be set
   */
  void setValueByLookupRow(ILookupRow<VALUE> lookupRow);

  /**
   * @return {@code true} if the smartfield should only show a proposal list if a search text is entered by the user.
   * {@code false} (which is the default) if all proposals should be shown if no text search is done.
   */
  boolean isSearchRequired();

  /**
   * Configures whether this smartfield only shows proposals if a text search string has been entered.<br>
   * Set this property to {@code true} if you expect a large amount of data for an unconstrained search.
   *
   * @param searchRequired
   *     {@code true} if the smartfield should only show a proposal list if a search text is entered by the user.
   *     {@code false} (which is the default) if all proposals should be shown if no text search is done.
   */
  void setSearchRequired(boolean searchRequired);
}
