package org.eclipse.scout.rt.client.ui.form.fields.plannerfield;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.activitymap.AbstractActivityMap;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.PlannerFieldTest.PlannerField.ActivityMap;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.PlannerFieldTest.PlannerField.ResourceTable;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField<T, P, RI, AI>}
 */
public class PlannerFieldTest {
  private PlannerField plannerField = null;

  @Before
  public void setUp() {
    plannerField = new PlannerField();
  }

  /**
   * @throws ProcessingException
   */
  @Test
  public void testActivityCellToResourceRow() throws ProcessingException {
    plannerField.loadResourceTableData();
    ITableRow row = plannerField.activityCellToResourceRow(new ActivityCell<String, Integer>("id1", null));
    String value = plannerField.getResourceTable().getResourceIdColumn().getValue(row);

    assertEquals("id1", value);
  }

  /**
   * Test field
   */
  public class PlannerField extends AbstractPlannerField<ResourceTable, ActivityMap, String, Integer> {

    @Override
    protected Object[][] execLoadResourceTableData() throws ProcessingException {
      return new Object[][]{{"id1", 5, new Date(500), new Date(600), "text"}};
    }

    @Override
    protected Object[][] execLoadActivityMapData(String[] resourceIds, ITableRow[] resourceRows) throws ProcessingException {
      return new Object[0][];
    }

    public class ResourceTable extends AbstractTable {
      public ResourceIdColumn getResourceIdColumn() throws ProcessingException {
        return getColumnSet().getColumnByClass(ResourceIdColumn.class);
      }

      @Order(10.0f)
      public class ResourceIdColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredPrimaryKey() {
          return true;
        }
      }
    }

    public class ActivityMap extends AbstractActivityMap<String, Integer> {
    }
  }
}
