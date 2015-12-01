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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.List;

import org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationConfig;
import org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationUtility;
import org.eclipse.scout.rt.client.mobile.transformation.MobileDeviceTransformation;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractMobileTable;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.DefaultOutlineChooserForm.MainBox.OutlinesTableField;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;

public class DefaultOutlineChooserForm extends AbstractMobileForm implements IOutlineChooserForm {

  public DefaultOutlineChooserForm() {
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
    return VIEW_ID_CENTER;
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("MobileOutlineChooserTitle");
  }

  public void startView() {
    startInternal(new ViewHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public OutlinesTableField getOutlinesTableField() {
    return getFieldByClass(OutlinesTableField.class);
  }

  @Override
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  @Override
  protected boolean getConfiguredFooterVisible() {
    return true;
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected boolean getConfiguredBorderVisible() {
      return false;
    }

    @Override
    protected void execInitField() {
      //Table already is scrollable, it's not necessary to make the form scrollable too
      DeviceTransformationConfig config = DeviceTransformationUtility.getDeviceTransformationConfig();
      if (config != null) {
        config.excludeFieldTransformation(this, MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE);
      }
    }

    @Order(10)
    public class OutlinesTableField extends AbstractTableField<OutlinesTableField.Table> {

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected int getConfiguredGridH() {
        return 2;
      }

      @Order(10)
      public class Table extends AbstractMobileTable {

        @Override
        protected boolean execIsAutoCreateTableRowForm() {
          return false;
        }

        @Override
        protected boolean getConfiguredAutoDiscardOnDelete() {
          return true;
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
        protected void execDecorateRow(ITableRow row) {
          final String outlineIcon = getOutlineColumn().getValue(row).getDefaultIconId();
          if (outlineIcon != null) {
            row.setIconId(outlineIcon);
          }
        }

        @Order(10)
        public class OutlineColumn extends AbstractColumn<IOutline> {

          @Override
          protected boolean getConfiguredDisplayable() {
            return false;
          }
        }

        @Order(20)
        public class LabelColumn extends AbstractStringColumn {

        }

        @Override
        protected void execRowsSelected(List<? extends ITableRow> rows) {
          if (CollectionUtility.hasElements(rows)) {
            IOutline outline = getOutlineColumn().getValue(CollectionUtility.firstElement(rows));
            MobileDesktopUtility.activateOutline(outline);
            getDesktop().hideForm(DefaultOutlineChooserForm.this);

            clearSelectionDelayed();
          }

        }
      }
    }
  }

  public class ViewHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      final OutlinesTableField.Table table = getOutlinesTableField().getTable();

      for (IOutline outline : getDesktop().getAvailableOutlines()) {
        if (outline.isVisible() && outline.getRootNode() != null) {
          ITableRow row = table.createRow(new Object[]{outline, outline.getTitle()});
          row.setEnabled(outline.isEnabled());
          table.addRow(row);
        }
      }
    }

    @Override
    protected void execFinally() {
      final OutlinesTableField.Table table = getOutlinesTableField().getTable();
      table.discardAllRows();
    }
  }
}
