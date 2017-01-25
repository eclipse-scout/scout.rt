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

import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageCalculateVisibleChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDetailFormActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitDetailFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;

public interface IPageExtension<OWNER extends AbstractPage> extends ITreeNodeExtension<OWNER> {

  void execPageDataLoaded(PagePageDataLoadedChain chain);

  void execPageActivated(PagePageActivatedChain chain);

  void execDataChanged(PageDataChangedChain chain, Object... dataTypes);

  void execInitPage(PageInitPageChain chain);

  void execInitDetailForm(PageInitDetailFormChain chain);

  void execPageDeactivated(PagePageDeactivatedChain chain);

  void execDisposePage(PageDisposePageChain chain);

  void execInitTable(PageInitTableChain chain);

  void execDetailFormActivated(PageDetailFormActivatedChain chain);

  boolean execCalculateVisible(PageCalculateVisibleChain chain);
}
