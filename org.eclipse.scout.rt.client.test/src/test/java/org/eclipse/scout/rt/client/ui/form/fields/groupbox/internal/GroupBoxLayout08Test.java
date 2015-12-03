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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h4>Vertical</h4>
 *
 * <pre>
 * -------------------------------------------------------
 *    Group01   |   Group01   |   Group05   |   Group05
 * -------------------------------------------------------
 *    Group02   |   Group02   |   Group06   |
 * -------------------------------------------------------
 *    Group03   |   Group03   |   Group07   |   Group07
 * -------------------------------------------------------
 *    Group04   |   Group04   |             |
 * -------------------------------------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * -------------------------------------------------------
 *    Group01   |   Group01   |   Group02   |   Group02
 * -------------------------------------------------------
 *    Group03   |   Group03   |   Group04   |   Group04
 * -------------------------------------------------------
 *    Group06   |   Group05   |   Group06   |
 * -------------------------------------------------------
 *    Group07   |   Group07   |             |
 * -------------------------------------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
@RunWith(PlatformTestRunner.class)
public class GroupBoxLayout08Test extends AbstractGroupBoxLayoutTest {

  @Test
  public void testVerticalLayout() throws Exception {
    MainBox groupBox = new MainBox();
    IGroupBoxBodyGrid grid = new VerticalSmartGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(4, grid.getGridColumnCount());
    assertEquals(4, grid.getGridRowCount());

    // group01
    assertGridData(0, 0, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox01.class).getGridData());

    // group02
    assertGridData(0, 1, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox02.class).getGridData());

    // group03
    assertGridData(0, 2, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox03.class).getGridData());

    // group04
    assertGridData(0, 3, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox04.class).getGridData());

    // group05
    assertGridData(2, 0, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox05.class).getGridData());
    // group06
    assertGridData(2, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox06.class).getGridData());
    // group07
    assertGridData(2, 2, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox07.class).getGridData());

  }

  @Test
  public void testHorizontalLayout() throws Exception {
    MainBox groupBox = new MainBox();
    IGroupBoxBodyGrid grid = new HorizontalGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(4, grid.getGridColumnCount());
    assertEquals(4, grid.getGridRowCount());

    // group01
    assertGridData(0, 0, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox01.class).getGridData());

    // group02
    assertGridData(2, 0, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox02.class).getGridData());

    // group03
    assertGridData(0, 1, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox03.class).getGridData());

    // group04
    assertGridData(2, 1, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox04.class).getGridData());

    // group05
    assertGridData(0, 2, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox05.class).getGridData());
    // group06
    assertGridData(2, 2, 1, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox06.class).getGridData());
    // group07
    assertGridData(0, 3, 2, 1, groupBox.getFieldByClass(GroupBoxLayout08Test.MainBox.GroupBox07.class).getGridData());

  }

  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 4;
    }

    @Order(10)
    public class GroupBox01 extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      public class TableField01 extends AbstractTableField<TableField01.Table> {
        public class Table extends AbstractTable {

        }
      }
    }

    @Order(20)
    public class GroupBox02 extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      public class TableField extends AbstractTableField<TableField.Table> {
        public class Table extends AbstractTable {

        }
      }
    }

    @Order(30)
    public class GroupBox03 extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      public class TableField extends AbstractTableField<TableField.Table> {
        public class Table extends AbstractTable {

        }
      }
    }

    @Order(40)
    public class GroupBox04 extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      public class TableField extends AbstractTableField<TableField.Table> {
        public class Table extends AbstractTable {

        }
      }
    }

    @Order(50)
    public class GroupBox05 extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      public class TableField extends AbstractTableField<TableField.Table> {
        public class Table extends AbstractTable {

        }
      }
    }

    @Order(60)
    public class GroupBox06 extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 1;
      }

      public class TableField extends AbstractTableField<TableField.Table> {
        public class Table extends AbstractTable {

        }
      }
    }

    @Order(70)
    public class GroupBox07 extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      public class TableField extends AbstractTableField<TableField.Table> {
        public class Table extends AbstractTable {

        }
      }
    }

    @Order(200)
    public class CloseButton extends AbstractCloseButton {
    }
  }
}
