package org.eclipse.scout.rt.client.extension.ui.form.fields.button;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonClickActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonSelectionChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;

public abstract class AbstractButtonExtension<OWNER extends AbstractButton> extends AbstractFormFieldExtension<OWNER> implements IButtonExtension<OWNER> {

  public AbstractButtonExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execSelectionChanged(ButtonSelectionChangedChain chain, boolean selection) throws ProcessingException {
    chain.execSelectionChanged(selection);
  }

  @Override
  public void execClickAction(ButtonClickActionChain chain) throws ProcessingException {
    chain.execClickAction();
  }
}
