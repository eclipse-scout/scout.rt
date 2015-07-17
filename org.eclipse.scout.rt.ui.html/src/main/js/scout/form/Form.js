scout.Form = function() {
  scout.Form.parent.call(this);
  this.rootGroupBox;
  this._addAdapterProperties(['rootGroupBox', 'views', 'dialogs', 'messageBoxes', 'fileChoosers']);
  this._locked;
  this._formController;
  this._messageBoxController;
  this._modalityController;

  this.attached = false; // Indicates whether this Form is currently visible to the user.
  this.renderInitialFocusEnabled = true; // Indicates whether this form should render its initial focus.
};
scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.prototype._init = function(model, session) {
  scout.Form.parent.prototype._init.call(this, model, session);
  // FIXME BSH Improve this logic - how about a mid-sized menubar? See also: GroupBox.js/init()
  if (this.isDialog() || this.searchForm) {
    this.rootGroupBox.menuBar.bottom();
    this.rootGroupBox.menuBar.large();
  }

  this._formController = new scout.FormController(this, session);
  this._messageBoxController = new scout.MessageBoxController(this, session);
  this._fileChooserController = new scout.FileChooserController(this, session);

  this._modalityController = new scout.ModalityController(this);
  this._modalityController.render = this.modal;
};

scout.Form.prototype._render = function($parent) {
  this._$parent = $parent;

  // Add modality glassPane if applicable; must precede appending the Form to the DOM.
  this._modalityController.addGlassPane();

  this._renderForm($parent);

  this.attached = true;
};

scout.Form.prototype._renderForm = function($parent) {
  this.$container = $('<div>')
    .appendTo($parent)
    .addClass(this.displayHint === 'dialog' ? 'dialog' : 'form') // FIXME AWE: rename class 'form' to view so we can use the displayHint as class-name
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
    this.$container.resizable({
      start: function(event, ui) {
        this.$container.resizable('option', 'maxHeight', $(window).height() - event.target.offsetTop);
        this.$container.resizable('option', 'maxWidth', $(window).width() - event.target.offsetLeft);
      }.bind(this),

      resize: function(event, ui) {
        this.htmlComp.revalidateLayout();
      }.bind(this)
    });
    this._updateDialogTitle();
  }

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.FormLayout(this));
  this.htmlComp.pixelBasedSizing = false;
  this.rootGroupBox.render(this.$container);

  if (this._locked) {
    this.disable();
  }

  if (this.isDialog()) {
    this.$container.addClassForAnimation('shown');
  }
};

scout.Form.prototype._postRender = function() {
  this._installFocusContext();
  if (this.renderInitialFocusEnabled) {
    this.renderInitialFocus();
  }

  // Render attached forms, message boxes and file choosers.
  this._formController.render();
  this._messageBoxController.render();
  this._fileChooserController.render();
};

scout.Form.prototype._updateDialogTitle = function() {
  if (this.title || this.subTitle) {
    var $titles = getOrAppendChildDiv(this.$container, 'title-box');
    // Render title
    if (this.title) {
      getOrAppendChildDiv($titles, 'title').text(this.title);
    } else {
      removeChildDiv($titles, 'title');
    }
    // Render subTitle
    if (this.title) {
      getOrAppendChildDiv($titles, 'sub-title').text(this.subTitle);
    } else {
      removeChildDiv($titles, 'sub-title');
    }
  } else {
    removeChildDiv(this.$container, 'title-box');
  }
  // Layout could have been changed, e.g. if subtitle becomes visible
  this.invalidateLayoutTree();

  // ----- Helper functions -----

  function getOrAppendChildDiv($parent, cssClass) {
    var $div = $parent.children('.' + cssClass);
    if ($div.length === 0) {
      $div = $parent.appendDiv(cssClass);
    }
    return $div;
  }

  function removeChildDiv($parent, cssClass) {
    $parent.children('.' + cssClass).remove();
  }
};

scout.Form.prototype.isDialog = function() {
  return this.displayHint === 'dialog';
};

scout.Form.prototype.isView = function() {
  return this.displayHint === 'view';
};

scout.Form.prototype._isClosable = function() {
  var i, btn,
    systemButtons = this.rootGroupBox.systemButtons;
  for (i = 0; i < systemButtons.length; i++) {
    btn = systemButtons[i];
    if (btn.visible &&
      btn.systemType === scout.Button.SystemType.CANCEL ||
      btn.systemType === scout.Button.SystemType.CLOSE) {
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
  // FIXME AWE: call displayTextChanged() when form is removed
  // test-case: SimpleWidgets outline, detail-forms, switch between nodes
  this._uninstallFocusContext();
  this._modalityController.removeGlassPane();
  this.attached = false;

  scout.Form.parent.prototype._remove.call(this);
};

scout.Form.prototype._renderTitle = function() {
  if (this.isDialog()) {
    this._updateDialogTitle();
  }
};

scout.Form.prototype._renderSubTitle = function() {
  if (this.isDialog()) {
    this._updateDialogTitle();
  }
};

scout.Form.prototype._renderIconId = function() {
  // TODO render icon
};

scout.Form.prototype._onFormClosed = function(event) {
  this.destroy();
};

scout.Form.prototype._onRequestFocus = function(formFieldId) {
  var formField = this.session.getOrCreateModelAdapter(formFieldId, this);
  if (formField) {
    // FIXME AWE/NBU/DWI: hier darf focus nicht direkt aufgerufen werden. Es muss geprüft werden ob
    // der focuscontext überhaupt "aktivierbar" ist, auch die modalität muss hier berücksichtigt werden
    // je nach dem kann es sein, dass das field gar nicht den fokus kriegt.
    formField.$field.focus();
  }
};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type === 'formClosed') {
    this._onFormClosed(event);
  } else if (event.type === 'requestFocus') {
    this._onRequestFocus(event.formField);
  } else if (event.type === 'formShow') {
    this._formController.registerAndRender(event.form);
  } else if (event.type === 'formHide') {
    this._formController.unregisterAndRemove(event.form);
  } else if (event.type === 'formActivate') {
    this._formController.activateForm(event.form);
  } else if (event.type === 'messageBoxShow') {
    this._messageBoxController.registerAndRender(event.messageBox);
  } else if (event.type === 'messageBoxHide') {
    this._messageBoxController.unregisterAndRemove(event.messageBox);
  } else if (event.type === 'fileChooserShow') {
    this._fileChooserController.registerAndRender(event.fileChooser);
  } else if (event.type === 'fileChooserHide') {
    this._fileChooserController.unregisterAndRemove(event.fileChooser);
  } else {
    $.log.warn('Model event not handled. Widget: Form. Event: ' + event.type + '.');
  }
};

/**
 * === Method required for objects that act as 'outlineContent', views or shells attached to a 'displayParent' ===
 *
 * Method invoked when:
 *  - this is a 'detailForm' and the outline content is displayed;
 *  - this is a 'view' and the view tab is selected;
 *  - this is a child 'dialog' or 'view' and its 'displayParent' is attached;
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already attached.
 */
scout.Form.prototype.attach = function() {
  if (this.attached || !this.rendered) {
    return;
  }

  this._$parent.append(this.$container);

  // If the parent was resized while this view was detached, the view has a wrong size.
  if (this.isView()) {
    var htmlComp = scout.HtmlComponent.get(this.$container);
    var htmlParent = htmlComp.getParent();
    htmlComp.setSize(htmlParent.getSize());
  }

  this._installFocusContext();
  this.session.detachHelper.afterAttach(this.$container);

  if (this.keyStrokeAdapter) {
    scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
  }

  // Attach child dialogs, message boxes and file choosers.
  this._formController.attachDialogs();
  this._messageBoxController.attach();
  this._fileChooserController.attach();

  this.attached = true;
};

/**
 * === Method required for objects that act as 'outlineContent', views or shells attached to a 'displayParent' ===
 *
 * Method invoked when:
 *  - this is a 'detailForm' and the outline content is hidden;
 *  - this is a 'view' and the view tab is deselected;
 *  - this is a child 'dialog' or 'view' and its 'displayParent' is detached;
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already detached.
 */
scout.Form.prototype.detach = function() {
  if (!this.attached || !this.rendered) {
    return;
  }

  if (scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }

  // Detach child dialogs, message boxes and file choosers, not views.
  this._formController.detachDialogs();
  this._messageBoxController.detach();
  this._fileChooserController.detach();

  this.session.detachHelper.beforeDetach(this.$container);
  this._uninstallFocusContext();
  this.$container.detach();

  this.attached = false;
};

scout.Form.prototype.renderInitialFocus = function() {
  var initialFocusField = this.session.getOrCreateModelAdapter(this.initialFocus, this);
  var $initialFocusControl = (initialFocusField ? initialFocusField.$field : null);
  $initialFocusControl = $initialFocusControl || scout.focusManager.getFirstFocusableElement(this.$container);
  $initialFocusControl.focus();
};

scout.Form.prototype._installFocusContext = function() {
  if (this.isDialog()) {
    this.$container.installFocusContext(scout.FocusRule.NONE, this.session.uiSessionId);
  }
};

scout.Form.prototype._uninstallFocusContext = function() {
  if (this.isDialog()) {
    this.$container.uninstallFocusContext(this.session.uiSessionId);
  }
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns the DOM elements to paint a 'modality glassPane' over, once a modal Form, message-box or file-chooser is showed with this Form as its 'displayParent'.
 */
scout.Form.prototype.modalityElements = function() {
  return [this.$container];
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns 'true' if this Form is currently accessible to the user.
 */
scout.Form.prototype.inFront = function() {
  return this.rendered && this.attached;
};
