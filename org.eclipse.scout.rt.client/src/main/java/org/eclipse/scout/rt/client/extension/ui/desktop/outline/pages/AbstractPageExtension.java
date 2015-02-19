package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;

public abstract class AbstractPageExtension<OWNER extends AbstractPage> extends AbstractTreeNodeExtension<OWNER> implements IPageExtension<OWNER> {

  public AbstractPageExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPageDataLoaded(PagePageDataLoadedChain chain) throws ProcessingException {
    chain.execPageDataLoaded();
  }

  @Override
  public void execPageActivated(PagePageActivatedChain chain) throws ProcessingException {
    chain.execPageActivated();
  }

  @Override
  public void execDataChanged(PageDataChangedChain chain, Object... dataTypes) throws ProcessingException {
    chain.execDataChanged(dataTypes);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) throws ProcessingException {
    chain.execInitPage();
  }

  @Override
  public void execPageDeactivated(PagePageDeactivatedChain chain) throws ProcessingException {
    chain.execPageDeactivated();
  }

  @Override
  public void execDisposePage(PageDisposePageChain chain) throws ProcessingException {
    chain.execDisposePage();
  }
}
