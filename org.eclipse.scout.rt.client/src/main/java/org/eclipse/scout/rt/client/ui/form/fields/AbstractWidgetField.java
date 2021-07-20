/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("74955146-e592-4dfa-ab58-25b0acf1c355")
public abstract class AbstractWidgetField<WIDGET extends IWidget> extends AbstractFormField implements IWidgetField<WIDGET> {

  public AbstractWidgetField() {
  }

  public AbstractWidgetField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setScrollable(getConfiguredScrollable());
    setFieldWidget(getConfiguredFieldWidget());
  }

  protected boolean getConfiguredScrollable() {
    return true;
  }

  @SuppressWarnings("unchecked")
  protected WIDGET getConfiguredFieldWidget() {
    IWidget contributed = CollectionUtility.firstElement(m_contributionHolder.getContributionsByClass(IWidget.class));
    if (contributed != null) {
      return (WIDGET) contributed;
    }
    Class<?>[] dpc = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<IWidget> configured = CollectionUtility.firstElement(ConfigurationUtility.filterClasses(dpc, IWidget.class));
    if (configured != null) {
      return (WIDGET) ConfigurationUtility.newInnerInstance(this, configured);
    }
    return null;
  }

  @Override
  public boolean isScrollable() {
    return propertySupport.getPropertyBool(PROP_SCROLLABLE);
  }

  @Override
  public void setScrollable(boolean scrollable) {
    propertySupport.setPropertyBool(PROP_SCROLLABLE, scrollable);
  }

  @SuppressWarnings("unchecked")
  @Override
  public WIDGET getFieldWidget() {
    return (WIDGET) propertySupport.getProperty(PROP_FIELD_WIDGET);
  }

  @Override
  public void setFieldWidget(WIDGET fieldWidget) {
    propertySupport.setProperty(PROP_FIELD_WIDGET, fieldWidget);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getFieldWidget()));
  }
}
