/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableCreateChildPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableCreateVirtualChildPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableInitSearchFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableLoadDataChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTablePopulateTableChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public interface IPageWithTableExtension<T extends ITable, OWNER extends AbstractPageWithTable<T>> extends IPageExtension<OWNER> {

  void execLoadData(PageWithTableLoadDataChain<? extends ITable> chain, SearchFilter filter);

  IPage<?> execCreateChildPage(PageWithTableCreateChildPageChain<? extends ITable> chain, ITableRow row);

  void execPopulateTable(PageWithTablePopulateTableChain<? extends ITable> chain);

  IPage<?> execCreateVirtualChildPage(PageWithTableCreateVirtualChildPageChain<? extends ITable> chain, ITableRow row);

  void execInitSearchForm(PageWithTableInitSearchFormChain<? extends ITable> chain);
}
