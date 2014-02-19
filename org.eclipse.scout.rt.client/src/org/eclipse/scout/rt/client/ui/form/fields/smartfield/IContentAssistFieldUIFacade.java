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

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public interface IContentAssistFieldUIFacade {

  boolean setTextFromUI(String newText);

  List<IMenu> firePopupFromUI();

  /**
   * This may result in a property change {@link IContentAssistField#PROP_PROPOSAL_FORM} see
   * {@link IContentAssistField#getProposalForm()}
   */
  void openProposalFromUI(String newText, boolean selectCurrentValue);

  boolean acceptProposalFromUI();

  void unregisterProposalFormFromUI(IContentAssistFieldProposalForm form);
}
