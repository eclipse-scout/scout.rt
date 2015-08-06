/**
 * Composite keystroke to provide a numeric keystroke to select view tabs.
 */
scout.ViewTabSelectKeyStroke = function(desktop) {
  scout.ViewTabSelectKeyStroke.parent.call(this);

  this._enabled = desktop.autoTabKeyStrokesEnabled;
  this._viewTabsController = desktop.viewTabsController;

  this.keyStroke = desktop.autoTabKeyStrokeModifier;
  this.initKeyStrokeParts();
  this.keyBoxDrawed = false;
  this.drawHint = true;

  this.asciiNum1= scout.keys['1'];
  this.asciiNum9 = scout.keys['9'];
};
scout.inherits(scout.ViewTabSelectKeyStroke, scout.KeyStroke);

/**
 * @override Action.js
 */
scout.ViewTabSelectKeyStroke.prototype.handle = function(event) {
  var viewIndex = event.which - this.asciiNum1;

  if (this._viewTabs().length && (viewIndex < this._viewTabs().length)) {
    var viewTab = this._viewTabs()[viewIndex];
    this._viewTabsController.selectViewTab(viewTab);
  }

  event.preventDefault(); // Prevent the browser from interpreting this keystroke, because 'CTRL + n' is typically reserved to select a browser tab.
};

/**
 * @override Action.js
 */
scout.ViewTabSelectKeyStroke.prototype.accept = function(event) {
  return this._enabled &&
      event &&
      event.which >= this.asciiNum1 &&
      event.which <= this.asciiNum9 &&
      event.ctrlKey === this.ctrl &&
      event.altKey === this.alt &&
      event.shiftKey === this.shift;
};

/**
 * @override Action.js
 */
scout.ViewTabSelectKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (scout.keyStrokeBox.keyStrokesAlreadyDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[1], scout.keys[9])) {
    return;
  }
  if (this.drawHint) {
    this._drawKeyBox($container);
    drawedKeys[this.keyStrokeName()] = true;
    scout.keyStrokeBox.keyStrokeRangeDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[1], scout.keys[9]);
  }
};
/**
 * @override Action.js
 */
scout.ViewTabSelectKeyStroke.prototype._drawKeyBox = function($container) {
  if (this.keyBoxDrawed) {
    return;
  }
  if (this._enabled && this._viewTabs()) {
    for (var i = 1; i < this._viewTabs().length + 1; i++) {
      var offsetLeft = 4;
      if (i <= 9) {
        scout.keyStrokeBox.drawSingleKeyBoxItem(offsetLeft, i, this._viewTabs()[i - 1].$container, this.ctrl, this.alt, this.shift, true);
      }
    }
    this.keyBoxDrawed = true;
  }
};
/**
 * @override Action.js
 */
scout.ViewTabSelectKeyStroke.prototype.removeKeyBox = function($container) {
  if (!this.keyBoxDrawed) {
    return;
  }
  for (var i = 0; i < this._viewTabs().length; i++) {
    $('.key-box', this._viewTabs()[i].$container).remove();
    $('.key-box-additional', this._viewTabs()[i].$container).remove();
  }
  this.keyBoxDrawed = false;
};

scout.ViewTabSelectKeyStroke.prototype._viewTabs = function() {
  return this._viewTabsController.viewTabs();
};
