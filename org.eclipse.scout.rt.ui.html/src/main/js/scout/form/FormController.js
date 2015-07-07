/**
 * Controller with functionality to register and render views and dialogs.
 *
 * The forms are put into the list 'views' and 'dialogs' contained in 'displayParent'.
 */
scout.FormController = function(displayParent, session) {
  this._displayParent = displayParent;
  this.session = session;
};

/**
 * Adds the given view or dialog to this controller and renders it.
 */
scout.FormController.prototype.registerAndRender = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._displayParent);

  if (form.displayHint === 'view') {
    this._renderView(form, true);
  } else {
    this._renderDialog(form, true);
  }
};

/**
 * Removes the given view or dialog from this controller and DOM. However, the form's adapter is not destroyed. That only happens once the Form is closed.
 */
scout.FormController.prototype.unregisterAndRemove = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._displayParent);
  if (form.displayHint === 'view') {
    this._removeView(form, true);
  } else {
    this._removeDialog(form, true);
  }
};

/**
 * Removes all dialogs registered with this controller from DOM.
 */
scout.FormController.prototype.removeDialogs = function(messageBox) {
  this._displayParent.dialogs.forEach(this._removeDialog.bind(this));
};

/**
 * Renders all dialogs and views registered with this controller.
 */
scout.FormController.prototype.render = function() {
  this._displayParent.dialogs.forEach(this._renderDialog.bind(this));
  this._displayParent.views.forEach(this._renderView.bind(this));
};

/**
 * Renders all dialogs registered with this controller.
 */
scout.FormController.prototype.renderDialogs = function() {
  this._displayParent.dialogs.forEach(this._renderDialog.bind(this));
};

/**
 * Activates the given view or dialog.
 */
scout.FormController.prototype.activateForm = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._displayParent);

  if (form.displayHint === 'view') {
    this._activateView(form);
  } else {
    this._activateDialog(form);
  }
};

scout.FormController.prototype._renderView = function(view, register) {
  if (register) {
    this._displayParent.views.push(view);
  }

  this.session.desktop.viewTabsController.createAndRenderViewTab(view, true);
  scout.focusManager.validateFocus(this.session.uiSessionId, 'desktop._renderView');
};

scout.FormController.prototype._renderDialog = function(dialog, register) {
  if (register) {
    this._displayParent.dialogs.push(dialog);
  }

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

scout.FormController.prototype._removeView = function(view, unregister) {
  if (unregister) {
    scout.arrays.remove(this._displayParent.views, view);
  }

  if (view.rendered) {
    view.remove();
  }
};

scout.FormController.prototype._removeDialog = function(dialog, unregister) {
  if (unregister) {
    scout.arrays.remove(this._displayParent.dialogs, dialog);
  }

  if (dialog.rendered) {
    dialog.remove();
  }
};

scout.FormController.prototype._activateView = function(view) {
  var viewTabsController = this.session.desktop.viewTabsController;

  var viewTab = viewTabsController.viewTab(view);
  viewTabsController.selectViewTab(viewTab);
};

scout.FormController.prototype._activateDialog = function(dialog) {
  // FIXME AWE: not implemented yet.
};
