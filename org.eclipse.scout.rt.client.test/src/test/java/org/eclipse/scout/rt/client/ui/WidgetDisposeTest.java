/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.WidgetDisposeTest.MainBox.NestedFormField;
import org.eclipse.scout.rt.client.ui.WidgetDisposeTest.MainBox.TableField;
import org.eclipse.scout.rt.client.ui.WidgetDisposeTest.MainBox.TableField.TestTable;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.AbstractFormFieldMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class WidgetDisposeTest {

  private final AtomicInteger m_counter = new AtomicInteger();

  @Test
  public void testDisposeRecursive() {
    m_counter.set(0);
    MainBox root = new MainBox();
    root.getFieldByClass(NestedFormField.class).setInnerForm(new NestedTestForm());
    root.dispose();
    Assert.assertEquals(9, m_counter.get());
  }

  /**
   * Test that a dispose event is not propagated to an inner form if it is marked as externally managed.
   */
  @Test
  public void testDisposeWithExternallyManagedWrappedForm() {
    m_counter.set(0);
    MainBox root = new MainBox();
    NestedTestForm form = new NestedTestForm();
    root.getFieldByClass(NestedFormField.class).setInnerForm(form, false /* not internally managed */);
    root.dispose();
    Assert.assertEquals(7, m_counter.get());
    Assert.assertFalse(form.isDisposeDone());

    m_counter.set(0);
    form.dispose();
    Assert.assertEquals(2, m_counter.get());
  }

  /**
   * Test that a dispose event is not propagated to an inner table if it is marked as externally managed.
   */
  @Test
  public void testDisposeWithExternallyManagedTable() {
    m_counter.set(0);
    MainBox root = new MainBox();
    TableField tableField = root.getFieldByClass(TableField.class);
    TestTable nestedTable = tableField.new TestTable();
    tableField.setTable(nestedTable, true /*managed externally*/);
    root.dispose();
    Assert.assertEquals(3, m_counter.get());
    Assert.assertFalse(nestedTable.isDisposeDone());

    m_counter.set(0);
    nestedTable.dispose();
    Assert.assertEquals(4, m_counter.get());
  }

  @Test
  public void testDisposeTreeField() {
    m_counter.set(0);
    TestTreeField root = new TestTreeField();
    root.dispose();
    Assert.assertEquals(2, m_counter.get());
  }

  /**
   * Test that a dispose event is not propagated to an inner tree if it is marked as externally managed.
   */
  @Test
  public void testDisposeWithExternallyManagedTree() {
    m_counter.set(0);
    TestTreeField root = new TestTreeField();
    ITree tree = root.new InnerTree();
    root.setTree(tree, true /*externally managed*/);
    root.dispose();
    Assert.assertEquals(1, m_counter.get());
    Assert.assertFalse(tree.isDisposeDone());

    m_counter.set(0);
    tree.dispose();
    Assert.assertEquals(1, m_counter.get());
  }

  public class TestTreeField extends AbstractTreeField {

    @Override
    protected void disposeFieldInternal() {
      m_counter.incrementAndGet();
      super.disposeFieldInternal();
    }

    public class InnerTree extends AbstractTree {

      @Override
      protected void disposeTreeInternal() {
        m_counter.incrementAndGet();
        super.disposeTreeInternal();
      }
    }
  }

  public class MainBox extends AbstractGroupBox {
    @Override
    protected void disposeFieldInternal() {
      m_counter.incrementAndGet();
      super.disposeFieldInternal();
    }

    @Order(1000)
    public class TableField extends AbstractTableField<TestTable> {

      @Override
      protected void disposeFieldInternal() {
        m_counter.incrementAndGet();
        super.disposeFieldInternal();
      }

      @Order(1000)
      public class TestTable extends AbstractTable {

        @Override
        protected void disposeTableInternal() {
          m_counter.incrementAndGet();
          super.disposeTableInternal();
        }

        @Order(1000)
        public class NormalMenu extends AbstractMenu {
          @Override
          protected void disposeActionInternal() {
            m_counter.incrementAndGet();
            super.disposeActionInternal();
          }
        }

        @Order(2000)
        @ClassId("f1a37849-afad-42f7-9553-eb11d1c23ea7")
        public class FormFieldMenu extends AbstractFormFieldMenu {
          @Override
          protected void disposeActionInternal() {
            m_counter.incrementAndGet();
            super.disposeActionInternal();
          }

          @Order(1000)
          public class BigDecimalField extends AbstractBigDecimalField {
            @Override
            protected void disposeFieldInternal() {
              m_counter.incrementAndGet();
              super.disposeFieldInternal();
            }
          }
        }
      }
    }

    @Order(2000)
    public class NestedFormField extends AbstractWrappedFormField<NestedTestForm> {
      @Override
      protected void disposeFieldInternal() {
        m_counter.incrementAndGet();
        super.disposeFieldInternal();
      }
    }
  }

  public class NestedTestForm extends AbstractForm {

    @Override
    protected void disposeFormInternal() {
      m_counter.incrementAndGet();
      super.disposeFormInternal();
    }

    @Order(1000)
    public class InnerMainBox extends AbstractGroupBox {
      @Override
      protected void disposeFieldInternal() {
        m_counter.incrementAndGet();
        super.disposeFieldInternal();
      }
    }
  }
}
