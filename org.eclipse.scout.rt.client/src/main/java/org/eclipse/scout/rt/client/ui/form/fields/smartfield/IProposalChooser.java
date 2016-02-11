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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.TriState;
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

  void forceProposalSelection();

  void dataFetchedDelegate(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount);

  String getSearchText();

  /**
   * @return the displayed result.
   */
  IContentAssistFieldDataFetchResult<LOOKUP_KEY> getSearchResult();

  boolean isActiveFilterEnabled();

  TriState getActiveFilter();

  /**
   * Delegates to contentAssistField to set the given activeState filter and triggers a new search.
   */
  void updateActiveFilter(TriState activeState);

  /**
   * Deselects all items in the proposal chooser (e.g. removes the selected state from table-rows)
   */
  void deselect();

  /**
   * In cases of initial lookups are used in init methods of the proposal chooser the job must not be canceled.
   * <p>
   * (e.g. TreeProposalChooser)
   */
  IFuture<Void> getInitialPolulatorFuture();

}
