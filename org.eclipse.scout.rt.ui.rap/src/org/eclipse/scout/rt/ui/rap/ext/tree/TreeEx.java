/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext.tree;

import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

@SuppressWarnings("restriction")
public class TreeEx extends Tree {
  private static final long serialVersionUID = 1L;

  private static final String BACKUPED_MENU = "backupedMenu";
  private boolean m_readOnly = false;
  private TreeItem m_contextItem;

  public TreeEx(Composite parent, int style) {
    super(parent, style | SWT.FULL_SELECTION);
    addListener(SWT.MouseDown, new Listener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void handleEvent(Event event) {
        m_contextItem = getItem(new Point(event.x, event.y));
      }
    });
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  /**
   * @return the contextItem
   */
  public TreeItem getContextItem() {
    return m_contextItem;
  }

  public void setReadOnly(boolean readOnly) {
    if (m_readOnly != readOnly) {
      m_readOnly = readOnly;
      if (readOnly) {
        setData(BACKUPED_MENU, getMenu());
      }
      else {
        setMenu((Menu) getData(BACKUPED_MENU));
      }
    }
  }

  public boolean isReadOnly() {
    return m_readOnly;
  }

  public Point getPreferredContentSize(int maxRowCount, TreeItem parentItem, int stage) {
    Rectangle max = new Rectangle(0, 0, 0, 0);
    int nr = getItemCount();
    if (parentItem != null) {
      nr = parentItem.getItemCount();
    }
    for (int r = 0; r < nr && r < maxRowCount; r++) {
      TreeItem item;
      if (parentItem == null) {
        item = getItem(r);
      }
      else {
        item = parentItem.getItem(r);
      }
      if (item.getItemCount() > 0) {
        Point childSize = getPreferredContentSize(maxRowCount, item, stage + 1);
        Rectangle bounds = new Rectangle(0, 0, childSize.x, childSize.y);
        max = max.union(bounds);
      }
      Rectangle bounds = item.getBounds();

      String text = item.getText();
      if (!StringUtility.hasText(text)) {
        if (item.getData() instanceof ITreeNode) {
          text = ((ITreeNode) item.getData()).getCell().getText();
        }
      }
      if (StringUtility.hasText(text)) {
        int textWidth = TextSizeUtil.stringExtent(item.getFont(), text).x;
        bounds.width = textWidth;
      }
      bounds.width += stage * 5;
      max = max.union(bounds);
    }
    int y = max.y + max.height;
    //Add some points in height to ensure we do not have a scrollbar where not needed (depending to the count of items)
    if (nr > 3) {
      y += 1;
    }
    else if (nr > 1) {
      y += 2;
    }
    else {
      y += 4;
    }
    return new Point(max.x + max.width, y);
  }
}
