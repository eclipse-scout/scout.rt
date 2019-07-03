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

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.classid.ClassId;

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
    setWidget(createWidget());
    if (getWidget() instanceof ICompositeField) {
      FormUtility.rebuildFieldGrid((ICompositeField) getWidget());
    }
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
