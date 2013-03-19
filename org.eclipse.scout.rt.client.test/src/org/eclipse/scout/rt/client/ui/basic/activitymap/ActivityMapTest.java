package org.eclipse.scout.rt.client.ui.basic.activitymap;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link org.eclipse.scout.rt.client.ui.basic.activitymap.AbstractActivityMap<RI, AI>}
 */
public class ActivityMapTest {

  private ActivityMap activityMap = null;

  @Before
  public void setUp() {
    activityMap = new ActivityMap();
  }

  @Test(expected = ClassCastException.class)
  public void testActivityCellResourceIdCast() {
    final int invalidResourceId = 6;
    final Double activityId = 5.0;
    new ActivityCell<String, Double>(new Object[]{invalidResourceId, activityId}).getResourceId().toString();
  }

  @Test(expected = ClassCastException.class)
  public void testActivityCellActivityIdCast() {
    new ActivityCell<String, Double>(new Object[]{"id1", "invalid type"}).getActivityId().doubleValue();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSetAndGetActivityCells() {
    ActivityCell<String, Double> cell = getTestCell();

    activityMap.addActivityCells(new ActivityCell[]{cell});
    activityMap.setSelectedActivityCell(cell);

    assertEquals(cell.getResourceId(), activityMap.getSelectedActivityCell().getResourceId());
    assertEquals(cell.getActivityId().intValue(), activityMap.getSelectedActivityCell().getActivityId().intValue());
  }

  private ActivityCell<String, Double> getTestCell() {
    final String resourceId = "id1";
    final Double activityId = 5.0;
    final Date startTime = new Date(5000);
    final Date endTime = new Date(6000);
    String text = "text1";
    return new ActivityCell<String, Double>(new Object[]{resourceId, activityId, startTime, endTime, text});
  }

  @Test
  public void testSetAndGetResourceIds() {
    activityMap.setResourceIds(null);
    assertEquals(activityMap.getResourceIds().getClass(), String[].class);
  }

  /**
   * Test activity map with generics
   */
  static class ActivityMap extends AbstractActivityMap<String, Double> {
  }
}
