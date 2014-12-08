package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageWithNodesChains.PageWithNodesCreateChildPagesChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public interface IPageWithNodesExtension<OWNER extends AbstractPageWithNodes> extends IPageExtension<OWNER> {

  void execCreateChildPages(PageWithNodesCreateChildPagesChain chain, List<IPage> pageList) throws ProcessingException;
}
