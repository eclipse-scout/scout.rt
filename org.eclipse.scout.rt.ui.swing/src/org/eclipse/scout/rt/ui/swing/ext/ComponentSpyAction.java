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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTree;
import javax.swing.RootPaneContainer;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.window.SwingWindowManager;

/**
 *
 */
public class ComponentSpyAction extends AbstractAction {
  private static final long serialVersionUID = 1L;

  private static JDialog currentDialog;

  @Override
  public void actionPerformed(ActionEvent e) {
    if (currentDialog != null) {
      currentDialog.dispose();
      currentDialog = null;
    }
    Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (c == null) return;
    Object s = null;
    if (c instanceof JTree) {
      TreePath path = ((JTree) c).getSelectionPath();
      if (path != null) {
        Object x = path.getLastPathComponent();
        if (x instanceof ITreeNode) {
          s = x;
        }
      }
    }
    if (s == null) {
      Component tmp = c;
      while (tmp != null) {
        if (tmp instanceof JComponent) {
          s = SwingScoutComposite.getScoutModelOnWidget((JComponent) tmp);
          if (s != null) {
            break;
          }
        }
        tmp = tmp.getParent();
      }
    }
    showDetails(c, s);
  }

  protected void showDetails(Component c, Object s) {
    TreeNode root = createTree(c, s);
    final JTreeEx tree = new JTreeEx();
    tree.setModel(new DefaultTreeModel(root));
    ((JComponent) tree.getCellRenderer()).setBorder(new LineBorder(Color.lightGray));
    tree.setRowHeight(-1);
    //expand all
    visit(root, new ITreeVisitor() {
      @Override
      public void visit(TreeNode node) {
        tree.expandPath(new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(node)));
      }
    });

    final JDialog dlg = new JDialog(SwingWindowManager.getInstance().getActiveWindow());
    dlg.getRootPane().setName("Synth.Dialog");
    dlg.setTitle("Component Spy - Swing / Scout");
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dlg.setModal(false);
    dlg.getContentPane().add(BorderLayout.CENTER, new JScrollPaneEx(tree));
    JPanelEx buttonPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.RIGHT));
    buttonPanel.add(new JButtonEx(new AbstractAction("Close") {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        dlg.dispose();
      }
    }));
    dlg.getContentPane().add(BorderLayout.SOUTH, buttonPanel);
    dlg.getContentPane().setPreferredSize(new Dimension(800, 600));
    dlg.pack();
    dlg.setVisible(true);
    currentDialog = dlg;
  }

  protected TreeNode createTree(Component c, Object s) {
    TextNode root = new TextNode("Root");
    TextNode scoutGroupNode = new TextNode("Scout Model Hierarchy");
    root.addChild(scoutGroupNode);
    scoutGroupNode.setParent(root);
    TextNode swingGroupNode = new TextNode("Swing Widget Hierarchy");
    root.addChild(swingGroupNode);
    swingGroupNode.setParent(root);
    //
    ScoutNode currentScoutNode = null;
    while (s != null) {
      ScoutNode scoutNode = new ScoutNode(s, currentScoutNode);
      currentScoutNode = scoutNode;
      Object next = null;
      if (next == null && s instanceof IFormField) {
        next = ((IFormField) s).getParentField();
      }
      if (next == null && s instanceof IForm) {
        next = ((IForm) s).getOuterFormField();
      }
      if (next == null) {
        int nestedCount = s.getClass().getName().replaceAll("[^$]", "").trim().length();
        Field f = null;
        try {
          f = s.getClass().getDeclaredField("this$" + (nestedCount - 1));
          f.setAccessible(true);
          next = f.get(s);
        }
        catch (Throwable t) {
          // nop
        }
      }
      s = next;
    }
    if (currentScoutNode != null) {
      scoutGroupNode.addChild(currentScoutNode);
      currentScoutNode.setParent(scoutGroupNode);
    }
    //
    SwingNode currentSwingNode = null;
    while (c != null && !(c instanceof RootPaneContainer)) {
      SwingNode swingNode = new SwingNode(c, currentSwingNode);
      currentSwingNode = swingNode;
      c = c.getParent();
    }
    if (currentSwingNode != null) {
      swingGroupNode.addChild(currentSwingNode);
      currentSwingNode.setParent(swingGroupNode);
    }
    return root;
  }

  protected void visit(TreeNode treeNode, ITreeVisitor v) {
    v.visit(treeNode);
    int n = treeNode.getChildCount();
    for (int i = 0; i < n; i++) {
      visit(treeNode.getChildAt(i), v);
    }
  }

  static class TextNode implements TreeNode {
    private static final long serialVersionUID = 1L;

    private Object m_userObject;
    private TreeNode m_parent;
    private Vector<TreeNode> m_children;

    public TextNode(Object data) {
      m_userObject = data;
      m_children = new Vector<TreeNode>();
    }

    public Object getUserObject() {
      return m_userObject;
    }

    public void addChild(TreeNode node) {
      m_children.add(node);
    }

    @Override
    public Enumeration children() {
      return m_children.elements();
    }

    @Override
    public boolean getAllowsChildren() {
      return m_children.size() > 0;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
      return m_children.get(childIndex);
    }

    @Override
    public int getChildCount() {
      return m_children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
      return m_children.indexOf(node);
    }

    @Override
    public TreeNode getParent() {
      return m_parent;
    }

    public void setParent(TreeNode node) {
      m_parent = node;
    }

    @Override
    public boolean isLeaf() {
      return m_children.size() == 0;
    }

    @Override
    public String toString() {
      return getUserObject().toString();
    }

  }

  static class ScoutNode implements TreeNode {
    private static final long serialVersionUID = 1L;

    private Object m_userObject;
    private boolean m_showContext;
    private ScoutNode m_markedChild;
    private TreeNode m_parent;
    private Vector<ScoutNode> m_children;

    public ScoutNode(Object data, ScoutNode markedChild) {
      m_userObject = data;
      m_markedChild = markedChild;
    }

    public Object getUserObject() {
      return m_userObject;
    }

    @Override
    public Enumeration children() {
      checkChildren();
      return m_children.elements();
    }

    @Override
    public boolean getAllowsChildren() {
      checkChildren();
      return m_children.size() > 0;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
      checkChildren();
      return m_children.get(childIndex);
    }

    @Override
    public int getChildCount() {
      checkChildren();
      return m_children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
      checkChildren();
      return m_children.indexOf(node);
    }

    @Override
    public TreeNode getParent() {
      return m_parent;
    }

    public void setParent(TreeNode node) {
      m_parent = node;
    }

    @Override
    public boolean isLeaf() {
      checkChildren();
      return m_children.size() == 0;
    }

    private void checkChildren() {
      if (m_children == null) {
        loadChildren();
      }
    }

    @Override
    public String toString() {
      return getHtml();
    }

    public String getHtml() {
      Object o = getUserObject();
      StringBuffer buf = new StringBuffer();
      buf.append("<html><head><style type=\"text/css\">td {border-width:1px; border-style:solid; border-color:gray;}</style></head><body>");
      //
      boolean marked = (getParent() instanceof ScoutNode && ((ScoutNode) getParent()).m_markedChild == this);
      //
      buf.append("<table>");
      buf.append("<tr>");
      buf.append("<td>");
      //
      String cname = o.getClass().getName();
      String prefix = cname.substring(0, cname.lastIndexOf('.') + 1);
      buf.append(prefix);
      buf.append("<b>");
      if (marked) {
        buf.append("<font color='blue'>");
      }
      buf.append(cname.substring(cname.lastIndexOf('.') + 1));
      if (marked) {
        buf.append("</font");
      }
      buf.append("</b>");
      //
      Class tmp = o.getClass();
      while (!tmp.getName().startsWith("org.eclipse.scout.")) {
        tmp = tmp.getSuperclass();
      }
      if (tmp != null) {
        buf.append("<br>");
        buf.append("&nbsp;Type: " + tmp.getSimpleName());
      }
      //
      if (o instanceof ICompositeField) {
        ICompositeField field = (ICompositeField) o;
        buf.append("<br>");
        buf.append("&nbsp;GroupGrid: rowCount=" + field.getGridRowCount() + ", columnCount=" + field.getGridColumnCount());
      }
      //
      if (o instanceof IFormField) {
        IFormField field = (IFormField) o;
        GridData gd = field.getGridData();
        buf.append("<br>");
        buf.append("&nbsp;GridData: x=" + gd.x + ", y=" + gd.y + ", w=" + gd.w + ", h=" + gd.h + ", weightX=" + gd.weightX + ", weightY=" + gd.weightY + ", useUiWidth=" + gd.useUiWidth + ", useUiHeight=" + gd.useUiHeight);
      }
      buf.append("</td>");
      buf.append("</tr>");
      buf.append("</table>");
      buf.append("</body></html>");
      return buf.toString();
    }

    private void loadChildren() {
      Vector<ScoutNode> newList = new Vector<ScoutNode>();
      Object o = getUserObject();
      IFormField[] childFields = null;
      if (o instanceof IForm) {
        childFields = new IFormField[]{((IForm) o).getRootGroupBox()};
      }
      else if (o instanceof ICompositeField) {
        childFields = ((ICompositeField) o).getFields();
      }
      if (childFields != null) {
        for (IFormField f : childFields) {
          ScoutNode newNode = null;
          if (m_markedChild != null && m_markedChild.getUserObject() == f) {
            newNode = m_markedChild;
          }
          else if (m_showContext) {
            newNode = new ScoutNode(f, null);
          }
          if (newNode != null) {
            newList.add(newNode);
            newNode.setParent(this);
          }
        }
      }
      m_children = newList;
    }
  }

  static class SwingNode implements TreeNode {
    private static final long serialVersionUID = 1L;

    private Object m_userObject;
    private boolean m_showContext;
    private SwingNode m_markedChild;
    private TreeNode m_parent;
    private Vector<SwingNode> m_children;

    public SwingNode(Object data, SwingNode markedChild) {
      m_userObject = data;
      m_markedChild = markedChild;
    }

    public Object getUserObject() {
      return m_userObject;
    }

    @Override
    public Enumeration children() {
      checkChildren();
      return m_children.elements();
    }

    @Override
    public boolean getAllowsChildren() {
      checkChildren();
      return m_children.size() > 0;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
      checkChildren();
      return m_children.get(childIndex);
    }

    @Override
    public int getChildCount() {
      checkChildren();
      return m_children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
      checkChildren();
      return m_children.indexOf(node);
    }

    @Override
    public TreeNode getParent() {
      return m_parent;
    }

    public void setParent(TreeNode node) {
      m_parent = node;
    }

    @Override
    public boolean isLeaf() {
      checkChildren();
      return m_children.size() == 0;
    }

    private void checkChildren() {
      if (m_children == null) {
        loadChildren();
      }
    }

    @Override
    public String toString() {
      return getHtml();
    }

    public String getHtml() {
      Component c = (Component) getUserObject();
      StringBuffer buf = new StringBuffer();
      buf.append("<html><head><style type=\"text/css\">td {border-width:1px; border-style:solid; border-color:gray;}</style></head><body>");
      boolean marked = (getParent() instanceof SwingNode && ((SwingNode) getParent()).m_markedChild == this);
      //
      buf.append("<table>");
      buf.append("<tr>");
      buf.append("<td>");
      //
      String cname = c.getClass().getName();
      String prefix = cname.substring(0, cname.lastIndexOf('.') + 1);
      buf.append(prefix);
      buf.append("<b>");
      if (marked) {
        buf.append("<font color='blue'>");
      }
      buf.append(cname.substring(cname.lastIndexOf('.') + 1));
      if (marked) {
        buf.append("</font");
      }
      buf.append("</b>");
      //
      Class tmp = c.getClass();
      while (!tmp.getName().startsWith("javax.swing.")) {
        tmp = tmp.getSuperclass();
      }
      if (tmp != null) {
        buf.append("<br>");
        buf.append("&nbsp;Type: " + tmp.getSimpleName());
      }
      //
      Insets insets = c instanceof JComponent ? ((JComponent) c).getInsets() : null;
      if (insets != null) {
        buf.append("<br>");
        buf.append("&nbsp;Insets: top=" + insets.top + ", left=" + insets.left + ", bottom=" + insets.bottom + ", right=" + insets.right);
      }
      //
      Rectangle r = c.getBounds();
      buf.append("<br>");
      buf.append("&nbsp;Bounds: " + r.x + ", " + r.y + ", " + r.width + ", " + r.height);
      //
      Dimension[] dims = new Dimension[]{c.getMinimumSize(), c.getPreferredSize(), c.getMaximumSize()};
      buf.append("<br>");
      buf.append("&nbsp;Sizes: (" + dims[0].width + ", " + dims[0].height + "), (" + dims[1].width + ", " + dims[1].height + "), (" + dims[2].width + ", " + dims[2].height + ")");
      //
      LogicalGridData gd = (c instanceof JComponent ? (LogicalGridData) ((JComponent) c).getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME) : null);
      if (gd != null) {
        buf.append("<br>");
        buf.append("&nbsp;GridData: x=" + gd.gridx + ", y=" + gd.gridy + ", w=" + gd.gridw + ", h=" + gd.gridh + ", weightX=" + gd.weightx + ", weightY=" + gd.weighty + ", useUiWidth=" + gd.useUiWidth + ", useUiHeight=" + gd.useUiHeight);
      }
      if (c instanceof JComponent) {
        JComponent component = (JComponent) c;
        buf.append("<br>");
        buf.append("&nbsp;Colors: Foreground=");
        buf.append(colorToString(component.getForeground()));
        if (!component.isForegroundSet()) {
          buf.append(" (from parent)");
        }
        buf.append(", Background=");
        buf.append(colorToString(component.getBackground()));
        if (!component.isBackgroundSet()) {
          buf.append(" (from parent)");
        }
      }

      //
      buf.append("</td>");
      buf.append("</tr>");
      buf.append("</table>");
      buf.append("</body></html>");
      return buf.toString();
    }

    private String colorToString(Color color) {
      if (color == null) {
        return "[null]";
      }
      return "[rgb=(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ") alpha=" + color.getAlpha() + " hex=" + Integer.toHexString(color.getRGB()) + "]";
    }

    private void loadChildren() {
      Vector<SwingNode> newList = new Vector<SwingNode>();
      Object o = getUserObject();
      Component[] childComps = null;
      if (o instanceof Container) {
        childComps = ((Container) o).getComponents();
      }
      else if (o instanceof RootPaneContainer) {
        childComps = new Component[]{((RootPaneContainer) o).getContentPane()};
      }
      if (childComps != null) {
        for (Component c : childComps) {
          SwingNode newNode = null;
          if (m_markedChild != null && m_markedChild.getUserObject() == c) {
            newNode = m_markedChild;
          }
          else if (m_showContext) {
            newNode = new SwingNode(c, null);
          }
          if (newNode != null) {
            newList.add(newNode);
            newNode.setParent(this);
          }
        }
      }
      m_children = newList;
    }
  }

  static interface ITreeVisitor {
    void visit(TreeNode node);
  }

  public static void main(String[] args) {
    new ComponentSpyAction().actionPerformed(null);
  }
}
