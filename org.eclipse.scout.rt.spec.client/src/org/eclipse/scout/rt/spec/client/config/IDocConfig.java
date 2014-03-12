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
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityConfig;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityTableConfig;

/**
 * A template for describing the configuration of the generated documentation.
 * <p>
 * Describes for each scout model object (e.g. for {@link IForm}, {@link IFormField}, {@link IColumn}) which kind of
 * text is as documentation.
 * </p>
 * <p>
 * Describes what should be used as title for each generated scout model object.
 * </p>
 */
public interface IDocConfig {

  /**
   * Configuration for documenting {@link IForm}.
   */
  IDocEntityConfig<IForm> getFormConfig();

  /**
   * Configuration for documenting {@link IColumn}.
   */
  IDocEntityTableConfig<IColumn<?>> getColumnTableConfig();

  /**
   * Configuration for documenting {@link IMenu}.
   */
  IDocEntityTableConfig<IMenu> getMenuTableConfig();

  /**
   * Configuration for documenting {@link ITableField}.
   */
  IDocEntityConfig<ITableField<? extends ITable>> getTableFieldConfig();

  /**
   * Configuration for documenting {@link ISmartField}.
   */
  IDocEntityConfig<ISmartField<?>> getSmartFieldConfig();

  /**
   * Configuration for documenting {@link Field}s.
   */
  IDocEntityTableConfig<IFormField> getFormFieldTableConfig();

  /**
   * Configuration for table page {@link IPageWithTable}.
   */
  IDocEntityConfig<IPageWithTable<? extends ITable>> getTablePageConfig();

  /**
   * Configuration suitable for different types (eg. field types, column types, ...)
   */
  IDocEntityTableConfig<Class<?>> getGenericTypesTableConfig();

  /**
   * Configuration for CodeTypes
   */
  IDocEntityTableConfig<Class<?>> getCodeTypeTypesTableConfig();

  /**
   * @return default top heading level for form spec
   */
  int getDefaultTopHeadingLevel();

  /**
   * setter for {@link #getDefaultTopHeadingLevel()}
   * 
   * @param topHeadingLevel
   */
  void setTopHeadingLevel(int topHeadingLevel);

  /**
   * setter for {@link #getCodeTypeTypesTableConfig()}
   * 
   * @param codeTypeTypesTableConfig
   */
  void setCodeTypeTypesTableConfig(IDocEntityTableConfig<Class<?>> codeTypeTypesTableConfig);

  /**
   * setter for {@link #getGenericTypesTableConfig()}
   * 
   * @param genericTypesTableConfig
   */
  void setGenericTypesTableConfig(IDocEntityTableConfig<Class<?>> genericTypesTableConfig);

  /**
   * setter for {@link #getTablePageConfig()}
   * 
   * @param tablePageConfig
   */
  void setTablePageConfig(IDocEntityConfig<IPageWithTable<? extends ITable>> tablePageConfig);

  /**
   * @param smartFieldConfig
   */
  void setSmartFieldConfig(IDocEntityConfig<ISmartField<?>> smartFieldConfig);

  /**
   * setter for {@link #getTableFieldConfig()}
   * 
   * @param tableFieldConfig
   */
  void setTableFieldConfig(IDocEntityConfig<ITableField<? extends ITable>> tableFieldConfig);

  /**
   * setter for {@link #getMenuTableConfig()}
   * 
   * @param menuTableConfig
   */
  void setMenuTableConfig(IDocEntityTableConfig<IMenu> menuTableConfig);

  /**
   * setter for {@link #getColumnTableConfig()}
   * 
   * @param columnTableConfig
   */
  void setColumnTableConfig(IDocEntityTableConfig<IColumn<?>> columnTableConfig);

  /**
   * setter for {@link #getFormFieldTableConfig()}
   * 
   * @param formFieldTableConfig
   */
  void setFormFieldTableConfig(IDocEntityTableConfig<IFormField> formFieldTableConfig);

  /**
   * setter for {@link #getFormConfig()}
   * 
   * @param formConfig
   */
  void setFormConfig(IDocEntityConfig<IForm> formConfig);

  /**
   * @return String for indentation inside table cell. This String needs to be repeated for each indent level.
   */
  String getIndent();

  /**
   * setter for {@link #getIndent()}
   * 
   * @param indent
   */
  void setIndent(String indent);

}
