package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;

/**
 * @since 6.0
 */
public abstract class AbstractInitializableMenu extends AbstractMenu {

  private boolean m_initialized = false;

  @Override
  protected void execInitAction() {
    super.execInitAction();
    m_initialized = true;
  }

  public boolean isInitialized() {
    return m_initialized;
  }
}
