package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuEvent;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuListener;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper;

/**
 * Adds all menus of the source element to the destination element by wrapping every single menu using
 * {@link OutlineMenuWrapper}. Also listens to menu structure changes and updates the menus on the destination element
 * accordingly.
 */
public class MenuMediator {
  private ContextMenuListener m_contextMenuListener;
  private final List<IMenu> m_menus = new ArrayList<>();
  private IContextMenuOwner m_source;
  private IContextMenuOwner m_destination;

  public MenuMediator(IContextMenuOwner source, IContextMenuOwner destination) {
    m_source = source;
    m_destination = destination;
  }

  public void install() {
    if (m_contextMenuListener != null) {
      return;
    }
    m_contextMenuListener = new P_SourceContextMenuListener();
    getSource().getContextMenu().addContextMenuListener(m_contextMenuListener);
    mediateMenus();
  }

  public void uninstall() {
    if (m_contextMenuListener == null) {
      return;
    }
    getSource().getContextMenu().removeContextMenuListener(m_contextMenuListener);
    m_contextMenuListener = null;
  }

  protected void mediateMenus() {
    IContextMenu contextMenu = getDestination().getContextMenu();
    contextMenu.removeChildActions(getMenus());

    List<IMenu> sourceMenus = getSource().getMenus();
    for (IMenu menu : sourceMenus) {
      m_menus.add(OutlineMenuWrapper.wrapMenu(menu));
    }
    contextMenu.addChildActions(m_menus);
  }

  public IContextMenuOwner getSource() {
    return m_source;
  }

  public IContextMenuOwner getDestination() {
    return m_destination;
  }

  public List<IMenu> getMenus() {
    return m_menus;
  }

  protected class P_SourceContextMenuListener implements ContextMenuListener {
    @Override
    public void contextMenuChanged(ContextMenuEvent event) {
      if (ContextMenuEvent.TYPE_STRUCTURE_CHANGED == event.getType()) {
        mediateMenus();
      }
    }
  }

}
