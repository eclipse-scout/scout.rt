scout.DesktopKeyStrokeAdapter = function(desktop) {
  scout.DesktopKeyStrokeAdapter.parent.call(this, desktop.navigation);
  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this._navigation = desktop.navigation;
  this._viewButtonBar = desktop.navigation.menu;
  this._taskbar = desktop.taskbar;
  this._tabs = desktop._allTabs;
  this._viewTabAutoKeyStroke = new scout.ViewTabAutoKeyStroke(desktop.autoTabKeyStrokesEnabled, this._tabs, desktop.autoTabKeyStrokeModifier);

  if (this._taskbar) {
    //    $('.taskbar-item', this._taskbar.$container).each(function(i, element) {
    //      var keyStroke = $(element).attr('data-shortcut');
    //      if (keyStroke) {
    //        keyStroke = keyStroke.toUpperCase();
    //        var shortcut = parseInt(keyStroke.replace('F', ''), 10) + 111;
    //        that.handlers.push({
    //          $element: $(element),
    //          accept: function(event) {
    //            if (event && event.which === shortcut && event.ctrlKey) {
    //              return true;
    //            }
    //            return false;
    //          },
    //          handle: function(event) {
    //            this.$element.click();
    //
    //            return false;
    //          }
    //        });
    //      }
    //    });
  }

  this.keyStrokes.push(this._viewTabAutoKeyStroke);
};

scout.inherits(scout.DesktopKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.DesktopKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeystrokes) {
  if (this.keyBoxDrawn) {
    return;
  }

  this.keyBoxDrawn = true;
  this._viewTabAutoKeyStroke.checkAndDrawKeyBox(null, drawedKeystrokes);
  if (this._taskbar) {
    $('.taskbar-item', this._taskbar.$container).each(function(i, e) {
      $(e).appendDiv('key-box', $(e).attr('data-shortcut'));
    });
  }

  if (this._viewButtonBar) {
    $('.view-item', this._viewButtonBar.$div).each(function(i, element) {
      if (i < 9) {
        $(element).appendDiv('key-box', i + 1);
      }
    });
  }
};

scout.DesktopKeyStrokeAdapter.prototype.removeKeyBox = function() {
  scout.DesktopKeyStrokeAdapter.parent.prototype.removeKeyBox.call(this);
  $('.tree-item-control').css('display', '');
  this._viewTabAutoKeyStroke.removeKeyBox();
  this.keyBoxDrawn = false;
};
