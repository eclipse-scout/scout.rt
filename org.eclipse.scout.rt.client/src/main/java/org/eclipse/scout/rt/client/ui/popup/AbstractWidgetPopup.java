/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.popup;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

import java.util.Collections;
import java.util.List;

/**
 * @since 9.0
 */
@ClassId("4ec758a3-178f-4de9-bc56-a4f28f801df1")
public abstract class AbstractWidgetPopup<T extends IWidget> extends AbstractPopup implements IWidgetPopup<T> {

  private T m_widget;

  public AbstractWidgetPopup() {
    this(true);
  }

  public AbstractWidgetPopup(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setClosable(getConfiguredClosable());
    setMovable(getConfiguredMovable());
    setResizable(getConfiguredResizable());
    setWidget(createWidget());
    if (getWidget() instanceof ICompositeField) {
      FormUtility.rebuildFieldGrid((ICompositeField) getWidget());
    }
  }

  protected boolean getConfiguredClosable() {
    return false;
  }

  @Override
  public void setClosable(boolean closable) {
    propertySupport.setPropertyBool(PROP_CLOSABLE, closable);
  }

  @Override
  public boolean isClosable() {
    return propertySupport.getPropertyBool(PROP_CLOSABLE);
  }

  protected boolean getConfiguredMovable() {
    return false;
  }

  @Override
  public void setMovable(boolean movable) {
    propertySupport.setPropertyBool(PROP_MOVABLE, movable);
  }

  @Override
  public boolean isMovable() {
    return propertySupport.getPropertyBool(PROP_MOVABLE);
  }

  protected boolean getConfiguredResizable() {
    return false;
  }

  @Override
  public void setResizable(boolean enabled) {
    propertySupport.setPropertyBool(PROP_RESIZABLE, enabled);
  }

  @Override
  public boolean isResizable() {
    return propertySupport.getPropertyBool(PROP_RESIZABLE);
  }

  protected Class<T> getConfiguredWidget() {
    return null;
  }

  protected T createWidget() {
    Class<T> configuredWidget = getConfiguredWidget();
    if (configuredWidget != null) {
      return ConfigurationUtility.newInnerInstance(this, configuredWidget);
    }
    return null;
  }

  @Override
  public T getWidget() {
    return m_widget;
  }

  public void setWidget(T widget) {
    m_widget = widget;
  }

  @Override
  protected boolean getConfiguredAnimateOpening() {
    return true;
  }

  @Override
  protected boolean getConfiguredAnimateResize() {
    return true;
  }

  @Override
  public void open() {
    init();
    super.open();
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getWidget()));
  }
}
