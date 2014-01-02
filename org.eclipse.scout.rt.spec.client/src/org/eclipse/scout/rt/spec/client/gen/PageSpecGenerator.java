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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;

/**
 * Creates Specification data for a page
 */
public class PageSpecGenerator {
  private final IDocConfig m_config;

  public PageSpecGenerator(IDocConfig config) {
    m_config = config;
  }

  public IDocSection getDocSection(IPageWithTable<ITable> page) {
//    m_config.getPageConfig();
//    IColumn<?>[] columns = page.getTable().getColumns();
//    IMenu[] menus = page.getTable().getMenus();
//    IDocSection columnSection = DocGenUtility.createDocSection(columns, m_config.getColumnConfig());
//    IDocSection menuSection = DocGenUtility.createDocSection(menus, m_config.getMenuConfig());
    //general form info
    return null;
  }
}
