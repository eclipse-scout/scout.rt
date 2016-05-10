package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithTableChains.PageWithTableInitSearchFormChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.BEANS;

public class PageWithTableExtension extends AbstractPageWithTableExtension<ITable, AbstractPageWithTable<ITable>> {

  public PageWithTableExtension(AbstractPageWithTable<ITable> owner) {
    super(owner);
  }

  @Override
  public void execInitSearchForm(PageWithTableInitSearchFormChain<? extends ITable> chain) {
    super.execInitSearchForm(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().notifyPageSearchFormInit(getOwner());
  }

}
