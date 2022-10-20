/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Dimension, HtmlComponent, LayoutConstants, LogicalGridData, Rectangle, TreeSet} from '../../index';
import $ from 'jquery';

/**
 * JavaScript port of org.eclipse.scout.rt.ui.swing.LogicalGridLayoutInfo.
 */
export default class LogicalGridLayoutInfo {

  constructor(model) {
    this.gridDatas = [];
    this.$components = null;
    this.cols = 0;
    this.compSize = [];
    this.rows = 0;
    this.width = [];
    this.widthHints = [];
    this.height = [];
    this.weightX = [];
    this.weightY = [];
    this.hgap = 0;
    this.vgap = 0;
    this.rowHeight = 0;
    this.columnWidth = 0;
    this.cellBounds = [];
    this.widthHint = null;
    this.widthOnly = false;
    $.extend(this, model);

    // create a modifiable copy of the grid datas
    let i, gd, x, y;
    for (i = 0; i < this.cons.length; i++) {
      this.gridDatas[i] = new LogicalGridData(this.cons[i]);
    }
    if (this.$components.length === 0) {
      return;
    }
    // eliminate unused rows and columns
    let usedCols = new TreeSet();
    let usedRows = new TreeSet();
    // ticket 86645 use member gridDatas instead of param cons
    for (i = 0; i < this.gridDatas.length; i++) {
      gd = this.gridDatas[i];
      if (gd.gridx < 0) {
        gd.gridx = 0;
      }
      if (gd.gridy < 0) {
        gd.gridy = 0;
      }
      if (gd.gridw < 1) {
        gd.gridw = 1;
      }
      if (gd.gridh < 1) {
        gd.gridh = 1;
      }
      for (x = gd.gridx; x < gd.gridx + gd.gridw; x++) {
        usedCols.add(x);
      }
      for (y = gd.gridy; y < gd.gridy + gd.gridh; y++) {
        usedRows.add(y);
      }
    }
    let maxCol = usedCols.last();
    for (x = maxCol; x >= 0; x--) {
      if (!usedCols.contains(x)) {
        // eliminate column
        // ticket 86645 use member gridDatas instead of param cons
        for (i = 0; i < this.gridDatas.length; i++) {
          gd = this.gridDatas[i];
          if (gd.gridx > x) {
            gd.gridx--;
          }
        }
      }
    }
    let maxRow = usedRows.last();
    for (y = maxRow; y >= 0; y--) {
      if (!usedRows.contains(y)) {
        // eliminate row
        // ticket 86645 use member gridDatas instead of param cons
        for (i = 0; i < this.gridDatas.length; i++) {
          gd = this.gridDatas[i];
          if (gd.gridy > y) {
            // ticket 86645
            gd.gridy--;
          }
        }
      }
    }
    //
    this.cols = usedCols.size();
    this.rows = usedRows.size();

    $.log.isTraceEnabled() && $.log.trace('(LogicalGridLayoutInfo#CTOR) $components.length=' + this.$components.length + ' usedCols=' + this.cols + ' usedRows=' + this.rows);
    this._initializeInfo();
  }

  _initializeInfo() {
    let compCount = this.$components.length;
    let uiHeightElements = [];
    for (let i = 0; i < compCount; i++) {
      // cleanup constraints
      let $comp = this.$components[i];
      let cons = this.gridDatas[i];
      if (cons.gridx < 0) {
        cons.gridx = 0;
      }
      if (cons.gridy < 0) {
        cons.gridy = 0;
      }
      if (cons.gridw < 1) {
        cons.gridw = 1;
      }
      if (cons.gridh < 1) {
        cons.gridh = 1;
      }
      if (cons.gridx >= this.cols) {
        cons.gridx = this.cols - 1;
      }
      if (cons.gridy >= this.rows) {
        cons.gridy = this.rows - 1;
      }
      if (cons.gridx + cons.gridw - 1 >= this.cols) {
        cons.gridw = this.cols - cons.gridx;
      }
      if (cons.gridy + cons.gridh >= this.rows) {
        cons.gridh = this.rows - cons.gridy;
      }

      // Calculate and cache component size
      let size = new Dimension(0, 0);
      if (cons.widthHint > 0) {
        // Use explicit width hint, if set
        size.width = cons.widthHint;
        // eslint-disable-next-line brace-style
      } else if (cons.useUiWidth || !cons.fillHorizontal) {
        // Calculate preferred width otherwise
        // This size is needed by _initializeColumns
        // But only if really needed by the logical grid layout (because it is expensive)
        size = this.uiSizeInPixel($comp, cons);
      }
      if (cons.heightHint > 0) {
        // Use explicit height hint, if set
        size.height = cons.heightHint;
      } else if (cons.useUiHeight || !cons.fillVertical) {
        // Otherwise check if preferred height should be calculated.
        // Don't do it now because because weightX need to be calculated first to get the correct width hints
        uiHeightElements.push({
          cons: cons,
          $comp: $comp,
          index: i
        });
      }
      this.compSize[i] = size;
    }

    // Calculate this.width and this.weightX
    this._initializeColumns();

    if (this.widthOnly) {
      // Abort here if only width is of interest
      this.height = arrays.init(this.rows, [0, 0, 0]);
      return;
    }

    // Calculate preferred heights using the width hints
    if (this.widthHint && uiHeightElements.length > 0) {
      let totalHGap = Math.max(0, (this.cols - 1) * this.hgap);
      this.widthHints = this.layoutSizes(this.widthHint - totalHGap, this.width, this.weightX);
    }
    uiHeightElements.forEach(function(elem) {
      let $comp = elem.$comp;
      let cons = elem.cons;
      let widthHint = this.widthHintForGridData(cons);
      if (!cons.fillHorizontal) {
        widthHint = Math.min(widthHint, this.compSize[elem.index].width);
      }
      this.compSize[elem.index] = this.uiSizeInPixel($comp, cons, {
        widthHint: widthHint
      });
    }, this);

    // Calculate this.height and this.weightY
    this._initializeRows();
  }

  _initializeColumns() {
    let compSize = this.compSize;
    let compCount = compSize.length;
    let prefWidths = arrays.init(this.cols, 0);
    let fixedWidths = arrays.init(this.cols, false);
    let i, j, k, prefw, cons;
    for (i = 0; i < compCount; i++) {
      cons = this.gridDatas[i];
      if (cons.gridw === 1) {
        if (cons.widthHint > 0) {
          prefw = cons.widthHint;
        } else if (cons.useUiWidth) {
          prefw = compSize[i].width;
        } else {
          prefw = this.logicalWidthInPixel(cons);
        }
        prefw = Math.floor(prefw);
        for (j = cons.gridx; j < cons.gridx + cons.gridw && j < this.cols; j++) {
          prefWidths[j] = Math.max(prefWidths[j], prefw);
          if (cons.weightx === 0) {
            fixedWidths[j] = true;
          }
        }
      }
    }
    for (i = 0; i < compCount; i++) {
      cons = this.gridDatas[i];
      if (cons.gridw > 1) {
        let hSpan = cons.gridw;
        let spanWidth = 0;
        let distWidth;
        // pref
        for (j = cons.gridx; j < cons.gridx + cons.gridw && j < this.cols; j++) {
          if (!fixedWidths[j]) {
            spanWidth += prefWidths[j];
          }
        }
        if (cons.widthHint > 0) {
          distWidth = cons.widthHint - spanWidth - (hSpan - 1) * this.hgap;
        } else if (cons.useUiWidth) {
          distWidth = compSize[i].width - spanWidth - (hSpan - 1) * this.hgap;
        } else {
          distWidth = this.logicalWidthInPixel(cons) - spanWidth - (hSpan - 1) * this.hgap;
        }
        if (distWidth > 0) {
          let equalWidth = Math.floor((distWidth + spanWidth) / hSpan);
          let remainder = (distWidth + spanWidth) % hSpan;
          let last = -1;
          for (j = cons.gridx; j < cons.gridx + cons.gridw && j < this.cols; j++) {
            last = j;
            if (!fixedWidths[j]) {
              prefWidths[j] = Math.max(equalWidth, prefWidths[j]);
            }
            if (cons.weightx === 0) {
              fixedWidths[j] = true;
            }
          }
          if (last > -1) {
            prefWidths[last] += remainder;
          }
        }
      }
    }

    let lc = LayoutConstants;
    for (i = 0; i < this.cols; i++) {
      this.width[i] = [];
      if (fixedWidths[i]) {
        this.width[i][lc.MIN] = prefWidths[i];
        this.width[i][lc.PREF] = prefWidths[i];
        this.width[i][lc.MAX] = prefWidths[i];
      } else {
        this.width[i][lc.MIN] = 0; // must be exactly 0!
        this.width[i][lc.PREF] = prefWidths[i];
        this.width[i][lc.MAX] = 10240;
      }
    }

    // averaged column weights, normalized so that sum of weights is equal to
    // 1.0
    for (i = 0; i < this.cols; i++) {
      if (fixedWidths[i]) {
        this.weightX[i] = 0;
      } else {
        let weightSum = 0;
        let weightCount = 0;
        for (k = 0; k < compCount; k++) {
          cons = this.gridDatas[k];
          if (cons.weightx > 0 && cons.gridx <= i && i <= cons.gridx + cons.gridw - 1) {
            weightSum += (cons.weightx / cons.gridw);
            weightCount++;
          }
        }
        this.weightX[i] = (weightCount > 0 ? weightSum / weightCount : 0);
      }
    }
    let sumWeightX = 0;
    for (i = 0; i < this.cols; i++) {
      sumWeightX += this.weightX[i];
    }
    if (sumWeightX >= 1e-6) {
      let f = 1.0 / sumWeightX;
      for (i = 0; i < this.cols; i++) {
        this.weightX[i] = this.weightX[i] * f;
      }
    }
  }

  _initializeRows() {
    let compSize = this.compSize;
    let compCount = compSize.length;
    let prefHeights = arrays.init(this.rows, 0);
    let fixedHeights = arrays.init(this.rows, false);
    let i, j, k, prefh, cons;
    for (i = 0; i < compCount; i++) {
      cons = this.gridDatas[i];
      if (cons.gridh === 1) {
        if (cons.heightHint > 0) {
          prefh = cons.heightHint;
        } else if (cons.useUiHeight) {
          prefh = compSize[i].height;
        } else {
          prefh = this.logicalHeightInPixel(cons);
        }
        prefh = Math.floor(prefh);
        for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
          prefHeights[j] = Math.max(prefHeights[j], prefh);
          if (cons.weighty === 0) {
            fixedHeights[j] = true;
          }
        }
      }
    }
    for (i = 0; i < compCount; i++) {
      cons = this.gridDatas[i];
      if (cons.gridh > 1) {
        let vSpan = cons.gridh;
        let spanHeight = 0;
        let distHeight;
        // pref
        for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
          if (!fixedHeights[j]) {
            spanHeight += prefHeights[j];
          }
        }
        if (cons.heightHint > 0) {
          distHeight = cons.heightHint - spanHeight - (vSpan - 1) * this.vgap;
        } else if (cons.useUiHeight) {
          distHeight = compSize[i].height - spanHeight - (vSpan - 1) * this.vgap;
        } else {
          distHeight = this.logicalHeightInPixel(cons) - spanHeight - (vSpan - 1) * this.vgap;
        }
        if (distHeight > 0) {
          let equalHeight = Math.floor((distHeight + spanHeight) / vSpan);
          let remainder = (distHeight + spanHeight) % vSpan;
          let last = -1;
          for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
            last = j;
            if (!fixedHeights[j]) {
              prefHeights[j] = Math.max(equalHeight, prefHeights[j]);
            }
            if (cons.weighty === 0) {
              fixedHeights[j] = true;
            }
          }
          if (last > -1) {
            prefHeights[last] += remainder;
          }
        }
      }
    }

    let lc = LayoutConstants;
    for (i = 0; i < this.rows; i++) {
      this.height[i] = [];
      if (fixedHeights[i]) {
        this.height[i][lc.MIN] = prefHeights[i];
        this.height[i][lc.PREF] = prefHeights[i];
        this.height[i][lc.MAX] = prefHeights[i];
      } else {
        this.height[i][lc.MIN] = 0; // must be exactly 0!
        this.height[i][lc.PREF] = prefHeights[i];
        this.height[i][lc.MAX] = 10240;
      }
    }

    // averaged row weights, normalized so that sum of weights is equal to 1.0
    for (i = 0; i < this.rows; i++) {
      if (fixedHeights[i]) {
        this.weightY[i] = 0;
      } else {
        let weightSum = 0;
        let weightCount = 0;
        for (k = 0; k < compCount; k++) {
          cons = this.gridDatas[k];
          if (cons.weighty > 0 && cons.gridy <= i && i <= cons.gridy + cons.gridh - 1) {
            weightSum += (cons.weighty / cons.gridh);
            weightCount++;
          }
        }
        this.weightY[i] = (weightCount > 0 ? weightSum / weightCount : 0);
      }
    }
    let sumWeightY = 0;
    for (i = 0; i < this.rows; i++) {
      sumWeightY += this.weightY[i];
    }
    if (sumWeightY >= 1e-6) {
      let f = 1.0 / sumWeightY;
      for (i = 0; i < this.rows; i++) {
        this.weightY[i] = this.weightY[i] * f;
      }
    }
  }

  layoutCellBounds(size, insets) {
    let w = this.layoutSizes(size.width - insets.horizontal() - Math.max(0, (this.cols - 1) * this.hgap), this.width, this.weightX);
    let h = this.layoutSizes(size.height - insets.vertical() - Math.max(0, (this.rows - 1) * this.vgap), this.height, this.weightY);
    this.cellBounds = arrays.init(this.rows, null);
    let y = insets.top,
      r, x, c;
    for (r = 0; r < this.rows; r++) {
      x = insets.left;
      this.cellBounds[r] = arrays.init(this.cols, null);
      for (c = 0; c < this.cols; c++) {
        this.cellBounds[r][c] = new Rectangle(x, y, w[c], h[r]);
        x += w[c];
        x += this.hgap;
      }
      y += h[r];
      y += this.vgap;
    }
    return this.cellBounds;
  }

  layoutSizes(targetSize, sizes, weights) {
    let i;
    let outSizes = arrays.init(sizes.length, 0);
    if (targetSize <= 0) {
      for (i = 0; i < sizes.length; i++) {
        outSizes[i] = sizes[i][LayoutConstants.MIN];
      }
      return outSizes;
    }
    let sumSize = 0;
    let tmpWeight = arrays.init(weights.length, 0.0);
    let sumWeight = 0;
    for (i = 0; i < sizes.length; i++) {
      outSizes[i] = sizes[i][LayoutConstants.PREF];
      sumSize += outSizes[i];
      tmpWeight[i] = weights[i];
      /**
       * auto correction: if weight is 0 and min / max sizes are NOT equal then
       * set weight to 1; if weight<eps set it to 0
       */
      if (tmpWeight[i] < LayoutConstants.EPS) {
        if (sizes[i][LayoutConstants.MAX] > sizes[i][LayoutConstants.MIN]) {
          tmpWeight[i] = 1;
        } else {
          tmpWeight[i] = 0;
        }
      }
      sumWeight += tmpWeight[i];
    }
    // normalize weights
    if (sumWeight > 0) {
      for (i = 0; i < tmpWeight.length; i++) {
        tmpWeight[i] = tmpWeight[i] / sumWeight;
      }
    }
    let deltaInt = targetSize - sumSize;
    // expand or shrink
    if (Math.abs(deltaInt) > 0) {
      // setup accumulators
      /* float[] */
      let accWeight = arrays.init(tmpWeight.length, 0.0);
      let hasTargets;
      if (deltaInt > 0) {
        // expand, if delta is > 0
        hasTargets = true;
        while (deltaInt > 0 && hasTargets) {
          hasTargets = false;
          for (i = 0; i < outSizes.length && deltaInt > 0; i++) {
            if (tmpWeight[i] > 0 && outSizes[i] < sizes[i][LayoutConstants.MAX]) {
              hasTargets = true;
              accWeight[i] += tmpWeight[i];
              if (accWeight[i] > 0) {
                accWeight[i] -= 1;
                outSizes[i] += 1;
                deltaInt -= 1;
              }
            }
          }
        }
      } else {
        // shrink, if delta is <= 0
        hasTargets = true;
        while (deltaInt < 0 && hasTargets) {
          hasTargets = false;
          for (i = 0; i < outSizes.length && deltaInt < 0; i++) {
            if (tmpWeight[i] > 0 && outSizes[i] > sizes[i][LayoutConstants.MIN]) {
              hasTargets = true;
              accWeight[i] += tmpWeight[i];
              if (accWeight[i] > 0) {
                accWeight[i] -= 1;
                outSizes[i] -= 1;
                deltaInt += 1;
              }
            }
          }
        }
      }
    }
    return outSizes;
  }

  logicalWidthInPixel(cons) {
    let gridW = cons.gridw;
    return (this.columnWidth * gridW) + (this.hgap * Math.max(0, gridW - 1));
  }

  logicalHeightInPixel(cons) {
    let gridH = cons.gridh,
      addition = cons.logicalRowHeightAddition || 0;
    return (this.rowHeight * gridH) + (this.vgap * Math.max(0, gridH - 1)) + addition;
  }

  uiSizeInPixel($comp, cons, options) {
    let htmlComp = HtmlComponent.get($comp);
    return htmlComp.prefSize(options).add(htmlComp.margins());
  }

  /**
   * @returns {number|null} the width hint for the given gridData
   */
  widthHintForGridData(gridData) {
    if (this.widthHints.length === 0) {
      return null;
    }
    let widthHint = (gridData.gridw - 1) * this.hgap;
    for (let i = gridData.gridx; i < gridData.gridx + gridData.gridw; i++) {
      widthHint += this.widthHints[i];
    }
    return widthHint;
  }
}
