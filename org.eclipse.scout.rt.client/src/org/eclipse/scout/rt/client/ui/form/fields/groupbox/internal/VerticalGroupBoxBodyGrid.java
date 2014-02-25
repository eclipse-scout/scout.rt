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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

/**
 * Grid (model) layout of items only visible items and non-process-buttons are
 * used
 */
public class VerticalGroupBoxBodyGrid extends AbstractGroupBoxBodyGrid {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(VerticalGroupBoxBodyGrid.class);

  public VerticalGroupBoxBodyGrid(IGroupBox groupBox) {
    super(groupBox);
  }

  /**
   * Make auto-layout based on grid-w and grid-h only. ignores grid-x and grid-y
   */
  @Override
  protected void layoutAllDynamic() {
    // STEP 1: component tree
    GridCell main = null;
    main = layoutControls(main);
    // STEP 2: logical layout
    if (main != null) {
      // set coordinates
      main.calculateGridLayout(0, 0);
      // store height
      setGridRows(main.getMaxY());
      setGridColumns(main.getMaxCols());
    }
  }

  private GridCell/* next main */layoutControls(GridCell main) {
    GridCell part = null;
    int gridColumns = getGridColumnCount();
    for (IFormField f : getFieldsExceptProcessButtons()) {
      GridCell c = new GridCell(f, gridColumns);
      if (c.data.w >= gridColumns) {
        c.data.w = gridColumns;
        // layout part of 1-column items and attach to main grid
        if (part != null) {
          if (gridColumns > 1) {// split
            int k = gridColumns;
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
      if (gridColumns > 1) {
        int k = gridColumns;
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

}
