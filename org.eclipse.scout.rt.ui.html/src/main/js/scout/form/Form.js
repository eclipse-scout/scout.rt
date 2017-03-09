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
  this.$statusIcons = [];
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
  //  this._syncStatus(this.status);

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

//scout.Form.prototype._setStatus = function(status) {
//  console.log('setstatus');
//};

/**
 * @override Widget.js
 */
scout.Form.prototype._renderProperties = function() {
  scout.Form.parent.prototype._renderProperties.call(this);
  this._renderTitle();
  this._renderSubTitle();
  this._renderIconId();
  this._renderClosable();
  this._renderSaveNeedAndSaveNeedVisible();
  this._renderCssClass();
  this._renderStatus();
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

    var $myWindow = this.$container.window();
    this.$container.resizable({
      start: function(event, ui) {
        this.$container.resizable('option', 'maxHeight', $myWindow.height() - event.target.offsetTop);
        this.$container.resizable('option', 'maxWidth', $myWindow.width() - event.target.offsetLeft);
      }.bind(this)
    });
    this.$container.on('resize', function(e) {
      var autoSizeOld = this.htmlComp.layout.autoSize;
      this.htmlComp.layout.autoSize = false;
      this.htmlComp.revalidateLayout();
      this.htmlComp.layout.autoSize = autoSizeOld;
      return false;
    }.bind(this));
    this._renderHeader();
  } else {
    layout = new scout.FormLayout(this);
  }

  this.htmlComp.setLayout(layout);

  this.rootGroupBox.render(this.$container);

  if (this._locked) {
    this.disable();
  }

  if (this.isDialog()) {
    this.$container.addClassForAnimation('animate-open');
  }
};

scout.Form.prototype._renderHeader = function() {
  if (this.isDialog()) {
    this.$header = this.$container.appendDiv('header');
    this.$statusContainer = this.$header.appendDiv('status-container');
    this.$icon = this.$header.appendDiv('icon-container');
    this.$title = this.$header.appendDiv('title');
    this.$subTitle = this.$header.appendDiv('sub-title');
  }
};

scout.Form.prototype.close = function() {
  this._send('formClosing');
};

scout.Form.prototype._postRender = function() {
  scout.Form.parent.prototype._postRender.call(this);

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

scout.Form.prototype._renderSaveNeeded = function() {
  this._renderSaveNeedAndSaveNeedVisible();
};

scout.Form.prototype._renderSaveNeededVisible = function() {
  this._renderSaveNeedAndSaveNeedVisible();
};

scout.Form.prototype._renderSaveNeedAndSaveNeedVisible = function() {
  if (!this.isDialog()) {
    return;
  }
  if (this.saveNeeded && this.saveNeededVisible) {
    this.$container.addClass('save-needed');
    if (this.$saveNeeded) {
      return;
    }
    if (this.$close) {
      this.$saveNeeded = this.$close.beforeDiv('status save-needer');
    } else {
      this.$saveNeeded = this.$statusContainer
        .appendDiv('status save-needer');
    }
  } else {
    this.$container.removeClass('save-needed');
    if (!this.$saveNeeded) {
      return;
    }
    this.$saveNeeded.remove();
    this.$saveNeeded = null;
  }
  // Layout could have been changed, e.g. if subtitle becomes visible
  this.invalidateLayoutTree();
};

scout.Form.prototype._renderAskIfNeedSave = function() {};

scout.Form.prototype._renderCssClass = function(cssClass, oldCssClass) {
  cssClass = cssClass || this.cssClass;
  this.$container.removeClass(oldCssClass);
  this.$container.addClass(cssClass);
  // Layout could have been changed, e.g. if subtitle becomes visible
  this.invalidateLayoutTree();
};

scout.Form.prototype._renderStatus = function() {
  if (!this.isDialog()) {
    return;
  }

  this.$statusIcons.forEach(function($icn) {
    $icn.remove();
  });

  this.$statusIcons = [];

  var flatenStatus = scout.Form.flatenStatus(this.status);
  if (flatenStatus) {
    var $prevIcon;
    flatenStatus.forEach(function(sts) {
      $prevIcon = this._renderSingleStatus(sts, $prevIcon);
      if($prevIcon){
      this.$statusIcons.push($prevIcon);
    }
    }.bind(this));
  }
  // Layout could have been changed, e.g. if subtitle becomes visible
  this.invalidateLayoutTree();
};

scout.Form.prototype._renderSingleStatus = function(status, $prevIcon) {
  if (status && status.iconId) {
    var $statusIcon = this.$statusContainer.appendIcon(status.iconId, 'status');
    $statusIcon.prependTo(this.$statusContainer);
    return $statusIcon;
  } else {
    return $prevIcon;
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
  if (this.isDialog()) {
    this.$title.text(this.title);
  } else if (this.isPopupWindow()) {
    this._updateTitleForWindow();
  }
  // Layout could have been changed, e.g. if subtitle becomes visible
  this.invalidateLayoutTree();
};

scout.Form.prototype._renderSubTitle = function() {
  if (this.isDialog()) {
    this.$subTitle.text(this.subTitle);
  } else if (this.isPopupWindow()) {
    this._updateTitleForWindow();
  }
  // Layout could have been changed, e.g. if subtitle becomes visible
  this.invalidateLayoutTree();
};

scout.Form.prototype._renderIconId = function() {
  if (this.isDialog()) {
    this.$icon.icon(this.iconId);
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }
};

scout.Form.prototype._renderClosable = function() {
  if (!this.isDialog()) {
    return;
  }
  this.$container.toggleClass('closable');
  if (this.closable) {
    if (this.$close) {
      return;
    }
    this.$close = this.$statusContainer.appendDiv('status closer')
      .on('click', this.close.bind(this));
  } else {
    if (!this.$close) {
      return;
    }
    this.$close.remove();
    this.$close = null;
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
    initialFocusField = this.session.getOrCreateModelAdapter(this.initialFocus, this);
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

scout.Form.prototype._onRequestFocus = function(formFieldId) {
  var formField = this.session.getOrCreateModelAdapter(formFieldId, this);
  this.requestFocus(formField);
};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type === 'requestFocus') {
    this._onRequestFocus(event.formField);
  } else {
    scout.Form.parent.prototype.onModelAction.call(this, event);
  }
};

// static functions
scout.Form.flatenStatus = function(status) {
  if (status) {
    var flatStatus = [];
    _collectLeafStatus.call(this,status, flatStatus);
    return flatStatus;
  }
  return null;
  // helper function
  function _collectLeafStatus(status, collector) {
    if (!status) {
      return;
    }
    if (status.children) {
      status.children.forEach(function(cs) {
        _collectLeafStatus(cs, collector);
      }.bind(this));
    } else {
      collector.push(status);
    }
  }

};


