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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.smartfield;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.smartfield.MobileSmartFieldProposalFormProvider;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.rt.ui.rap.form.fields.smartfield.RwtScoutSmartField;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileSmartField extends RwtScoutSmartField {

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);

    getUiField().addMouseListener(new P_FieldSelectionListener());
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    getScoutObject().setProposalFormProvider(new MobileSmartFieldProposalFormProvider());
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);

    getUiField().setEnabled(false);
  }

  /**
   * Does nothing because the proposal form is always closed in the model.
   */
  @Override
  protected boolean hideProposalPopup() {
    return true;
  }

  /**
   * Does nothing because the proposal form is always opened directly in the model and automatically added to the
   * desktop (autoAddRemoveOnDesktop is set to true by MobileSmartFieldProposalFormProvider)
   */
  @Override
  protected void showProposalPopup(ISmartFieldProposalForm form) {
  }

  /**
   * @see {@link #showProposalPopup(ISmartFieldProposalForm)} and {@link #hideProposalPopup()}
   */
  @Override
  protected void setProposalFormFromScout(ISmartFieldProposalForm form) {

  }

  /**
   * Since direct editing is not possible there is no need to do anything on traverse events.
   */
  @Override
  protected boolean handleUiTraverseVerifier() {
    return true;
  }

  /**
   * Selects the whole text when focus is gained. <br/>
   * Does NOT request proposal popup as the original smartfield does if
   * error status is set.
   */
  @Override
  protected void handleUiFocusGained() {
    getUiField().setSelection(0, getUiField().getText().length());
  }

  private class P_FieldSelectionListener extends MouseAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void mouseUp(MouseEvent e) {
      if (!getScoutObject().isEnabled()) {
        return;
      }

      getUiField().forceFocus();

      requestProposalSupportFromUi(ISmartField.BROWSE_ALL_TEXT, true, 0);
    }
  }

}
