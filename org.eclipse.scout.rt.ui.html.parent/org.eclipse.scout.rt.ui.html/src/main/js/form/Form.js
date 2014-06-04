scout.Form = function(model, session) {
  scout.Form.parent.call(this, model, session);
  this._$title;
  this._$parent;
  this._resizeHandler;
  this._rootGroupBox;
};

scout.inherits(scout.Form, scout.ModelAdapter);

/**
 * @override
 */
scout.Form.prototype.attach = function($parent) {
  if (!this.$container) {
    this._render($parent);
    this._applyModel();
  } else {
    this.$container.appendTo($parent);
    if (this.$glasspane) {
      this.$glasspane.appendTo($parent);
    }
  }
};

scout.Form.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = $parent.appendDiv(undefined, 'form');

  // TODO AWE: append form title section (including ! ? and progress indicator)
  // TODO AWE: append [X] when form is dialog and closable.
  this._$title = this.$container.appendDiv(undefined, 'form-title', this.model.title);

  var rootGroupBox = this.session.widgetMap[this.model.rootGroupBox.id];
  if (!rootGroupBox) {
    rootGroupBox =  this.session.objectFactory.create(this.model.rootGroupBox);
  }
  rootGroupBox.attach(this.$container);
  this._rootGroupBox = rootGroupBox;

  // TODO AWE: (form) collect system buttons

  if (this.model.displayHint == 'dialog') {
    this.$container.addClass('dialog-form');

    // TODO AWE: (form) hacky hack: add something to close the form
    var closeButton = $('<button>Close [X]</button>');
    closeButton.prependTo(this.$container);
    var that = this;
    closeButton.on('click', function() {
      that.session.send('formClosing', that.model.id);
    });
  }

  // we must keep a stable reference to the resize-handler, so we can remove the handler in the dispose method later
  this._rootGroupBox.updateLayout();
  this._resizeHandler = this._rootGroupBox.updateLayout.bind(this._rootGroupBox);
  $(window).on('resize', this._resizeHandler);
};

scout.Form.prototype.detach = function() {
  scout.Form.parent.prototype.detach.call(this);
  this._rootGroupBox.dispose();
};

// TODO AWE: (C.GU) hier sollten wir doch besser die setEnabled() method verwenden / überscheiben.
scout.Form.prototype.enable = function() {
  this.$glasspane.remove();
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

scout.Form.prototype.onModelCreate = function() {};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type_ == 'formClosed') {
    this.dispose();
  } else {
    $.log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};

scout.Form.prototype.dispose = function() {
  scout.Form.parent.prototype.dispose.call(this);
  $(window).off('resize', this._resizeHandler);
};
