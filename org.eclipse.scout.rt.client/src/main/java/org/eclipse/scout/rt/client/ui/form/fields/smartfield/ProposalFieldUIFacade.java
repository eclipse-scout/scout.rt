package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProposalFieldUIFacade<LOOKUP_KEY> extends ContentAssistFieldUIFacade<LOOKUP_KEY> {

  private static final Logger LOG = LoggerFactory.getLogger(ProposalFieldUIFacade.class);

  ProposalFieldUIFacade(AbstractContentAssistField<?, LOOKUP_KEY> field) {
    super(field);
  }

  @Override
  public void acceptProposalFromUI(String text, boolean chooser, boolean forceClose) {
    if (ignoreUiEvent()) {
      return;
    }
    LOG.debug("acceptProposalFromUI text={} chooser={} forceClose={}", text, chooser, forceClose);

    if (chooser && getField().isProposalChooserRegistered()) {
      handleChangedDisplaytext(text, forceClose);
    }
    else {
      openProposalChooser(text);
    }
  }
}
