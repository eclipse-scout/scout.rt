package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithNodesChains.PageWithNodesCreateChildPagesChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public abstract class AbstractPageWithNodesExtension<OWNER extends AbstractPageWithNodes> extends AbstractPageExtension<OWNER> implements IPageWithNodesExtension<OWNER> {

  public AbstractPageWithNodesExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execCreateChildPages(PageWithNodesCreateChildPagesChain chain, List<IPage<?>> pageList) {
    chain.execCreateChildPages(pageList);
  }
}
