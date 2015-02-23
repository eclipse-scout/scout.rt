/**
 *
 */
package org.eclipse.scout.rt.ui.swt.action;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;

public class SwtScoutToolbarAction extends AbstractSwtScoutAction {

  private IToolBarManager m_toolbarManager;

  public SwtScoutToolbarAction(IAction scoutAction, ISwtEnvironment environment, IToolBarManager toolbarManager) {
    this(scoutAction, environment, toolbarManager, true);
  }

  public SwtScoutToolbarAction(IAction scoutAction, ISwtEnvironment environment, IToolBarManager toolbarManager, boolean attachScout) {
    super(scoutAction, environment, false);
    m_toolbarManager = toolbarManager;
    if (attachScout) {
      attachScout();
    }
  }

  @Override
  protected void updateUi() {
    m_toolbarManager.update(true);
  }

}
