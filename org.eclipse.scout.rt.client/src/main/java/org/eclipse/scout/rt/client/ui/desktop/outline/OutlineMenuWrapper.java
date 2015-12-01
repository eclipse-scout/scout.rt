/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionUIFacade;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class OutlineMenuWrapper implements IMenu {

  private IMenu m_wrappedMenu;
  private List<IMenu> m_childMenus;
  private boolean m_localEnabled = true;
  private boolean m_localEnabledInheritAccessibility = true;
  private Set<IMenuType> m_menuTypes;
  public final static IMenuTypeMapper AUTO_MENU_TYPE_MAPPER = new IMenuTypeMapper() {
    @Override
    public IMenuType map(IMenuType menuType) {
      return menuType;
    }
  };

  /**
   * maps a menuType of the wrapped menu to the menuType of the wrapperMenu
   */
  public interface IMenuTypeMapper {
    IMenuType map(IMenuType menuType);
  }

  /**
   * Constructs a wrapper for a menu where the menuType of the menu and of each menu in the sub-hierarchy is the same as
   * in the original
   *
   * @param wrappedMenu
   */
  public OutlineMenuWrapper(IMenu wrappedMenu) {
    this(wrappedMenu, AUTO_MENU_TYPE_MAPPER);
  }

  /**
   * @param wrappedMenu
   * @param mapper
   *          the menuTypes for the menu and for each menu in the sub-hierarchy are individually computed with this
   *          mapper
   */
  public OutlineMenuWrapper(IMenu wrappedMenu, IMenuTypeMapper mapper) {
    m_wrappedMenu = wrappedMenu;
    Set<IMenuType> originalTypes = wrappedMenu.getMenuTypes();
    m_menuTypes = new HashSet<IMenuType>(originalTypes.size());
    for (IMenuType menuType : originalTypes) {
      m_menuTypes.add(mapper.map(menuType));
    }
    setup(mapper);
  }

  protected void setup(IMenuTypeMapper mapper) {
    List<IMenu> childActions = m_wrappedMenu.getChildActions();
    List<IMenu> wrappedChildActions = new ArrayList<IMenu>(childActions.size());
    // create child wrapper
    for (IMenu m : childActions) {
      wrappedChildActions.add(new OutlineMenuWrapper(m, mapper));
    }
    m_childMenus = wrappedChildActions;
  }

  public IMenu getWrappedMenu() {
    return m_wrappedMenu;
  }

  @Override
  public Object getOwnerValue() {
    return m_wrappedMenu.getOwnerValue();
  }

  @Override
  public void handleOwnerValueChanged(Object newValue) {
    // void
  }

  @Override
  public IMenu getParent() {
    return m_wrappedMenu.getParent();
  }

  @Override
  public void setParent(IMenu parent) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean hasChildActions() {
    return m_childMenus.size() > 0;
  }

  @Override
  public int getChildActionCount() {
    return m_childMenus.size();
  }

  @Override
  public List<IMenu> getChildActions() {
    return m_childMenus;
  }

  @Override
  public void setChildActions(Collection<? extends IMenu> actionList) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void addChildAction(IMenu action) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void addChildActions(Collection<? extends IMenu> actionList) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void removeChildAction(IMenu action) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void removeChildActions(Collection<? extends IMenu> actionList) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void initAction() {
    m_wrappedMenu.initAction();
  }

  @Override
  public void dispose() {
    m_wrappedMenu.dispose();
  }

  @Override
  public void doAction() {
    m_wrappedMenu.doAction();
  }

  @Override
  public Object getProperty(String name) {
    return m_wrappedMenu.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    m_wrappedMenu.setProperty(name, value);
  }

  @Override
  public boolean hasProperty(String name) {
    return m_wrappedMenu.hasProperty(name);
  }

  @Override
  public String getActionId() {
    return m_wrappedMenu.getActionId();
  }

  @Override
  public String getIconId() {
    return m_wrappedMenu.getIconId();
  }

  @Override
  public void setIconId(String iconId) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public String getText() {
    return m_wrappedMenu.getText();
  }

  @Override
  public void setText(String text) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public String getTextWithMnemonic() {
    return m_wrappedMenu.getTextWithMnemonic();
  }

  @Override
  public String getKeyStroke() {
    return m_wrappedMenu.getKeyStroke();
  }

  @Override
  public void setKeyStroke(String text) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public String getTooltipText() {
    return m_wrappedMenu.getTooltipText();
  }

  @Override
  public void setTooltipText(String text) {
    m_wrappedMenu.setTooltipText(text);
  }

  @Override
  public boolean isSeparator() {
    return m_wrappedMenu.isSeparator();
  }

  @Override
  public void setSeparator(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isSelected() {
    return m_wrappedMenu.isSelected();
  }

  @Override
  public void setSelected(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isEnabled() {
    return m_localEnabled && m_localEnabledInheritAccessibility && m_wrappedMenu.isEnabled();
  }

  @Override
  public void setEnabled(boolean b) {
    m_localEnabled = b;
  }

  @Override
  public boolean isVisible() {
    return m_wrappedMenu.isVisible();
  }

  @Override
  public void setVisible(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isInheritAccessibility() {
    return m_wrappedMenu.isInheritAccessibility();
  }

  @Override
  public void setInheritAccessibility(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void setEnabledPermission(Permission p) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isEnabledGranted() {
    return m_wrappedMenu.isEnabledGranted();
  }

  @Override
  public void setEnabledGranted(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isEnabledProcessingAction() {
    return m_wrappedMenu.isEnabledProcessingAction();
  }

  @Override
  public void setEnabledProcessingAction(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isEnabledInheritAccessibility() {
    return m_wrappedMenu.isEnabledInheritAccessibility();
  }

  @Override
  public void setEnabledInheritAccessibility(boolean enabled) {
    m_localEnabledInheritAccessibility = enabled;
  }

  @Override
  public void setVisiblePermission(Permission p) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isVisibleGranted() {
    return m_wrappedMenu.isVisibleGranted();
  }

  @Override
  public void setVisibleGranted(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public boolean isToggleAction() {
    return m_wrappedMenu.isToggleAction();
  }

  @Override
  public void setToggleAction(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public char getMnemonic() {
    return m_wrappedMenu.getMnemonic();
  }

  @Override
  public IActionUIFacade getUIFacade() {
    return m_wrappedMenu.getUIFacade();
  }

  @Override
  public int acceptVisitor(IActionVisitor visitor) {
    switch (visitor.visit(this)) {
      case IActionVisitor.CANCEL:
        return IActionVisitor.CANCEL;
      case IActionVisitor.CANCEL_SUBTREE:
        return IActionVisitor.CONTINUE;
      case IActionVisitor.CONTINUE_BRANCH:
        visitChildren(visitor);
        return IActionVisitor.CANCEL;
      default:
        return visitChildren(visitor);
    }
  }

  private int visitChildren(IActionVisitor visitor) {
    for (IAction t : getChildActions()) {
      switch (t.acceptVisitor(visitor)) {
        case IActionVisitor.CANCEL:
          return IActionVisitor.CANCEL;
      }
    }
    return IActionVisitor.CONTINUE;
  }

  @Override
  public boolean isThisAndParentsEnabled() {
    return m_wrappedMenu.isThisAndParentsEnabled();
  }

  @Override
  public boolean isThisAndParentsVisible() {
    return m_wrappedMenu.isThisAndParentsVisible();
  }

  @Override
  public ITypeWithClassId getContainer() {
    return m_wrappedMenu.getContainer();
  }

  @Override
  public void setContainerInternal(ITypeWithClassId container) {
    // void
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_wrappedMenu.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_wrappedMenu.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_wrappedMenu.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_wrappedMenu.removePropertyChangeListener(propertyName, listener);
  }

  @Override
  public double getOrder() {
    return m_wrappedMenu.getOrder();
  }

  @Override
  public void setOrder(double order) {
    m_wrappedMenu.setOrder(order);
  }

  @Override
  public String classId() {
    return m_wrappedMenu.classId();
  }

  @Override
  public Set<IMenuType> getMenuTypes() {
    return CollectionUtility.hashSet(m_menuTypes);
  }

  @Override
  public int getHorizontalAlignment() {
    return m_wrappedMenu.getHorizontalAlignment();
  }

  @Override
  public void setHorizontalAlignment(int horizontalAlignment) {
    m_wrappedMenu.setHorizontalAlignment(horizontalAlignment);
  }

  @Override
  public void setView(boolean visible, boolean enabled) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public String toString() {
    return super.toString() + " wrapping '" + m_wrappedMenu.toString() + "'";
  }
}
