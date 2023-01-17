/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.view;

import org.eclipse.scout.rt.client.extension.ui.action.view.IViewButtonExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("87ad04db-8bf0-4eb4-866e-f547b632e020")
public abstract class AbstractViewButton extends AbstractAction implements IViewButton {

  public AbstractViewButton() {
    super();
  }

  public AbstractViewButton(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setDisplayStyle(getConfiguredDisplayStyle());
  }

  @Order(10)
  @ConfigProperty(ConfigProperty.OBJECT)
  protected DisplayStyle getConfiguredDisplayStyle() {
    return DisplayStyle.MENU;
  }

  @Override
  public DisplayStyle getDisplayStyle() {
    return (DisplayStyle) propertySupport.getProperty(PROP_DISPLAY_STYLE);
  }

  @Override
  public void setDisplayStyle(DisplayStyle displayStyle) {
    propertySupport.setProperty(PROP_DISPLAY_STYLE, displayStyle);
  }

  protected static class LocalViewButtonExtension<OWNER extends AbstractViewButton> extends LocalActionExtension<OWNER> implements IViewButtonExtension<OWNER> {

    public LocalViewButtonExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IViewButtonExtension<? extends AbstractViewButton> createLocalExtension() {
    return new LocalViewButtonExtension<>(this);
  }
}
