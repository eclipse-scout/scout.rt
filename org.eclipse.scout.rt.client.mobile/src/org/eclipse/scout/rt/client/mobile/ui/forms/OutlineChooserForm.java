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
package org.eclipse.scout.rt.client.mobile.ui.forms;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm.MainBox.OutlinesTableField;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.AbstractIcons;

public class OutlineChooserForm extends AbstractForm {

  public OutlineChooserForm() throws ProcessingException {
    super();
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_VIEW;
  }

  @Override
  protected String getConfiguredDisplayViewId() {
    return VIEW_ID_PAGE_TABLE;
  }

  @Override
  protected String getConfiguredTitle() {
    return "Sichten";//TODO rst Texts
  }

  public void startView() throws ProcessingException {
    startInternal(new ViewHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public OutlinesTableField getOutlinesTableField() {
    return getFieldByClass(OutlinesTableField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected boolean getConfiguredBorderVisible() {
      return false;
    }

    @Order(10.0)
    public class OutlinesTableField extends AbstractTableField<OutlinesTableField.Table> {

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected int getConfiguredGridH() {
        return 2;
      }

      @Order(10.0)
      public class Table extends AbstractTable {

        @Override
        protected boolean getConfiguredAutoDiscardOnDelete() {
          return true;
        }

        @Override
        protected String getConfiguredDefaultIconId() {
          return AbstractIcons.TreeNode;
        }

        @Override
        protected boolean getConfiguredAutoResizeColumns() {
          return true;
        }

        @Override
        protected boolean getConfiguredSortEnabled() {
          return false;
        }

        public LabelColumn getLableColumn() {
          return getColumnSet().getColumnByClass(LabelColumn.class);
        }

        public OutlineColumn getOutlineColumn() {
          return getColumnSet().getColumnByClass(OutlineColumn.class);
        }

        @Override
        protected void execDecorateRow(ITableRow row) throws ProcessingException {
          final String outlineIcon = getOutlineColumn().getValue(row).getIconId();
          if (outlineIcon != null) {
            row.setIconId(outlineIcon);
          }
        }

        @Order(10.0)
        public class OutlineColumn extends AbstractColumn<IOutline> {

          @Override
          protected boolean getConfiguredDisplayable() {
            return false;
          }
        }

        @Order(20.0)
        public class LabelColumn extends AbstractStringColumn {

          @Override
          protected String getConfiguredHeaderText() {
            return "Sichten";//TODO rst texts
          }
        }

        @Override
        protected void execRowAction(ITableRow row) throws ProcessingException {
          activateOutline(getOutlineColumn().getValue(row));
        }

        /**
         * Mostly copy-pasted from AbstractOutlineViewButton. Make code there reusable!
         */
        //FIXME CGU make it reusable in scout
        private void activateOutline(IOutline outline) {
          IDesktop desktop = ClientJob.getCurrentSession().getDesktop();
          desktop.setOutlineTableFormVisible(true);

          if (desktop.getOutline() != outline) {
            desktop.setOutline(outline);
          }
          //show again root node
          ITreeNode newSelectedNode;
          if (outline.isRootNodeVisible()) {
            newSelectedNode = outline.getRootPage();
          }
          else {
            newSelectedNode = outline.getSelectedNode();
            while (newSelectedNode != null && newSelectedNode.getParentNode() != outline.getRootPage()) {
              newSelectedNode = newSelectedNode.getParentNode();
            }
          }
          outline.selectNode(newSelectedNode);
          // collapse outline
          if (outline.isRootNodeVisible()) {
            outline.collapseAll(outline.getRootPage());
            if (outline.getRootPage() instanceof AbstractPage && ((AbstractPage) outline.getRootPage()).isInitialExpanded()) {
              outline.setNodeExpanded(outline.getRootPage(), true);
            }
          }
          else {
            for (IPage root : outline.getRootPage().getChildPages()) {
              outline.collapseAll(root);
            }
            for (IPage root : outline.getRootPage().getChildPages()) {
              if (root instanceof AbstractPage && ((AbstractPage) root).isInitialExpanded()) {
                outline.setNodeExpanded(root, true);
              }
            }
          }

          getDesktop().removeForm(OutlineChooserForm.this);
        }
      }
    }
  }

  @Order(10.0)
  public class ViewHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      final OutlinesTableField.Table table = getOutlinesTableField().getTable();

      IOutline[] outlines = getDesktop().getAvailableOutlines();
      for (IOutline outline : outlines) {
        if (outline.isVisible() && outline.getRootNode() != null) {
          ITableRow row = table.createRow(new Object[]{outline, outline.getTitle()});
          row.setEnabled(outline.isEnabled());
          table.addRow(row);
        }
      }
    }

    @Override
    protected void execFinally() throws ProcessingException {
      final OutlinesTableField.Table table = getOutlinesTableField().getTable();
      table.discardAllRows();
    }
  }
}
