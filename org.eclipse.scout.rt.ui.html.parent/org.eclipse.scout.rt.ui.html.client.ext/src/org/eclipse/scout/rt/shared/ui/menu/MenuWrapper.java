package org.eclipse.scout.rt.shared.ui.menu;

import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionUIFacade;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.internal.TablePageTreeMenuWrapper;

/**
 * copy from {@link TablePageTreeMenuWrapper}
 */
public class MenuWrapper implements IMenu {

  private IMenu m_wrappedMenu;
  private IPropertyObserver m_menuOwner;
  private List<IMenu> m_childMenus;
  private boolean m_localEnabled = true;
  private boolean m_localEnabledInheritAccessibility = true;
  private Set<IMenuType> m_menuTypes;

  /**
   * API to ensure at least one menu type!
   */
  public MenuWrapper(IMenu wrappedMenu, IMenuType firstMenuType, IMenuType... additionalTypes) {
    Set<IMenuType> menuTypeSet = CollectionUtility.hashSet(firstMenuType);
    if (additionalTypes != null) {
      for (IMenuType t : additionalTypes) {
        if (t != null) {
          menuTypeSet.add(t);
        }
      }
    }
    m_wrappedMenu = wrappedMenu;
    m_menuTypes = menuTypeSet;
    setup();
  }

  public MenuWrapper(IMenu wrappedMenu, Set<? extends IMenuType> menuTypes) {
    m_wrappedMenu = wrappedMenu;
    m_menuTypes = CollectionUtility.hashSet(menuTypes);
    setup();
  }

  /**
   *
   */
  protected void setup() {
    List<IMenu> childActions = m_wrappedMenu.getChildActions();
    List<IMenu> wrappedChildActions = new ArrayList<IMenu>(childActions.size());
    // create child wrapper
    for (IMenu m : childActions) {
      wrappedChildActions.add(new MenuWrapper(m, getMenuTypes()));
    }
    m_childMenus = wrappedChildActions;
  }

  public IMenu getWrappedMenu() {
    return m_wrappedMenu;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isSingleSelectionAction() {
    return m_wrappedMenu.isSingleSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setSingleSelectionAction(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isMultiSelectionAction() {
    return m_wrappedMenu.isMultiSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setMultiSelectionAction(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isEmptySpaceAction() {
    return m_wrappedMenu.isEmptySpaceAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setEmptySpaceAction(boolean b) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void handleOwnerValueChanged(Object newValue) throws ProcessingException {
    // void
  }

  @Override
  public void aboutToShow() {
    m_wrappedMenu.aboutToShow();
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
  public void setChildActions(List<? extends IMenu> actionList) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void addChildAction(IMenu action) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void addChildActions(List<? extends IMenu> actionList) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void removeChildAction(IMenu action) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void removeChildActions(List<? extends IMenu> actionList) {
    throw new UnsupportedOperationException("read only wrapper");
  }

  @Override
  public void initAction() throws ProcessingException {
    m_wrappedMenu.initAction();
  }

  @Override
  public void doAction() throws ProcessingException {
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

  @SuppressWarnings("deprecation")
  @Override
  public void prepareAction() {
    m_wrappedMenu.prepareAction();
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
  public String classId() {
    return m_wrappedMenu.classId();
  }

  @Override
  public Set<IMenuType> getMenuTypes() {
    return CollectionUtility.hashSet(m_menuTypes);
  }

  @Override
  public String toString() {
    return super.toString() + " wrapping '" + m_wrappedMenu.toString() + "'";
  }
}
