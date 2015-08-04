scout.DesktopKeyStrokeAdapter = function(desktop) {
  scout.DesktopKeyStrokeAdapter.parent.call(this, desktop.navigation);
  this._navigation = desktop.navigation;
  this._viewButtonBar = desktop.navigation.menu;
  this._taskbar = desktop.taskbar;
  this._tabs = desktop._allTabs;
  this._desktop = desktop;
  this._viewTabAutoKeyStroke = new scout.ViewTabAutoKeyStroke(desktop.autoTabKeyStrokesEnabled, this._tabs, desktop.autoTabKeyStrokeModifier);

  this.registerKeyStroke(this._viewTabAutoKeyStroke);
  this.registerKeyStroke(new scout.DesktopBackspaceKeyStroke());
  this.installDesktopModelKeystrokes();
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
  $('.tree-node-control').css('display', '');
  this._viewTabAutoKeyStroke.removeKeyBox();
  this.keyBoxDrawn = false;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    if (this.keyStrokes[i].$container) {
      this.keyStrokes[i].removeKeyBox(this.keyStrokes[i].$container);
    }
  }

};

scout.DesktopKeyStrokeAdapter.prototype.installDesktopModelKeystrokes = function() {
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
  if (this.keyStrokes.length > 0 && this._desktop.actions) {
    this.keyStrokes = this.keyStrokes.concat(this._desktop.actions);
  } else if (this._desktop.actions) {
    this.keyStrokes = this._desktop.actions;
  }
};

scout.DesktopBackspaceKeyStroke = function() {
  scout.DesktopBackspaceKeyStroke.parent.call(this);
  this.keyStroke = 'Backspace';
  this.drawHint = false;
  this.initKeyStrokeParts();
  this.preventDefaultOnEvent = true;
};
scout.inherits(scout.DesktopBackspaceKeyStroke, scout.KeyStroke);

scout.DesktopBackspaceKeyStroke.prototype.handle = function(event) {};
