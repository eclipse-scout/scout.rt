/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;

public abstract class AbstractStatusMenuMapping extends AbstractPropertyObserver implements IStatusMenuMapping {
  private Class<? extends IMenu> m_menuClass;
  private IFormField m_parentField;

  public AbstractStatusMenuMapping() {
    this(true);
  }

  public AbstractStatusMenuMapping(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    initConfig();
  }

  protected void initConfig() {
    m_menuClass = getConfiguredMenu();
    setCodes(getConfiguredCodes());
    setSeverities(getConfiguredSeverities());
  }

  @Override
  public void setParentFieldInternal(IFormField parentField) {
    m_parentField = parentField;
  }

  @Override
  public void init() {
    if (m_menuClass != null) {
      setMenu(m_parentField.getMenuByClass(m_menuClass));
    }
  }

  @Override
  public IMenu getMenu() {
    return (IMenu) propertySupport.getProperty(PROP_MENU);
  }

  @Override
  public void setMenu(IMenu menu) {
    propertySupport.setProperty(PROP_MENU, menu);
  }

  @Order(10)
  protected Class<? extends IMenu> getConfiguredMenu() {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Integer> getCodes() {
    return (List<Integer>) propertySupport.getProperty(PROP_CODES);
  }

  @Override
  public void setCodes(List<Integer> codes) {
    propertySupport.setProperty(PROP_CODES, new ArrayList<>(codes));
  }

  /**
   * Configures the codes the status should have to display the menu. If an empty list is returned, the menu is shown
   * for every code.
   */
  @Order(20)
  protected List<Integer> getConfiguredCodes() {
    return new ArrayList<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Integer> getSeverities() {
    return (List<Integer>) propertySupport.getProperty(PROP_SEVERITIES);
  }

  @Override
  public void setSeverities(List<Integer> severities) {
    propertySupport.setProperty(PROP_SEVERITIES, new ArrayList<>(severities));
  }

  /**
   * Configures the severities the status should have to display the menu. If an empty list is returned, the menu is
   * shown for every severity.
   */
  @Order(30)
  protected List<Integer> getConfiguredSeverities() {
    return new ArrayList<>();
  }
}
