package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ProfilesBox.ProfilesTableField;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ProfilesBox.ProfilesTableField.Table;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ProfilesBox.ProfilesTableField.Table.NewMenu;
import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Regresion tests for {@link OrganizeColumnsForm}
 *
 * @since 7.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OrganizeColumnsFormRegressionTest {
  private final IntegerHolder lastFocusedRowIndex;
  private Table profilesTable;

  public OrganizeColumnsFormRegressionTest() {
    lastFocusedRowIndex = new IntegerHolder();
  }

  /***
   * Regression test: Even when deleting and re-adding profiles, adding a new profile should always cause the "name"
   * column of the new row to be selected.
   */
  @Test
  public void testFocusWhenAddingProfile() {
    givenAProfilesTable();

    whenAddingARow();
    thenFocusWasRequestedInRow(0);

    whenAddingARow();
    thenFocusWasRequestedInRow(1);

    whenDeletingRow(1);
    Assert.assertEquals(1, profilesTable.getRowCount());

    whenAddingARow();
    // crucial: this previously triggered a regression, since no focus was requested at all.
    thenFocusWasRequestedInRow(1);
  }

  private void givenAProfilesTable() {
    ITable table = mock(ITable.class);
    OrganizeColumnsForm form = new OrganizeColumnsForm(table);

    profilesTable = form.getProfilesTableField().getTable();

    profilesTable.addTableListener(new TableListener() {

      @Override
      public void tableChangedBatch(List<? extends TableEvent> batch) {
        for (TableEvent e : batch) {
          tableChanged(e);
        }
      }

      @Override
      public void tableChanged(TableEvent e) {
        if (e.getType() == TableEvent.TYPE_REQUEST_FOCUS_IN_CELL) {
          IColumn focusedColumn = CollectionUtility.firstElement(e.getColumns());
          ITableRow focusedRow = CollectionUtility.firstElement(e.getRows());

          if (focusedColumn.equals(profilesTable.getConfigNameColumn())) {
            lastFocusedRowIndex.setValue(focusedRow.getRowIndex());
          }
        }
      }
    });

    profilesTable.deselectAllRows();
  }

  private void whenDeletingRow(int rowIndex) {
    profilesTable.selectRow(rowIndex);
    profilesTable.getMenuByClass(ProfilesTableField.Table.DeleteMenu.class).doAction();
    ModelJobs.yield();
  }

  private void thenFocusWasRequestedInRow(Integer rowIndex) {
    String expectedFocus = lastFocusedRowIndex.getValue() == null ? "not requested at all" : "requested in index" + lastFocusedRowIndex.getValue();
    Assert.assertEquals(
        "Focus should have been requested in row " + rowIndex +
            ", but was actually " + expectedFocus + ".",
        rowIndex,
        lastFocusedRowIndex.getValue());
    lastFocusedRowIndex.setValue(null);
  }

  private void whenAddingARow() {
    NewMenu newMenu = profilesTable.getMenuByClass(ProfilesTableField.Table.NewMenu.class);
    newMenu.doAction();
    ModelJobs.yield();
  }
}
