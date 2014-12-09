package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.action.tool.IToolButtonExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.FormToolButtonChains.FormToolButtonStartFormChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;

public interface IFormToolButtonExtension<FORM extends IForm, OWNER extends AbstractFormToolButton<FORM>> extends IToolButtonExtension<OWNER> {

  void execStartForm(FormToolButtonStartFormChain<? extends IForm> chain) throws ProcessingException;
}
