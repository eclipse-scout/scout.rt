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
 * position is only used if form is a view. this position determines at which position the tab is placed.
 */
scout.FormController.prototype.registerAndRender = function(formAdapterId, position) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._displayParent);
  form.displayParent = this._displayParent;
  if (form.isPopupWindow()) {
    this._renderPopupWindow(form);
  } else if (form.isView()) {
    this._renderView(form, true, position);
  } else {
    this._renderDialog(form, true);
  }
};

scout.FormController.prototype._renderPopupWindow = function(formAdapterId, position) {
  throw new Error('popup window only supported by DesktopFormController');
};

/**
 * Removes the given view or dialog from this controller and DOM. However, the form's adapter is not destroyed. That only happens once the Form is closed.
 */
scout.FormController.prototype.unregisterAndRemove = function(formAdapterId) {
  var form = this.session.getModelAdapter(formAdapterId);
  if (!form) {
    return;
  }

  if (form.isPopupWindow()) {
    this._removePopupWindow(form);
  } else if (form.isView()) {
    this._removeView(form);
  } else {
    this._removeDialog(form);
  }
};

scout.FormController.prototype._removePopupWindow = function(form) {
  throw new Error('popup window only supported by DesktopFormController');
};

/**
 * Renders all dialogs and views registered with this controller.
 */
scout.FormController.prototype.render = function() {
  this._displayParent.dialogs.forEach(function(dialog) {
    dialog.displayParent = this._displayParent;
    this._renderDialog(dialog, false);
  }.bind(this));
  this._displayParent.views.forEach(function(view, position) {
    view.displayParent = this._displayParent;
    this._renderView(view, false, position);
  }.bind(this));
};

/**
 * Activates the given view or dialog.
 */
scout.FormController.prototype.activateForm = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._displayParent);

  // FIXME awe: (2nd screen) handle popupWindow?
  if (form.displayHint === scout.Form.DisplayHint.VIEW) {
    this._activateView(form);
  } else {
    this._activateDialog(form);
  }
};

scout.FormController.prototype._renderView = function(view, register, position) {
  if (register) {
    if (position !== undefined) {
      scout.arrays.insert(this._displayParent.views, view, position);
    } else {
      this._displayParent.views.push(view);
    }
  }

  // Only render view if 'displayParent' is rendered yet; if not, the view will be rendered once 'displayParent' is rendered.
  if (!this._displayParent.rendered) {
    return;
  }
  // Prevent "Already rendered" errors / FIXME bsh, dwi: Remove this hack! Fix in on model if possible. See #162954.
  if (view.rendered) {
    return;
  }

  var viewTabsController = this.session.desktop.viewTabsController;

  // Create the view-tab.
  var viewTab = viewTabsController.createAndRenderViewTab(view, this._displayParent.views.indexOf(view));
  viewTabsController.selectViewTab(viewTab);
};

scout.FormController.prototype._renderDialog = function(dialog, register) {
  if (register) {
    this._displayParent.dialogs.push(dialog);
  }
  if (this._displayParent instanceof scout.Form) {
    dialog.on('remove', function() {
      if (this._displayParent.dialogs.length > 0) {
        this.session.desktop._setFormActivated(this._displayParent.dialogs[this._displayParent.dialogs.length - 1]);
      } else if (this._displayParent.parent instanceof scout.Outline) {
        // if displayParent is a page
        this.session.desktop._setOutlineActivated();
      } else {
        this.session.desktop._setFormActivated(this._displayParent);
      }
    }.bind(this));
  }

  // Only render dialog if 'displayParent' is rendered yet; if not, the dialog will be rendered once 'displayParent' is rendered.
  if (!this._displayParent.rendered) {
    return;
  }
  // Prevent "Already rendered" errors / FIXME bsh, dwi: Remove this hack! Fix in on model if possible. See #162954.
  if (dialog.rendered) {
    return;
  }

  if (dialog.isPopupWindow()) {
    this._renderPopupWindow(dialog);
  } else {
    dialog.render(this.session.desktop.$container);
    this._layoutDialog(dialog);

    // Only display the dialog if its 'displayParent' is visible to the user.
    if (!this._displayParent.inFront()) {
      dialog.detach();
    }
  }
};

scout.FormController.prototype._removeView = function(view) {
  scout.arrays.remove(this._displayParent.views, view);
  if (view.rendered) {
    view.remove();
  }
};

scout.FormController.prototype._removeDialog = function(dialog) {
  scout.arrays.remove(this._displayParent.dialogs, dialog);
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
  if (this._displayParent.inFront() && !dialog.attached) {
    dialog.attach();
  }
};

/**
 * Attaches all dialogs to their original DOM parents.
 * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already attached.
 */
scout.FormController.prototype.attachDialogs = function() {
  this._displayParent.dialogs.forEach(function(dialog) {
    dialog.attach();
  }, this);
};

/**
 * Detaches all dialogs from their DOM parents. Thereby, modality glassPanes are not detached.
 * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already detached.
 */
scout.FormController.prototype.detachDialogs = function() {
  this._displayParent.dialogs.forEach(function(dialog) {
    dialog.detach();
  }, this);
};

scout.FormController.prototype._layoutDialog = function(dialog) {
  var left, top, opticalMiddleOffset, dialogSize,
    $document = dialog.$container.document(),
    documentSize = new scout.Dimension($document.width(), $document.height());

  dialog.htmlComp.pixelBasedSizing = true;
  dialog.htmlComp.validateLayout();

  dialogSize = dialog.htmlComp.getSize(true);
  left = (documentSize.width - dialogSize.width) / 2;
  top = (documentSize.height - dialogSize.height) / 2;

  // optical middle
  opticalMiddleOffset = Math.min(top / 5, 10);
  top -= opticalMiddleOffset;

  dialog.$container
    .cssLeft(left)
    .cssTop(top);

  // FIXME dwi: If not validated anew, focus on single-button forms is not gained.
  //                 Maybe, this is the same problem as in BusyIndicator.js
  this.session.focusManager.validateFocus();
};
