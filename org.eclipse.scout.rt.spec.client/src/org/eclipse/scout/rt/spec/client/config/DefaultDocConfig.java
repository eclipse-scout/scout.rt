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
package org.eclipse.scout.rt.spec.client.config;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultColumnConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultFormConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultFormFieldConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultMenuConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultSmartFieldConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultTableFieldConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultTablePageConfig;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityConfig;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityListConfig;

/**
 * A default template that should be possible to use for most projects.
 */
public class DefaultDocConfig implements IDocConfig {

  @Override
  public IDocEntityConfig<IForm> getFormConfig() {
    return new DefaultFormConfig();
  }

  @Override
  public IDocEntityListConfig<IFormField> getFieldListConfig() {
    return new DefaultFormFieldConfig();
  }

  @Override
  public IDocEntityListConfig<IColumn<?>> getColumnConfig() {
    return new DefaultColumnConfig();
  }

  @Override
  public IDocEntityListConfig<IMenu> getMenuConfig() {
    return new DefaultMenuConfig();
  }

  @Override
  public IDocEntityConfig<ITableField<? extends ITable>> getTableFieldConfig() {
    return new DefaultTableFieldConfig();
  }

  @Override
  public IDocEntityConfig<ISmartField<?>> getSmartFieldConfig() {
    return new DefaultSmartFieldConfig();
  }

  @Override
  public IDocEntityConfig<IPageWithTable<? extends ITable>> getTablePageConfig() {
    return new DefaultTablePageConfig();
  }

}
