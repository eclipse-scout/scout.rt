package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateChildPagesChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public abstract class AbstractOutlineExtension<OWNER extends AbstractOutline> extends AbstractTreeExtension<OWNER> implements IOutlineExtension<OWNER> {

  public AbstractOutlineExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execCreateChildPages(OutlineCreateChildPagesChain chain, List<IPage> pageList) throws ProcessingException {
    chain.execCreateChildPages(pageList);
  }
}
