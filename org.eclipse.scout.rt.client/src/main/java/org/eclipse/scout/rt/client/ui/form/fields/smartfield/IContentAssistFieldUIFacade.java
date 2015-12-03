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

/**
 * @since Scout 6.0
 */
public interface IContentAssistFieldUIFacade {

  /**
   * Called when smart-field is clicked or when a key (character or navigation) is pressed in the smart-field. Proposal
   * chooser must be closed.<br/>
   * Calling this method may cause a property change for property {@code PROP_PROPOSAL_CHOOSER} in
   * {@link IContentAssistField}.
   */
  void openProposalChooserFromUI(String text, boolean selectCurrentValue);

  /**
   * Called when ever a (non-navigation) key is pressed in the smart-field. Proposal chooser must be open.
   */
  void proposalTypedFromUI(String text);

  /**
   * Called when Escape key has been pressed or a mouse click event occurs. Proposal chooser must be open.<br/>
   * Calling this method may cause a property change for property {@code PROP_PROPOSAL_CHOOSER} in
   * {@link IContentAssistField}.
   */
  void cancelProposalChooserFromUI();

  /**
   * Called when Enter key has been pressed.<br/>
   * Calling this method may cause a property change for property {@code PROP_PROPOSAL_CHOOSER} in
   * {@link IContentAssistField}.
   *
   * @param text
   * @param chooser
   * @param forceClose
   *          whether or not the proposal chooser must be closed - this is the case when a user clicks on another field,
   *          but not when he tries to leave the field by using the TAB key.
   */
  void acceptProposalFromUI(String text, boolean chooser, boolean forceClose);

}
