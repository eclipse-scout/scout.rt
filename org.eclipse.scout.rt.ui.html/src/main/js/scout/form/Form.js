scout.Form = function() {
  scout.Form.parent.call(this);
  this.rootGroupBox;
  this._addAdapterProperties(['rootGroupBox', 'views', 'dialogs', 'messageBoxes', 'fileChoosers']);
  this._locked;
  this.formController;
  this.messageBoxController;
  this.fileChooserController;
  this._glassPaneRenderer;

  this.attached = false; // Indicates whether this Form is currently visible to the user.
  this.renderInitialFocusEnabled = true; // Indicates whether this form should render its initial focus.
};
scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.prototype._init = function(model, session) {
  scout.Form.parent.prototype._init.call(this, model, session);
  if (this.isDialog() || this.searchForm || this.parent instanceof scout.WrappedFormField) {
    this.rootGroupBox.menuBar.bottom();
  }

  this.formController = new scout.FormController(this, session);
  this.messageBoxController = new scout.MessageBoxController(this, session);
  this.fileChooserController = new scout.FileChooserController(this, session);

  // Only render glassPanes if modal and not being a wrapped Form.
  var renderGlassPanes = (this.modal && !(this.parent instanceof scout.WrappedFormField));
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this, renderGlassPanes, session.uiSessionId);
};

scout.Form.prototype._render = function($parent) {
  this._$parent = $parent;
  this._glassPaneRenderer.renderGlassPanes();
  this._renderForm($parent);
  this.attached = true;
};

scout.Form.prototype._renderForm = function($parent) {
  var layout, $handle;

  this.$container = $('<div>')
    .appendTo($parent)
    .addClass(this.displayHint === 'dialog' ? 'dialog' : 'form') // FIXME AWE: rename class 'form' to view so we can use the displayHint as class-name
  .data('model', this);

  if (this.isDialog()) {
    layout = new scout.DialogLayout(this);
    $handle = this.$container.appendDiv('drag-handle');
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
        var autoSizeOld = this.htmlComp._layout.autoSize;
        this.htmlComp._layout.autoSize = false;
        this.htmlComp.revalidateLayout();
        this.htmlComp._layout.autoSize = autoSizeOld;
        // jquery ui resize event bubbles up to the window -> never propagate
        return false;
      }.bind(this)
    });
    this._updateDialogTitle();
  } else {
    layout = new scout.FormLayout(this);
  }

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
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
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();
};

scout.Form.prototype._updateDialogTitle = function() {
  if (this.title || this.subTitle) {
    var $titles = getOrAppendChildDiv(this.$container, 'title-box');
    // Render title
    if (this.title) {
      getOrAppendChildDiv($titles, 'title')
        .text(this.title)
        .icon(this.iconId);
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
  // FIXME AWE: call acceptInput() when form is removed
  // test-case: SimpleWidgets outline, detail-forms, switch between nodes
  this._glassPaneRenderer.removeGlassPanes();
  this._uninstallFocusContext(); // Must be called after removing the glasspanes. Otherwise, the newly activated focus context cannot gain focus because still covert by glasspane.
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
  if (this.isDialog()) {
    this._updateDialogTitle();
  }
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

  this._installFocusContext(scout.FocusRule.NONE);
  this.session.detachHelper.afterAttach(this.$container);

  if (this.keyStrokeAdapter) {
    scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
  }

  // form is attached even if children are not yet
  this.attached = true;

  // Attach child dialogs, message boxes and file choosers.
  this.formController.attachDialogs();
  this.messageBoxController.attach();
  this.fileChooserController.attach();
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
  this.formController.detachDialogs();
  this.messageBoxController.detach();
  this.fileChooserController.detach();

  this.session.detachHelper.beforeDetach(this.$container);
  this._uninstallFocusContext();
  this.$container.detach();

  this.attached = false;
};

scout.Form.prototype.renderInitialFocus = function() {
  if (this.rendered) {
    scout.focusManager.requestFocus(this.session.uiSessionId, this._initialFocusElement());
  }
};

scout.Form.prototype._initialFocusElement = function() {
  var initialFocusField = this.session.getOrCreateModelAdapter(this.initialFocus, this);
  if (initialFocusField) {
    return initialFocusField.$field[0];
  } else {
    return scout.focusManager.findFirstFocusableElement(this.session.uiSessionId, this.$container);
  }
};

scout.Form.prototype._installFocusContext = function() {
  if (this.isDialog()) {
    this.$container.installFocusContext(this.session.uiSessionId, scout.FocusRule.NONE);
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
 * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is showed with this Form as its 'displayParent'.
 */
scout.Form.prototype.glassPaneTargets = function() {
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
