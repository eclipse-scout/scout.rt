/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.IActionUIFacade;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.IReadOnlyMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.platform.classid.ClassId;
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
public class OutlineMenuWrapper extends AbstractWidget implements IReadOnlyMenu {

  private final IMenu m_wrappedMenu;
  private final PropertyChangeListener m_wrappedMenuPropertyChangeListener;
  private final Set<IMenuType> m_menuTypes;
  private final Predicate<IMenu> m_menuFilter;
  private final IMenuTypeMapper m_menuTypeMapper;

  public static final Predicate<IMenu> ACCEPT_ALL_FILTER = menu -> true;

  public static final IMenuTypeMapper AUTO_MENU_TYPE_MAPPER = menuType -> menuType;

  /**
   * maps a menuType of the wrapped menu to the menuType of the wrapperMenu
   */
  @FunctionalInterface
  public interface IMenuTypeMapper {
    IMenuType map(IMenuType menuType);
  }

  /**
   * Constructs a wrapper for a menu where the menuType of the menu and of each menu in the sub-hierarchy is the same as
   * in the original
   */
  protected OutlineMenuWrapper(IMenu wrappedMenu) {
    this(wrappedMenu, AUTO_MENU_TYPE_MAPPER, ACCEPT_ALL_FILTER);
  }

  protected OutlineMenuWrapper(IMenu wrappedMenu, IMenuTypeMapper mapper) {
    this(wrappedMenu, mapper, ACCEPT_ALL_FILTER);
  }

  /**
   * @param menu
   *     the menu to wrap
   * @param menuTypeMapper
   *     the menuTypes for the menu and for each menu in the sub-hierarchy are individually computed with this
   *     mapper
   */
  protected OutlineMenuWrapper(IMenu menu, IMenuTypeMapper menuTypeMapper, Predicate<IMenu> menuFilter) {
    m_wrappedMenu = menu;
    m_menuTypeMapper = menuTypeMapper;
    m_menuFilter = menuFilter;
    m_menuTypes = mapMenuTypes(menu, menuTypeMapper);
    m_wrappedMenuPropertyChangeListener = new P_WrappedMenuPropertyChangeListener();
    setup(menuTypeMapper);
  }

  protected Set<IMenuType> mapMenuTypes(IMenu menu, IMenuTypeMapper mapper) {
    Set<IMenuType> originalTypes = menu.getMenuTypes();
    Set<IMenuType> mappedTypes = new HashSet<>(originalTypes.size());
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
    List<IMenu> wrappedChildActions = new ArrayList<>(childActions.size());
    // create child wrappers
    for (IMenu m : MenuUtility.filterMenusRec(childActions, m_menuFilter)) {
      wrappedChildActions.add(new OutlineMenuWrapper(m, m_menuTypeMapper, m_menuFilter));
    }
    propertySupport.setProperty(PROP_CHILD_ACTIONS, wrappedChildActions);
  }

  @Override
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
  public IWidget getParent() {
    return m_wrappedMenu.getParent();
  }

  @Override
  public boolean setParentInternal(IWidget parent) {
    unsupported();
    return false;
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
  protected void initConfig() {
    // NOP
  }

  @Override
  protected void disposeInternal() {
    m_wrappedMenu.removePropertyChangeListener(m_wrappedMenuPropertyChangeListener);
    super.disposeInternal();
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
  public boolean setProperty(String name, Object value) {
    return m_wrappedMenu.setProperty(name, value);
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
  public Object getImage() {
    return m_wrappedMenu.getImage();
  }

  @Override
  public void setImage(Object image) {
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
  public String getTextPosition() {
    return m_wrappedMenu.getTextPosition();
  }

  @Override
  public void setTextPosition(String position) {
    unsupported();
  }

  @Override
  public boolean isHtmlEnabled() {
    return m_wrappedMenu.isHtmlEnabled();
  }

  @Override
  public void setHtmlEnabled(boolean htmlEnabled) {
    unsupported();
  }

  @Override
  public int getActionStyle() {
    return m_wrappedMenu.getActionStyle();
  }

  @Override
  public void setActionStyle(int actionStyle) {
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
  public boolean isEnabledGranted() {
    return m_wrappedMenu.isEnabledGranted();
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
  public boolean isEnabledIncludingParents() {
    return m_wrappedMenu.isEnabledIncludingParents();
  }

  @Override
  public boolean isVisibleIncludingParents() {
    return m_wrappedMenu.isVisibleIncludingParents();
  }

  @Override
  public IWidget getContainer() {
    return m_wrappedMenu.getContainer();
  }

  @Override
  public void setContainerInternal(IWidget container) {
    unsupported();
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
  public void setEnabled(boolean enabled, boolean updateParents, boolean updateChildren, String dimension) {
    // NOP
  }

  @Override
  public boolean isEnabled(String dimension) {
    return m_wrappedMenu.isEnabled(dimension);
  }

  @Override
  public boolean isEnabled(Predicate<String> filter) {
    return m_wrappedMenu.isEnabled(filter);
  }

  @Override
  public boolean isPreventDoubleClick() {
    return m_wrappedMenu.isPreventDoubleClick();
  }

  @Override
  public void setPreventDoubleClick(boolean preventDoubleClick) {
    unsupported();
  }

  @Override
  public boolean isStackable() {
    return m_wrappedMenu.isStackable();
  }

  @Override
  public void setStackable(boolean stackable) {
    unsupported();
  }

  @Override
  public boolean isShrinkable() {
    return m_wrappedMenu.isShrinkable();
  }

  @Override
  public void setShrinkable(boolean shrinkable) {
    unsupported();
  }

  @Override
  public String getSubMenuVisibility() {
    return m_wrappedMenu.getSubMenuVisibility();
  }

  @Override
  public void setSubMenuVisibility(String subMenuVisibility) {
    unsupported();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[wrappedMenu=" + m_wrappedMenu.getClass().getSimpleName()
        + " text='" + getText() + "'"
        + " enabled=" + isEnabled()
        + " enabledGranted=" + isEnabledGranted()
        + " inheritAccessibility=" + isInheritAccessibility()
        + " visible=" + isVisible()
        + " visibleGranted=" + isVisibleGranted()
        + "]";
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
