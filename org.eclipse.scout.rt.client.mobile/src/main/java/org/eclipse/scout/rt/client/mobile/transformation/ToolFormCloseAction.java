package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;

public class ToolFormCloseAction extends AbstractMenu {
  private IForm m_form;

  public ToolFormCloseAction(IForm form) {
    m_form = form;
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("CloseButton");
  }

  @Override
  protected void execAction() throws ProcessingException {
    MobileDesktopUtility.closeToolForm(m_form);
  }
}
