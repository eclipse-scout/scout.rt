/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link GroupBoxBodyGrid}
 */
public class GroupBoxBodyGridTest {

  @Order(10)
  public static class GroupBox extends AbstractGroupBox {
    @Override
    protected int getConfiguredGridColumnCount() {
      return 3;
    }

    @Order(10)
    public class FirstField extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "One";
      }
    }

    @Order(20)
    public class SecondField extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Two";
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }
    }

    @Order(30)
    public class ThirdField extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Three";
      }
    }

    @Order(40)
    public class FourthField extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Four";
      }
    }

    /*
    @Order(50)
    public class FifthField extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Four";
      }
    }
    */
  }

  @Test
  public void testLayout3x3() {
    GroupBox g = new GroupBox();
    GroupBoxBodyGrid grid = new GroupBoxBodyGrid(g);
    grid.validate();
    Assert.assertEquals(2, grid.getGridRowCount());
    Assert.assertEquals(2, grid.getGridColumnCount());
    /*
    GridData gd1 = g.getFieldByClass(GroupBox.FirstField.class).getGridData();
    GridData gd2 = g.getFieldByClass(GroupBox.SecondField.class).getGridData();
    GridData gd3 = g.getFieldByClass(GroupBox.ThirdField.class).getGridData();
    GridData gd4 = g.getFieldByClass(GroupBox.FourthField.class).getGridData();
    */
    //TODO [imo] once this is verified, add assertions
  }
}
