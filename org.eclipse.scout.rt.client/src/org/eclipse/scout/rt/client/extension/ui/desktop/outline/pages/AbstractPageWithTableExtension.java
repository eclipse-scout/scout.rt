package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import org.eclipse.scout.commons.exception.ProcessingException;
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

public abstract class AbstractPageWithTableExtension<T extends ITable, OWNER extends AbstractPageWithTable<T>> extends AbstractPageExtension<OWNER> implements IPageWithTableExtension<T, OWNER> {

  public AbstractPageWithTableExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execLoadData(PageWithTableLoadDataChain<? extends ITable> chain, SearchFilter filter) throws ProcessingException {
    chain.execLoadData(filter);
  }

  @Override
  public IPage execCreateChildPage(PageWithTableCreateChildPageChain<? extends ITable> chain, ITableRow row) throws ProcessingException {
    return chain.execCreateChildPage(row);
  }

  @Override
  public void execPopulateTable(PageWithTablePopulateTableChain<? extends ITable> chain) throws ProcessingException {
    chain.execPopulateTable();
  }

  @Override
  public IPage execCreateVirtualChildPage(PageWithTableCreateVirtualChildPageChain<? extends ITable> chain, ITableRow row) throws ProcessingException {
    return chain.execCreateVirtualChildPage(row);
  }

  @Override
  public void execInitSearchForm(PageWithTableInitSearchFormChain<? extends ITable> chain) throws ProcessingException {
    chain.execInitSearchForm();
  }
}
