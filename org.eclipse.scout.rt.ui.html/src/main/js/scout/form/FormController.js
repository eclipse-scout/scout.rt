/**
 * The FormController provides functionality to show forms like dialogs and views.
 */
scout.FormController = function(parent, session, funcDialogStore, funcViewStore) {
  this._parent = parent;
  this.session = session;
  this._funcDialogStore = funcDialogStore;
  this._funcViewStore = funcViewStore;
  /**
   * Key = Form ID, Value = Instance of DesktopViewTab
   */
   this._viewTabMap = {};
};

/**
 * Adds the given Form to the 'formStore' and DOM.
 */
scout.FormController.prototype.addAndShow = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._parent);
  this._invokeViewElseDialogFunction(form, this._addAndShowView.bind(this), this._addAndShowDialog.bind(this));
};

/**
 * Removes the given Form from the 'formStore' and DOM. However, the form's adapter is not destroyed. That only happens once the Form is closed.
 */
scout.FormController.prototype.removeAndHide = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._parent);
  this._invokeViewElseDialogFunction(form, this._removeAndHideView.bind(this), this._removeAndHideDialog.bind(this));
};

/**
 * Adds all Forms contained in 'formStore' to the DOM.
 */
scout.FormController.prototype.showAll = function() {
  this._funcDialogStore().forEach(this._showDialog.bind(this));
  // FIXME DWI: (von A.WE) Problem: _showView ruft Desktop#_addTab auf, dort wird dann die jeweilige view gerendert.
  // Das darf aber nicht sein. Beim initialen Load soll nur die aktive view gerendert werden. Von allen anderen
  // Views darf nur der Tab der View gerendert werden.
  this._funcViewStore().forEach(this._showView.bind(this));
};

/**
 * Activates the given Form.
 * FIXME: not working for dialogs.
 */
scout.FormController.prototype.activateForm = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._parent);
  this._invokeViewElseDialogFunction(form, this._activateView.bind(this), this._activateDialog.bind(this));
};

scout.FormController.prototype._invokeViewElseDialogFunction = function(form, funcView, funcDialog) {
  if (form.displayHint === 'view') {
    funcView(form);
  } else {
    funcDialog(form);
  }
};

// ==== Dialog specific functionality ==== //

scout.FormController.prototype._addAndShowDialog = function(dialog) {
  this._funcDialogStore().push(dialog);
  this._showDialog(dialog);
};

scout.FormController.prototype._removeAndHideDialog = function(dialog) {
  scout.arrays.remove(this._funcDialogStore(), dialog);

  if (dialog.rendered) {
    dialog.remove();
  }
};

scout.FormController.prototype._showDialog = function(dialog) {
  dialog.render(this.session.desktop.$container);
  dialog.htmlComp.pixelBasedSizing = true;

  var prefSize = dialog.htmlComp.getPreferredSize(),
    dialogMargins = dialog.htmlComp.getMargins(),
    documentSize = new scout.Dimension($(document).width(), $(document).height()),
    dialogSize = new scout.Dimension();

  // class .dialog may specify a margin
  var maxWidth = (documentSize.width - dialogMargins.left - dialogMargins.right);
  var maxHeight = (documentSize.height - dialogMargins.top - dialogMargins.bottom);

  // Ensure the dialog is not larger than viewport
  dialogSize.width = Math.min(maxWidth, prefSize.width);
  dialogSize.height = Math.min(maxHeight, prefSize.height);

  var marginLeft = (documentSize.width - dialogSize.width) / 2;
  var marginTop = (documentSize.height - dialogSize.height) / 2;

  // optical middle
  var opticalMiddleOffset = Math.min(marginTop / 5, 10);
  marginTop -= opticalMiddleOffset;

  dialog.htmlComp.setSize(dialogSize);

  dialog.$container
    .cssMarginLeft(marginLeft)
    .cssMarginTop(marginTop);
};

scout.FormController.prototype._activateDialog = function(dialog) {
  // FIXME AWE: (modal dialog) - show dialogs
};

// ==== View specific functionality ==== //

scout.FormController.prototype._addAndShowView = function(view) {
  this._funcViewStore().push(view);
  this._showView(view);
};

scout.FormController.prototype._removeAndHideView = function(view) {
  scout.arrays.remove(this._funcViewStore(), view);
  if (view.rendered) {
    var viewTab = this._viewTabMap[view.id];
    this.session.desktop._removeTab(viewTab);
  }
  delete this._viewTabMap[view.id];
};

scout.FormController.prototype._showView = function(view) {
  var desktop = this.session.desktop;
  var viewTab = new scout.DesktopViewTab(view, desktop.$bench);
  viewTab.events.on('tabClicked', desktop._setSelectedTab.bind(desktop));
  if (desktop._hasTaskBar()) {
    viewTab.renderTab(desktop._$viewTabBar);
  }
  this._viewTabMap[view.id] = viewTab;
  desktop._addTab(viewTab);
  scout.focusManager.validateFocus(this.session.uiSessionId, 'desktop._renderView');
};

scout.FormController.prototype._activateView = function(view) {
  var viewTab = this._viewTabMap[view.id];
  this.session.desktop._setSelectedTab(viewTab);
};
