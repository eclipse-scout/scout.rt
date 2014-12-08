package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;

public abstract class AbstractProposalFieldExtension<LOOKUP_KEY, OWNER extends AbstractProposalField<LOOKUP_KEY>> extends AbstractContentAssistFieldExtension<String, LOOKUP_KEY, OWNER> implements IProposalFieldExtension<LOOKUP_KEY, OWNER> {

  public AbstractProposalFieldExtension(OWNER owner) {
    super(owner);
  }
}
