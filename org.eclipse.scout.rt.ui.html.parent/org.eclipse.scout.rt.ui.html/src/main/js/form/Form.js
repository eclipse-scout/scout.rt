scout.Form = function() {
  scout.Form.parent.call(this);
  this.rootGroupBox;
  this.menus = [];
  this.staticMenus = [];
  this._addAdapterProperties(['rootGroupBox', 'menus']);
  this._locked;
  this.menuBarPosition = 'top';
  this._$glassPane;
};
scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.prototype._render = function($parent) {
  var menuItems = [];

  if (this.isDialog()) {
    this.menuBarPosition = 'bottom';
    // FIXME BSH Try to consolidate management of glasspanes in desktop (but: Session.showFatalMessage())
    this._$glassPane = scout.fields.new$Glasspane().appendTo($parent);
    $parent = this._$glassPane;
  }

  this.$container = $('<div>')
    .appendTo($parent)
    .attr('id', 'Form-' + this.id)
    // FIXME AWE: (modal dialog) rename class 'form' to view so we can use the displayHint as class-name
    .addClass(this.displayHint === 'dialog' ? 'dialog' : 'form')
    .data('model', this);

  if (this.isDialog()) {
    var $handle = this.$container.appendDiv('drag-handle');
    this.$container.makeDraggable($handle);

    if (this.closable) {
      this.$container.appendDiv('closable')
        .on('click', function() {
          this.session.send(this.id, 'formClosing');
        }.bind(this));
    }
    this.$container.resizable({resize: function(event, ui) {
      this.htmlComp.invalidate();
      this.htmlComp.layout();
    }.bind(this)});
    this._setDialogTitle();

    this.$container.hide();
    setTimeout(function() {
      this.$container.addClass('shown').show();
      this._$glassPane.installFocusContext('auto');
    }.bind(this));
  }

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout(this));
  this.htmlComp.pixelBasedSizing = false;
  this.rootGroupBox.render(this.$container);

  menuItems = this.staticMenus
    .concat(this.menus)
    .concat(this.rootGroupBox.processButtons);
  this.menuBar = new scout.MenuBar(this.$container, this.menuBarPosition, scout.FormMenuItemsOrder.order);
  this.menuBar.updateItems(menuItems);

  if (this._locked) {
    this.disable();
  }
};

scout.Form.prototype._setDialogTitle = function() {
  if (this.title || this.subTitle) {
    var $titles = this.$container.appendDiv('title-box');
    if (this.title) {
      $titles.appendDiv('title').text(this.title);
    }
    if (this.subTitle) {
      $titles.appendDiv('sub-title').text(this.subTitle);
    }
  }
};

scout.Form.prototype.isDialog = function() {
  return this.displayHint === 'dialog';
};

scout.Form.prototype._isClosable = function() {
  var i, btn,
    systemButtons = this.rootGroupBox.systemButtons;
  for (i = 0; i < systemButtons.length; i++) {
    btn = systemButtons[i];
    if (btn.visible &&
        btn.systemType === scout.Button.SYSTEM_TYPE.CANCEL ||
        btn.systemType === scout.Button.SYSTEM_TYPE.CLOSE) {
      return true;
    }
  }
  return false;
};

scout.Form.prototype.onResize = function() {
  $.log.trace('(Form#onResize) window was resized -> layout Form container');
  var htmlComp = scout.HtmlComponent.get(this.$container);
  var $parent = this.$container.parent();
  var parentSize = new scout.Dimension($parent.width(), $parent.height());
  htmlComp.setSize(parentSize);
};

scout.Form.prototype.appendTo = function($parent) {
  this.$container.appendTo($parent);
};

scout.Form.prototype._remove = function() {
  scout.Form.parent.prototype._remove.call(this);
  if (this.menuBar) {
    this.menuBar.remove();
  }
  if (this._$glassPane) {
    this._$glassPane.fadeOutAndRemove();
  }
};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type === 'formClosed') {
    this.destroy();
  } else {
    $.log.warn('Model event not handled. Widget: Form. Event: ' + event.type + '.');
  }
};
