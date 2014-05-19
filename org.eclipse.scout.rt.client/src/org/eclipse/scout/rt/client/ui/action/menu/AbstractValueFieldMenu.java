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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 *
 */
public class AbstractValueFieldMenu extends AbstractMenu implements IValueFieldMenu {

  private boolean m_skipCalculateAvailability;

  public AbstractValueFieldMenu() {
    super();
  }

  public AbstractValueFieldMenu(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigProperty(ConfigProperty.VALUE_FIELD_MENU_TYPE)
  @Order(140)
  protected EnumSet<ValueFieldMenuType> getConfiguredMenuType() {
    return EnumSet.<ValueFieldMenuType> of(ValueFieldMenuType.NotEmpty);
  }

  @Override
  protected void calculateAvailability(Object newOwnerValue) {
    if (m_skipCalculateAvailability) {
      return;
    }
    if (hasChildActions()) {
      setAvailableInternal(true);
      return;
    }
    boolean available = false;
    available |= newOwnerValue == null && getMenuType().contains(ValueFieldMenuType.Empty);
    available |= newOwnerValue != null && getMenuType().contains(ValueFieldMenuType.NotEmpty);
    setAvailableInternal(available);
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void initConfig() {
    // guard to ensure calculate availability is not called when only  legacy from super type is initialized
    try {
      m_skipCalculateAvailability = true;
      super.initConfig();
    }
    finally {
      m_skipCalculateAvailability = false;
    }
    if (!ConfigurationUtility.isMethodOverwrite(AbstractValueFieldMenu.class, "getConfiguredMenuType", new Class[0], this.getClass())) {
      // legacy
      Set<ValueFieldMenuType> menuType = new HashSet<ValueFieldMenuType>();
      if (isSingleSelectionAction()) {
        menuType.add(ValueFieldMenuType.NotEmpty);
      }
      if (isMultiSelectionAction()) {
        menuType.add(ValueFieldMenuType.NotEmpty);
      }
      if (isEmptySpaceAction()) {
        menuType.add(ValueFieldMenuType.Empty);
      }
      EnumSet<ValueFieldMenuType> menuTypeEnumSet = EnumSet.<ValueFieldMenuType> copyOf(menuType);
      setMenuType(menuTypeEnumSet);
    }
    else {
      setMenuType(getConfiguredMenuType());
    }
    calculateAvailability(null);
  }

  @Override
  public IValueField<?> getOwner() {
    return (IValueField<?>) super.getOwner();
  }

  @Override
  public void setOwnerInternal(IPropertyObserver owner) {
    if (owner == null || owner instanceof IValueField<?>) {
      super.setOwnerInternal(owner);
    }
    else {
      throw new IllegalArgumentException("menu owner must be an instance of IValueField.");
    }
  }

  public void setMenuType(EnumSet<ValueFieldMenuType> menuType) {
    propertySupport.setProperty(PROP_MENU_TYPE, menuType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EnumSet<ValueFieldMenuType> getMenuType() {
    return (EnumSet<ValueFieldMenuType>) propertySupport.getProperty(PROP_MENU_TYPE);
  }

}
