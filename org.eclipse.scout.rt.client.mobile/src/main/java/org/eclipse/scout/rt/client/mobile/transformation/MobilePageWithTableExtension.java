package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableCreateChildPageChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

public class MobilePageWithTableExtension extends AbstractPageWithTableExtension<ITable, AbstractPageWithTable<ITable>> {

  public MobilePageWithTableExtension(AbstractPageWithTable<ITable> owner) {
    super(owner);
  }

  @Override
  public IPage<?> execCreateChildPage(PageWithTableCreateChildPageChain<? extends ITable> chain, ITableRow row) {
    IPage<?> page = chain.execCreateChildPage(row);
    if (page instanceof IPageWithTable) {
      IPageWithTable pageWithTable = (IPageWithTable) page;
      pageWithTable.setLeaf(false);
      pageWithTable.setAlwaysCreateChildPage(true);
    }
    return page;
  }

}
