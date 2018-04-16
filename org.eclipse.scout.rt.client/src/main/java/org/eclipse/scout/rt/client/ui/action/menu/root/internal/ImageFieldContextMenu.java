/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.ImageFieldMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.IImageField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CompositeObject;

@ClassId("c07663cf-fbe6-4514-8653-81e547b58445")
public class ImageFieldContextMenu extends FormFieldContextMenu<IImageField> implements IFormFieldContextMenu {

  /**
   * @param owner
   */
  public ImageFieldContextMenu(IImageField owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // init current menu types
    setCurrentMenuTypes(getMenuTypesForValues());
    calculateLocalVisibility();
  }

  protected Set<IMenuType> getMenuTypesForValues() {
    IImageField field = getContainer();
    Set<IMenuType> menuTypes = new HashSet<>();
    if (field.getImageId() != null) {
      menuTypes.add(ImageFieldMenuType.ImageId);
    }
    if (field.getImageUrl() != null) {
      menuTypes.add(ImageFieldMenuType.ImageUrl);
    }
    if (field.getImage() != null) {
      menuTypes.add(ImageFieldMenuType.Image);
    }
    if (menuTypes.isEmpty()) {
      return Collections.singleton(ImageFieldMenuType.Null);
    }
    else {
      return menuTypes;
    }
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

  protected void handleOwnerValueChanged() {
    IImageField container = getContainer();
    if (container != null) {
      final Object ownerValue = new CompositeObject(container.getImageId(), container.getImageUrl(), container.getImage());
      setCurrentMenuTypes(getMenuTypesForValues());
      visit(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()), IMenu.class);
    }
    calculateLocalVisibility();
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();
    if (IImageField.PROP_IMAGE_ID.equals(propertyName) ||
        IImageField.PROP_IMAGE_URL.equals(propertyName) ||
        IImageField.PROP_IMAGE.equals(propertyName)) {
      handleOwnerValueChanged();
    }
    else {
      super.handleOwnerPropertyChanged(evt);
    }
  }

}
