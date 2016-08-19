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
  this._addAdapterProperties(['rootGroupBox', 'views', 'dialogs', 'initialFocus', 'messageBoxes', 'fileChoosers']);

  this.displayHint = scout.Form.DisplayHint.DIALOG;
  this.maximizeEnabled = true;
  this.maximized = false;
  this.minimizeEnabled = true;
  this.minimized = false;
  this.modal = true;
  this.dialogs = [];
  this.views = [];
  this.messageBoxes = [];
  this.fileChoosers = [];
  this.closable = true;
  this.cacheBounds = false;
  this.rootGroupBox;
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
scout.inherits(scout.Form, scout.Widget);

scout.Form.DisplayHint = {
  DIALOG: 'dialog',
  POPUP_WINDOW: 'popupWindow',
  VIEW: 'view'
};

scout.Form.prototype._init = function(model) {
  scout.Form.parent.prototype._init.call(this, model);

  this.formController = new scout.FormController(this, this.session);
  this.messageBoxController = new scout.MessageBoxController(this, this.session);
  this.fileChooserController = new scout.FileChooserController(this, this.session);

  this._syncRootGroupBox(this.rootGroupBox);

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

scout.Form.prototype._syncRootGroupBox = function(rootGroupBox) {
  this._setProperty('rootGroupBox', rootGroupBox);
  if (this.rootGroupBox &&
     (this.isDialog() || this.searchForm || this.parent instanceof scout.WrappedFormField)) {
    this.rootGroupBox.menuBar.bottom();
  }
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

  if (this.uiCssClass) {
    this.$container.addClass(this.uiCssClass);
  }

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.pixelBasedSizing = false;
  if (this.isDialog()) {
    layout = new scout.DialogLayout(this);
    this.htmlComp.validateRoot = true;
    $handle = this.$container.appendDiv('drag-handle');
    this.$container.makeDraggable($handle, $.throttle(this.onMove.bind(this), 1000 / 60)); // 60fps

    if (this.closable) {
      this.$container
        .appendDiv('closer')
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
  this.trigger('rendered');
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
  var titleText = this.title;
  if (!titleText && this.closable) {
    // Add '&nbsp;' to prevent title-box of a closable form from collapsing if title is empty
    titleText = scout.strings.plainText('&nbsp;');
  }
  if (titleText || this.subTitle) {
    var $titles = getOrAppendChildDiv(this.$container, 'title-box');
    // Render title
    if (titleText) {
      getOrAppendChildDiv($titles, 'title')
        .text(titleText)
        .icon(this.iconId);
    } else {
      removeChildDiv($titles, 'title');
    }
    // Render subTitle
    if (scout.strings.hasText(titleText)) {
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
  this.formController.remove();
  this.messageBoxController.remove();
  this.fileChooserController.remove();

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

/**
 * Method invoked when:
 *  - this is a 'detailForm' and the outline content is displayed;
 *  - this is a 'view' and the view tab is selected;
 *  - this is a child 'dialog' or 'view' and its 'displayParent' is attached;
 * @override Widget.js
 */
scout.Form.prototype._attach = function() {
  this.$parent.append(this.$container);

  // If the parent was resized while this view was detached, the view has a wrong size.
  if (this.isView()) {
    this.invalidateLayoutTree(false);
  }

  this.session.detachHelper.afterAttach(this.$container);

  // form is attached even if children are not yet
  if ((this.isView() || this.isDialog()) && !this.detailForm) {
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

/**
 * This method returns the HtmlElement (DOM node) which is used by FocusManager/FocusContext/Popup
 * to focus the initial element. The impl. of these classes relies on HtmlElements, so we can not
 * easily use the focus() method of scout.FormField here. Furthermore, some classes like scout.Button
 * are sometimes 'adapted' by a ButtonAdapterMenu, which means the Button itself is not rendered, but
 * we must know the $container of the adapter menu to focus the correct element. That's why we call
 * the getFocusableElement() method.
 */
scout.Form.prototype._initialFocusElement = function() {
  var focusElement,
    initialFocusField = this.initialFocus;

  if (initialFocusField) {
    focusElement = initialFocusField.getFocusableElement();
  }
  if (!focusElement) {
    focusElement = this.session.focusManager.findFirstFocusableElement(this.$container);
  }
  return focusElement;
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
 * Returns 'true' if this Form is currently accessible to the user.
 */
scout.Form.prototype.inFront = function() {
  return this.rendered && this.attached;
};

scout.Form.prototype.requestFocus = function(formField) {
  if (!formField) {
    return;
  }
  formField.focus();
};
