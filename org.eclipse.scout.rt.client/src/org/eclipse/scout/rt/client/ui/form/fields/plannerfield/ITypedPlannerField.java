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
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCellTyped;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMapTyped;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * In Scout 3.9 IPlannerField will be typed and this class will be removed.
 * The planner contains a list of resources that are associated with 0..n
 * timeline activities.<br>
 * As "meeting planner", the subjects are "persons" and the activities are
 * "busy/free" timespans.
 * <p>
 * This class strintly uses java.util.Date staticly AND dynamicly<br>
 * All Date-Objects and subclasses are run through {@link com.bsiag.DateUtility#toUtilDate(java.util.Date)}()
 * <p>
 * The planner field contains one inner class extending {@link ITable} and another one extending
 * {@link IActivityMapTyped}<br>
 * The inner table contains a primary key column, the type of the primary key column corresponds with RI the type
 * parameter for the resourceId. The the type parameter AI for the activityId is passed on together with RI to
 * {@link IActivityMapTyped}<br>
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class ExamplePlannerField extends AbstractPlannerField&lt;ExamplePlannerField.ResourceTable, ExamplePlannerField.ActivityMap, Long, Long&gt; {
 * 
 *   protected Object[][] execLoadActivityMapData(Long[] resourceIds, ITableRow[] resourceRows) throws ProcessingException {
 *    return ...;
 *   }
 * 
 *   public class ResourceTable extends AbstractTable {
 *     public class ResourceIdColumn extends AbstractColumn&lt;Long&gt; {
 *     }
 *   }
 * 
 *   public class ActivityMap extends AbstractActivityMap&lt;Long, Long&gt; {
 *     protected void execCellAction(Long resourceId, MinorTimeColumn column, ActivityCell&lt;Long, Long&gt; activityCell) throws ProcessingException {
 *     }
 * 
 *     protected void execDecorateActivityCell(ActivityCell&lt;Long, Long&gt; cell) {
 *     }
 *   }
 * }
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * </pre>
 */

public interface ITypedPlannerField<T extends ITable, P extends IActivityMapTyped<RI, AI>, RI, AI> extends IFormField {

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

  ITableRow activityCellToResourceRow(ActivityCellTyped<RI, AI> activityCell);

  ITableRow[] activityCellsToResourceRows(ActivityCellTyped<RI, AI>[] activityCells);

  ActivityCellTyped<RI, AI>[] resourceRowToActivityCells(ITableRow resourceRow);

  ActivityCellTyped<RI, AI>[] resourceRowsToActivityCells(ITableRow[] resourceRows);

  IPlannerFieldUIFacade getUIFacade();

}
