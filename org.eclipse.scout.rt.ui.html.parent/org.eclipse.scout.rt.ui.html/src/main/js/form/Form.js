scout.Form = function() {
  scout.Form.parent.call(this);
  this._$title;
  this._$parent;
  this.rootGroupBox;
  this.menus = [];
  this.staticMenus = [];
  this._addAdapterProperties(['rootGroupBox', 'menus']);
  this._locked;
};
scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.prototype._render = function($parent) {
  var i, btn, systemButtons, menuItems = [], closeable = false;

  this._$parent = $parent;
  this.$container = $('<div>')
    .appendTo($parent)
    .attr('id', 'Form-' + this.id)
    .addClass('form')
    .data('model', this);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout());
  this.htmlComp.pixelBasedSizing = false;

  this.rootGroupBox.render(this.$container);

  menuItems = this.staticMenus.concat(this.menus);
  systemButtons = this.rootGroupBox.systemButtons;
  for (i = 0; i < systemButtons.length; i++) {
    btn = systemButtons[i];
    if (btn.visible &&
        btn.systemType === scout.Button.SYSTEM_TYPE.CANCEL ||
        btn.systemType === scout.Button.SYSTEM_TYPE.CLOSE) {
      closeable = true;
    }
  }
  // FIXME AWE: (menu) prettify this
  menuItems = menuItems.concat(this.rootGroupBox.processButtons);

  // FIXME AWE: (menu) braucht es this.menuBar als instanz variable?
  // FIXME AWE: (menu) check menuBarPosition within forms (PhoneForm) --> move to groupbox
  this.menuBar = new scout.MenuBar(this.$container, 'top', scout.FormMenuItemsOrder.order);
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

/**
 * @override
 */
scout.Form.prototype.dispose = function() {
  scout.Form.parent.prototype.dispose.call(this);
};

scout.Form.prototype.onModelCreate = function() {};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type === 'formClosed') {
    this.destroy();
  } else {
    $.log.warn('Model event not handled. Widget: Form. Event: ' + event.type + '.');
  }
};
