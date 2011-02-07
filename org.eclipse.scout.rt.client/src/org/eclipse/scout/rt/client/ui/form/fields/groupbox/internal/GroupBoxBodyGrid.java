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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import java.util.ArrayList;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;

/**
 * Grid (model) layout of items only visible items and non-process-buttons are
 * used
 */
public class GroupBoxBodyGrid {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(GroupBoxBodyGrid.class);

  private final IGroupBox m_groupBox;
  private IFormField[] m_fieldsExceptProcessButtons;
  private int m_gridColumns;
  private int m_gridRows;

  public GroupBoxBodyGrid(IGroupBox groupBox) {
    m_groupBox = groupBox;
  }

  public void validate() {
    // reset old state
    m_gridRows = 0;
    // STEP 0: column count
    m_gridColumns = -1;
    IGroupBox tmp = m_groupBox;
    while (m_gridColumns < 0 && tmp != null) {
      m_gridColumns = tmp.getGridColumnCountHint();
      tmp = tmp.getParentGroupBox();
    }
    if (m_gridColumns < 0) {
      m_gridColumns = 2;
    }
    int containingGridXYCount = 0;
    int notContainingGridXYCount = 0;
    // build
    ArrayList<IFormField> fieldsExceptProcessButtons = new ArrayList<IFormField>();
    IFormField[] fields = m_groupBox.getFields();
    for (int i = 0; i < fields.length; i++) {
      IFormField f = fields[i];
      if (f.isVisible()) {
        if ((f instanceof IButton) && (((IButton) f).isProcessButton())) {
          // ignore
        }
        else {
          fieldsExceptProcessButtons.add(f);
          GridData hints = f.getGridDataHints();
          if (hints.x >= 0 && hints.y >= 0) {
            containingGridXYCount++;
          }
          else {
            notContainingGridXYCount++;
          }
        }
      }
      else {
        GridData data = GridDataBuilder.createFromHints(f, 1);
        f.setGridDataInternal(data);
      }
    }
    m_fieldsExceptProcessButtons = fieldsExceptProcessButtons.toArray(new IFormField[0]);
    boolean isContainingXAndY = (containingGridXYCount > 0 && notContainingGridXYCount == 0);
    if (isContainingXAndY) {
      layoutAllStatic();
    }
    else {
      layoutAllDynamic();
    }
  }

  /**
   * Make layout based on grid-x, grid-y, grid-w and grid-h No auto-layout
   */
  private void layoutAllStatic() {
    int totalGridW = 1;
    int totalGridH = 0;
    for (int i = 0; i < m_fieldsExceptProcessButtons.length; i++) {
      IFormField f = m_fieldsExceptProcessButtons[i];
      GridData hints = GridDataBuilder.createFromHints(f, 1);
      totalGridW = Math.max(totalGridW, hints.x + hints.w);
      totalGridH = Math.max(totalGridH, hints.y + hints.h);
    }
    for (int i = 0; i < m_fieldsExceptProcessButtons.length; i++) {
      IFormField f = m_fieldsExceptProcessButtons[i];
      GridData hints = GridDataBuilder.createFromHints(f, totalGridW);
      f.setGridDataInternal(hints);
    }
    m_gridColumns = totalGridW;
    m_gridRows = totalGridH;
  }

  /**
   * Make auto-layout based on grid-w and grid-h only. ignores grid-x and grid-y
   */
  private void layoutAllDynamic() {
    // STEP 1: component tree
    GridCell main = null;
    main = layoutControls(main);
    // STEP 2: logical layout
    if (main != null) {
      // set coordinates
      main.calculateGridLayout(0, 0);
      // store height
      m_gridRows = main.getMaxY();
      m_gridColumns = main.getMaxCols();
    }
  }

  private GridCell/* next main */layoutControls(GridCell main) {
    GridCell part = null;
    for (int i = 0; i < m_fieldsExceptProcessButtons.length; i++) {
      IFormField f = m_fieldsExceptProcessButtons[i];
      GridCell c = new GridCell(f, m_gridColumns);
      if (c.data.w >= m_gridColumns) {
        c.data.w = m_gridColumns;
        // layout part of 1-column items and attach to main grid
        if (part != null) {
          if (m_gridColumns > 1) {// split
            int k = m_gridColumns;
            GridCell splitRoot = part;
            while (k >= 2) {
              int maxWeight = splitRoot.getMaxWeight();
              GridCell split = splitRoot.getSplitCell(0, maxWeight, k);
              if (split != null && split != splitRoot) {
                split.getUp().setDown(null);
                int splitOffMaxCols = Math.max(1, splitRoot.getMaxCols());
                //maximum columns in part before split (normally 1)
                splitRoot.setRight(split);
                splitRoot = split;
                // reduce by maximum column count of split-off part
                k = k - splitOffMaxCols;
              }
              else {
                break;
              }
            }// end for k
          }
          if (main == null) {
            main = part;
          }
          else {
            main.addBottomCell(part);
          }
          part = null;
        }// end part!=null
        if (main == null) {
          main = c;
        }
        else {
          main.addBottomCell(c);
        }
      }// end c.w>=m_gridColumns
      else {
        if (part == null) {
          part = c;
        }
        else {
          part.addBottomCell(c);
        }
      }
    }
    // add and split rest
    if (part != null) {
      if (m_gridColumns > 1) {
        int k = m_gridColumns;
        GridCell splitRoot = part;
        while (k >= 2) {
          int maxWeight = splitRoot.getMaxWeight();
          GridCell split = splitRoot.getSplitCell(0, maxWeight, k);
          if (split != null && split != splitRoot) {
            split.getUp().setDown(null);
            int splitOffMaxCols = Math.max(1, splitRoot.getMaxCols());
            // maximum columns in part before split (normally 1)
            splitRoot.setRight(split);
            splitRoot = split;
            // reduce by maximum column count of split-off part
            k = k - splitOffMaxCols;
          }
          else {
            break;
          }
        }// end for k
      }
      if (main == null) {
        main = part;
      }
      else {
        main.addBottomCell(part);
      }
      part = null;
    }
    return main;
  }

  public int getGridColumnCount() {
    return m_gridColumns;
  }

  public int getGridRowCount() {
    return m_gridRows;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getGridRowCount() + " " + getGridColumnCount() + "]";
  }
}
