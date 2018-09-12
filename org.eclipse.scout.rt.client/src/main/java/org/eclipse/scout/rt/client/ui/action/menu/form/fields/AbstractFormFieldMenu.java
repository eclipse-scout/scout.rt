/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu.form.fields;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("eb87dee2-72ae-4a15-90d7-ce3b38beb10a")
public abstract class AbstractFormFieldMenu extends AbstractMenu implements IFormFieldMenu {

  private IFormField m_field;

  public AbstractFormFieldMenu(boolean callInitializer) {
    super(callInitializer);
  }

  public AbstractFormFieldMenu() {
    super();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    Class<? extends IFormField> fieldClass = getConfiguredField();
    if (fieldClass != null) {
      m_field = ConfigurationUtility.newInnerInstance(this, fieldClass);
      m_field.setGridDataInternal(m_field.getGridDataHints());
    }
  }

  protected Class<? extends IFormField> getConfiguredField() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClassIgnoringInjectFieldAnnotation(dca, IFormField.class);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(m_field));
  }

  @Override
  public IFormField getField() {
    return m_field;
  }
}
