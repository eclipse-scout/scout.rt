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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * @param <VALUE_TYPE>
 *          type of the value
 * @param <KEY_TYPE>
 *          type of the key of the lookup call
 */
public interface IContentAssistField<VALUE_TYPE, KEY_TYPE> extends IValueField<VALUE_TYPE> {

  /**
   * {@link IContentAssistFieldProposalForm}
   */
  String PROP_PROPOSAL_FORM = "proposalForm";
  /**
   * {@link String}
   */
  String PROP_BROWSE_ICON_ID = "browseIconId";
  /**
   * {@link String}
   */
  String PROP_ICON_ID = "iconId";

  String BROWSE_ALL_TEXT = "*";

  void addSmartFieldListener(ContentAssistFieldListener listener);

  void removeSmartFieldListener(ContentAssistFieldListener listener);

  IContentAssistFieldProposalForm<KEY_TYPE> getProposalForm();

  IContentAssistFieldProposalFormProvider<KEY_TYPE> getProposalFormProvider();

  void setProposalFormProvider(IContentAssistFieldProposalFormProvider<KEY_TYPE> provider);

  /**
   * true: inactive rows are display and can be also be parsed using the UI
   * facade according to {@link #getActiveFilter()} false: inactive rows are
   * only display when the smart field valud is set by the model. The UI facade
   * cannot choose such a value.
   */
  boolean isActiveFilterEnabled();

  /**
   * see {@link #isActiveFilterEnabled()}
   */
  void setActiveFilterEnabled(boolean b);

  /**
   * This has only an effect if {@link #isActiveFilterEnabled()} is set to true.
   * true: include only active values false: include only inactive values
   * undefined: include active and inactive values
   */
  TriState getActiveFilter();

  /**
   * see {@link #getActiveFilter()}
   */
  void setActiveFilter(TriState t);

  String getBrowseIconId();

  void setBrowseIconId(String s);

  int getBrowseMaxRowCount();

  void setBrowseMaxRowCount(int n);

  String getIconId();

  void setIconId(String s);

  /**
   * For performance optimization, style loading is done lazily.
   * However, sometimes it is useful to apply these changes immediately.
   * <p>
   * This method is called automatically by {@link #getDisplayText()}, {@link #getTooltipText()},
   * {@link #getBackgroundColor()}, {@link #getForegroundColor()} and {@link #getFont()}
   */
  void applyLazyStyles();

  boolean isBrowseAutoExpandAll();

  void setBrowseAutoExpandAll(boolean b);

  boolean isBrowseHierarchy();

  void setBrowseHierarchy(boolean b);

  boolean isBrowseLoadIncremental();

  void setBrowseLoadIncremental(boolean b);

  /**
   * see {@link AbstractSmartField#execBrowseNew(String)}
   */
  String getBrowseNewText();

  /**
   * see {@link AbstractSmartField#execBrowseNew(String)}
   */
  void setBrowseNewText(String s);

  /**
   * see {@link AbstractSmartField#execBrowseNew(String)}
   */
  void doBrowseNew(String newText);

  /**
   * Filter selection of hierarchy browse tree. The level reported here is
   * different than the one used in
   * {@link AbstractTree#execAcceptSelection(org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode, int)} such as this
   * level is one smaller. This is because a tree smart field
   * assumes its tree to have multiple roots, but the ITree model is built as
   * single-root tree with invisible root node. level=-1 is the invisible
   * (anonymous) root level=0 are the multiple roots of the smart tree ...
   */
  @ConfigOperation
  @Order(330)
  boolean acceptBrowseHierarchySelection(KEY_TYPE value, int level, boolean leaf);

  /**
   * variant A
   */
  Class<? extends ICodeType<?, KEY_TYPE>> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType<?, KEY_TYPE>> codeType);

  /**
   * variant B
   */
  ILookupCall<KEY_TYPE> getLookupCall();

  void setLookupCall(ILookupCall<KEY_TYPE> call);

  void prepareKeyLookup(ILookupCall<KEY_TYPE> call, KEY_TYPE key) throws ProcessingException;

  void prepareTextLookup(ILookupCall<KEY_TYPE> call, String text) throws ProcessingException;

  void prepareBrowseLookup(ILookupCall<KEY_TYPE> call, String browseHint, TriState activeState) throws ProcessingException;

  void prepareRecLookup(ILookupCall<KEY_TYPE> call, KEY_TYPE parentKey, TriState activeState) throws ProcessingException;

  /**
   * If the browse lookup call yields exactly one value, assign it to the
   * smartfield, otherwise do nothing.
   * 
   * @param background
   *          true (default) if assignment should be done later which allows for
   *          one batch call for all smartfields. Using background=false assigns
   *          the value immediately, which results in an immediate call to the
   *          data provider. Whenever possible, background=true should be used
   *          to allow for batch calls to the backend.
   * @since 22.05.2009
   */
  void setUniquelyDefinedValue(boolean background) throws ProcessingException;

  List<IMenu> getMenus();

  boolean hasMenus();

  /**
   * updates the lookup rows with the same search text as last time.
   * 
   * @param selectCurrentValue
   * @param synchronous
   */
  void doSearch(boolean selectCurrentValue, boolean synchronous);

  /**
   * @param searchText
   * @param selectCurrentValue
   * @param synchronous
   */
  void doSearch(String searchText, boolean selectCurrentValue, boolean synchronous);

  /**
   * Revert the value and the text to the value that the smartfield had before a
   * new text (part)was entered. Do not use this method directly This method is
   * only called from one of the choosers outside the smartfield when the
   * chooser was terminated
   */
  void revertValue();

  /**
   * This method is normally used by a {@link IContentAssistFieldProposalForm#acceptProposal()}
   */
  void acceptProposal(ILookupRow<KEY_TYPE> row);

  List<? extends ILookupRow<KEY_TYPE>> callKeyLookup(KEY_TYPE key) throws ProcessingException;

  List<? extends ILookupRow<KEY_TYPE>> callTextLookup(String text, int maxRowCount) throws ProcessingException;

  /**
   * Note: {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called back in the model thread. The smartfield is automatically starting
   * an internal background thread and syncs the result back into the model
   * thread.
   * 
   * @return the created async job if applicable or null, see
   *         {@link LookupCall#getDataByTextInBackground(ILookupCallFetcher)}
   */
  JobEx callTextLookupInBackground(String text, int maxRowCount, ILookupCallFetcher<KEY_TYPE> fetcher);

  List<? extends ILookupRow<KEY_TYPE>> callBrowseLookup(String browseHint, int maxRowCount) throws ProcessingException;

  List<? extends ILookupRow<KEY_TYPE>> callBrowseLookup(String browseHint, int maxRowCount, TriState activeState) throws ProcessingException;

  /**
   * Note: {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called back in the model thread. The smartfield is automatically starting
   * an internal background thread and syncs the result back into the model
   * thread.
   * 
   * @return the created async job if applicable or null, see
   *         {@link LookupCall#getDataByAllInBackground(ILookupCallFetcher)}
   */
  JobEx callBrowseLookupInBackground(String browseHint, int maxRowCount, ILookupCallFetcher<KEY_TYPE> fetcher);

  /**
   * Note: {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called back in the model thread. The smartfield is automatically starting
   * an internal background thread and syncs the result back into the model
   * thread.
   * 
   * @return the created async job if applicable or null, see
   *         {@link LookupCall#getDataByAllInBackground(ILookupCallFetcher)}
   */
  JobEx callBrowseLookupInBackground(String browseHint, int maxRowCount, TriState activeState, ILookupCallFetcher<KEY_TYPE> fetcher);

  List<? extends ILookupRow<KEY_TYPE>> callSubTreeLookup(KEY_TYPE parentKey) throws ProcessingException;

  List<? extends ILookupRow<KEY_TYPE>> callSubTreeLookup(KEY_TYPE parentKey, TriState activeState) throws ProcessingException;

  IContentAssistFieldUIFacade getUIFacade();

  KEY_TYPE getValueAsLookupKey();

  /**
   * Sets the height of the proposal form in pixel. (The proposal form is smaller if there are not enought values to
   * fill the proposal.)
   * 
   * @param proposalFormHeight
   *          height in pixel
   */
  void setProposalFormHeight(int proposalFormHeight);

  /**
   * @return the height of the proposal form in pixel
   */
  int getProposalFormHeight();

}
