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
package org.eclipse.scout.rt.ui.swing.basic.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.icons.CheckboxIcon;
import org.eclipse.scout.rt.ui.swing.icons.CompositeIcon;

public class SwingTreeCellRenderer implements TreeCellRenderer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingTreeCellRenderer.class);
  private static final long serialVersionUID = 1L;

  private final ISwingEnvironment m_env;
  private TreeCellRenderer m_internalRenderer;
  private SwingScoutTree m_swingScoutTree;

  public SwingTreeCellRenderer(ISwingEnvironment env, TreeCellRenderer internalRenderer, SwingScoutTree swingScoutTree) {
    m_env = env;
    m_internalRenderer = internalRenderer;
    m_swingScoutTree = swingScoutTree;
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int rowIndex, boolean focused) {
    // reset
    if (m_internalRenderer instanceof JComponent) {
      JComponent j = (JComponent) m_internalRenderer;
      j.setBackground(tree.getBackground());
      j.setForeground(tree.getForeground());
      j.setFont(tree.getFont());
    }
    //
    ITree scoutTree = m_swingScoutTree.getScoutObject();
    ITreeNode node = (ITreeNode) value;
    ICell cell = node.getCell();
    Component comp = m_internalRenderer.getTreeCellRendererComponent(tree, cell.getText(), selected, expanded, leaf, rowIndex, focused);
    JLabel label = (comp instanceof JLabel ? (JLabel) comp : null);
    if (label == null) {
      return comp;
    }
    m_env.getHtmlValidator().removeHtmlRenderer(cell, cell.getText(), label);
    label.setEnabled(scoutTree.isEnabled() && node.isEnabled() && cell.isEnabled());
    // icon
    String iconName = cell.getIconId();
    CheckboxIcon checkboxIcon = null;
    if (scoutTree != null && scoutTree.isCheckable()) {
      // top inset is used to ensure the checkbox to be on the same position as the label text displayed
      checkboxIcon = m_env.createCheckboxWithMarginIcon(new Insets(0, 0, 0, 5));
      checkboxIcon.setSelected(node.isChecked());
      checkboxIcon.setEnabled(label.isEnabled());
    }
    //deco icon
    Icon decoIcon = null;
    if (iconName != null) {
      if (expanded) {
        decoIcon = m_env.getIcon(iconName + "_open");
      }
      if (decoIcon == null) {
        decoIcon = m_env.getIcon(iconName);
      }
    }
    //composite icon
    Icon icon = null;
    if (checkboxIcon != null && decoIcon != null) {
      icon = new CompositeIcon(0, checkboxIcon, decoIcon);
    }
    else if (checkboxIcon != null) {
      icon = checkboxIcon;
    }
    else if (decoIcon != null) {
      icon = decoIcon;
    }
    label.setIcon(icon);
    label.setDisabledIcon(label.getIcon());
    // tooltip
    String s = cell.getTooltipText();
    s = SwingUtility.createHtmlLabelText(s, true);
    label.setToolTipText(s);
    // background
    if (cell.getBackgroundColor() != null) {
      Color color = ColorUtility.createColor(cell.getBackgroundColor());
      if (selected) {
        color = color.darker();
      }
      label.setBackground(color);
    }
    // foreground
    if (cell.getForegroundColor() != null) {
      Color color = ColorUtility.createColor(cell.getForegroundColor());
      if (selected) {
        color = color.brighter();
      }
      label.setForeground(color);
    }
    // font
    if (cell.getFont() != null) {
      Font oldf = label.getFont();
      Font newf = SwingUtility.createFont(cell.getFont(), oldf);
      if (oldf != null) {// only override font style, not size and face
        label.setFont(new Font(oldf.getName(), newf != null ? newf.getStyle() : oldf.getStyle(), oldf.getSize()));
      }
    }
    return label;
  }
}
