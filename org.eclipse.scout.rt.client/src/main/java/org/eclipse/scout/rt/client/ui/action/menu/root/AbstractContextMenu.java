/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu.root;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.action.menu.root.IContextMenuExtension;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.EventListenerList;

@ClassId("b34571a2-032b-4910-921a-bec4acd110ed")
public abstract class AbstractContextMenu<T extends IPropertyObserver> extends AbstractMenu implements IContextMenu {

  private final EventListenerList m_listeners = new EventListenerList();
  private final PropertyChangeListener m_menuVisibilityListener = new P_VisibilityOfMenuItemChangedListener();

  public AbstractContextMenu(T container, List<? extends IMenu> initialChildList) {
    this(container, initialChildList, true);
  }

  public AbstractContextMenu(T container, List<? extends IMenu> initialChildList, boolean callInitializer) {
    super(false);
    setContainerInternal(container);
    if (callInitializer) {
      callInitializer();
    }
    setChildActions(initialChildList);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getContainer() {
    return (T) super.getContainer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    calculateLocalVisibility();
    getContainer().addPropertyChangeListener(new P_OwnerPropertyListener());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<? extends IMenuType> getCurrentMenuTypes() {
    return (Set<? extends IMenuType>) propertySupport.getProperty(PROP_CURRENT_MENU_TYPES);
  }

  protected void setCurrentMenuTypes(Set<? extends IMenuType> menuTypes) {
    propertySupport.setProperty(PROP_CURRENT_MENU_TYPES, menuTypes);
  }

  @Override
  public void addContextMenuListener(ContextMenuListener listener) {
    m_listeners.add(ContextMenuListener.class, listener);
  }

  @Override
  public void removeContextMenuListener(ContextMenuListener listener) {
    m_listeners.remove(ContextMenuListener.class, listener);
  }

  protected void fireContextMenuEvent(ContextMenuEvent event) {
    for (ContextMenuListener l : m_listeners.getListeners(ContextMenuListener.class)) {
      l.contextMenuChanged(event);
    }
  }

  @Override
  protected void afterChildMenusAdd(Collection<? extends IMenu> newChildMenus) {
    super.afterChildMenusAdd(newChildMenus);
    addScoutMenuVisibilityListenerRec(newChildMenus);
    calculateLocalVisibility();
    fireContextMenuEvent(new ContextMenuEvent(this, ContextMenuEvent.TYPE_STRUCTURE_CHANGED));
  }

  @Override
  protected void afterChildMenusRemove(Collection<? extends IMenu> childMenusToRemove) {
    super.afterChildMenusRemove(childMenusToRemove);
    removeScoutMenuVisibilityListenerRec(childMenusToRemove);
    calculateLocalVisibility();
    fireContextMenuEvent(new ContextMenuEvent(this, ContextMenuEvent.TYPE_STRUCTURE_CHANGED));
  }

  /**
   * @param oldValue
   * @param newValue
   */
  protected void handleChildActionsChanged(List<IMenu> oldValue, List<IMenu> newValue) {
    removeScoutMenuVisibilityListenerRec(oldValue);
    addScoutMenuVisibilityListenerRec(newValue);
    fireContextMenuEvent(new ContextMenuEvent(this, ContextMenuEvent.TYPE_STRUCTURE_CHANGED));
  }

  protected void addScoutMenuVisibilityListenerRec(Collection<? extends IMenu> menus) {
    if (menus != null) {
      for (IMenu m : menus) {
        m.addPropertyChangeListener(IMenu.PROP_CHILD_ACTIONS, m_menuVisibilityListener);
        m.addPropertyChangeListener(IMenu.PROP_VISIBLE, m_menuVisibilityListener);
        addScoutMenuVisibilityListenerRec(m.getChildActions());
      }
    }
  }

  protected void removeScoutMenuVisibilityListenerRec(Collection<? extends IMenu> menus) {
    if (menus != null) {
      for (IMenu m : menus) {
        m.removePropertyChangeListener(IMenu.PROP_CHILD_ACTIONS, m_menuVisibilityListener);
        m.removePropertyChangeListener(IMenu.PROP_VISIBLE, m_menuVisibilityListener);
        removeScoutMenuVisibilityListenerRec(m.getChildActions());
      }
    }
  }

  protected void calculateLocalVisibility() {
    final IActionFilter activeFilter = ActionUtility.createMenuFilterMenuTypes(getCurrentMenuTypes(), true);
    if (activeFilter != null) {
      final BooleanHolder visibleHolder = new BooleanHolder(false);
      acceptVisitor(new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            if (menu.hasChildActions() || menu.isSeparator() || menu instanceof IContextMenu) {
              return CONTINUE;
            }
            else if (activeFilter.accept(menu)) {
              visibleHolder.setValue(true);
              return CANCEL;
            }
          }
          return CONTINUE;
        }
      });
      setVisible(visibleHolder.getValue());
    }
  }

  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
  }

  private class P_VisibilityOfMenuItemChangedListener implements PropertyChangeListener {
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      if (IMenu.PROP_CHILD_ACTIONS.equals(evt.getPropertyName())) {
        handleChildActionsChanged((List<IMenu>) evt.getOldValue(), (List<IMenu>) evt.getNewValue());
      }
      calculateLocalVisibility();
    }
  }

  private class P_OwnerPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handleOwnerPropertyChanged(evt);
    }
  }

  protected static class LocalContextMenuExtension<OWNER extends AbstractContextMenu> extends LocalMenuExtension<OWNER> implements IContextMenuExtension<OWNER> {

    public LocalContextMenuExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IContextMenuExtension<? extends AbstractContextMenu> createLocalExtension() {
    return new LocalContextMenuExtension<AbstractContextMenu>(this);
  }

}
