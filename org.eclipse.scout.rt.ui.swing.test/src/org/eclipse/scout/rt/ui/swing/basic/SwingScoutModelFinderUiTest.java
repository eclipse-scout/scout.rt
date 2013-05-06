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
package org.eclipse.scout.rt.ui.swing.basic;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.ui.swing.AbstractSwingApplication;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.CancelButton;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.DateField;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.GroupBox;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.GroupBox.ListBoxField;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.GroupBox.NestedGroupBox;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.GroupBox.NestedGroupBox.TestButton;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.LabelField;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.OkButton;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.SmartField;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.StringField;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.StringField2;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutModelFinderUiTest.AllFieldsTestForm.MainBox.TreeBoxField;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link SwingScoutModelFinder}. Tests, if the correct scout model is found for a swing component.
 */
public class SwingScoutModelFinderUiTest {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutModelFinderUiTest.class);
  private static final IIconLocator NULL_ICON_LOCATOR = new P_NullIconLocator();

  @Test
  public void test() throws InterruptedException, InvocationTargetException {
    final IClientSession clientSession = EasyMock.createNiceMock(IClientSession.class);
    clientSession.getIconLocator();
    EasyMock.expectLastCall().andReturn(NULL_ICON_LOCATOR).anyTimes();
    EasyMock.replay(clientSession);

    SwingUtilities.invokeAndWait(new Runnable() {
      final ISwingEnvironment env = new AbstractSwingApplication() {

        @Override
        protected IClientSession getClientSession() {
          return clientSession;
        }
      }.getSwingEnvironment();
      final SwingScoutModelFinder m_swingScoutModelFinder = new SwingScoutModelFinder();

      @Override
      public void run() {
        try {
          //create swing environment
          env.showGUI(clientSession);

          //form fields
          AllFieldsTestForm testScoutForm = new AllFieldsTestForm();
          for (IFormField f : testScoutForm.getAllFields()) {
            testFormField(f);
          }

        }
        catch (ProcessingException e) {
          Assert.fail("" + e.getMessage());
        }

      }

      private void testFormField(IFormField formFieldModel) {
        JPanel testContainer = new JPanel();
        ISwingScoutFormField formField = env.createFormField(testContainer, formFieldModel);
        Object resolvedScoutModel = m_swingScoutModelFinder.getScoutModel(formField.getSwingField());
        Assert.assertEquals("Finding scout model failed ", formFieldModel, resolvedScoutModel);
      }
    });

  }

  public static class AllFieldsTestForm extends AbstractForm {

    public AllFieldsTestForm() throws ProcessingException {
      super();
    }

    public CancelButton getCancelButton() {
      return getFieldByClass(CancelButton.class);
    }

    public void startNew() throws ProcessingException {
      startInternal(new NewHandler());
    }

    public DateField getDateField() {
      return getFieldByClass(DateField.class);
    }

    public StringField2 getStringField2() {
      return getFieldByClass(StringField2.class);
    }

    public GroupBox getGroupBox() {
      return getFieldByClass(GroupBox.class);
    }

    public LabelField getLabelField() {
      return getFieldByClass(LabelField.class);
    }

    public ListBoxField getListBoxField() {
      return getFieldByClass(ListBoxField.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public NestedGroupBox getNestedGroupBox() {
      return getFieldByClass(NestedGroupBox.class);
    }

    public OkButton getOkButton() {
      return getFieldByClass(OkButton.class);
    }

    public SmartField getSmartField() {
      return getFieldByClass(SmartField.class);
    }

    public StringField getStringField() {
      return getFieldByClass(StringField.class);
    }

    public TestButton getTestButton() {
      return getFieldByClass(TestButton.class);
    }

    public TreeBoxField getTreeBoxField() {
      return getFieldByClass(TreeBoxField.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return "MainBox";
      }

      @Override
      protected String getConfiguredBackgroundColor() {
        return "fffff";
      }

      @Order(10.0)
      public class OkButton extends AbstractOkButton {
      }

      @Order(20.0)
      public class CancelButton extends AbstractCancelButton {
      }

      @Order(30.0)
      public class StringField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return "String Field";
        }
      }

      @Order(30.0)
      public class StringField2 extends AbstractStringField {

        @Override
        protected boolean getConfiguredWrapText() {
          return true;
        }

        @Override
        protected boolean getConfiguredMultilineText() {
          return true;
        }
      }

      @Order(40.0)
      public class SmartField extends AbstractSmartField<Long> {
      }

      @Order(50.0)
      public class TreeBoxField extends AbstractTreeBox<Long> {
      }

      @Order(60.0)
      public class LabelField extends AbstractLabelField {
      }

      @Order(70.0)
      public class DateField extends AbstractDateField {
      }

//      @Order(70.0)
//      public class DateField2 extends AbstractDateField {
//        @Override
//        protected boolean getConfiguredHasTime() {
//          return true;
//        }
//      }

      @Order(80.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class ListBoxField extends AbstractListBox<Long> {
        }

        @Order(20.0)
        public class NestedGroupBox extends AbstractGroupBox {

          @Order(10.0)
          public class TestButton extends AbstractButton {
          }
        }
      }

      @Order(10)
      public class GroupBox1 extends AbstractGroupBox {
        @Order(10)
        public class SequenceField extends AbstractSequenceBox {

          @Order(10)
          public class StartField extends AbstractDateField {
            @Override
            protected String getConfiguredLabel() {
              return "from";
            }
          }

          @Order(20)
          public class EndField extends AbstractDateField {
            @Override
            protected String getConfiguredLabel() {
              return "to";
            }
          }
        }
      }

      @Order(10.0)
      public class TreeBox extends AbstractTreeBox<Long> {

        @Override
        protected int getConfiguredGridH() {
          return 8;
        }

        @Override
        protected String getConfiguredLabel() {
          return "TreeBox";
        }

        @Override
        protected boolean getConfiguredAutoExpandAll() {
          return true;
        }

        @Override
        protected void execLoadChildNodes(ITreeNode parentNode) throws ProcessingException {
          LookupRow[] data = new LookupRow[]{
              new LookupRow(1L, "Element 1", null, null, null, null, null, true, null),
              new LookupRow(2L, "Element 1a", null, null, null, null, null, true, 1L),
              new LookupRow(3L, "Element 1b", null, null, null, null, null, true, 1L),
              new LookupRow(4L, "Element 2", null, null, null, null, null, true, null),
              new LookupRow(5L, "Element 2a", null, null, null, null, null, true, 4L)
          };
          ITreeNode[] children = getTreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, false);
          getTree().removeAllChildNodes(parentNode);
          getTree().addChildNodes(parentNode, children);
          parentNode.setChildrenLoaded(true);
        }
      }

    }

    public class NewHandler extends AbstractFormHandler {
    }
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected String getConfiguredTitle() {
      return "Software";
    }

    @Override
    protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
      return new Object[][]{
          new Object[]{1L, "Apache"},
          new Object[]{2L, "Eclipse"},
          new Object[]{3L, "Oracle"},};
    }

    @Override
    protected IPage execCreateChildPage(ITableRow row) throws ProcessingException {
      return new PageWithNode();
    }

    public class Table extends AbstractTable {

      public KeyColumn getKeyColumn() {
        return getColumnSet().getColumnByClass(KeyColumn.class);
      }

      public NameColumn getNameColumn() {
        return getColumnSet().getColumnByClass(NameColumn.class);
      }

      @Order(10)
      public class KeyColumn extends AbstractLongColumn {
        @Override
        protected boolean getConfiguredPrimaryKey() {
          return true;
        }

        @Override
        protected boolean getConfiguredDisplayable() {
          return false;
        }
      }

      @Order(20)
      public class NameColumn extends AbstractStringColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Name";
        }

        @Override
        protected int getConfiguredWidth() {
          return 400;
        }
      }

      @Order(100)
      public class DetailMenu extends AbstractMenu {
        @Override
        protected String getConfiguredText() {
          return "&Show";
        }

      }

      @Order(110)
      public class DeleteMenu extends AbstractMenu {
        @Override
        protected String getConfiguredText() {
          return "&Delete";
        }

      }
    }
  }

  public static class PageWithNode extends AbstractPageWithNodes {
  }

  public static class P_NullIconLocator implements IIconLocator {

    @Override
    public IconSpec getIconSpec(String name) {
      return null;
    }
  }
}
