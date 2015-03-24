/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * A proposal chooser for content assist fields.
 *
 * @param <T>
 *          Type of the proposal model.
 * @param <LOOKUP_KEY>
 * @since 6.0.0
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=461727
 */
public interface IProposalChooser<T, LOOKUP_KEY> extends IPropertyObserver {

  String PROP_SEARCH_RESULT = "searchResult";
  String PROP_ACTIVE_STATE_FILTER_ENABLED = "activeStateFilterEnabled";
  String PROP_ACTIVE_STATE_FILTER = "activeStateFilter";
  String PROP_STATUS = "status";
  String PROP_STATUS_VISIBLE = "statusVisible";

  /**
   * Legacy event required for old Swing client.
   * FIXME AWE: remove before release 6.0.0 is shipped.
   */
  String SWING_STRUCTURE_CHANGED = "swingStructureChanged";

  void setStatus(IStatus status);

  void setStatusVisible(boolean visible);

  IStatus getStatus();

  boolean isStatusVisible();

  ILookupRow<LOOKUP_KEY> getAcceptedProposal();

  /**
   * Returns the model used to implement a proposal chooser. Typically this is either an ITable or an ITree.
   */
  T getModel();

  /**
   * Disposes the model used to implement the proposal chooser and terminates running jobs.
   */
  void dispose();

  void forceProposalSelection() throws ProcessingException;

  void dataFetchedDelegate(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount);

  String getSearchText();

  /**
   * @return the displayed result.
   */
  IContentAssistFieldDataFetchResult<LOOKUP_KEY> getSearchResult();

  /**
   * Stores input provided by the proposal chooser and closes the proposal chooser.
   */
  void doOk() throws ProcessingException;

  // FIXME AWE: besser acceptProposal() nennen

  boolean isActiveFilterEnabled();

  TriState getActiveFilter();

  /**
   * Delegates to contentAssistField to set the given activeState filter and triggers a new search.
   */
  void updateActiveFilter(TriState activeState);

}
