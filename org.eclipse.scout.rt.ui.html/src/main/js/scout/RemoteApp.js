scout.RemoteApp = function() { //
};
scout.inherits(scout.RemoteApp, scout.App);

scout.RemoteApp.prototype._doBootstrap = function(options) {
  return [
    scout.logging.bootstrap(),
    scout.device.bootstrap(),
    scout.defaultValues.bootstrap(),
    scout.fonts.bootstrap(options.fonts)
  ];
};

/**
 * @override
 */
scout.RemoteApp.prototype._createSession = function($entryPoint, options) {
  options = options || {};
  options.remote = true;
  options.$entryPoint = $entryPoint;
  var session = scout.create('Session', options, {
    ensureUniqueId: false
  });
  session.start();
  return session;
};

/**
 * @override
 */
scout.RemoteApp.prototype._init = function(options) {
  scout.RemoteApp.modifyWidgetPrototype();
  scout.RemoteApp.modifyTablePrototype();
  scout.RemoteApp.modifyBooleanColumnPrototype();
  scout.RemoteApp.parent.prototype._init.call(this, options);
};

/**
 * Static method to modify the prototype of scout.Widget.
 */
scout.RemoteApp.modifyWidgetPrototype = function() {
  // _createChild
  scout.objects.replacePrototypeFunction(scout.Widget, '_createChild', function(model) {
    if (model instanceof scout.Widget) {
      return model;
    }

    // Remote case
    var modelAdapter = findModelAdapter(this);
    if (modelAdapter) { // If the widget (or one of its parents) has a remote-adapter, all its properties must be remotable
      return this.session.getOrCreateWidget(model, this); // model is a String, contains (remote) object ID
    }

    // Local case (default)
    model.parent = this;
    return scout.create(model);

    function findModelAdapter(widget) {
      while (widget) {
        if (widget.modelAdapter) {
          return widget.modelAdapter;
        }
        widget = widget.parent;
      }
      return null;
    }
  });
};

/**
 * Static method to modify the prototype of scout.Table.
 */
scout.RemoteApp.modifyTablePrototype = function() {
  // prepareCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'prepareCellEdit', function(column, row, openFieldPopupOnCellEdit) {
    this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
    this.trigger('prepareCellEdit', {
      column: column,
      row: row
    });
  });

  // completeCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'completeCellEdit', function(field) {
    this.trigger('completeCellEdit', {
      field: field
    });
  });

  // cancelCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'cancelCellEdit', function(field) {
    this.trigger('cancelCellEdit', {
      field: field
    });
  });
};

scout.RemoteApp.modifyBooleanColumnPrototype = function() {
  // _toggleCellValue
  scout.objects.replacePrototypeFunction(scout.BooleanColumn, '_toggleCellValue', function(row, cell) {
    // NOP - do nothing, since server will handle the click, see Java AbstractTable#interceptRowClickSingleObserver
  });
};
