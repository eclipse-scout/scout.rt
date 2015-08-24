package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.action.tool.AbstractToolButtonExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.FormToolButtonChains.FormToolButtonInitFormChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;

public abstract class AbstractFormToolButtonExtension<FORM extends IForm, OWNER extends AbstractFormToolButton<FORM>> extends AbstractToolButtonExtension<OWNER>implements IFormToolButtonExtension<FORM, OWNER> {

  public AbstractFormToolButtonExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execInitForm(FormToolButtonInitFormChain<FORM> chain, FORM form) throws ProcessingException {
    chain.execInitForm(form);
  }
}
