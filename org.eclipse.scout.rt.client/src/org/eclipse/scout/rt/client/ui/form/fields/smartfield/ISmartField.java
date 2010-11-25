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

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public interface ISmartField<T> extends IValueField<T> {

  /**
   * {@link ISmartFieldProposalForm}
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

  void addSmartFieldListener(SmartFieldListener listener);

  void removeSmartFieldListener(SmartFieldListener listener);

  ISmartFieldProposalForm getProposalForm();

  /**
   * Code-Assistant Don't just allow smart field values, but also custom text as
   * valid values; smartfield is simply used as code assistent
   */
  boolean isAllowCustomText();

  /**
   * Code-Assistant Don't just allow smart field values, but also custom text as
   * valid values; smartfield is simply used as code assistent
   */
  void setAllowCustomText(boolean b);

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
   * level is one smaller. This is beacuase a tree smart field
   * assumes its tree to have multiple roots, but the ITree model is built as
   * single-root tree with invisible root node. level=-1 is the invisible
   * (annonymous) root level=0 are the multiple roots of the smart tree ...
   */
  @ConfigOperation
  @Order(330)
  boolean acceptBrowseHierarchySelection(T value, int level, boolean leaf);

  /**
   * variant A
   */
  Class<? extends ICodeType> getCodeTypeClass();

  void setCodeTypeClass(Class<? extends ICodeType> codeType);

  /**
   * variant B
   */
  LookupCall getLookupCall();

  void setLookupCall(LookupCall call);

  void prepareKeyLookup(LookupCall call, T key) throws ProcessingException;

  void prepareTextLookup(LookupCall call, String text) throws ProcessingException;

  void prepareBrowseLookup(LookupCall call, String browseHint, TriState activeState) throws ProcessingException;

  void prepareRecLookup(LookupCall call, T parentKey, TriState activeState) throws ProcessingException;

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

  IMenu[] getMenus();

  boolean hasMenus();

  /**
   * Revert the value and the text to the value that the smartfield had before a
   * new text (part)was entered. Do not use this method directly This method is
   * only called from one of the choosers outside the smartfield when the
   * chooser was terminated
   */
  void revertValue();

  /**
   * This method is normally used by a {@link ISmartFieldProposalForm#acceptProposal()}
   */
  void acceptProposal(LookupRow row);

  LookupRow[] callKeyLookup(T key) throws ProcessingException;

  LookupRow[] callTextLookup(String text, int maxRowCount) throws ProcessingException;

  /**
   * Note: {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called back in the model thread. The smartfield is automatically starting
   * an internal background thread and syncs the result back into the model
   * thread.
   */
  void callTextLookupInBackground(String text, int maxRowCount, ILookupCallFetcher fetcher);

  LookupRow[] callBrowseLookup(String browseHint, int maxRowCount) throws ProcessingException;

  LookupRow[] callBrowseLookup(String browseHint, int maxRowCount, TriState activeState) throws ProcessingException;

  /**
   * Note: {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called back in the model thread. The smartfield is automatically starting
   * an internal background thread and syncs the result back into the model
   * thread.
   */
  void callBrowseLookupInBackground(String browseHint, int maxRowCount, ILookupCallFetcher fetcher);

  /**
   * Note: {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called back in the model thread. The smartfield is automatically starting
   * an internal background thread and syncs the result back into the model
   * thread.
   */
  void callBrowseLookupInBackground(String browseHint, int maxRowCount, TriState activeState, ILookupCallFetcher fetcher);

  LookupRow[] callSubTreeLookup(T parentKey) throws ProcessingException;

  LookupRow[] callSubTreeLookup(T parentKey, TriState activeState) throws ProcessingException;

  ISmartFieldUIFacade getUIFacade();
}
