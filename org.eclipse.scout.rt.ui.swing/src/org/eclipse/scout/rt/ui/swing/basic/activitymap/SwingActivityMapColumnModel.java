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
package org.eclipse.scout.rt.ui.swing.basic.activitymap;

import org.eclipse.scout.rt.client.ui.basic.activitymap.MajorTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.activitymap.MinorTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.ActivityMapColumnModel;

public class SwingActivityMapColumnModel implements ActivityMapColumnModel {
  private TimeScale m_scale;

  public SwingActivityMapColumnModel(TimeScale scale) {
    m_scale = scale;
  }

  public Object[] getMajorColumns() {
    return m_scale.getMajorTimeColumns();
  }

  public String getMajorColumnText(Object column, int size) {
    MajorTimeColumn m = (MajorTimeColumn) column;
    switch (size) {
      case SMALL: {
        return m.getSmallText();
      }
      case MEDIUM: {
        return m.getMediumText();
      }
      case LARGE: {
        return m.getLargeText();
      }
    }
    return null;
  }

  public String getMajorColumnTooltipText(Object column) {
    MajorTimeColumn m = (MajorTimeColumn) column;
    return m.getTooltipText();
  }

  public double[] getMajorColumnRange(Object majorColumn) {
    return m_scale.getRangeOf((MajorTimeColumn) majorColumn);
  }

  public Object[] getMinorColumns(Object majorColumn) {
    MajorTimeColumn m = (MajorTimeColumn) majorColumn;
    return m.getMinorTimeColumns();
  }

  public String getMinorColumnText(Object column, int size) {
    MinorTimeColumn m = (MinorTimeColumn) column;
    switch (size) {
      case SMALL: {
        return m.getSmallText();
      }
      case MEDIUM: {
        return m.getMediumText();
      }
      case LARGE: {
        return m.getLargeText();
      }
    }
    return null;
  }

  public String getMinorColumnTooltipText(Object column) {
    MinorTimeColumn m = (MinorTimeColumn) column;
    return m.getTooltipText();
  }

  public double[] getMinorColumnRange(Object minorColumn) {
    return m_scale.getRangeOf((MinorTimeColumn) minorColumn);
  }

  public double[] snapRange(double d) {
    return m_scale.snapRange(d);
  }

}
