scout.Form = function() {
  scout.Form.parent.call(this);
  this._$title;
  this._$parent;
  this._resizeHandler;
  this.rootGroupBox;
  this._addAdapterProperties(['rootGroupBox']);
};
scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.prototype.attach = function($parent) {
  if (!this.$container) {
    this.render($parent);
  } else {
    this.$container.appendTo($parent);
    if (this.$glasspane) {
      this.$glasspane.appendTo($parent);
    }
  }
};

scout.Form.prototype.detach= function() {
  if (this.isRendered()) {
    this.$container.detach();
  }
  if (this.$glasspane) {
    this.$glasspane.detach();
  }
};

scout.Form.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = $parent.appendDiv(undefined, 'form');

  this.rootGroupBox.render(this.$container);

  var closeable = false;
  var systemButtons = this.rootGroupBox.getSystemButtons();
  if (systemButtons) {
    // TODO AWE: CSS for button-bar / position / visible
    var $buttonBar = $('<div class="button-bar"></div>');
    var i, button;
    for (i=0; i<systemButtons.length; i++) {
      button = systemButtons[i];
      button.render($buttonBar);
      if (button.visible &&
          button.systemType == scout.Button.SYSTEM_TYPE.CANCEL ||
          button.systemType == scout.Button.SYSTEM_TYPE.CLOSE) {
        closeable = true;
      }
    }
    this.$container.append($buttonBar);
  }

  if (this.displayHint == 'dialog') {
    // TODO AWE: append form title section (including ! ? and progress indicator)
    var $dialogBar = $('<div class="dialog-bar"></div>');
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
    this.$container.addClass('dialog-form');
    this.$container.prepend($dialogBar);
  }

  // we must keep a stable reference to the resize-handler, so we can remove the handler in the dispose method later
  this.rootGroupBox.updateLayout(true);
  this._resizeHandler = this.rootGroupBox.updateLayout.bind(this.rootGroupBox);
  $(window).on('resize', this._resizeHandler);
};

// TODO AWE: (C.GU) hier sollten wir doch besser die setEnabled() method verwenden / überscheiben.
scout.Form.prototype.enable = function() {
  this.$glasspane.remove();
  this.$glasspane = null;
};

scout.Form.prototype.disable = function() {
  this.$glasspane = this._$parent.appendDiv(undefined, 'glasspane'); // FIXME CGU how to do this properly? disable every mouse and keyevent?
  // FIXME CGU adjust values on resize
  this.$glasspane.
    width(this.$container.width()).
    height(this.$container.height()).
    css('top', this.$container.position().top).
    css('left', this.$container.position().left);
};

scout.Form.prototype.destroy = function() {
  scout.Form.parent.prototype.destroy.call(this);
  this.remove();
};

scout.Form.prototype.onModelCreate = function() {};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type_ == 'formClosed') {
    this.destroy();
  } else {
    $.log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};

scout.Form.prototype.dispose = function() {
  scout.Form.parent.prototype.dispose.call(this);
  $(window).off('resize', this._resizeHandler);
  this.rootGroupBox.dispose();
};
