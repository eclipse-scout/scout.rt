/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Form = function() {
  scout.Form.parent.call(this);
  this.rootGroupBox;
  this._addAdapterProperties(['rootGroupBox', 'views', 'dialogs', 'messageBoxes', 'fileChoosers']);
  this._locked;
  this.formController;
  this.messageBoxController;
  this.fileChooserController;
  this._glassPaneRenderer;
  /**
   * Whether this form should render its initial focus
   */
  this.renderInitialFocusEnabled = true;
};
scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.DisplayHint = {
  DIALOG: 'dialog',
  POPUP_WINDOW: 'popupWindow',
  VIEW: 'view'
};

scout.Form.prototype._init = function(model) {
  scout.Form.parent.prototype._init.call(this, model);
  if (this.isDialog() || this.searchForm || this.parent instanceof scout.WrappedFormField) {
    this.rootGroupBox.menuBar.bottom();
  }

  this.formController = new scout.FormController(this, this.session);
  this.messageBoxController = new scout.MessageBoxController(this, this.session);
  this.fileChooserController = new scout.FileChooserController(this, this.session);

  // Only render glassPanes if modal and not being a wrapped Form.
  var renderGlassPanes = (this.modal && !(this.parent instanceof scout.WrappedFormField));
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this.session, this, renderGlassPanes);
  var glasspaneRendererHandler = function(event) {
    //render glasspanes on parents after initialized
    if (event.newProperties.displayParent) {
      this._glassPaneRenderer.renderGlassPanes();
    }
  }.bind(this);
  this.on('propertyChange', glasspaneRendererHandler);
  this.one('destroy', function() {
    this.off('propertyChange', glasspaneRendererHandler);
  }.bind(this));
};

/**
 * @override Widget.js
 */
scout.Form.prototype._renderProperties = function() {
  scout.Form.parent.prototype._renderProperties.call(this);
  this._updateTitle();
};

scout.Form.prototype._render = function($parent) {
  this._renderForm($parent);
};

scout.Form.prototype._renderForm = function($parent) {
  var layout, $handle;

  this.$container = $parent.appendDiv()
    .addClass(this.isDialog() ? 'dialog' : 'form')
    .data('model', this);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.pixelBasedSizing = false;
  if (this.isDialog()) {
    layout = new scout.DialogLayout(this);
    this.htmlComp.validateRoot = true;
    $handle = this.$container.appendDiv('drag-handle');
    this.$container.makeDraggable($handle, $.throttle(this.onMove.bind(this), 1000 / 60)); // 60fps

    if (this.closable) {
      this.$container
        .appendDiv('closable')
        .on('click', this.close.bind(this));
    }
    var $myWindow = this.$container.window();
    this.$container.resizable({
      start: function(event, ui) {
        this.$container.resizable('option', 'maxHeight', $myWindow.height() - event.target.offsetTop);
        this.$container.resizable('option', 'maxWidth', $myWindow.width() - event.target.offsetLeft);
      }.bind(this),

      resize: function(event, ui) {
        var autoSizeOld = this.htmlComp.layout.autoSize;
        this.htmlComp.layout.autoSize = false;
        this.htmlComp.revalidateLayout();
        this.htmlComp.layout.autoSize = autoSizeOld;
        // jquery ui resize event bubbles up to the window -> never propagate
        return false;
      }.bind(this)
    });
    this._updateTitle();
  } else {
    layout = new scout.FormLayout(this);
  }

  this.htmlComp.setLayout(layout);
  this.rootGroupBox.render(this.$container);

  if (this._locked) {
    this.disable();
  }

  if (this.isDialog()) {
    this.$container.addClassForAnimation('shown');
  }
};

scout.Form.prototype.close = function() {
  this._send('formClosing');
};

scout.Form.prototype._postRender = function() {
  this._trigger('rendered');
  this._installFocusContext();

  if (this.renderInitialFocusEnabled) {
    this.renderInitialFocus();
  }

  // Render attached forms, message boxes and file choosers.
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();
};

scout.Form.prototype._updateTitle = function() {
  if (this.isPopupWindow()) {
    this._updateTitleForWindow();
  } else if (this.isDialog()) {
    this._updateTitleForDom();
  }
};

scout.Form.prototype._updateTitleForWindow = function() {
  var formTitle = scout.strings.join(' - ', this.title, this.subTitle),
    applicationTitle = this.session.desktop.title;
  this.popupWindow.title(formTitle || applicationTitle);
};

scout.Form.prototype._updateTitleForDom = function() {
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
  return this.displayHint === scout.Form.DisplayHint.DIALOG;
};

scout.Form.prototype.isPopupWindow = function() {
  return this.displayHint === scout.Form.DisplayHint.POPUP_WINDOW;
};

scout.Form.prototype.isView = function() {
  return this.displayHint === scout.Form.DisplayHint.VIEW;
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

scout.Form.prototype.onMove = function(newOffset) {
  this.trigger('move', newOffset);
};

scout.Form.prototype.appendTo = function($parent) {
  this.$container.appendTo($parent);
};

scout.Form.prototype._remove = function() {
  // FIXME awe: call acceptInput() when form is removed
  // test-case: SimpleWidgets outline, detail-forms, switch between nodes
  this._glassPaneRenderer.removeGlassPanes();
  this._uninstallFocusContext();
  scout.Form.parent.prototype._remove.call(this);
};

scout.Form.prototype._renderTitle = function() {
  this._updateTitle();
};

scout.Form.prototype._renderSubTitle = function() {
  this._updateTitle();
};

scout.Form.prototype._renderIconId = function() {
  this._updateTitle();
};

scout.Form.prototype._onRequestFocus = function(formFieldId) {
  var formField = this.session.getOrCreateModelAdapter(formFieldId, this);
  if (formField) {
    // FIXME awe, nbu: (focus) hier darf focus nicht direkt aufgerufen werden. Es muss geprüft werden ob
    // der focuscontext überhaupt "aktivierbar" ist, auch die modalität muss hier berücksichtigt werden
    // je nach dem kann es sein, dass das field gar nicht den fokus kriegt.
    formField.$field.focus();
  }
};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type === 'requestFocus') {
    this._onRequestFocus(event.formField);
  } else {
    scout.Form.parent.prototype.onModelAction.call(this, event);
  }
};

/**
 * Method invoked when:
 *  - this is a 'detailForm' and the outline content is displayed;
 *  - this is a 'view' and the view tab is selected;
 *  - this is a child 'dialog' or 'view' and its 'displayParent' is attached;
 * @override Widget.js
 */
scout.Form.prototype._attach = function() {
  this._$parent.append(this.$container);

  // If the parent was resized while this view was detached, the view has a wrong size.
  if (this.isView()) {
    this.invalidateLayoutTree(false);
  }

  this.session.detachHelper.afterAttach(this.$container);

  // form is attached even if children are not yet
  var outlineContent;
  if (this.session.desktop.bench) {
    //FIXME CGU what if bench is not available, e.g. on mobile?
    outlineContent = this.session.desktop.bench.outlineContent;
  }
  if ((this.isView() || this.isDialog()) && outlineContent !== this) {
    //notify model this form is active
    this.session.desktop._setFormActivated(this);
  }

  // Attach child dialogs, message boxes and file choosers.
  this.formController.attachDialogs();
  this.messageBoxController.attach();
  this.fileChooserController.attach();
  scout.Form.parent.prototype._attach.call(this);
};

/**
 * Method invoked when:
 *  - this is a 'detailForm' and the outline content is hidden;
 *  - this is a 'view' and the view tab is deselected;
 *  - this is a child 'dialog' or 'view' and its 'displayParent' is detached;
 * @override Widget.js
 */
scout.Form.prototype._detach = function() {
  // Detach child dialogs, message boxes and file choosers, not views.
  this.formController.detachDialogs();
  this.messageBoxController.detach();
  this.fileChooserController.detach();

  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();
  scout.Form.parent.prototype._detach.call(this);
};

scout.Form.prototype.renderInitialFocus = function() {
  if (this.rendered) {
    this.session.focusManager.requestFocus(this._initialFocusElement());
  }
};

scout.Form.prototype._initialFocusElement = function() {
  var initialFocusField = this.session.getOrCreateModelAdapter(this.initialFocus, this);
  if (initialFocusField) {
    return initialFocusField.$field[0];
  } else {
    return this.session.focusManager.findFirstFocusableElement(this.$container);
  }
};

scout.Form.prototype._installFocusContext = function() {
  if (this.isDialog() || this.isPopupWindow()) {
    this.session.focusManager.installFocusContext(this.$container, scout.focusRule.NONE);
  }
};

scout.Form.prototype._uninstallFocusContext = function() {
  if (this.isDialog() || this.isPopupWindow()) {
    this.session.focusManager.uninstallFocusContext(this.$container);
  }
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is showed with this Form as its 'displayParent'.
 */
scout.Form.prototype.glassPaneTargets = function() {
  if (this.$container) {
    return [this.$container];
  } else {
    var renderedHandler = function(event) {
      deferred.ready([event.eventOn.$container]);
    };
    var deferred = new scout.DeferredGlassPaneTarget();
    this.one('rendered', renderedHandler);
    this.one('destroy', function() {
      this.off('rendered', renderedHandler);
    }.bind(this));
    return [deferred];
  }
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns 'true' if this Form is currently accessible to the user.
 */
scout.Form.prototype.inFront = function() {
  return this.rendered && this.attached;
};
