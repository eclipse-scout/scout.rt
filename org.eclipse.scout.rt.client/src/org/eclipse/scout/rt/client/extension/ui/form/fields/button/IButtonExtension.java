package org.eclipse.scout.rt.client.extension.ui.form.fields.button;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonClickActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonSelectionChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;

public interface IButtonExtension<OWNER extends AbstractButton> extends IFormFieldExtension<OWNER> {

  void execSelectionChanged(ButtonSelectionChangedChain chain, boolean selection) throws ProcessingException;

  void execClickAction(ButtonClickActionChain chain) throws ProcessingException;
}
