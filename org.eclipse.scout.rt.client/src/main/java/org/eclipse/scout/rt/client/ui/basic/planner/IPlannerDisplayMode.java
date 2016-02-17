/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.planner;

import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendarDisplayMode;

/**
 * Interface providing display-modes for planner (extends calendar).
 */

public interface IPlannerDisplayMode extends ICalendarDisplayMode {

  int CALENDAR_WEEK = 5;
  int YEAR = 6;

}
