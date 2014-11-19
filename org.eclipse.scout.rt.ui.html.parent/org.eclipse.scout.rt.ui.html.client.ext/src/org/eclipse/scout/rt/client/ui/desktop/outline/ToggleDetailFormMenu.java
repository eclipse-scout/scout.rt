package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage5;
import org.eclipse.scout.rt.client.ui.form.FormMenuType;
import org.eclipse.scout.rt.shared.ui.menu.AbstractMenu5;

public class ToggleDetailFormMenu extends AbstractMenu5 {

  private IPage m_page;

  private boolean m_detailFormVisible;

  public ToggleDetailFormMenu(IPage page, boolean detailFormVisible) {
    super(false);
    m_page = page;
    m_detailFormVisible = detailFormVisible;
    callInitializer();
  }

  @Override
  protected String getConfiguredText() {
    return m_detailFormVisible ? "Zurück" : "Weiter"; // TODO AWE translation
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(FormMenuType.System);
  }

  @Override
  protected void execAction() throws ProcessingException {
    if (m_page instanceof IPage5) {
      ((IPage5) m_page).setDetailFormVisible(m_detailFormVisible);
    }
  }
}
