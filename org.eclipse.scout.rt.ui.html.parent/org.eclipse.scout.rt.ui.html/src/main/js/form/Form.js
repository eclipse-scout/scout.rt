scout.Form = function() {
  scout.Form.parent.call(this);
  this._$title;
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

  if (this.displayHint === 'dialog') {
    this._$glassPane = $parent.appendDiv('glasspane');
    $parent = this._$glassPane;
  }

  this.$container = $('<div>')
    .appendTo($parent)
    .attr('id', 'Form-' + this.id)
    .addClass(this.displayHint === 'dialog' ? 'dialog' : 'form') // FIXME AWE: (modal dialog) rename class 'form' to view
    // so we can use the displayHint as class-name
    .data('model', this);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout());
  this.htmlComp.pixelBasedSizing = false;
  this.rootGroupBox.render(this.$container);

  menuItems = this.staticMenus
    .concat(this.menus)
    .concat(this.rootGroupBox.processButtons);
  this.menuBar = new scout.MenuBar(this.$container, this.menuBarPosition, scout.FormMenuItemsOrder.order);
  this.menuBar.updateItems(menuItems);

// FIXME AWE: (menu) un-comment, check if this is the right place. Probably we should only
// have the close-buttons on real modal dialogs or we should add an X icon to the tabs in
// in the task-bar (all but the last tab should be closeable).
//  if (closeable) {
//    var $closeButton = $('<button>').text('X');
//    this.menuBar.$container.append($closeButton);
//    $closeButton.on('click', function() {
//      this.session.send(this.id, 'formClosing');
//    }.bind(this));
//  }

  if (this._locked) {
    this.disable();
  }
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
  var htmlCont = scout.HtmlComponent.get(this.$container);
  var $parent = this.$container.parent();
  var parentSize = new scout.Dimension($parent.width(), $parent.height());
  htmlCont.setSize(parentSize);
};

scout.Form.prototype.appendTo = function($parent) {
  this.$container.appendTo($parent);
};

scout.Form.prototype.onModelCreate = function() {};

scout.Form.prototype._remove = function() {
  scout.Form.parent.prototype._remove.call(this);
  if (this.menuBar) {
    this.menuBar.remove();
  }
  if (this._$glassPane) {
    this._$glassPane.remove();
  }
};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type === 'formClosed') {
    this.destroy();
  } else {
    $.log.warn('Model event not handled. Widget: Form. Event: ' + event.type + '.');
  }
};
