package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * Visitor calling {@link IMenu#handleOwnerValueChanged(Object)} on menus, if the menu type allows it.
 */
public class MenuOwnerChangedVisitor implements IActionVisitor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MenuOwnerChangedVisitor.class);
  private final Object m_ownerValue;
  private final Set<? extends IMenuType> m_menuTypes;

  public MenuOwnerChangedVisitor(Object ownerValue, Set<? extends IMenuType> menuTypes) {
    m_ownerValue = ownerValue;
    m_menuTypes = menuTypes;
  }

  @Override
  public int visit(IAction action) {
    if (action instanceof IMenu && !Collections.disjoint(((IMenu) action).getMenuTypes(), m_menuTypes)) {
      IMenu menu = (IMenu) action;
      try {
        menu.handleOwnerValueChanged(m_ownerValue);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Exception ex) {
        LOG.error("Error handling handleOwnerValueChanged in " + menu.getClass().getName(), ex);
      }
    }
    return CONTINUE;
  }

}
