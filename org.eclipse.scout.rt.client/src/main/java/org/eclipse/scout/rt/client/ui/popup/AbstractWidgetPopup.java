/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.popup;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 9.0
 */
@ClassId("4ec758a3-178f-4de9-bc56-a4f28f801df1")
public abstract class AbstractWidgetPopup<T extends IWidget> extends AbstractPopup implements IWidgetPopup<T> {

  private T m_content;

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
    setContent(createContent());
    if (getContent() instanceof ICompositeField) {
      FormUtility.rebuildFieldGrid((ICompositeField) getContent());
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

  protected Class<T> getConfiguredContent() {
    return null;
  }

  protected T createContent() {
    Class<T> configuredContent = getConfiguredContent();
    if (configuredContent != null) {
      return ConfigurationUtility.newInnerInstance(this, configuredContent);
    }
    return null;
  }

  @Override
  public T getContent() {
    return m_content;
  }

  public void setContent(T content) {
    m_content = content;
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
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getContent()));
  }
}
