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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.FormFieldPropertyDelegator;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.Order;

/**
 * Group box which represents a {@link ITabBox} and contains a {@link TabBoxTableField} to represent the tab items.
 * 
 * @since 3.9.0
 */
public class TabBoxGroupBox extends AbstractGroupBox {
  private FormFieldPropertyDelegator<ITabBox, IGroupBox> m_propertyDelegator;

  public TabBoxGroupBox(ITabBox tabBox) {
    super(false);
    m_propertyDelegator = new FormFieldPropertyDelegator<ITabBox, IGroupBox>(tabBox, this);
    callInitializer();
    setFormInternal(tabBox.getForm());
    rebuildFieldGrid();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  protected void execInitField() {
    getTableField().initField();
  }

  @Override
  protected void execDisposeField() {
    super.execDisposeField();

    m_propertyDelegator.dispose();
  }

  public ITabBox getTabBox() {
    return m_propertyDelegator.getSender();
  }

  public TableField getTableField() {
    return getFieldByClass(TableField.class);
  }

  @Order(10)
  public class TableField extends TabBoxTableField {

    @Override
    protected ITabBox getConfiguredTabBox() {
      return TabBoxGroupBox.this.getTabBox();
    }

  }

}
