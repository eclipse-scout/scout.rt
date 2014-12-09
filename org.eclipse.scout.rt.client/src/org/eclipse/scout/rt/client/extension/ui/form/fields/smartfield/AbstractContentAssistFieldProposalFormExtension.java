package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractContentAssistFieldProposalForm;

public abstract class AbstractContentAssistFieldProposalFormExtension<LOOKUP_KEY, OWNER extends AbstractContentAssistFieldProposalForm<LOOKUP_KEY>> extends AbstractFormExtension<OWNER> implements IContentAssistFieldProposalFormExtension<LOOKUP_KEY, OWNER> {

  public AbstractContentAssistFieldProposalFormExtension(OWNER owner) {
    super(owner);
  }
}
