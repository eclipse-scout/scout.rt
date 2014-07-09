scout.Form = function() {
  scout.Form.parent.call(this);
  this._$title;
  this._$parent;
  this.rootGroupBox;
  this._addAdapterProperties('rootGroupBox');
  this._locked;
};
scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = $parent.appendDiv(undefined, 'form');
  this.$container.data('model', this);

  this.rootGroupBox.render(this.$container);

  var closeable = false;
  var detachable = true; // FIXME How to determine 'detachable' property?
  var systemButtons = this.rootGroupBox.getSystemButtons();
  if (systemButtons) {
    // TODO AWE: CSS for button-bar / position / visible
    var $buttonBar = $.makeDiv('', 'button-bar', '');
    var i, button;
    for (i = 0; i < systemButtons.length; i++) {
      button = systemButtons[i];
      button.render($buttonBar);
      if (button.visible && (button.systemType == scout.Button.SYSTEM_TYPE.CANCEL || button.systemType == scout.Button.SYSTEM_TYPE.CLOSE)) {
        closeable = true;
      }
    }
    this.$container.append($buttonBar);
  }

  if (this.displayHint == 'dialog') {
    // TODO AWE: append form title section (including ! ? and progress indicator)
    var $dialogBar = $.makeDiv('', 'dialog-bar', '');
    var $dialogTitle = $('<span class="dialog-title">' + this.title + '</span>');
    $dialogBar.append($dialogTitle);
    if (closeable) {
      var $closeButton = $('<button>X</button>');
      $dialogBar.append($closeButton);
      var that = this;
      $closeButton.on('click', function() {
        that.session.send('formClosing', that.id);
      });
    }
    if (detachable) {
      var $detachButton = $('<button title="Detach form">D</button>');
      $dialogBar.append($detachButton);
      $detachButton.on('click', function() {
        // FIXME BSH Set correct url or write content
        //        w.document.write('<html><head><title>Test</title></head><body>Hello</body></html>');
        //        w.document.close(); //finish "loading" the page
        var w = scout.openWindow(window.location.href, 'scout:form:' + that.id, 800, 600);
        w.parentScout = window.scout;
      });
    }
    this.$container.addClass('dialog-form');
    this.$container.prepend($dialogBar);
  }

  if (this._locked) {
    this.disable();
  }
};

scout.Form.prototype._remove = function() {
  scout.Form.parent.prototype._remove.call(this);

  if (this.$glasspane) {
    this.$glasspane.remove();
  }
};

scout.Form.prototype.appendTo = function($parent) {
  this.$container.appendTo($parent);
  if (this.$glasspane) {
    this.$glasspane.appendTo($parent);
  }
};

// TODO AWE: (C.GU) hier sollten wir doch besser die setEnabled() method verwenden / überscheiben.
scout.Form.prototype.enable = function() {
  this.$glasspane.remove();
  this.$glasspane = null;
};

scout.Form.prototype.disable = function() {
  this.$glasspane = this._$parent.appendDiv(undefined, 'glasspane'); // FIXME CGU how to do this properly? disable every mouse and keyevent?
  // FIXME CGU adjust values on resize
  this.$glasspane
    .width(this.$container.width())
    .height(this.$container.height())
    .css('top', this.$container.position().top)
    .css('left', this.$container.position().left);
};

scout.Form.prototype.onModelCreate = function() {};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type_ == 'formClosed') {
    this.destroy();
  } else {
    $.log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};
