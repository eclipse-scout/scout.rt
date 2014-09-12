// JavaScript port of LogicalGridLayoutInfo.java
// plus some ports of JRE Swing/AWT classes

// TODO AWE: (layout) check if we can remove other *layout*.js files in this folder
scout.SwingLayoutUtility = function() {
};

scout.SwingLayoutUtility.MIN = 0;
scout.SwingLayoutUtility.PREF = 1;
scout.SwingLayoutUtility.MAX = 2;
scout.SwingLayoutUtility.EPS = 1e-6;

scout.Dimension = function(width, height) {
  this.width = width || 0;
  this.height = height || 0;
};

scout.Dimension.prototype.toString = function() {
  return 'Dimension[width=' + this.width + ' height=' + this.height + ']';
};

scout.Dimension.prototype.equals = function(o) {
  return this.width === o.width &&
    this.height === o.height;
};

scout.Insets = function(top, right, bottom, left) {
  this.top = top || 0;
  this.right = right || 0;
  this.bottom = bottom || 0;
  this.left = left || 0;
};

scout.Insets.prototype.toString = function() {
  return 'Insets[top=' + this.top + ' right=' + this.right + ' bottom=' + this.bottom + ' left=' + this.left + ']';
};

scout.Rectangle = function(x, y, width, height) {
  this.x = x;
  this.y = y;
  this.width = width;
  this.height = height;
};

scout.Rectangle.prototype.toString = function() {
 return 'Rectangle[x=' + this.x + ' y=' + this.y + ' width=' + this.width + ' height=' + this.height + ']';
};

// INFO: copy & paste von der JRE
scout.Rectangle.prototype.union = function(r) {
        var tx2 = this.width;
        var ty2 = this.height;
        if ((tx2 | ty2) < 0) {
            // This rectangle has negative dimensions...
            // If r has non-negative dimensions then it is the answer.
            // If r is non-existant (has a negative dimension), then both
            // are non-existant and we can return any non-existant rectangle
            // as an answer.  Thus, returning r meets that criterion.
            // Either way, r is our answer.
            return new scout.Rectangle(r.x, r.y, r.width, r.height);
        }
        var rx2 = r.width;
        var ry2 = r.height;
        if ((rx2 | ry2) < 0) {
            return new scout.Rectangle(this.x, this.y, this.width, this.height);
        }
        var tx1 = this.x;
        var ty1 = this.y;
        tx2 += tx1;
        ty2 += ty1;
        var rx1 = r.x;
        var ry1 = r.y;
        rx2 += rx1;
        ry2 += ry1;
        if (tx1 > rx1) tx1 = rx1;
        if (ty1 > ry1) ty1 = ry1;
        if (tx2 < rx2) tx2 = rx2;
        if (ty2 < ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 will never underflow since both original rectangles
        // were already proven to be non-empty
        // they might overflow, though...
        if (tx2 > Number.MAX_VALUE) tx2 = Number.MAX_VALUE;
        if (ty2 > Number.MAX_VALUE) ty2 = Number.MAX_VALUE;
        return new scout.Rectangle(tx1, ty1, tx2, ty2);
};

scout.SwingEnvironment = function() {
  this.formRowHeight = 23;
  this.formRowGap = 6;
  this.formColumnWidth = 360;
  this.formColumnGap = 12;
};

scout.TreeSet = function() {
  this.array = [];
  this.properties = {};
};

scout.TreeSet.prototype.add = function(value) {
  if (!this.contains(value)) {
    this.array.push(value);
    this.array.sort();
    this.properties[value] = true;
  }
};

scout.TreeSet.prototype.size = function() {
  return this.array.length;
};

scout.TreeSet.prototype.contains = function(value) {
  return (value in this.properties);
};

scout.TreeSet.prototype.last = function() {
  return this.array[this.array.length - 1];
};

scout.LogicalGridLayoutInfo = function(env, components, cons, hgap, vgap) {
  /*LogicalGridData[] */ this.gridDatas = [];
  /*Component[]*/ this.components = components;
  /*int*/ this.cols;
  /*int*/ this.rows;
  /*int[][]*/ this.width = [];
  /*int[][]*/ this.height = [];
  /*double[]*/ this.weightX;
  /*double[]*/ this.weightY;
  /*private int*/ this.m_hgap = hgap;
  /*private int*/ this.m_vgap = vgap;
  /*private Rectangle[][]*/ this.m_cellBounds = [];

    // create a modifiable copy of the grid datas
    var i, gd, x, y;
    for (i = 0; i < cons.length; i++) {
      this.gridDatas[i] = new scout.LogicalGridData(cons[i]);
    }
    if (components.length === 0) {
      this.cols = 0;
      this.rows = 0;
      this.width = [];
      this.height = [];
      this.weightX = [];
      this.weightY = [];
      return;
    }
    // eliminate unused rows and columns
    var /*TreeSet<Integer>*/ usedCols = new scout.TreeSet();
    var /*TreeSet<Integer>*/ usedRows = new scout.TreeSet();
    // ticket 86645 use member gridDatas instead of param cons
    for (i=0; i<this.gridDatas.length; i++) {
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
        for (i=0; i<this.gridDatas.length; i++) {
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
        for (i=0; i<this.gridDatas.length; i++) {
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
    this.width = []; // new int[cols][3];
    this.height = []; // new int[rows][3];
    this.weightX = []; // new double[cols];
    this.weightY = []; // new double[rows];
    console.log('usedCols='+this.cols+' usedRows='+ this.rows);
    this._initializeInfo(env, hgap, vgap);
};

scout.LogicalGridLayoutInfo.prototype._initializeInfo = function(env, hgap, vgap) {
    var compCount = this.components.length;
    var /*Dimension[]*/ compSize = [];
    // cache component sizes and cleanup constraints
  var comp, cons, d;
    for (var i = 0; i < compCount; i++) {
      /*Component*/ comp = this.components[i];
      /*LogicalGridData*/ cons = this.gridDatas[i];
      /*Dimension*/ d = this.uiSizeInPixel(comp);
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
  console.log('cons='+cons);
  this._initializeColumns(env, compSize, hgap);
  this._initializeRows(env, compSize, vgap);
};

arrayInit = function(length, initValue) {
  var array = [], i;
  for (i=0; i<length; i++) {
    array[i] = initValue;
  }
  return array;
};

scout.LogicalGridLayoutInfo.prototype._initializeColumns = function(env, compSize, hgap) {
  var compCount = compSize.length;
  var prefWidths = arrayInit(this.cols, 0);
  var fixedWidths = arrayInit(this.cols, false);
  var i, j, k, prefw;
  for (i = 0; i < compCount; i++) {
      var cons = this.gridDatas[i];
      if (cons.gridw == 1) {
        if (cons.widthHint > 0) {
          prefw = cons.widthHint;
        }
        else if (cons.useUiWidth) {
          prefw = compSize[i].width;
        }
        else {
          prefw = this.logicalWidthInPixel(env, cons);
        }
        for (j = cons.gridx; j < cons.gridx + cons.gridw && j < this.cols; j++) {
          prefWidths[j] = Math.max(prefWidths[j], prefw);
          if (cons.weightx == 0) {
            fixedWidths[j] = true;
          }
        }
      }
    }
    for (i = 0; i < compCount; i++) {
      var cons = this.gridDatas[i];
      if (cons.gridw > 1) {
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
        }
        else if (cons.useUiWidth) {
          distWidth = compSize[i].width - spanWidth - (hSpan - 1) * hgap;
        }
        else {
          distWidth = this.logicalWidthInPixel(env, cons) - spanWidth - (hSpan - 1) * hgap;
        }
        if (distWidth > 0) {
          var equalWidth = (distWidth + spanWidth) / hSpan;
          var remainder = (distWidth + spanWidth) % hSpan;
          var last = -1;
          for (j = cons.gridx; j < cons.gridx + cons.gridw && j < this.cols; j++) {
            if (fixedWidths[j]) {
              prefWidths[last = j] = prefWidths[j];
            }
            else {
              prefWidths[last = j] = Math.max(equalWidth, prefWidths[j]);
            }
            if (cons.weightx == 0) {
              fixedWidths[j] = true;
            }
          }
          if (last > -1) {
            prefWidths[last] += remainder;
          }
        }
      }
    }

    for (i = 0; i < this.cols; i++) {
      this.width[i] = [];
      if (fixedWidths[i]) {
        this.width[i][scout.SwingLayoutUtility.MIN] = prefWidths[i];
        this.width[i][scout.SwingLayoutUtility.PREF] = prefWidths[i];
        this.width[i][scout.SwingLayoutUtility.MAX] = prefWidths[i];
      }
      else {
        this.width[i][scout.SwingLayoutUtility.MIN] = 0;// must be exactly 0!
        this.width[i][scout.SwingLayoutUtility.PREF] = prefWidths[i];
        this.width[i][scout.SwingLayoutUtility.MAX] = 10240;
      }
    }

    // averaged column weights, normalized so that sum of weights is equal to
    // 1.0
    for (i = 0; i < this.cols; i++) {
      if (fixedWidths[i]) {
        this.weightX[i] = 0;
      }
      else {
        var weightSum = 0;
        var weightCount = 0;
        for (k = 0; k < compCount; k++) {
          var cons = this.gridDatas[k];
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

scout.LogicalGridLayoutInfo.prototype._initializeRows = function(env, compSize, vgap) {
  var compCount = compSize.length;
  var prefHeights = arrayInit(this.rows, 0);
  var fixedHeights = arrayInit(this.rows, false);
  var i, j, k, prefh;
  for (i = 0; i < compCount; i++) {
      var cons = this.gridDatas[i];
      if (cons.gridh == 1) {
        if (cons.heightHint > 0) {
          prefh = cons.heightHint;
        }
        else if (cons.useUiHeight) {
          prefh = compSize[i].height;
        }
        else {
          prefh = this.logicalHeightInPixel(env, cons);
        }
        for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
          prefHeights[j] = Math.max(prefHeights[j], prefh);
          if (cons.weighty == 0) {
            fixedHeights[j] = true;
          }
        }
      }
    }
    for (i = 0; i < compCount; i++) {
      var cons = this.gridDatas[i];
      if (cons.gridh > 1) {
        var vSpan = cons.gridh;
        var spanHeight = 0;
        var distHeight;
        // pref
        for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
          spanHeight += prefHeights[j];
        }
        if (cons.heightHint > 0) {
          distHeight = cons.heightHint - spanHeight - (vSpan - 1) * vgap;
        }
        else if (cons.useUiHeight) {
          distHeight = compSize[i].height - spanHeight - (vSpan - 1) * vgap;
        }
        else {
          distHeight = this.logicalHeightInPixel(env, cons) - spanHeight - (vSpan - 1) * vgap;
        }
        if (distHeight > 0) {
          var equalHeight = (distHeight + spanHeight) / vSpan;
          var remainder = (distHeight + spanHeight) % vSpan;
          var last = -1;
          for (j = cons.gridy; j < cons.gridy + cons.gridh && j < this.rows; j++) {
            prefHeights[last = j] = Math.max(equalHeight, prefHeights[j]);
            if (cons.weighty == 0) {
              fixedHeights[j] = true;
            }
          }
          if (last > -1) {
            prefHeights[last] += remainder;
          }
        }
      }
    }

    for (i = 0; i < this.rows; i++) {
      this.height[i] = [];
      if (fixedHeights[i]) {
        this.height[i][scout.SwingLayoutUtility.MIN] = prefHeights[i];
        this.height[i][scout.SwingLayoutUtility.PREF] = prefHeights[i];
        this.height[i][scout.SwingLayoutUtility.MAX] = prefHeights[i];
      }
      else {
        this.height[i][scout.SwingLayoutUtility.MIN] = 0;// must be exactly 0!
        this.height[i][scout.SwingLayoutUtility.PREF] = prefHeights[i];
        this.height[i][scout.SwingLayoutUtility.MAX] = 10240;
      }
    }

    // averaged row weights, normalized so that sum of weights is equal to 1.0
    for (i = 0; i < this.rows; i++) {
      if (fixedHeights[i]) {
        this.weightY[i] = 0;
      }
      else {
        var weightSum = 0;
        var weightCount = 0;
        for (k = 0; k < compCount; k++) {
          var cons = this.gridDatas[k];
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
    /*int[]*/ var w = this.layoutSizes(size.width - insets.left - insets.right - Math.max(0, (this.cols - 1) * this.m_hgap), this.width, this.weightX);
    /*int[]*/ var h = this.layoutSizes(size.height - insets.top - insets.bottom - Math.max(0, (this.rows - 1) * this.m_vgap), this.height, this.weightY);
    this.m_cellBounds = arrayInit(this.rows, null);
    var y = insets.top, r, x, c;
    for (r = 0; r < this.rows; r++) {
      x = insets.left;
      this.m_cellBounds[r] = arrayInit(this.cols, null);
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
    /*int[]*/ var outSizes = arrayInit(sizes.length, 0);
    if (targetSize <= 0) {
      return [];
    }
    var sumSize = 0;
    var tmpWeight = arrayInit(weights.length, 0.0);
    var sumWeight = 0, i;
    for (i = 0; i < sizes.length; i++) {
      outSizes[i] = sizes[i][scout.SwingLayoutUtility.PREF];
      sumSize += outSizes[i];
      tmpWeight[i] = weights[i];
      /**
       * auto correction: if weight is 0 and min / max sizes are NOT equal then
       * set weight to 1; if weight<eps set it to 0
       */
      if (tmpWeight[i] < scout.SwingLayoutUtility.EPS) {
        if (sizes[i][scout.SwingLayoutUtility.MAX] > sizes[i][scout.SwingLayoutUtility.MIN]) {
          tmpWeight[i] = 1;
        }
        else {
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
      /*float[]*/ var accWeight = arrayInit(tmpWeight.length, 0.0);
      var hasTargets;
      if (deltaInt > 0) {
        // expand
        hasTargets = true;
        while (deltaInt > 0 && hasTargets) {
          hasTargets = false;
          for (i = 0; i < outSizes.length && deltaInt > 0; i++) {
            if (tmpWeight[i] > 0 && outSizes[i] < sizes[i][scout.SwingLayoutUtility.MAX]) {
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
      }
      else {// delta<0
        // shrink
        hasTargets = true;
        while (deltaInt < 0 && hasTargets) {
          hasTargets = false;
          for (i = 0; i < outSizes.length && deltaInt < 0; i++) {
            if (tmpWeight[i] > 0 && outSizes[i] > sizes[i][scout.SwingLayoutUtility.MIN]) {
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

scout.LogicalGridLayoutInfo.prototype.logicalWidthInPixel = function(env, cons) {
  var gridW = cons.gridw;
  return env.formColumnWidth * gridW + env.formColumnGap * Math.max(0, gridW - 1);
};

scout.LogicalGridLayoutInfo.prototype.logicalHeightInPixel = function(env, cons) {
  var gridH = cons.gridh;
  return env.formRowHeight * gridH + env.formRowGap * Math.max(0, gridH - 1);
};

scout.LogicalGridLayoutInfo.prototype.uiSizeInPixel = function($comp) {
  var prefSize, layout;
  layout = $comp.data('layout');
  if (layout) {
    prefSize = layout.preferredLayoutSize($comp);
    $.log('(LogicalGridLayoutInfo#uiSizeInPixel) ' + scout.Layout.debugComponent($comp) + ' impl. preferredSize=' + prefSize);
  } else {
    // TODO: hier koennten wir eigentlich einen fehler werfen, weil das nicht passieren sollte
    prefSize = scout.Dimension($comp.width(), $comp.height());
    $.log('(LogicalGridLayoutInfo#uiSizeInPixel) ' + scout.Layout.debugComponent($comp) + ' size of HTML element=' + prefSize);
  }
  return prefSize;
};


// -----------------

scout.LogicalGridDataBuilder = function() {
};

// ip = input, op = output
scout.LogicalGridDataBuilder.prototype.build = function(ip) {
  var op = new scout.LogicalGridData();
  op.gridx = ip.x;
  op.gridy = ip.y;
  op.gridw = ip.w;
  op.gridh = ip.h;
  op.weightx = ip.weightX;
  op.weighty = ip.weightY;
  if (op.weightx < 0) {
    // inherit
    op.weightx = Math.max(1.0, op.gridw);
  }
  if (op.weighty < 0) {
    // inherit
    // TODO AWE: (layout) impl. _inheritWeightY
    // op.weighty = this._inheritWeightY(m_scoutField);
  }
  op.useUiWidth = ip.useUiWidth;

  // When having the label on top the container of the field must not have a fix size but use the calculated ui height instead.
  /*
  TODO AWE: (layout) impl. label position special handling
  if (m_scoutField.getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
    op.useUiHeight = true;
  }
  else {
  */
    op.useUiHeight = ip.useUiHeight;
  //}

  op.horizontalAlignment = ip.horizontalAlignment;
  op.verticalAlignment = ip.verticalAlignment;
  op.fillHorizontal = ip.fillHorizontal;
  op.fillVertical = ip.fillVertical;
  op.widthHint = ip.widthInPixel;
  op.heightHint = ip.heightInPixel;
  if (op.weighty === 0 || (op.weighty < 0 && op.gridh <= 1)) {
    op.fillVertical = false;
  }
  return op;
};

scout.LogicalGridDataBuilder.prototype._inheritWeightY = function(f) {
// see: SwingScoutFormFieldGridData
};

scout.LogicalGridDataBuilder.prototype._inheritWeightYRec = function(f) {
//see: SwingScoutFormFieldGridData
};

