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
import org.eclipse.scout.rt.spec.client.config.entity.DefaultCodeTypeTypesTableConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultColumnTableConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultFormConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultFormFieldTableConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultMenuTableConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultSmartFieldConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultTableFieldConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultTablePageConfig;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultTypesTableConfig;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityConfig;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityTableConfig;

/**
 * A default template that should be possible to use for most projects.
 */
public class DefaultDocConfig implements IDocConfig {

  private static final String DEFAULT_INDENT = "&nbsp;&nbsp;&nbsp;";

  private IDocEntityConfig<IForm> m_formConfig;
  private IDocEntityTableConfig<IFormField> m_formFieldTableConfig;
  private IDocEntityTableConfig<IColumn<?>> m_columnTableConfig;
  private IDocEntityTableConfig<IMenu> m_menuTableConfig;
  private IDocEntityConfig<ITableField<? extends ITable>> m_tableFieldConfig;
  private IDocEntityConfig<ISmartField<?>> m_smartFieldConfig;
  private IDocEntityConfig<IPageWithTable<? extends ITable>> m_tablePageConfig;
  private IDocEntityTableConfig<Class<?>> m_genericTypesTableConfig;
  private IDocEntityTableConfig<Class<?>> m_codeTypeTypesTableConfig;
  private int m_defaultTopHeadingLevel;
  private String m_indent;

  public DefaultDocConfig() {
    m_formFieldTableConfig = new DefaultFormFieldTableConfig();
    m_formConfig = new DefaultFormConfig();
    m_columnTableConfig = new DefaultColumnTableConfig();
    m_menuTableConfig = new DefaultMenuTableConfig();
    m_tableFieldConfig = new DefaultTableFieldConfig();
    m_smartFieldConfig = new DefaultSmartFieldConfig();
    m_tablePageConfig = new DefaultTablePageConfig();
    m_genericTypesTableConfig = new DefaultTypesTableConfig();
    m_codeTypeTypesTableConfig = new DefaultCodeTypeTypesTableConfig();
    m_defaultTopHeadingLevel = 2;
    m_indent = DEFAULT_INDENT;
  }

  @Override
  public IDocEntityConfig<IForm> getFormConfig() {
    return m_formConfig;
  }

  @Override
  public IDocEntityTableConfig<IFormField> getFormFieldTableConfig() {
    return m_formFieldTableConfig;
  }

  @Override
  public IDocEntityTableConfig<IColumn<?>> getColumnTableConfig() {
    return m_columnTableConfig;
  }

  @Override
  public IDocEntityTableConfig<IMenu> getMenuTableConfig() {
    return m_menuTableConfig;
  }

  @Override
  public IDocEntityConfig<ITableField<? extends ITable>> getTableFieldConfig() {
    return m_tableFieldConfig;
  }

  @Override
  public IDocEntityConfig<ISmartField<?>> getSmartFieldConfig() {
    return m_smartFieldConfig;
  }

  @Override
  public IDocEntityConfig<IPageWithTable<? extends ITable>> getTablePageConfig() {
    return m_tablePageConfig;
  }

  @Override
  public IDocEntityTableConfig<Class<?>> getGenericTypesTableConfig() {
    return m_genericTypesTableConfig;
  }

  @Override
  public IDocEntityTableConfig<Class<?>> getCodeTypeTypesTableConfig() {
    return m_codeTypeTypesTableConfig;
  }

  @Override
  public int getDefaultTopHeadingLevel() {
    return m_defaultTopHeadingLevel;
  }

  @Override
  public void setFormConfig(IDocEntityConfig<IForm> formConfig) {
    m_formConfig = formConfig;
  }

  @Override
  public void setFormFieldTableConfig(IDocEntityTableConfig<IFormField> formFieldTableConfig) {
    m_formFieldTableConfig = formFieldTableConfig;
  }

  @Override
  public void setColumnTableConfig(IDocEntityTableConfig<IColumn<?>> columnTableConfig) {
    m_columnTableConfig = columnTableConfig;
  }

  @Override
  public void setMenuTableConfig(IDocEntityTableConfig<IMenu> menuTableConfig) {
    m_menuTableConfig = menuTableConfig;
  }

  @Override
  public void setTableFieldConfig(IDocEntityConfig<ITableField<? extends ITable>> tableFieldConfig) {
    m_tableFieldConfig = tableFieldConfig;
  }

  @Override
  public void setSmartFieldConfig(IDocEntityConfig<ISmartField<?>> smartFieldConfig) {
    m_smartFieldConfig = smartFieldConfig;
  }

  @Override
  public void setTablePageConfig(IDocEntityConfig<IPageWithTable<? extends ITable>> tablePageConfig) {
    m_tablePageConfig = tablePageConfig;
  }

  @Override
  public void setGenericTypesTableConfig(IDocEntityTableConfig<Class<?>> genericTypesTableConfig) {
    m_genericTypesTableConfig = genericTypesTableConfig;
  }

  @Override
  public void setCodeTypeTypesTableConfig(IDocEntityTableConfig<Class<?>> codeTypeTypesTableConfig) {
    m_codeTypeTypesTableConfig = codeTypeTypesTableConfig;
  }

  @Override
  public void setTopHeadingLevel(int topHeadingLevel) {
    m_defaultTopHeadingLevel = topHeadingLevel;
  }

  @Override
  public String getIndent() {
    return m_indent;
  }

  @Override
  public void setIndent(String indent) {
    m_indent = indent;
  }

}
