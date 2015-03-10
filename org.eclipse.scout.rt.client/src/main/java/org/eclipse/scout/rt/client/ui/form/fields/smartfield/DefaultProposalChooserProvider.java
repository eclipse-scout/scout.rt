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

import org.eclipse.scout.commons.exception.ProcessingException;

public class DefaultProposalChooserProvider<LOOKUP_KEY> implements IProposalChooserProvider<LOOKUP_KEY> {

  @Override
  public IProposalChooser<?, LOOKUP_KEY> createProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) throws ProcessingException {
    if (contentAssistField.isBrowseHierarchy()) {
      return new TreeProposalChooser<LOOKUP_KEY>(contentAssistField, allowCustomText);
    }
    else {
      return new TableProposalChooser<LOOKUP_KEY>(contentAssistField, allowCustomText);
    }
  }

}
