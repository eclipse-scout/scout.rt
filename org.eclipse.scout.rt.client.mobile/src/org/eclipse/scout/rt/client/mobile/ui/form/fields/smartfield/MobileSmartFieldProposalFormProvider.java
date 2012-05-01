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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.smartfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalFormProvider;

/**
 * @since 3.8.0
 */
public class MobileSmartFieldProposalFormProvider implements ISmartFieldProposalFormProvider {

  @Override
  public ISmartFieldProposalForm createProposalForm(ISmartField smartField) throws ProcessingException {
    ISmartFieldProposalForm form;
    if (smartField.isBrowseHierarchy()) {
      form = new MobileSmartTreeForm(smartField);
    }
    else {
      form = new MobileSmartTableForm(smartField);
    }
    form.setAutoAddRemoveOnDesktop(true);

    return form;
  }

}
