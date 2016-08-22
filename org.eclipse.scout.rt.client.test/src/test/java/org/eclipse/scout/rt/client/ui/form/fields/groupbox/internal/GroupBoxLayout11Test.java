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

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h4>Vertical</h4>
 *
 * <pre>
 * ---------------------------
 *    Group01   |   Group01
 * ---------------------------
 *    Group02   |   Group03
 * ---------------------------
 *    Group04   |   Group04
 * ---------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * ---------------------------
 *    Group01   |   Group01
 * ---------------------------
 *    Group02   |   Group03
 * ---------------------------
 *    Group04   |   Group04
 * ---------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 13.03.2014
 */
@RunWith(PlatformTestRunner.class)
public class GroupBoxLayout11Test extends AbstractGroupBoxLayoutTest {

  @Test
  public void testVerticalLayout() throws Exception {
    LayoutGroupBox groupBox = new LayoutGroupBox();
    IGroupBoxBodyGrid grid = new VerticalSmartGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(2, grid.getGridColumnCount());
    assertEquals(3, grid.getGridRowCount());

    assertGridData(0, 0, 2, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group01.class).getGridData());
    assertGridData(0, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group02.class).getGridData());
    assertGridData(1, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group03.class).getGridData());
    assertGridData(0, 2, 2, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group04.class).getGridData());
  }

  @Test
  public void testHorizontalLayout() throws Exception {
    LayoutGroupBox groupBox = new LayoutGroupBox();
    IGroupBoxBodyGrid grid = new HorizontalGroupBoxBodyGrid();
    grid.validate(groupBox);

    // groupbox
    assertEquals(2, grid.getGridColumnCount());
    assertEquals(3, grid.getGridRowCount());

    assertGridData(0, 0, 2, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group01.class).getGridData());
    assertGridData(0, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group02.class).getGridData());
    assertGridData(1, 1, 1, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group03.class).getGridData());
    assertGridData(0, 2, 2, 1, groupBox.getFieldByClass(GroupBoxLayout11Test.LayoutGroupBox.Group04.class).getGridData());
  }

  public class LayoutGroupBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 2;
    }

    @Order(20)
    public class Group01 extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected boolean execCalculateVisible() {
        return true;
      }
    }

    @Order(30)
    public class Group02 extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridColumnCount() {
        return 1;
      }

      @Override
      protected int getConfiguredGridW() {
        return 1;
      }

      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected boolean execCalculateVisible() {
        return true;
      }
    }

    @Order(40)
    public class Group03 extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridColumnCount() {
        return 1;
      }

      @Override
      protected int getConfiguredGridW() {
        return 1;
      }

      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected boolean execCalculateVisible() {
        return true;
      }
    }

    @Order(50)
    public class Group04 extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      @Override
      protected boolean execCalculateVisible() {
        return true;
      }
    }

    @Order(60)
    public class OkButton extends AbstractOkButton {
    }

    @Order(70)
    public class CancelButton extends AbstractCancelButton {
    }
  }
}
