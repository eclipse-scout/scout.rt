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
package org.eclipse.scout.rt.client.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.progress.ISimpleProgress;
import org.eclipse.scout.rt.client.services.common.progress.ISimpleProgressService;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.ResetButton;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.ResultsGroupBox;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.SettingsGroupBox;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.StatsGroupBox;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.TestButton;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.TestGroupBox;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.ResultsGroupBox.ResultsTableField;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.SettingsGroupBox.Level1Box;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.SettingsGroupBox.Level2Box;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.SettingsGroupBox.Level3Box;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.StatsGroupBox.FailedTestsField;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.StatsGroupBox.SuccessfulTestsField;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.StatsGroupBox.TotalTestsField;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.StatsGroupBox.WarningTestsField;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.TestGroupBox.TestsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.checkbox.AbstractCheckBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.services.common.test.ITest;
import org.eclipse.scout.rt.shared.services.common.test.TestUtility;
import org.eclipse.scout.service.SERVICES;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 */
@FormData
@Deprecated
@SuppressWarnings("deprecation")
public class DefaultClientTestForm extends AbstractForm {

  public DefaultClientTestForm() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return "Auto-Test";
  }

  @Override
  protected boolean getConfiguredModal() {
    return false;
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TestGroupBox getTestGroupBox() {
    return getFieldByClass(TestGroupBox.class);
  }

  public SettingsGroupBox getSettingsGroupBox() {
    return getFieldByClass(SettingsGroupBox.class);
  }

  public StatsGroupBox getStatsGroupBox() {
    return getFieldByClass(StatsGroupBox.class);
  }

  public Level1Box getLevel1Box() {
    return getFieldByClass(Level1Box.class);
  }

  public Level2Box getLevel2Box() {
    return getFieldByClass(Level2Box.class);
  }

  public Level3Box getLevel3Box() {
    return getFieldByClass(Level3Box.class);
  }

  public SuccessfulTestsField getSuccessfulTestsField() {
    return getFieldByClass(SuccessfulTestsField.class);
  }

  public FailedTestsField getFailedTestsField() {
    return getFieldByClass(FailedTestsField.class);
  }

  public TotalTestsField getTotalTestsField() {
    return getFieldByClass(TotalTestsField.class);
  }

  public WarningTestsField getWarningTestsField() {
    return getFieldByClass(WarningTestsField.class);
  }

  public ResultsGroupBox getResultsGroupBox() {
    return getFieldByClass(ResultsGroupBox.class);
  }

  public ResultsTableField getResultsTableField() {
    return getFieldByClass(ResultsTableField.class);
  }

  public TestsTableField getTestsTableField() {
    return getFieldByClass(TestsTableField.class);
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  public ResetButton getResetButton() {
    return getFieldByClass(ResetButton.class);
  }

  public TestButton getTestButton() {
    return getFieldByClass(TestButton.class);
  }

  @Order(20.0)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 2;
    }

    @Order(10.0)
    public class TestGroupBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 2;
      }

      @Override
      protected String getConfiguredLabel() {
        return "Select Tests";
      }

      @Order(100.0)
      public class TestsTableField extends AbstractTableField<TestsTableField.Table> {

        @Order(10.0)
        public class Table extends AbstractTable {

          @Override
          protected boolean getConfiguredAutoResizeColumns() {
            return true;
          }

          public ProductColumn getProductColumn() {
            return getColumnSet().getColumnByClass(ProductColumn.class);
          }

          public SubTitleColumn getSubTitleColumn() {
            return getColumnSet().getColumnByClass(SubTitleColumn.class);
          }

          public TitleColumn getTitleColumn() {
            return getColumnSet().getColumnByClass(TitleColumn.class);
          }

          public ClazzColumn getClazzColumn() {
            return getColumnSet().getColumnByClass(ClazzColumn.class);
          }

          @Order(10.0)
          public class ProductColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 120;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "Product";
            }

          }

          @Order(20.0)
          public class TitleColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 170;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "Title";
            }
          }

          @Order(30.0)
          public class SubTitleColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 120;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "SubTitle";
            }
          }

          @Order(80.0)
          public class ClazzColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 310;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "Type";
            }

          }

          @Override
          protected void execInitTable() throws ProcessingException {
          }
        }

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected int getConfiguredGridH() {
          return 10;
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

      }

    }

    @Order(20.0)
    public class SettingsGroupBox extends AbstractGroupBox {

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
        return "Settings";
      }

      @Order(10.0)
      public class Level1Box extends AbstractCheckBox {
        @Override
        protected String getConfiguredLabel() {
          return "OK";
        }
      }

      @Order(20.0)
      public class Level2Box extends AbstractCheckBox {
        @Override
        protected String getConfiguredLabel() {
          return "WARNING";
        }
      }

      @Order(30.0)
      public class Level3Box extends AbstractCheckBox {
        @Override
        protected String getConfiguredLabel() {
          return "ERROR";
        }
      }
    }

    @Order(25.0)
    public class StatsGroupBox extends AbstractGroupBox {

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
        return "Statistics";
      }

      @Order(10.0)
      public class SuccessfulTestsField extends AbstractLongField {
        @Override
        protected String getConfiguredLabel() {
          return "Successful Tests";
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return false;
        }

      }

      @Order(20.0)
      public class WarningTestsField extends AbstractLongField {
        @Override
        protected String getConfiguredLabel() {
          return "Tests with Warnings";
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return false;
        }

      }

      @Order(30.0)
      public class FailedTestsField extends AbstractLongField {
        @Override
        protected String getConfiguredLabel() {
          return "Failed Tests";
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return false;
        }

      }

      @Order(40.0)
      public class TotalTestsField extends AbstractLongField {
        @Override
        protected String getConfiguredLabel() {
          return "Total Tests";
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return false;
        }

      }

    }

    @Order(30.0)
    public class ResultsGroupBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 2;
      }

      @Override
      protected String getConfiguredLabel() {
        return "Results";
      }

      @Order(10.0)
      public class ResultsTableField extends AbstractTableField<ResultsTableField.Table> {

        @Order(10.0)
        public class Table extends AbstractTable {

          @Override
          protected boolean getConfiguredAutoResizeColumns() {
            return true;
          }

          public ProductColumn getProductColumn() {
            return getColumnSet().getColumnByClass(ProductColumn.class);
          }

          public SubTitleColumn getSubTitleColumn() {
            return getColumnSet().getColumnByClass(SubTitleColumn.class);
          }

          public TitleColumn getTitleColumn() {
            return getColumnSet().getColumnByClass(TitleColumn.class);
          }

          public ResultColumn getResultColumn() {
            return getColumnSet().getColumnByClass(ResultColumn.class);
          }

          public DurationColumn getDurationColumn() {
            return getColumnSet().getColumnByClass(DurationColumn.class);
          }

          @Order(10.0)
          public class ProductColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 80;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "Product";
            }

          }

          @Order(20.0)
          public class TitleColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 170;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "Title";
            }

          }

          @Order(30.0)
          public class SubTitleColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 120;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "SubTitle";
            }
          }

          @Order(50.0)
          public class ResultColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 80;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "Result";
            }
          }

          @Order(100.0)
          public class DurationColumn extends AbstractLongColumn {
            @Override
            protected int getConfiguredWidth() {
              return 40;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return "Duration [ms]";
            }

          }

          @Override
          protected void execInitTable() throws ProcessingException {
          }
        }

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected int getConfiguredGridH() {
          return 10;
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }
    }

    @Order(40.0)
    public class CloseButton extends AbstractCloseButton {

      @Override
      protected String getConfiguredLabel() {
        return "Close";
      }
    }

    @Order(50.0)
    public class TestButton extends AbstractButton {

      @Override
      protected String getConfiguredLabel() {
        return "Run Selected Tests";
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        HashSet<String> selectedNames = new HashSet<String>();
        selectedNames.addAll(Arrays.asList(getTestsTableField().getTable().getClazzColumn().getSelectedValues()));
        ArrayList<ITest> tests = new ArrayList<ITest>();
        for (ITest t : SERVICES.getServices(ITest.class)) {
          if (selectedNames.contains(t.getClass().getSimpleName())) {
            tests.add(t);
          }
        }
        runTests(tests.toArray(new ITest[tests.size()]));
      }
    }

    @Order(55.0)
    public class ResetButton extends AbstractButton {

      @Override
      protected String getConfiguredLabel() {
        return "Reset Results";
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getResultsTableField().getTable().discardAllRows();
        getTotalTestsField().setValue(null);
        getFailedTestsField().setValue(null);
        getWarningTestsField().setValue(null);
        getSuccessfulTestsField().setValue(null);
      }

    }

    @Order(60.0)
    public class ExportButton extends AbstractButton {

      @Override
      protected String getConfiguredLabel() {
        return "Export to Excel";
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        exportTestResults();
      }
    }
  }

  public void runTests(ITest[] tests) {
    ISimpleProgressService pgSvc = SERVICES.getService(ISimpleProgressService.class);
    ISimpleProgress pg = pgSvc.addProgress("Test...");
    try {
      FormBasedTestContext ctx = new FormBasedTestContext(DefaultClientTestForm.this);
      TestUtility.runTests(ctx, tests);
    }
    finally {
      pgSvc.removeProgress(pg);
    }
  }

  public void exportTestResults() throws ProcessingException {
    // nop
  }

  @Order(10.0)
  public class NewHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      super.execLoad();
      getLevel1Box().setValue(true);
      getLevel2Box().setValue(true);
      getLevel3Box().setValue(true);
      // load tests
      TestsTableField.Table table = getTestsTableField().getTable();
      try {
        table.setTableChanging(true);
        //
        for (ITest t : SERVICES.getServices(ITest.class)) {
          ITableRow r = table.createRow();
          table.getProductColumn().setValue(r, t.getProduct());
          table.getTitleColumn().setValue(r, t.getTitle());
          table.getSubTitleColumn().setValue(r, t.getSubTitle());
          table.getClazzColumn().setValue(r, t.getClass().getSimpleName());
          table.addRow(r);
        }
      }
      finally {
        table.setTableChanging(false);
      }
      //
      getTestsTableField().getTable().selectAllEnabledRows();
    }

  }

  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  @Override
  protected boolean getConfiguredMaximizeEnabled() {
    return true;
  }

}
