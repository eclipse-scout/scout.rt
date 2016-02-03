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
package org.eclipse.scout.rt.ui.rap.basic.tree;

import java.util.Map;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.RwtIcons;
import org.eclipse.scout.rt.ui.rap.basic.AbstractRwtScoutCellTextHelper;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutCellTextHelper;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class RwtScoutTreeModel extends LabelProvider implements ITreeContentProvider, IFontProvider, IColorProvider {
  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutTreeModel.class);

  private final ITree m_scoutTree;
  private final IRwtScoutTree m_uiTree;
  private final TreeViewer m_treeViewer;
  private Image m_imgCheckboxTrue;
  private Image m_imgCheckboxFalse;
  private Color m_disabledForegroundColor;
  private final IRwtEnvironment m_env;

  public RwtScoutTreeModel(ITree tree, IRwtScoutTree uiTree, TreeViewer treeViewer) {
    m_scoutTree = tree;
    m_uiTree = uiTree;
    m_treeViewer = treeViewer;
    m_env = getUiTree().getUiEnvironment();
    m_imgCheckboxTrue = m_env.getIcon(RwtIcons.CheckboxYes);
    m_imgCheckboxFalse = m_env.getIcon(RwtIcons.CheckboxNo);
    m_disabledForegroundColor = m_env.getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
  }

  protected ITree getScoutTree() {
    return m_scoutTree;
  }

  private IRwtScoutTree getUiTree() {
    return m_uiTree;
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    ITreeNode scoutNode = (ITreeNode) parentElement;
    return scoutNode.getFilteredChildNodes().toArray();
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
    if (getScoutTree() != null) {
      if (getScoutTree().isRootNodeVisible()) {
        return new Object[]{getScoutTree().getRootNode()};
      }
      else {
        return getScoutTree().getRootNode().getFilteredChildNodes().toArray();
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
    } //check
    Image checkBoxImage = null;
    if (getScoutTree().isCheckable()) {
      if (scoutNode.isChecked()) {
        checkBoxImage = m_imgCheckboxTrue;
      }
      else {
        checkBoxImage = m_imgCheckboxFalse;
      }
    }
    //
    //deco
    String iconId = scoutNode.getCell().getIconId();
    Image decoImage = null;
    decoImage = m_env.getIcon(iconId);
    //merge
    if (checkBoxImage != null && decoImage != null) {
      //TODO rap/rwt: new GC(Image) is not possible since in rwt an image does not implement Drawable.
      return checkBoxImage;
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
    final ITreeNode scoutNode = (ITreeNode) element;

    if (scoutNode == null) {
      return "";
    }

    final IRwtScoutCellTextHelper cellTextHelper = createCellTextHelper(m_env, getUiTree());

    return cellTextHelper.processCellText(scoutNode.getCell());
  }

  protected IRwtScoutCellTextHelper createCellTextHelper(IRwtEnvironment env, IRwtScoutComposite<?> uiComposite) {
    return new P_RwtScoutTreeModelCellTextHelper(env, uiComposite);
  }

  @Override
  public Font getFont(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    if (scoutNode.getCell().getFont() != null) {
      return m_env.getFont(scoutNode.getCell().getFont(), m_treeViewer.getTree().getFont());
    }
    return null;
  }

  @Override
  public Color getForeground(Object element) {
    ITreeNode scoutNode = (ITreeNode) element;
    ICell scoutCell = scoutNode.getCell();
    Color col = m_env.getColor(scoutCell.getForegroundColor());
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
      return m_env.getColor(scoutNode.getCell().getBackgroundColor());
    }
    return null;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  private class P_RwtScoutTreeModelCellTextHelper extends AbstractRwtScoutCellTextHelper {

    public P_RwtScoutTreeModelCellTextHelper(IRwtEnvironment env, IRwtScoutComposite<?> uiComposite) {
      super(env, uiComposite);
    }

    @Override
    protected Map<String, String> createAdditionalLinkParams() {
      return null;
    }

    @Override
    protected boolean isMultilineScoutObject() {
      return false;
    }

    @Override
    protected boolean isWrapText() {
      return false;
    }

  }
}
