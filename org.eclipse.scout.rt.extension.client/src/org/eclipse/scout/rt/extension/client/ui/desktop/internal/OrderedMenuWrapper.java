/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop.internal;

import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.List;

import org.eclipse.scout.commons.annotations.IOrdered;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.IActionUIFacade;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.IWrappedObject;

/**
 * Wrapper for {@link IMenu}s providing the wrapped menu's order.
 * 
 * @since 3.9.0
 */
public class OrderedMenuWrapper implements IMenu, IOrdered, IWrappedObject<IMenu> {

  private final IMenu m_menu;
  private final double m_order;

  public OrderedMenuWrapper(IMenu menu, double order) {
    m_menu = menu;
    m_order = order;
  }

  @Override
  public double getOrder() {
    return m_order;
  }

  @Override
  public IMenu getWrappedObject() {
    return m_menu;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_menu.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_menu.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_menu.removePropertyChangeListener(listener);
  }

  @Override
  public IMenu getParent() {
    return m_menu.getParent();
  }

  @Override
  public Object getProperty(String name) {
    return m_menu.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    m_menu.setProperty(name, value);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_menu.removePropertyChangeListener(propertyName, listener);
  }

  @Override
  public void setParent(IMenu parent) {
    m_menu.setParent(parent);
  }

  @Override
  public boolean hasChildActions() {
    return m_menu.hasChildActions();
  }

  @Override
  public int getChildActionCount() {
    return m_menu.getChildActionCount();
  }

  @Override
  public List<IMenu> getChildActions() {
    return m_menu.getChildActions();
  }

  @Override
  public void setChildActions(List<IMenu> newList) {
    m_menu.setChildActions(newList);
  }

  @Override
  public void doAction() throws ProcessingException {
    m_menu.doAction();
  }

  @Override
  public boolean hasProperty(String name) {
    return m_menu.hasProperty(name);
  }

  @Override
  public String getActionId() {
    return m_menu.getActionId();
  }

  @Override
  public String getIconId() {
    return m_menu.getIconId();
  }

  @Override
  public void setIconId(String iconId) {
    m_menu.setIconId(iconId);
  }

  @Override
  public boolean isSeparator() {
    return m_menu.isSeparator();
  }

  @Override
  public void setSeparator(boolean b) {
    m_menu.setSeparator(b);
  }

  @Override
  public String getText() {
    return m_menu.getText();
  }

  @Override
  public void setText(String text) {
    m_menu.setText(text);
  }

  @Override
  public String getKeyStroke() {
    return m_menu.getKeyStroke();
  }

  @Override
  public void setKeyStroke(String text) {
    m_menu.setKeyStroke(text);
  }

  @Override
  public String getTooltipText() {
    return m_menu.getTooltipText();
  }

  @Override
  public void setTooltipText(String text) {
    m_menu.setTooltipText(text);
  }

  @Override
  public boolean isSelected() {
    return m_menu.isSelected();
  }

  @Override
  public void setSelected(boolean b) {
    m_menu.setSelected(b);
  }

  @Override
  public boolean isEnabled() {
    return m_menu.isEnabled();
  }

  @Override
  public void setEnabled(boolean b) {
    m_menu.setEnabled(b);
  }

  @Override
  public boolean isVisible() {
    return m_menu.isVisible();
  }

  @Override
  public void setVisible(boolean b) {
    m_menu.setVisible(b);
  }

  @Override
  public boolean isInheritAccessibility() {
    return m_menu.isInheritAccessibility();
  }

  @Override
  public void setInheritAccessibility(boolean b) {
    m_menu.setInheritAccessibility(b);
  }

  @Override
  public void setEnabledPermission(Permission p) {
    m_menu.setEnabledPermission(p);
  }

  @Override
  public boolean isEnabledGranted() {
    return m_menu.isEnabledGranted();
  }

  @Override
  public void setEnabledGranted(boolean b) {
    m_menu.setEnabledGranted(b);
  }

  @Override
  public boolean isEnabledProcessingAction() {
    return m_menu.isEnabledProcessingAction();
  }

  @Override
  public void setEnabledProcessingAction(boolean b) {
    m_menu.setEnabledProcessingAction(b);
  }

  @Override
  public void setVisiblePermission(Permission p) {
    m_menu.setVisiblePermission(p);
  }

  @Override
  public boolean isVisibleGranted() {
    return m_menu.isVisibleGranted();
  }

  @Override
  public void setVisibleGranted(boolean b) {
    m_menu.setVisibleGranted(b);
  }

  @Override
  public boolean isSingleSelectionAction() {
    return m_menu.isSingleSelectionAction();
  }

  @Override
  public void setSingleSelectionAction(boolean b) {
    m_menu.setSingleSelectionAction(b);
  }

  @Override
  public boolean isMultiSelectionAction() {
    return m_menu.isMultiSelectionAction();
  }

  @Override
  public void setMultiSelectionAction(boolean b) {
    m_menu.setMultiSelectionAction(b);
  }

  @Override
  public boolean isEmptySpaceAction() {
    return m_menu.isEmptySpaceAction();
  }

  @Override
  public void setEmptySpaceAction(boolean b) {
    m_menu.setEmptySpaceAction(b);
  }

  @Override
  public boolean isToggleAction() {
    return m_menu.isToggleAction();
  }

  @Override
  public void setToggleAction(boolean b) {
    m_menu.setToggleAction(b);
  }

  @Override
  public char getMnemonic() {
    return m_menu.getMnemonic();
  }

  @Override
  public void prepareAction() {
    m_menu.prepareAction();
  }

  @Override
  public IActionUIFacade getUIFacade() {
    return m_menu.getUIFacade();
  }

  @Override
  public int acceptVisitor(IActionVisitor visitor) {
    return m_menu.acceptVisitor(visitor);
  }

  @Override
  public boolean isThisAndParentsEnabled() {
    return m_menu.isThisAndParentsEnabled();
  }

  @Override
  public boolean isThisAndParentsVisible() {
    return m_menu.isThisAndParentsVisible();
  }
}
