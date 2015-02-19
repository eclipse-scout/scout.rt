package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;

public interface IPageExtension<OWNER extends AbstractPage> extends ITreeNodeExtension<OWNER> {

  void execPageDataLoaded(PagePageDataLoadedChain chain) throws ProcessingException;

  void execPageActivated(PagePageActivatedChain chain) throws ProcessingException;

  void execDataChanged(PageDataChangedChain chain, Object... dataTypes) throws ProcessingException;

  void execInitPage(PageInitPageChain chain) throws ProcessingException;

  void execPageDeactivated(PagePageDeactivatedChain chain) throws ProcessingException;

  void execDisposePage(PageDisposePageChain chain) throws ProcessingException;
}
