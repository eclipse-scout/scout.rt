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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public interface IContentAssistFieldProposalForm<KEY_TYPE> extends IForm {

  String PROP_SEARCH_RESULT = "searchResult";

  IContentAssistField<?, KEY_TYPE> getContentAssistField();

  void startForm() throws ProcessingException;

  /**
   * Force a proposal to be selected if possible, this means that for example if there is just one proposal, then select
   * it,
   * or if multiple values are there, select the first one.
   */
  void forceProposalSelection() throws ProcessingException;

  /**
   * @return the displayed result.
   */
  IContentAssistFieldDataFetchResult<KEY_TYPE> getSearchResult();

  /**
   * This method may call {@link ISmartField#acceptProposal(LookupRow)}
   * 
   * @return true if a propsal was accepted (might be a single match or the
   *         selcted one that is enabled)
   */
  ILookupRow<KEY_TYPE> getAcceptedProposal() throws ProcessingException;

  /**
   * @param rows
   * @param failed
   * @param maxCount
   * @param selectCurrentValue
   */
  void dataFetchedDelegate(IContentAssistFieldDataFetchResult<KEY_TYPE> result, int maxCount);

  /**
   * @param status
   */
  void setTablePopulateStatus(IProcessingStatus status);

  /**
   * delegate method to search result.
   * 
   * @return the search text of the last successful search.
   */
  String getSearchText();

}
