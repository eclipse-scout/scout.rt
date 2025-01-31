/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableComputeTableEmptySpaceMenusChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableCreateChildPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableInitSearchFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableLoadDataChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTablePopulateTableChain;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public interface IPageWithTableExtension<T extends ITable, OWNER extends AbstractPageWithTable<T>> extends IPageExtension<OWNER> {

  void execLoadData(PageWithTableLoadDataChain<? extends ITable> chain, SearchFilter filter);

  IPage<?> execCreateChildPage(PageWithTableCreateChildPageChain<? extends ITable> chain, ITableRow row);

  void execPopulateTable(PageWithTablePopulateTableChain<? extends ITable> chain);

  void execInitSearchForm(PageWithTableInitSearchFormChain<? extends ITable> chain);

  List<IMenu> execComputeTableEmptySpaceMenus(PageWithTableComputeTableEmptySpaceMenusChain<? extends ITable> chain);
}
