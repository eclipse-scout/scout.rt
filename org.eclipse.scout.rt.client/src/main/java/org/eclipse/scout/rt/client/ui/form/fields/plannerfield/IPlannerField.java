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

import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * The planner contains a list of resources that are associated with 0..n timeline activities.<br>
 * As "meeting planner", the subjects are "persons" and the activities are "busy/free" timespans.
 * <p>
 * This class strintly uses java.util.Date staticly AND dynamicly<br>
 * All Date-Objects and subclasses are run through
 * {@link org.eclipse.scout.rt.platform.time.bsiag.DateUtility#toUtilDate(java.util.Date)}()
 */
public interface IPlannerField<P extends IPlanner<?, ?>> extends IFormField {

  String PROP_SPLITTER_POSITION = "splitterPosition";

  P getPlanner();

  int getSplitterPosition();

  void setSplitterPosition(int splitterPosition);

  /**
   * (re)load table data
   */
  void loadResources();

  IPlannerFieldUIFacade getUIFacade();

}
