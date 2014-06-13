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
package org.eclipse.scout.rt.spec.client.gen;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.IDocTable;
import org.eclipse.scout.rt.spec.client.out.internal.Section;

/**
 * Creates Specification data for a page
 */
public class PageSpecGenerator {
  private final IDocConfig m_config;

  public PageSpecGenerator(IDocConfig config) {
    m_config = config;
  }

  public IDocSection getDocSection(IPageWithTable<? extends ITable> page) {
    IDocEntityConfig<IPageWithTable<? extends ITable>> tablePageConfig = m_config.getTablePageConfig();

    IDocTable tableSpec = DocGenUtility.createDocTable(page, tablePageConfig, true);

    List<IMenu> menus = page.getTable().getMenus();
    IDocSection menuSection = DocGenUtility.createDocSection(menus, m_config.getMenuTableConfig(), false);

    List<IColumn<?>> columns = page.getTable().getColumns();
    IDocSection columnSection = DocGenUtility.createDocSection(columns, m_config.getColumnTableConfig(), false);

    String title = tablePageConfig.getTitleExtractor().getText(page);
    return new Section(title, tableSpec, columnSection, menuSection);
  }
}
