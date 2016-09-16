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
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IValueFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("c07663cf-fbe6-4514-8653-81e547b58445")
@SuppressWarnings("bsiRulesDefinition:orderMissing")
public class ValueFieldContextMenu extends FormFieldContextMenu<IValueField<?>> implements IValueFieldContextMenu {

  /**
   * @param owner
   */
  public ValueFieldContextMenu(IValueField owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // init current menu types
    setCurrentMenuTypes(MenuUtility.getMenuTypesForValueFieldValue(getContainer().getValue()));
    calculateLocalVisibility();
  }

  @Override
  protected void afterChildMenusAdd(Collection<? extends IMenu> newChildMenus) {
    super.afterChildMenusAdd(newChildMenus);
    handleOwnerEnabledChanged();
  }

  @Override
  protected void afterChildMenusRemove(Collection<? extends IMenu> childMenusToRemove) {
    super.afterChildMenusRemove(childMenusToRemove);
    handleOwnerEnabledChanged();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    IValueField<?> container = getContainer();
    if (container != null) {
      final Object ownerValue = container.getValue();
      setCurrentMenuTypes(MenuUtility.getMenuTypesForValueFieldValue(ownerValue));
      acceptVisitor(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()));
    }
    calculateLocalVisibility();
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    super.handleOwnerPropertyChanged(evt);
    if (IValueField.PROP_VALUE.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
  }

}
