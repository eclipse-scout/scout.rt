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
package org.eclipse.scout.rt.ui.swt.basic.tree;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtIcons;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class SwtScoutTreeModel extends LabelProvider implements ITreeContentProvider, IFontProvider, IColorProvider {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutTreeModel.class);
  private static final boolean COMPOSITE_ICON_ENABLED = "true".equals(Activator.getDefault().getBundle().getBundleContext().getProperty("scout.fix355669"));

  private ITree m_tree;
  private final ISwtEnvironment m_environment;
  private final TreeViewer m_treeViewer;
  private Image m_imgCheckboxTrue;
  private Image m_imgCheckboxFalse;
  private Color m_disabledForegroundColor;

  public SwtScoutTreeModel(ITree tree, ISwtEnvironment environment, TreeViewer treeViewer) {
    m_tree = tree;
    m_environment = environment;
    m_treeViewer = treeViewer;
    m_imgCheckboxTrue = Activator.getIcon(SwtIcons.CheckboxYes);
    m_imgCheckboxFalse = Activator.getIcon(SwtIcons.CheckboxNo);
    m_disabledForegroundColor = m_environment.getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    ITreeNode scoutNode = (ITreeNode) parentElement;
    return scoutNode.getFilteredChildNodes();
  }

  @Override
  public Object getParent(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    return scoutNode.getParentNode();
  }

  @Override
  public boolean hasChildren(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    return !scoutNode.isLeaf();
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_tree != null) {
      if (m_tree.isRootNodeVisible()) {
        return new Object[]{m_tree.getRootNode()};
      }
      else {
        return m_tree.getRootNode().getFilteredChildNodes();
      }
    }
    else {
      return new Object[0];
    }
  }

  @Override
  public Image getImage(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    if (scoutNode == null) {
      return null;
    }
    //check
    Image checkBoxImage = null;
    if (m_tree.isCheckable()) {
      if (scoutNode.isChecked()) {
        checkBoxImage = m_imgCheckboxTrue;
      }
      else {
        checkBoxImage = m_imgCheckboxFalse;
      }
    }
    //deco
    String iconId = scoutNode.getCell().getIconId();
    Image decoImage = null;
    decoImage = m_environment.getIcon(iconId);
    //merge
    if (COMPOSITE_ICON_ENABLED && checkBoxImage != null && decoImage != null) {
      String key = checkBoxImage.handle + "_" + iconId;
      ImageRegistry reg = Activator.getDefault().getImageRegistry();
      Image compositeImage = reg.get(key);
      if (compositeImage == null) {
        int w1 = checkBoxImage.getBounds().width;
        int w2 = decoImage.getBounds().width;
        int h = Math.max(checkBoxImage.getBounds().height, decoImage.getBounds().height);
        compositeImage = new Image(Display.getCurrent(), w1 + w2, h);
        GC gc = new GC(compositeImage);
        gc.drawImage(checkBoxImage, 0, 0);
        gc.drawImage(decoImage, w1, 0);
        gc.dispose();
        reg.put(key, compositeImage);
      }
      return compositeImage;
    }
    if (checkBoxImage != null) {
      return checkBoxImage;
    }
    if (decoImage != null) {
      return decoImage;
    }
    return null;
  }

  @Override
  public String getText(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    String text = scoutNode.getCell().getText();
    text = StringUtility.removeNewLines(text);
    return text;
  }

  @Override
  public Font getFont(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    if (scoutNode.getCell().getFont() != null) {
      return m_environment.getFont(scoutNode.getCell().getFont(), m_treeViewer.getTree().getFont());
    }
    return null;
  }

  @Override
  public Color getForeground(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    ICell scoutCell = scoutNode.getCell();
    Color col = m_environment.getColor(scoutCell.getForegroundColor());
    if (col == null) {
      if (!scoutCell.isEnabled()) {
        col = m_disabledForegroundColor;
      }
    }
    return col;
  }

  @Override
  public Color getBackground(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    if (scoutNode.getCell().getBackgroundColor() != null) {
      return m_environment.getColor(scoutNode.getCell().getBackgroundColor());
    }
    return null;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

}
