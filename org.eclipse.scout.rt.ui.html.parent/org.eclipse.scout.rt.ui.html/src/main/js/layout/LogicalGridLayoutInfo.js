// FIXME AWE: (layout) work in progress
// JavaScript port of LogicalGridLayoutInfo.java

scout = function() {}

scout.SwingLayoutUtility = function() {
};

scout.SwingLayoutUtility.MIN = 0;
scout.SwingLayoutUtility.PREF = 1;
scout.SwingLayoutUtility.MAX = 2;
scout.SwingLayoutUtility.EPS = 1e-6;

scout.Dimension = function(width, height) {
  this.width = width;
  this.height = height;
};

scout.Insets = function(top, right, bottom, left) {
  this.top = left;
  this.right = left;
  this.bottom = left;
  this.left = left;
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

scout.SwingEnvironment = function() {
  this.formRowHeight = 28;
  this.formRowGap = 8;
  this.formColumnWidth = 360;
  this.formColumnGap = 8;
};

scout.LogicalGridData = function(template) {
  this.gridx;
  this.gridy;
  this.gridw = 1;
  this.gridh = 1;
  this.weightx;
  this.weighty;
  this.useUiWidth;
  this.useUiHeight;
  this.widthHint;
  this.heightHint;
  this.horizontalAlignment = -1;
  this.verticalAlignment = -1;
  this.fillHorizontal = true;
  this.fillVertical = true;
  this.topInset;
  if (template) {
    this.gridx = template.gridx;
    this.gridy = template.gridy;
    this.gridw = template.gridw;
    this.gridh = template.gridh;
    this.weightx = template.weightx;
    this.weighty = template.weighty;
    this.useUiWidth = template.useUiWidth;
    this.useUiHeight = template.useUiHeight;
    this.widthHint = template.widthHint;
    this.heightHint = template.heightHint;
    this.horizontalAlignment = template.horizontalAlignment;
    this.verticalAlignment = template.verticalAlignment;
    this.fillHorizontal = template.fillHorizontal;
    this.fillVertical = template.fillVertical;
    this.topInset = template.topInset;
  }
};

arrayContains = function(array, element) {
  for (var i=0; i<array.length; i++) {
    if (array[i] === element) {
    return true;
  }
  }
  return false;
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
    if (components.length == 0) {
      this.cols = 0;
      this.rows = 0;
      this.width = [];
      this.height = [];
      this.weightX = [];
      this.weightY = [];
      return;
    }
    // eliminate unused rows and columns
    var /*TreeSet<Integer>*/ usedCols = [];
    var /*TreeSet<Integer>*/ usedRows = [];
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
        usedCols.push(x);
      }
      for (y = gd.gridy; y < gd.gridy + gd.gridh; y++) {
        usedRows.push(y);
      }
    }
  usedCols.sort();
  usedRows.sort();
    var maxCol = usedCols[usedCols.length -1];
    for (x = maxCol; x >= 0; x--) {
      if (!arrayContains(usedCols, x)) {
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
    var maxRow = usedRows[usedRows.length -1];
    for (y = maxRow; y >= 0; y--) {
      if (!arrayContains(usedRows, y)) {
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
    this.cols = usedCols.length;
    this.rows = usedRows.length;
    this.width = []; // new int[cols][3];
    this.height = []; // new int[rows][3];
    this.weightX = []; // new double[cols];
    this.weightY = []; // new double[rows];
  console.log('usedCols='+usedCols+' usedRows='+usedRows);
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
    //this._initializeRows(env, compSize, vgap);
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
          distWidth = logicalWidthInPixel(env, cons) - spanWidth - (hSpan - 1) * hgap;
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

scout.LogicalGridLayoutInfo.prototype.uiSizeInPixel = function(comp) {
  return new scout.Dimension(100, 20 /*comp.getPreferredSize()*/); // FIXME AWE: das haben wir leider nicht
};

