/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * JavaScript port of org.eclipse.scout.rt.ui.swing.LogicalGridLayoutInfo.
 */
scout.LogicalGridLayoutInfo = function($components, cons, hgap, vgap, options) {
  this.gridDatas = [];
  this.$components = $components;
  this.cols;
  this.rows;
  this.width = [];
  this.height = [];
  this.weightX;
  this.weightY;
  this.m_hgap = hgap;
  this.m_vgap = vgap;
  this.m_cellBounds = [];
  this.options = options || {};

  // create a modifiable copy of the grid datas
  var i, gd, x, y;
  for (i = 0; i < cons.length; i++) {
    this.gridDatas[i] = new scout.LogicalGridData(cons[i]);
  }
  if ($components.length === 0) {
    this.cols = 0;
    this.rows = 0;
    this.width = [];
    this.height = [];
    this.weightX = [];
    this.weightY = [];
    return;
  }
  // eliminate unused rows and columns
  var usedCols = new scout.TreeSet();
  var usedRows = new scout.TreeSet();
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
  var maxCol = usedCols.last();
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
  var maxRow = usedRows.last();
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

  this.logicalColumnWidth = scout.HtmlEnvironment.formColumnWidth;
  this.lastLogicalColumnWidthDiff = 0;
  if (this.options.widthHint) {
    this.logicalColumnWidth = Math.floor((this.options.widthHint - (this.m_hgap * Math.max(0, this.cols - 1))) / this.cols);
    this.lastLogicalColumnWidthDiff = options.widthHint - (this.logicalColumnWidth * this.cols) - (this.m_hgap * Math.max(0, this.cols - 1));
  }

  this.width = [];
  this.height = [];
  this.weightX = [];
  this.weightY = [];
  $.log.trace('(LogicalGridLayoutInfo#CTOR) $components.length=' + $components.length + ' usedCols=' + this.cols + ' usedRows=' + this.rows);
  this._initializeInfo(hgap, vgap);
  console.log('--- LogicalGridLayoutInfo initialized [widthHint=' + options.widthHint + ', heightHint=' + options.heightHint + ']');
};

scout.LogicalGridLayoutInfo.prototype._initializeInfo = function(hgap, vgap) {
  var comp,
    compCount = this.$components.length,
    compSize = [];
  // cache component sizes and cleanup constraints
  var $comp, cons, d;
  for (var i = 0; i < compCount; i++) {
    $comp = this.$components[i];
    cons = this.gridDatas[i];
    if (cons.useUiHeight || cons.useUiWidth || !cons.fillVertical || !cons.fillHorizontal) {
      var useUiWidth = cons.useUiWidth || !cons.fillHorizontal;
      var useUiHeight = cons.useUiHeight || !cons.fillVertical;
      // Only read preferred size if really needed by the logical grid layout
      var innerOptions = {};
      var innerMargins = new scout.Insets();
      if (useUiWidth && useUiHeight) {
        // Don't propagate hints if both dimensions should use uiSize
      } else {
        innerMargins = scout.graphics.getMargins($comp);
        if (useUiHeight) {
          var wh = this.options.widthHint;
          if (!wh) {
            wh = Math.max(cons.widthHint, 0);
          }
          if (!wh) {
            wh = this.logicalWidthInPixel(cons);
          }
          var fWidth = Math.floor((wh - Math.max(0, this.cols - 1) * hgap) / this.cols);
          var deltaX = wh - (this.cols * fWidth) - (Math.max(0, this.cols - 1) * hgap);
          var isLastCol = (cons.gridx + cons.gridw === this.cols);
          innerOptions.widthHint = (cons.gridw * fWidth) + (Math.max(0, cons.gridw - 1) * hgap) + (isLastCol ? deltaX : 0) - innerMargins.horizontal();
        }
        if (useUiWidth) {
          var hh = this.options.heightHint;
          if (!hh) {
            hh = Math.max(cons.heightHint, 0);
          }
          if (!hh) {
            hh = this.logicalHeightInPixel(cons);
          }
          var fHeight = Math.floor((hh - Math.max(0, this.rows - 1) * vgap) / this.rows);
          var deltaY = hh - (this.rows * fHeight) - (Math.max(0, this.rows - 1) * vgap);
          var isLastRow = (cons.gridy + cons.gridh === this.rows);
          innerOptions.heightHint = (cons.gridh * fHeight) + (Math.max(0, cons.gridh - 1) * vgap) + (isLastRow ? deltaY : 0) - innerMargins.vertical();
        }
      }
      d = this.uiSizeInPixel($comp, innerOptions)
        .add(innerMargins);
      if ($.log.isTraceEnabled()) {
        comp = scout.HtmlComponent.optGet($comp);
        $.log.trace('(LogicalGridLayoutInfo#initializeInfo $comp = ' + comp ? comp.debug() : '' + ' size=' + d);
      }
    } else {
      d = new scout.Dimension(0, 0);
    }
    if (cons.widthHint > 0) {
      d.width = cons.widthHint;
    }
    if (cons.heightHint > 0) {
      d.height = cons.heightHint;
    }
    compSize[i] = d;
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
  }
  this.compSize = compSize;
  this._initializeColumns(compSize, hgap);
  this._initializeRows(compSize, vgap);
};

scout.LogicalGridLayoutInfo.prototype._initializeColumns = function(compSize, hgap) {
  var compCount = compSize.length;
  var prefWidths = scout.arrays.init(this.cols, 0);
  var fixedWidths = scout.arrays.init(this.cols, false);
  var i, j, k, prefw, cons;
  for (i = 0; i < compCount; i++) {
    cons = this.gridDatas[i];
    if (cons.gridw === 1 && false) { // XXX
      if (cons.widthHint > 0) {
        prefw = cons.widthHint;
      } else if (cons.useUiWidth || cons.useUiHeight) {
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
    if (cons.gridw > 1 || true) { // XXX
      var hSpan = cons.gridw;
      var spanWidth = 0;
      var distWidth;
      // pref
      for (j = cons.gridx; j < cons.gridx + cons.gridw && j < this.cols; j++) {
        if (!fixedWidths[j]) {
          spanWidth += prefWidths[j];
        }
      }
      if (cons.widthHint > 0) {
        distWidth = cons.widthHint - spanWidth - (hSpan - 1) * hgap;
      } else if (cons.useUiWidth){//} || cons.useUiHeight) {
        distWidth = compSize[i].width - spanWidth - (hSpan - 1) * hgap;
      } else {
        distWidth = this.logicalWidthInPixel(cons) - spanWidth - (hSpan - 1) * hgap;
      }
      if (distWidth > 0) {
        var equalWidth = Math.floor((distWidth + spanWidth) / hSpan);
        var remainder = (distWidth + spanWidth) % hSpan;
        for (j = cons.gridx; j < cons.gridx + cons.gridw && j < this.cols; j++) {
          if (fixedWidths[j]) {
            continue;
          }
          var last = (j === Math.min(cons.gridx + cons.gridw, this.cols) - 1);
          prefWidths[j] = Math.max(equalWidth + (last ? remainder : 0), prefWidths[j]);
          if (cons.weightx === 0) {
            fixedWidths[j] = true;
          }
        }
      }
    }
  }

  var lc = scout.LayoutConstants;
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
      var weightSum = 0;
      var weightCount = 0;
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
  var sumWeightX = 0;
  for (i = 0; i < this.cols; i++) {
    sumWeightX += this.weightX[i];
  }
  if (sumWeightX >= 1e-6) {
    var f = 1.0 / sumWeightX;
    for (i = 0; i < this.cols; i++) {
      this.weightX[i] = this.weightX[i] * f;
    }
  }
};

scout.LogicalGridLayoutInfo.prototype._initializeRows = function(compSize, vgap) {
  var compCount = compSize.length;
  var prefHeights = scout.arrays.init(this.rows, 0);
  var fixedHeights = scout.arrays.init(this.rows, false);
  var i, j, k, prefh, cons;
  for (i = 0; i < compCount; i++) {
    cons = this.gridDatas[i];
    if (cons.gridh === 1 && false) { // XXX
      if (cons.heightHint > 0) {
        prefh = cons.heightHint;
      } else if (cons.useUiHeight) {//} || cons.useUiWidth) {
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
    if (cons.gridh > 1 || true) { // XXX
      var vSpan = cons.gridh;
      var spanHeight = 0;
      var distHeight;
      // pref
      for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
        if (!fixedHeights[j]) {
          spanHeight += prefHeights[j];
        }
      }
      if (cons.heightHint > 0) {
        distHeight = cons.heightHint - spanHeight - (vSpan - 1) * vgap;
      } else if (cons.useUiHeight || cons.useUiWidth) {
        distHeight = compSize[i].height - spanHeight - (vSpan - 1) * vgap;
      } else {
        distHeight = this.logicalHeightInPixel(cons) - spanHeight - (vSpan - 1) * vgap;
      }
      if (distHeight > 0) {
        var equalHeight = Math.floor((distHeight + spanHeight) / vSpan);
        var remainder = (distHeight + spanHeight) % vSpan;
        for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
          if (fixedHeights[j]) {
            continue;
          }
          var last = (j === Math.min(cons.gridy + cons.gridh, this.rows) - 1);
          prefHeights[j] = Math.max(equalHeight + (last ? remainder : 0), prefHeights[j]);
          if (cons.weighty === 0) {
            fixedHeights[j] = true;
          }
        }
      }
    }
  }

  var lc = scout.LayoutConstants;
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
      var weightSum = 0;
      var weightCount = 0;
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
  var sumWeightY = 0;
  for (i = 0; i < this.rows; i++) {
    sumWeightY += this.weightY[i];
  }
  if (sumWeightY >= 1e-6) {
    var f = 1.0 / sumWeightY;
    for (i = 0; i < this.rows; i++) {
      this.weightY[i] = this.weightY[i] * f;
    }
  }
};

scout.LogicalGridLayoutInfo.prototype.layoutCellBounds = function(size, insets) {
  var w = this.layoutSizes(size.width - insets.horizontal() - (Math.max(0, this.cols - 1) * this.m_hgap), this.width, this.weightX);
  var h = this.layoutSizes(size.height - insets.vertical() - (Math.max(0, this.rows - 1) * this.m_vgap), this.height, this.weightY);
  this.m_cellBounds = scout.arrays.init(this.rows, null);
  var y = insets.top,
    r, x, c;
  for (r = 0; r < this.rows; r++) {
    x = insets.left;
    this.m_cellBounds[r] = scout.arrays.init(this.cols, null);
    for (c = 0; c < this.cols; c++) {
      this.m_cellBounds[r][c] = new scout.Rectangle(x, y, w[c], h[r]);
      x += w[c];
      x += this.m_hgap;
    }
    y += h[r];
    y += this.m_vgap;
  }
  return this.m_cellBounds;
};

scout.LogicalGridLayoutInfo.prototype.layoutSizes = function(targetSize, sizes, weights) {
  var i;
  var outSizes = scout.arrays.init(sizes.length, 0);
  if (targetSize <= 0) {
    for (i = 0; i < sizes.length; i++) {
      outSizes[i] = sizes[i][scout.LayoutConstants.MIN];
    }
    return outSizes;
  }
  var sumSize = 0;
  var tmpWeight = scout.arrays.init(weights.length, 0.0);
  var sumWeight = 0;
  for (i = 0; i < sizes.length; i++) {
    outSizes[i] = sizes[i][scout.LayoutConstants.PREF];
    sumSize += outSizes[i];
    tmpWeight[i] = weights[i];
    /**
     * auto correction: if weight is 0 and min / max sizes are NOT equal then
     * set weight to 1; if weight<eps set it to 0
     */
    if (tmpWeight[i] < scout.LayoutConstants.EPS) {
      if (sizes[i][scout.LayoutConstants.MAX] > sizes[i][scout.LayoutConstants.MIN]) {
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
  var deltaInt = targetSize - sumSize;
  // expand or shrink
  if (Math.abs(deltaInt) > 0) {
    // setup accumulators
    /*float[]*/
    var accWeight = scout.arrays.init(tmpWeight.length, 0.0);
    var hasTargets;
    if (deltaInt > 0) {
      // expand
      hasTargets = true;
      while (deltaInt > 0 && hasTargets) {
        hasTargets = false;
        for (i = 0; i < outSizes.length && deltaInt > 0; i++) {
          if (tmpWeight[i] > 0 && outSizes[i] < sizes[i][scout.LayoutConstants.MAX]) {
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
    } else { // delta<0
      // shrink
      hasTargets = true;
      while (deltaInt < 0 && hasTargets) {
        hasTargets = false;
        for (i = 0; i < outSizes.length && deltaInt < 0; i++) {
          if (tmpWeight[i] > 0 && outSizes[i] > sizes[i][scout.LayoutConstants.MIN]) {
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
};

scout.LogicalGridLayoutInfo.prototype.logicalWidthInPixel = function(cons) {
  var logicalColumnWidth = this.logicalColumnWidth;
  var lastColumn = (cons.gridx + cons.gridw === this.cols);
  return (cons.gridw * logicalColumnWidth) +
    (Math.max(0, cons.gridw - 1) * this.m_hgap) +
    (lastColumn ? this.lastLogicalColumnWidthDiff : 0);
  //  var gridW = cons.gridw;
  //  return (scout.HtmlEnvironment.formColumnWidth * gridW) + (this.m_hgap * Math.max(0, gridW - 1));
};

scout.LogicalGridLayoutInfo.prototype.logicalHeightInPixel = function(cons) {
  var gridH = cons.gridh,
    addition = cons.logicalRowHeightAddition || 0;
  return (scout.HtmlEnvironment.formRowHeight * gridH) + (this.m_vgap * Math.max(0, gridH - 1)) + addition;
};

scout.LogicalGridLayoutInfo.prototype.uiSizeInPixel = function($comp, options) {
  if (options.widthHint && options.heightHint) {
    return new scout.Dimension(options.widthHint, options.heightHint);
  }
  var htmlComp = scout.HtmlComponent.get($comp);
  return htmlComp.getPreferredSize(options);
};
