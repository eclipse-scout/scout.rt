/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.net.URL;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public interface ITreeUIFacade {

  boolean isUIProcessing();

  void setNodeExpandedFromUI(ITreeNode node, boolean on);

  void setNodeSelectedAndExpandedFromUI(ITreeNode node);

  void setNodesSelectedFromUI(ITreeNode[] nodes);

  IMenu[] fireNodePopupFromUI();

  IMenu[] fireEmptySpacePopupFromUI();

  /**
   * Single mouse click on a node or (for checkable trees) the space key
   */
  void fireNodeClickFromUI(ITreeNode node);

  /**
   * Double mouse click on a node or enter
   */
  void fireNodeActionFromUI(ITreeNode node);

  boolean getNodesDragEnabledFromUI();

  TransferObject fireNodesDragRequestFromUI();

  void fireNodeDropActionFromUI(ITreeNode node, TransferObject dropData);

  void fireHyperlinkActionFromUI(ITreeNode node, URL url);
}
