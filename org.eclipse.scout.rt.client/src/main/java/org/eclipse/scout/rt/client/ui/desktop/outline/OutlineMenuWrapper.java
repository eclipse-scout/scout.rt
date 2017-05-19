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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.IActionUIFacade;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.IReadOnlyMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * This class is used to create a read-only menu-instance when an existing menu instance which belongs to a component is
 * used somewhere else. For instance: menus of a page-node are automatically copied to the the page-table, because its
 * the logical place to have them there. The menu-wrapper delegates most read- or get-methods to the wrapped menu
 * instance, all write- or set-methods are implemented as NOP or throw an {@link UnsupportedOperationException}. Thus
 * the state of the wrapper should only change, when the original wrapped menu changes.
 * <p>
 * <b>IMPORTANT</b>: do not use this class in cases where you must change the state of the wrapper-instance directly,
 * since the class is not intended for that purpose. Only use it when you want to use an existing menu somewhere else,
 * using the state of the original menu.
 */
@ClassId("28ec2113-6461-4810-9527-253d0bf68788")
public class OutlineMenuWrapper extends AbstractPropertyObserver implements IMenu, IReadOnlyMenu {

  private IMenu m_wrappedMenu;
  private PropertyChangeListener m_wrappedMenuPropertyChangeListener;
  private Set<IMenuType> m_menuTypes;
  private IActionFilter m_menuFilter;
  private IMenuTypeMapper m_menuTypeMapper;
  
  public static final IActionFilter ACCEPT_ALL_FILTER = new IActionFilter() {

    @Override
    public boolean accept(IAction action) {
      return true;
    }
  };

  public static final IMenuTypeMapper AUTO_MENU_TYPE_MAPPER = new IMenuTypeMapper() {
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
   * Returns a wrapper for the given menu, or if the menu is already a wrapper instance, the same menu-instance.
   */
  public static IMenu wrapMenu(IMenu menu) {
    if (menu instanceof OutlineMenuWrapper) {
      return menu; // already wrapped - don't wrap again
    }
    else {
      return new OutlineMenuWrapper(menu);
    }
  }

  /**
   * Returns the wrapped menu if the given menu is a wrapper instance or the same menu instance otherwise.
   */
  public static IMenu unwrapMenu(IMenu menu) {
    if (menu instanceof OutlineMenuWrapper) {
      return unwrapMenu(((OutlineMenuWrapper) menu).getWrappedMenu());
    }
    else {
      return menu;
    }
  }

  /**
   * Returns true if the given menu is a wrapper menu and the wrapped menu is contained in the given collection of
   * menus.
   */
  public static boolean containsWrappedMenu(Collection<IMenu> menus, IMenu menu) {
    if (menu instanceof OutlineMenuWrapper) {
      OutlineMenuWrapper wrapper = (OutlineMenuWrapper) menu;
      return menus.contains(wrapper.getWrappedMenu());
    }
    return false;
  }

  /**
   * Constructs a wrapper for a menu where the menuType of the menu and of each menu in the sub-hierarchy is the same as
   * in the original
   *
   * @param wrappedMenu
   */
  protected OutlineMenuWrapper(IMenu wrappedMenu) {
    this(wrappedMenu, AUTO_MENU_TYPE_MAPPER, ACCEPT_ALL_FILTER);
  }

  public OutlineMenuWrapper(IMenu wrappedMenu, IMenuTypeMapper mapper) {
    this(wrappedMenu, mapper, ACCEPT_ALL_FILTER);
  }

  /**
   * @param menu
   *          the menu to wrap
   * @param menuTypeMapper
   *          the menuTypes for the menu and for each menu in the sub-hierarchy are individually computed with this
   *          mapper
   */
  public OutlineMenuWrapper(IMenu menu, IMenuTypeMapper menuTypeMapper, IActionFilter menuFilter) {
    m_wrappedMenu = menu;
    m_menuTypeMapper = menuTypeMapper;
    m_menuFilter = menuFilter;
    m_menuTypes = mapMenuTypes(menu, menuTypeMapper);
    m_wrappedMenuPropertyChangeListener = new P_WrappedMenuPropertyChangeListener();
    setup(menuTypeMapper);
  }

  protected Set<IMenuType> mapMenuTypes(IMenu menu, IMenuTypeMapper mapper) {
    Set<IMenuType> originalTypes = menu.getMenuTypes();
    Set<IMenuType> mappedTypes = new HashSet<IMenuType>(originalTypes.size());
    for (IMenuType menuType : originalTypes) {
      mappedTypes.add(mapper.map(menuType));
    }
    return mappedTypes;
  }

  protected void setup(final IMenuTypeMapper mapper) {
    wrapChildActions();
    m_wrappedMenu.addPropertyChangeListener(m_wrappedMenuPropertyChangeListener);
  }

  protected void wrapChildActions() {
    List<IMenu> childActions = m_wrappedMenu.getChildActions();
    List<IMenu> wrappedChildActions = new ArrayList<IMenu>(childActions.size());
    // create child wrappers
    for (IAction a : ActionUtility.getActions(m_wrappedMenu.getChildActions(), m_menuFilter)) {
      if (a instanceof IMenu) {
        wrappedChildActions.add(new OutlineMenuWrapper((IMenu) a, m_menuTypeMapper, m_menuFilter));
      }
    }
    propertySupport.setProperty(PROP_CHILD_ACTIONS, wrappedChildActions);
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
    // NOP
  }

  @Override
  public IMenu getParent() {
    return m_wrappedMenu.getParent();
  }

  @Override
  public void setParent(IMenu parent) {
    unsupported();
  }

  private void unsupported() {
    throw new UnsupportedOperationException("Method unsupported by menu-wrapper, you should change the state of the original menu instead");
  }

  private List<IMenu> getChildActionsInternal() {
    return propertySupport.getPropertyList(PROP_CHILD_ACTIONS);
  }

  @Override
  public boolean hasChildActions() {
    return CollectionUtility.hasElements(getChildActionsInternal());
  }

  @Override
  public int getChildActionCount() {
    return CollectionUtility.size(getChildActionsInternal());
  }

  @Override
  public List<IMenu> getChildActions() {
    return CollectionUtility.arrayList(getChildActionsInternal());
  }

  @Override
  public void setChildActions(Collection<? extends IMenu> actionList) {
    unsupported();
  }

  @Override
  public void addChildAction(IMenu action) {
    unsupported();
  }

  @Override
  public void addChildActions(Collection<? extends IMenu> actionList) {
    unsupported();
  }

  @Override
  public void removeChildAction(IMenu action) {
    unsupported();
  }

  @Override
  public void removeChildActions(Collection<? extends IMenu> actionList) {
    unsupported();
  }

  @Override
  public void initAction() {
    // NOP
  }

  @Override
  public void dispose() {
    m_wrappedMenu.removePropertyChangeListener(m_wrappedMenuPropertyChangeListener);
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
    unsupported();
  }

  @Override
  public String getText() {
    return m_wrappedMenu.getText();
  }

  @Override
  public void setText(String text) {
    unsupported();
  }

  @Override
  public String getKeyStroke() {
    return m_wrappedMenu.getKeyStroke();
  }

  @Override
  public void setKeyStroke(String text) {
    unsupported();
  }

  @Override
  public int getKeyStrokeFirePolicy() {
    return m_wrappedMenu.getKeyStrokeFirePolicy();
  }

  @Override
  public void setKeyStrokeFirePolicy(int keyStrokeFirePolicy) {
    m_wrappedMenu.setKeyStrokeFirePolicy(keyStrokeFirePolicy);
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
    unsupported();
  }

  @Override
  public boolean isSelected() {
    return m_wrappedMenu.isSelected();
  }

  @Override
  public void setSelected(boolean b) {
    unsupported();
  }

  @Override
  public boolean isEnabled() {
    return m_wrappedMenu.isEnabled();
  }

  @Override
  public void setEnabled(boolean b) {
    // NOP
  }

  @Override
  public boolean isVisible() {
    return m_wrappedMenu.isVisible();
  }

  @Override
  public void setVisible(boolean b) {
    unsupported();
  }

  @Override
  public boolean isInheritAccessibility() {
    return m_wrappedMenu.isInheritAccessibility();
  }

  @Override
  public void setInheritAccessibility(boolean b) {
    unsupported();
  }

  @Override
  public void setEnabledPermission(Permission p) {
    unsupported();
  }

  @Override
  public boolean isEnabledGranted() {
    return m_wrappedMenu.isEnabledGranted();
  }

  @Override
  public void setEnabledGranted(boolean b) {
    // NOP
  }

  @Override
  public boolean isEnabledProcessingAction() {
    return m_wrappedMenu.isEnabledProcessingAction();
  }

  @Override
  public boolean isEnabledInheritAccessibility() {
    return m_wrappedMenu.isEnabledInheritAccessibility();
  }

  @Override
  public void setEnabledInheritAccessibility(boolean enabled) {
    // NOP
  }

  @Override
  public void setVisiblePermission(Permission p) {
    unsupported();
  }

  @Override
  public boolean isVisibleGranted() {
    return m_wrappedMenu.isVisibleGranted();
  }

  @Override
  public void setVisibleGranted(boolean b) {
    unsupported();
  }

  @Override
  public boolean isToggleAction() {
    return m_wrappedMenu.isToggleAction();
  }

  @Override
  public void setToggleAction(boolean b) {
    unsupported();
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
  public boolean isEnabledIncludingParents() {
    return m_wrappedMenu.isEnabledIncludingParents();
  }

  @Override
  public boolean isVisibleIncludingParents() {
    return m_wrappedMenu.isVisibleIncludingParents();
  }

  @Override
  public IPropertyObserver getContainer() {
    return m_wrappedMenu.getContainer();
  }

  @Override
  public void setContainerInternal(IPropertyObserver container) {
    // void
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
  public byte getHorizontalAlignment() {
    return m_wrappedMenu.getHorizontalAlignment();
  }

  @Override
  public void setHorizontalAlignment(byte horizontalAlignment) {
    m_wrappedMenu.setHorizontalAlignment(horizontalAlignment);
  }

  @Override
  public String getCssClass() {
    return m_wrappedMenu.getCssClass();
  }

  @Override
  public void setCssClass(String cssClass) {
    m_wrappedMenu.setCssClass(cssClass);
  }

  @Override
  public void setView(boolean visible, boolean enabled) {
    unsupported();
  }

  @Override
  public void setVisible(boolean visible, String dimension) {
    unsupported();
  }

  @Override
  public boolean isVisible(String dimension) {
    return m_wrappedMenu.isVisible(dimension);
  }

  @Override
  public void setEnabled(boolean enabled, String dimension) {
    unsupported();
  }

  @Override
  public boolean isEnabled(String dimension) {
    return m_wrappedMenu.isEnabled(dimension);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append("[wrappedMenu=" + m_wrappedMenu.getClass().getSimpleName());
    sb.append(" text='" + getText()).append("'");
    sb.append(" enabled=" + isEnabled());
    sb.append(" enabledGranted=" + isEnabledGranted());
    sb.append(" inheritAccessibility=" + isInheritAccessibility());
    sb.append(" visible=" + isVisible());
    sb.append(" visibleGranted=" + isVisibleGranted());
    sb.append("]");
    return sb.toString();
  }

  private class P_WrappedMenuPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IActionNode.PROP_CHILD_ACTIONS.equals(evt.getPropertyName())) {
        // Special handling for PROP_CHILD_ACTIONS, wrap child actions as well
        wrapChildActions();
      }
      else {
        // Duplicate all other events (pretending they came from the outline menu wrapper)
        PropertyChangeEvent copy = new PropertyChangeEvent(OutlineMenuWrapper.this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        propertySupport.firePropertyChange(copy);
      }
    }
  }
}
