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
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityListConfig;

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
  IDocEntityListConfig<IColumn<?>> getColumnConfig();

  /**
   * Configuration for documenting {@link IMenu}.
   */
  IDocEntityListConfig<IMenu> getMenuConfig();

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
  IDocEntityListConfig<IFormField> getFieldListConfig();

  /**
   * Configuration for table page {@link IPageWithTable}.
   */
  IDocEntityConfig<IPageWithTable<? extends ITable>> getTablePageConfig();

  /**
   * Configuration for types (eg. field types, column types, ...)
   */
  IDocEntityListConfig<Class<?>> getTypesConfig();

}
