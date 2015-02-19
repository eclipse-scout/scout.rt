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
package org.eclipse.scout.rt.ui.swing.window.desktop;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.ui.swing.window.desktop.layout.IMultiSplitStrategy;

/**
 * Column split of a 3x3 matrix
 * <p>
 * 4 splits per row
 * <p>
 * Caching of split locations
 */
public class ColumnSplitStrategy implements IMultiSplitStrategy {
  protected int m_span;
  protected int[][] m_location;
  protected int[][] m_definedLocation;
  private Job m_storageJob;
  private final ClientUIPreferences m_prefs;

  public ColumnSplitStrategy(ClientUIPreferences prefs) {
    m_prefs = prefs;
    m_span = 1000;
    m_definedLocation = new int[][]{new int[]{0, 250, 750, 1000}, new int[]{0, 250, 750, 1000}, new int[]{0, 250, 750, 1000}};
    if (m_prefs != null) {
      int[][] saved = null;
      try {
        saved = validateImportedLocations(m_prefs.getDesktopColumnSplits(3, 4));
      }
      catch (Throwable t) {
        //nop
      }
      if (saved != null) {
        for (int r = 0; r < m_definedLocation.length; r++) {
          for (int c = 0; c < m_definedLocation[r].length; c++) {
            m_definedLocation[r][c] = saved[r][c];
          }
        }
      }
    }
    //
    m_location = new int[m_definedLocation.length][m_definedLocation[0].length];
    for (int r = 0; r < m_definedLocation.length; r++) {
      for (int c = 0; c < m_definedLocation[r].length; c++) {
        m_location[r][c] = m_definedLocation[r][c];
      }
    }
  }

  protected int[][] validateImportedLocations(int[][] in) {
    if (in != null && in.length == 3 && in[0].length == 4) {
      for (int r = 0; r < in.length; r++) {
        in[r][0] = 0;
        if (r == 0) {
          if (in[r][3] < 60) {
            in[r][3] = 60;
          }
        }
        else {
          in[r][3] = in[0][3];
        }
        //
        if (in[r][1] - 20 < in[r][0]) {
          in[r][1] = in[r][0] + 20;
        }
        if (in[r][2] - 20 < in[r][1]) {
          in[r][2] = in[r][1] + 20;
        }
        //
        if (in[r][1] + 20 > in[r][2]) {
          in[r][1] = in[r][2] - 20;
        }
        if (in[r][2] + 20 > in[r][3]) {
          in[r][2] = in[r][3] - 20;
        }
        //
        if (in[r][1] + 20 > in[r][2]) {
          int mid = (in[r][1] + in[r][2]) / 2;
          in[r][1] = mid - 10;
          in[r][2] = mid + 10;
        }
      }
      return in;
    }
    return null;
  }

  @Override
  public void updateSpan(int newSpan) {
    m_span = newSpan;
    for (int r = 0; r < 3; r++) {
      int rightColumnWidth = m_definedLocation[r][3] - m_definedLocation[r][2];
      m_location[r][3] = m_span;
      m_location[r][2] = m_span - rightColumnWidth;
      m_location[r][1] = m_definedLocation[r][1];
      if (m_location[r][1] + 20 > m_location[r][2]) {
        int mid = (m_location[r][1] + m_location[r][2]) / 2;
        m_location[r][1] = mid - 10;
        m_location[r][2] = mid + 10;
      }
    }
  }

  @Override
  public int getSplitLocation(int row, int col) {
    return m_location[row][col];
  }

  @Override
  public void setSplitLocation(int row, int col, int newLocation) {
    switch (col) {
      case 0: {
        //nop
        break;
      }
      case 1: {
        if (m_location[row][0] + 20 < newLocation && newLocation + 20 < m_location[row][2]) {
          m_location[row][col] = newLocation;
        }
        break;
      }
      case 2: {
        if (m_location[row][1] + 20 < newLocation && newLocation + 20 < m_location[row][3]) {
          m_location[row][col] = newLocation;
        }
        break;
      }
      case 3: {
        //nop
        break;
      }
    }
    //save new values as new definition
    for (int r = 0; r < m_definedLocation.length; r++) {
      for (int c = 0; c < m_definedLocation[r].length; c++) {
        m_definedLocation[r][c] = m_location[r][c];
      }
    }
    enqueueStore();
  }

  protected void enqueueStore() {
    if (m_prefs == null) {
      return;
    }
    if (m_storageJob == null) {
      m_storageJob = new Job("Store column splits") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          m_prefs.setDesktopColumnSplits(m_definedLocation);
          return Status.OK_STATUS;
        }
      };
    }
    m_storageJob.cancel();
    m_storageJob.schedule(400L);
  }
}
