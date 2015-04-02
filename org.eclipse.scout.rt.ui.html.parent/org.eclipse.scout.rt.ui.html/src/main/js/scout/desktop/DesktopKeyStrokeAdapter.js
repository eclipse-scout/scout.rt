scout.DesktopKeyStrokeAdapter = function(desktop) {
  scout.DesktopKeyStrokeAdapter.parent.call(this, desktop.navigation);
  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this._navigation = desktop.navigation;
  this._viewButtonBar = desktop.navigation.menu;
  this._taskbar = desktop.taskbar;
  this._tabs = desktop._allTabs;
  this._desktop = desktop;
  this._viewTabAutoKeyStroke = new scout.ViewTabAutoKeyStroke(desktop.autoTabKeyStrokesEnabled, this._tabs, desktop.autoTabKeyStrokeModifier);

  this.installDesktopModelKeystrokes();
  this.keyStrokes.push(this._viewTabAutoKeyStroke);
};

scout.inherits(scout.DesktopKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.DesktopKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeyStrokes) {
  if (this.keyBoxDrawn) {
    return;
  }

  this.keyBoxDrawn = true;
  this._viewTabAutoKeyStroke.checkAndDrawKeyBox(null, drawedKeyStrokes);

  for (var i = 0; i < this.keyStrokes.length; i++) {
    if (this.keyStrokes[i].$container) {
      this.keyStrokes[i].checkAndDrawKeyBox(this.keyStrokes[i].$container, drawedKeyStrokes);
    }
  }
};

scout.DesktopKeyStrokeAdapter.prototype.removeKeyBox = function() {
  scout.DesktopKeyStrokeAdapter.parent.prototype.removeKeyBox.call(this);
  $('.tree-item-control').css('display', '');
  this._viewTabAutoKeyStroke.removeKeyBox();
  this.keyBoxDrawn = false;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    if (this.keyStrokes[i].$container) {
      this.keyStrokes[i].removeKeyBox(this.keyStrokes[i].$container);
    }
  }

};

scout.DesktopKeyStrokeAdapter.prototype.installDesktopModelKeystrokes = function() {
  scout.DesktopKeyStrokeAdapter.parent.prototype.installModelKeystrokes.call(this);
  if (this.keyStrokes.length > 0 && this._desktop.viewButtons) {
    this.keyStrokes = this.keyStrokes.concat(this._desktop.viewButtons);
  } else if (this._desktop.viewButtons) {
    this.keyStrokes = this._desktop.viewButtons;
  }
  if (this.keyStrokes.length > 0 && this._desktop.keyStrokes) {
    this.keyStrokes = this.keyStrokes.concat(this._desktop.keyStrokes);
  } else if (this._desktop.keyStrokes) {
    this.keyStrokes = this._desktop.keyStrokes;
  }
  if (this.keyStrokes.length > 0 && this._desktop.menus) {
    this.keyStrokes = this.keyStrokes.concat(this._desktop.menus);
  } else if (this._desktop.menus) {
    this.keyStrokes = this._desktop.menus;
  }
  if (this.keyStrokes.length > 0 && this._desktop.toolButtons) {
    this.keyStrokes = this.keyStrokes.concat(this._desktop.toolButtons);
  } else if (this._desktop.toolButtons) {
    this.keyStrokes = this._desktop.toolButtons;
  }
};
