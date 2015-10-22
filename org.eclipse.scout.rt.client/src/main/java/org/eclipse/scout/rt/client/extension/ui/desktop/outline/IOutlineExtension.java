package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateChildPagesChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateRootPageChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public interface IOutlineExtension<OWNER extends AbstractOutline> extends ITreeExtension<OWNER> {

  void execCreateChildPages(OutlineCreateChildPagesChain chain, List<IPage<?>> pageList);

  IPage<?> execCreateRootPage(OutlineCreateRootPageChain chain);
}
