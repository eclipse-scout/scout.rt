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
  this._addWidgetProperties(['rootGroupBox', 'views', 'dialogs', 'initialFocus', 'messageBoxes', 'fileChoosers']);

  this.askIfNeedSave = true;
  this.data = {};
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
  this.resizable = true;
  this.rootGroupBox;
  this.saveNeeded = false;
  this.saveNeededVisible = false;
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
scout.inherits(scout.Form, scout.Widget);

scout.Form.DisplayHint = {
  DIALOG: 'dialog',
  POPUP_WINDOW: 'popupWindow',
  VIEW: 'view'
};

scout.Form.prototype._init = function(model) {
  scout.Form.parent.prototype._init.call(this, model);

  this.resolveTextKeys(['title']);
  this._setViews(this.views);
  this.formController = scout.create('FormController', {
    displayParent: this,
    session: this.session
  });

  this.messageBoxController = new scout.MessageBoxController(this, this.session);
  this.fileChooserController = new scout.FileChooserController(this, this.session);

  this._setRootGroupBox(this.rootGroupBox);
  this._setStatus(this.status);

  // Only render glassPanes if modal and not being a wrapped Form.
  var renderGlassPanes = (this.modal && !(this.parent instanceof scout.WrappedFormField));
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this, renderGlassPanes);
  var propertyChangeHandler = function(event) {
    // render glasspanes on parents after initialized
    if (event.propertyName === 'displayParent') {
      this._glassPaneRenderer.renderGlassPanes();
    }
  }.bind(this);
  this.on('propertyChange', propertyChangeHandler);
  this.one('destroy', function() {
    this.off('propertyChange', propertyChangeHandler);
  }.bind(this));

  this._installLifecycle();
};

scout.Form.prototype._render = function() {
  this._renderForm();
};

/**
 * @override Widget.js
 */
scout.Form.prototype._renderProperties = function() {
  scout.Form.parent.prototype._renderProperties.call(this);
  this._renderTitle();
  this._renderSubTitle();
  this._renderIconId();
  this._renderClosable();
  this._renderSaveNeeded();
  this._renderCssClass();
  this._renderStatus();
};

scout.Form.prototype._postRender = function() {
  scout.Form.parent.prototype._postRender.call(this);

  this._installFocusContext();
  if (this.renderInitialFocusEnabled) {
    this.renderInitialFocus();
  }

  // Render attached forms, message boxes and file choosers.
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();
};

scout.Form.prototype._remove = function() {
  this.formController.remove();
  this.messageBoxController.remove();
  this.fileChooserController.remove();
  this._glassPaneRenderer.removeGlassPanes();
  this._uninstallFocusContext();
  scout.Form.parent.prototype._remove.call(this);
};

scout.Form.prototype._renderForm = function() {
  var layout, $handle;

  this.$container = this.$parent.appendDiv()
    .addClass(this.isDialog() ? 'dialog' : 'form')
    .data('model', this);

  if (this.uiCssClass) {
    this.$container.addClass(this.uiCssClass);
  }

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.pixelBasedSizing = false;
  if (this.isDialog()) {
    layout = new scout.DialogLayout(this);
    this.htmlComp.validateRoot = true;
    $handle = this.$container.appendDiv('drag-handle');
    this.$container.draggable($handle, $.throttle(this._onMove.bind(this), 1000 / 60)); // 60fps
    if (this.resizable) {
      this._initResizable();
    }
    this._renderHeader();
  } else {
    layout = new scout.FormLayout(this);
  }

  this.htmlComp.setLayout(layout);
  this.rootGroupBox.render();

  if (this._locked) {
    this.disable();
  }

  if (this.isDialog()) {
    this.$container.addClassForAnimation('animate-open');
  }
};

scout.Form.prototype._installLifecycle = function() {
  this.lifecycle = this._createLifecycle();
  this.lifecycle.handle('load', this._onLifecycleLoad.bind(this));
  this.lifecycle.handle('save', this._onLifecycleSave.bind(this));
  this.lifecycle.on('reset', this._onLifecycleReset.bind(this));
  this.lifecycle.on('close', this._onLifecycleClose.bind(this));
};

scout.Form.prototype._createLifecycle = function() {
  return scout.create('FormLifecycle', {
    widget: this,
    askIfNeedSave: this.askIfNeedSave
  });
};

/**
 * Renders the form by adding it to the desktop.
 */
scout.Form.prototype.open = function() {
  this.displayParent = this.displayParent || this.session.desktop;
  this.session.desktop.showForm(this, this.displayParent);
  return this.load();
};

/**
 * Initializes the life cycle and calls the {@link _load} function.
 * @returns {Promise}.
 */
scout.Form.prototype.load = function() {
  return this.lifecycle.load();
};

scout.Form.prototype._onLifecycleLoad = function() {
  return this._load().then(function(data) {
    this.setData(data);
    this.importData();
    this.trigger('load');
  }.bind(this));
};

/**
 * Method may be implemented to load the data. By default, the provided this.data is returned.
 */
scout.Form.prototype._load = function() {
  return $.resolvedPromise().then(function() {
    return this.data;
  }.bind(this));
};

scout.Form.prototype.setData = function(data) {
  this.setProperty('data', data);
};

scout.Form.prototype.importData = function() {
  // NOP
};

scout.Form.prototype.exportData = function() {
  // NOP
};

/**
 * Saves and closes the form.
 * @returns {Promise}.
 */
scout.Form.prototype.ok = function() {
  return this.lifecycle.ok();
};

/**
 * Saves the changes without closing the form.
 * @returns {Promise}.
 */
scout.Form.prototype.save = function() {
  return this.lifecycle.save();
};

scout.Form.prototype._onLifecycleSave = function() {
  var data = this.exportData();
  return this._save(data).then(function() {
    this.setData(data);
    this.trigger('save');
  }.bind(this));
};

scout.Form.prototype._save = function(data) {
  return $.resolvedPromise();
};

/**
 * Resets the form to its initial state.
 * @returns {Promise}.
 */
scout.Form.prototype.reset = function() {
  this.lifecycle.reset();
};

scout.Form.prototype._onLifecycleReset = function() {
  this.trigger('reset');
};

/**
 * Closes the form if there are no changes made. Otherwise it shows a message box asking to save the changes.
 * @returns {Promise}.
 */
scout.Form.prototype.cancel = function() {
  return this.lifecycle.cancel();
};

/**
 * Closes the form and discards any unsaved changes.
 * @returns {Promise}.
 */
scout.Form.prototype.close = function() {
  return this.lifecycle.close();
};

/**
 * Destroys the form and removes it from the desktop.
 */
scout.Form.prototype._onLifecycleClose = function() {
  var event = new scout.Event();
  this.trigger('close', event);
  if (!event.defaultPrevented) {
    this._close();
  }
};

scout.Form.prototype._close = function() {
  this.session.desktop.hideForm(this);
  this.destroy();
};

/**
 * This function is called when the user presses the "x" icon.<p>
 * It will either call {@link #close()} or {@link #cancel()), depending on the enabled and visible system buttons, see {@link _abort}.
 */
scout.Form.prototype.abort = function() {
  var event = new scout.Event();
  this.trigger('abort', event);
  if (!event.defaultPrevented) {
    this._abort();
  }
};

/**
 * Will call {@link #close()} if there is a close menu or button, otherwise {@link #cancel()) will be called.
 */
scout.Form.prototype._abort = function() {
  // Search for a close button in the menus and buttons of the root group box
  var hasCloseButton = this.rootGroupBox.controls
    .concat(this.rootGroupBox.menus)
    .filter(function(control) {
      var enabled = control.enabled;
      if (control.enabledComputed !== undefined) {
        enabled = control.enabledComputed; // Menus don't have enabledComputed, only form fields
      }
      return control.visible && enabled && control.systemType && control.systemType !== scout.Button.SystemType.NONE;
    })
    .some(function(control) {
      return control.systemType === scout.Button.SystemType.CLOSE;
    });

  if (hasCloseButton) {
    this.close();
  } else {
    this.cancel();
  }
};

scout.Form.prototype.setClosable = function(closable) {
  this.setProperty('closable', closable);
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
      .on('click', this._onCloseIconClick.bind(this));
  } else {
    if (!this.$close) {
      return;
    }
    this.$close.remove();
    this.$close = null;
  }
};

scout.Form.prototype._onCloseIconClick = function() {
  this.abort();
};

scout.Form.prototype._initResizable = function() {
  this.$container
    .resizable()
    .on('resize', this._onResize.bind(this));
};

scout.Form.prototype._onResize = function(event) {
  var autoSizeOld = this.htmlComp.layout.autoSize;
  this.htmlComp.layout.autoSize = false;
  this.htmlComp.revalidateLayout();
  this.htmlComp.layout.autoSize = autoSizeOld;
  this.updateCacheBounds();
  return false;
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

scout.Form.prototype._setRootGroupBox = function(rootGroupBox) {
  this._setProperty('rootGroupBox', rootGroupBox);
  if (this.rootGroupBox &&
    (this.isDialog() || this.searchForm || this.parent instanceof scout.WrappedFormField)) {
    this.rootGroupBox.menuBar.bottom();
  }
};

scout.Form.prototype._renderSaveNeeded = function() {
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

scout.Form.prototype.setAskIfNeedSave = function(askIfNeedSave) {
  this.setProperty('askIfNeedSave', askIfNeedSave);
  if (this.lifecycle) {
    this.lifecycle.setAskIfNeedSave(askIfNeedSave);
  }
};

scout.Form.prototype.setSaveNeededVisible = function(visible) {
  this.setProperty('saveNeededVisible', visible);
};

scout.Form.prototype._renderSaveNeededVisible = function() {
  this._renderSaveNeeded();
};

scout.Form.prototype._renderCssClass = function(cssClass, oldCssClass) {
  cssClass = cssClass || this.cssClass;
  this.$container.removeClass(oldCssClass);
  this.$container.addClass(cssClass);
  // Layout could have been changed, e.g. if subtitle becomes visible
  this.invalidateLayoutTree();
};

scout.Form.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.Form.prototype._setStatus = function(status) {
  status = scout.Status.ensure(status);
  this._setProperty('status', status);
};

scout.Form.prototype._renderStatus = function() {
  if (!this.isDialog()) {
    return;
  }

  this.$statusIcons.forEach(function($icn) {
    $icn.remove();
  });

  this.$statusIcons = [];

  if (this.status) {
    var statusList = this.status.asFlatList();
    var $prevIcon;
    statusList.forEach(function(sts) {
      $prevIcon = this._renderSingleStatus(sts, $prevIcon);
      if ($prevIcon) {
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
    if (status.cssClass()) {
      $statusIcon.addClass(status.cssClass());
    }
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
      $div = this.$parent.appendDiv(cssClass);
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

scout.Form.prototype._onMove = function(newOffset) {
  this.trigger('move', newOffset);
  this.updateCacheBounds();
};

scout.Form.prototype.updateCacheBounds = function() {
  if (this.cacheBounds) {
    this.storeCacheBounds(scout.graphics.offsetBounds(this.$container));
  }
};

scout.Form.prototype.appendTo = function($parent) {
  this.$container.appendTo($parent);
};

scout.Form.prototype.setTitle = function(title) {
  this.setProperty('title', title);
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

scout.Form.prototype.setSubTitle = function(subTitle) {
  this.setProperty('subTitle', subTitle);
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

scout.Form.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
};

scout.Form.prototype._renderIconId = function() {
  if (this.isDialog()) {
    this.$icon.icon(this.iconId);
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }
};

/**
 * Disables the form and calls setEnabled on the root group-box of the form.
 *
 * @param updateChildren
 *          (optional) If true the enabled property of all child form fields (recursive) are updated to same value as well.
 *
 * @override Widget.js
 */
scout.Form.prototype.setEnabled = function(enabled, updateChildren) {
  scout.Form.parent.prototype.setEnabled.call(this, enabled);
  this.rootGroupBox.setEnabled(enabled, undefined, updateChildren);
};

scout.Form.prototype._setViews = function(views) {
  if (views) {
    views.forEach(function(view) {
      view.setDisplayParent(this);
    }.bind(this));
  }
  this._setProperty('views', views);
};

/**
 * @override Widget.js
 */
scout.Form.prototype.setDisabledStyle = function(disabledStyle) {
  this.rootGroupBox.setDisabledStyle(disabledStyle);
};

scout.Form.prototype.setDisplayParent = function(displayParent) {
  if (this.displayParent === displayParent) {
    return;
  }
  this.setProperty('displayParent', displayParent);
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
    if (!this.initialFocus) {
      this.session.focusManager.requestFocus(this.session.focusManager.findFirstFocusableElement(this.$container));
    } else if (this.initialFocus instanceof scout.FormField) {
      this.initialFocus.focus();
    }
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
  var focusElement;
  if (this.initialFocus) {
    focusElement = this.initialFocus.getFocusableElement();
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

scout.Form.prototype.visitFields = function(visitor) {
  this.rootGroupBox.visit(visitor);
};

scout.Form.prototype.storeCacheBounds = function(bounds) {
  if (this.cacheBounds) {
    var storageKey = 'scout:formBounds:' + this.cacheBoundsKey;
    scout.webstorage.setItem(localStorage, storageKey, JSON.stringify(bounds));
  }
};

scout.Form.prototype.readCacheBounds = function() {
  if (!this.cacheBounds) {
    return null;
  }

  var storageKey = 'scout:formBounds:' + this.cacheBoundsKey;
  var bounds = scout.webstorage.getItem(localStorage, storageKey);
  if (!bounds) {
    return null;
  }
  bounds = JSON.parse(bounds);
  return new scout.Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
};

/**
 * Returns the form the widget belongs to (returns the first parent which is a {@link scout.Form}.
 */
scout.Form.findForm = function(widget) {
  var parent = widget.parent;
  while (parent && !(parent instanceof scout.Form)) {
    parent = parent.parent;
  }
  return parent;
};
