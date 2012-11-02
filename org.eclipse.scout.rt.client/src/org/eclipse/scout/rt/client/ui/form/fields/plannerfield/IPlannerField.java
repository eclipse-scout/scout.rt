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
package org.eclipse.scout.rt.client.ui.form.fields.plannerfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * The planner contains a list of resources that are associated with 0..n
 * timeline activities.<br>
 * As "meeting planner", the subjects are "persons" and the activities are
 * "busy/free" timespans.
 * <p>
 * This class strintly uses java.util.Date staticly AND dynamicly<br>
 * All Date-Objects and subclasses are run through {@link com.bsiag.DateUtility#toUtilDate(java.util.Date)}()
 * <p>
 * The planner field contains a ITable and a IPlanner<br>
 * The inner table contains at least one primary key column, the first primary key column of type Long is assumed to be
 * the resourceId column.<br>
 * which values are passed to {@link #execLoadActivityMapData(ITableRow[])} and {@link #execLoadActivities(ITableRow[])}
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class ExamplePlannerField extends AbstractPlannerField&lt;ExamplePlannerField.ResourceTable, ExamplePlannerField.ActivityMap&gt; {
 *   public class ResourceTable extends AbstractTable {
 *     public class ResourceIdColumn extends AbstractLongColumn {
 *     }
 *   }
 * 
 *   public class ActivityMap extends AbstractActivityMap {
 *   }
 * }
 * </pre>
 */

public interface IPlannerField<T extends ITable, P extends IActivityMap> extends IFormField {

  String PROP_MINI_CALENDAR_COUNT = "miniCalendarCount";

  String PROP_SPLITTER_POSITION = "splitterPosition";

  T getResourceTable();

  P getActivityMap();

  int getMiniCalendarCount();

  void setMiniCalendarCount(int n);

  int getSplitterPosition();

  void setSplitterPosition(int splitterPosition);

  /**
   * (re)load table data
   */
  void loadResourceTableData() throws ProcessingException;

  /**
   * (re)load activity map data
   */
  void loadActivityMapData() throws ProcessingException;

  /**
   * (re)load activity map data of selected resources only
   */
  void loadActivityMapDataOfSelectedRecources() throws ProcessingException;

  ITableRow activityCellToResourceRow(ActivityCell activityCell);

  ITableRow[] activityCellsToResourceRows(ActivityCell[] activityCells);

  ActivityCell[] resourceRowToActivityCells(ITableRow resourceRow);

  ActivityCell[] resourceRowsToActivityCells(ITableRow[] resourceRows);

  IPlannerFieldUIFacade getUIFacade();

}
