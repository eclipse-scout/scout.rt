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

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * @since 3.8.0
 */
public class DefaultContentAssistFieldProposalFormProvider<LOOKUP_TYPE> implements IContentAssistFieldProposalFormProvider<LOOKUP_TYPE> {

  @Override
  public IContentAssistFieldProposalForm<LOOKUP_TYPE> createProposalForm(IContentAssistField<?, LOOKUP_TYPE> smartField, boolean allowCustomText) throws ProcessingException {
    IContentAssistFieldProposalForm<LOOKUP_TYPE> form;
    if (smartField.isBrowseHierarchy()) {
      form = new ContentAssistTreeForm<LOOKUP_TYPE>(smartField, allowCustomText);
    }
    else {
      form = new ContentAssistTableForm<LOOKUP_TYPE>(smartField, allowCustomText);
    }
    form.setAutoAddRemoveOnDesktop(false);
    return form;
  }

}
