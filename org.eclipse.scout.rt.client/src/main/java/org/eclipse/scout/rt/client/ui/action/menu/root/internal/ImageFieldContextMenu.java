/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
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

@ClassId("ea5a9db8-b80c-4795-9e33-187a1750dc48")
public class ImageFieldContextMenu extends FormFieldContextMenu<IImageField> implements IFormFieldContextMenu {

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

  protected void handleOwnerValueChanged() {
    IImageField container = getContainer();
    if (container != null) {
      final Object ownerValue = new CompositeObject(container.getImageId(), container.getImageUrl(), container.getImage());
      setCurrentMenuTypes(getMenuTypesForValues());
      if (container.isInitConfigDone()) {
        visit(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()), IMenu.class);
      }
    }
    calculateLocalVisibility();
  }

  @Override
  protected boolean isOwnerPropertyChangedListenerRequired() {
    return true;
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    super.handleOwnerPropertyChanged(evt);
    String propertyName = evt.getPropertyName();
    if (IImageField.PROP_IMAGE_ID.equals(propertyName) ||
        IImageField.PROP_IMAGE_URL.equals(propertyName) ||
        IImageField.PROP_IMAGE.equals(propertyName)) {
      handleOwnerValueChanged();
    }
  }
}
