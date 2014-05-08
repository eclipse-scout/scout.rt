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

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 *
 */
public class AbstractValueFieldMenu extends AbstractMenu implements IValueFieldMenu {

  @SuppressWarnings("deprecation")
  @Override
  protected final boolean getConfiguredSingleSelectionAction() {
    return super.getConfiguredSingleSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected final boolean getConfiguredMultiSelectionAction() {
    return super.getConfiguredMultiSelectionAction();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected final boolean getConfiguredEmptySpaceAction() {
    return super.getConfiguredEmptySpaceAction();
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredNullValueMenu() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  protected boolean getConfiguredNotNullValueMenu() {
    return true;
  }

  @Override
  protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
    boolean visible = false;
    if (isNullValueMenu()) {
      visible = newOwnerValue == null;
    }
    if (!visible && isNotNullValueMenu()) {
      visible = newOwnerValue != null;
    }
    setVisible(visible);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setNotNullValueMenu(getConfiguredNotNullValueMenu());
    setNullValueMenu(getConfiguredNullValueMenu());
  }

  @Override
  public IValueField<?> getOwner() {
    return (IValueField<?>) super.getOwner();
  }

  @Override
  public void setOwnerInternal(IPropertyObserver owner) {
    if (owner instanceof IValueField<?>) {
      super.setOwnerInternal(owner);
    }
    else {
      throw new IllegalArgumentException("menu owner must be an instance of IValueField.");
    }
  }

  @Override
  public void setNotNullValueMenu(boolean notNullValueMenu) {
    propertySupport.setPropertyBool(PROP_NOT_NULL_VALUE_MENU, notNullValueMenu);
  }

  @Override
  public boolean isNotNullValueMenu() {
    return propertySupport.getPropertyBool(PROP_NOT_NULL_VALUE_MENU);
  }

  @Override
  public void setNullValueMenu(boolean nullValueMenu) {
    propertySupport.setPropertyBool(PROP_NULL_VALUE_MENU, nullValueMenu);
  }

  @Override
  public boolean isNullValueMenu() {
    return propertySupport.getPropertyBool(PROP_NULL_VALUE_MENU);
  }

}
