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

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public interface ISmartFieldProposalForm extends IForm {
  String PROP_SEARCH_TEXT = "searchText";

  ISmartField<?> getSmartField();

  @FormData
  String getSearchText();

  @FormData
  void setSearchText(String text);

  void startForm() throws ProcessingException;

  /**
   * @param selectCurrentValue
   *          select the current smartfield value in the proposal table/tree/custom
   *          If necessary in a tree, load the tree children until the key is found
   * @param synchronous
   *          true to execute the lookup call synchronous
   */
  void update(boolean selectCurrentValue, boolean synchronous) throws ProcessingException;

  /**
   * Force a proposal to be selected if possible, this means that for example if there is just one proposal, then select
   * it,
   * or if multiple values are there, select the first one.
   */
  void forceProposalSelection() throws ProcessingException;

  /**
   * This method may call {@link ISmartField#acceptProposal(LookupRow)}
   * 
   * @return true if a propsal was accepted (might be a single match or the
   *         selcted one that is enabled)
   */
  LookupRow getAcceptedProposal() throws ProcessingException;

}
