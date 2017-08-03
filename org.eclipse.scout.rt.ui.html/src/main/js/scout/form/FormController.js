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
scout.FormController = function(model) {
  this.displayParent = model.displayParent;
  this.session = model.session;
};

/**
 * Adds the given view or dialog to this controller and renders it.
 * position is only used if form is a view. this position determines at which position the tab is placed.
 * if select view is set the view rendered in _renderView is also selected.
 */
scout.FormController.prototype.registerAndRender = function(form, position, selectView) {
  form._setProperty('displayParent', this.displayParent);
  if (form.isPopupWindow()) {
    this._renderPopupWindow(form);
  } else if (form.isView()) {
    this._renderView(form, true, position, selectView);
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
scout.FormController.prototype.unregisterAndRemove = function(form) {
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
  this._renderViews();

  this._renderDialogs();
};

scout.FormController.prototype._renderViews = function() {
  this.displayParent.views.forEach(function(view, position) {
    view._setProperty('displayParent', this.displayParent);
    this._renderView(view, false, position, false);
  }.bind(this));
};

scout.FormController.prototype._renderDialogs = function() {
  this.displayParent.dialogs.forEach(function(dialog) {
    dialog._setProperty('displayParent', this.displayParent);
    this._renderDialog(dialog, false);
  }.bind(this));
};
/**
 * Removes all dialogs and views registered with this controller.
 */
scout.FormController.prototype.remove = function() {
  this.displayParent.dialogs.forEach(function(dialog) {
    this._removeDialog(dialog, false);
  }.bind(this));
  this.displayParent.views.forEach(function(view, position) {
    this._removeView(view, false);
  }.bind(this));
};

/**
 * Activates the given view or dialog.
 */
scout.FormController.prototype.activateForm = function(form) {
  // TODO [7.0] awe: (2nd screen) handle popupWindow?
  if (form.displayHint === scout.Form.DisplayHint.VIEW) {
    this._activateView(form);
  } else {
    this._activateDialog(form);
  }
};

scout.FormController.prototype.acceptView = function(view, register, position, selectView) {
  // Only render view if 'displayParent' is rendered yet; if not, the view will be rendered once 'displayParent' is rendered.
  if (!this.displayParent.rendered) {
    return false;
  }
  return true;
};

scout.FormController.prototype._renderView = function(view, register, position, selectView) {
  if (register) {
    if (position !== undefined) {
      scout.arrays.insert(this.displayParent.views, view, position);
    } else {
      this.displayParent.views.push(view);
    }
  }

  // Display parent may implement acceptView, if not implemented -> use default
  if (this.displayParent.acceptView) {
    if (!this.displayParent.acceptView(view)) {
      return;
    }
  } else if (!this.acceptView(view)) {
    return;
  }

  // Prevent "Already rendered" errors --> TODO [7.0] BSH: Remove this hack! Fix in on model if possible. See #162954.
  if (view.rendered) {
    return false;
  }
  if (this.session.desktop.displayStyle === scout.Desktop.DisplayStyle.COMPACT && !this.session.desktop.bench) {
    // Show bench and hide navigation if this is the first view to be shown
    this.session.desktop.sendOutlineToBack();
    this.session.desktop.switchToBench();
  } else if (this.session.desktop.bench.removalPending) {
    // If a new form should be shown while the bench is being removed because the last form was closed, schedule the rendering to make sure the bench and the new form will be opened right after the bench has been removed
    setTimeout(this._renderView.bind(this, view, register, position, selectView));
    return;
  }
  this.session.desktop.bench.addView(view, selectView);
};

scout.FormController.prototype.acceptDialog = function(dialog) {
  // Only render dialog if 'displayParent' is rendered yet; if not, the dialog will be rendered once 'displayParent' is rendered.
  if (!this.displayParent.rendered) {
    return false;
  }
  return true;
};

scout.FormController.prototype._renderDialog = function(dialog, register) {
  var desktop = this.session.desktop;
  if (register) {
    this.displayParent.dialogs.push(dialog);
  }

  // Display parent may implement acceptDialog, if not implemented -> use default
  if (this.displayParent.acceptDialog) {
    if (!this.displayParent.acceptDialog(dialog)) {
      return;
    }
  } else if (!this.acceptDialog(dialog)) {
    return;
  }

  // Prevent "Already rendered" errors --> TODO [7.0] BSH: Remove this hack! Fix in on model if possible. See #162954.
  if (dialog.rendered) {
    return false;
  }

  dialog.on('remove', function() {
    if (this.displayParent.dialogs.length > 0) {
      desktop._setFormActivated(this.displayParent.dialogs[this.displayParent.dialogs.length - 1]);
    } else if (this.displayParent instanceof scout.Form && !this.displayParent.detailForm) {
      // activate display parent, but not if it is the detail form
      desktop._setFormActivated(this.displayParent);
    } else {
      desktop._setOutlineActivated();
    }
  }.bind(this));

  if (dialog.isPopupWindow()) {
    this._renderPopupWindow(dialog);
  } else {
    dialog.render(desktop.$container);
    this._layoutDialog(dialog);
    desktop._setFormActivated(dialog);

    // Only display the dialog if its 'displayParent' is visible to the user.
    if (!this.displayParent.inFront()) {
      dialog.detach();
    }
  }
};

scout.FormController.prototype._removeView = function(view, unregister) {
  unregister = scout.nvl(unregister, true);
  if (unregister) {
    scout.arrays.remove(this.displayParent.views, view);
  }
  // in COMPACT case views are already removed.
  if (this.session.desktop.bench) {
    this.session.desktop.bench.removeView(view);
  }
};

scout.FormController.prototype._removeDialog = function(dialog, unregister) {
  unregister = scout.nvl(unregister, true);
  if (unregister) {
    scout.arrays.remove(this.displayParent.dialogs, dialog);
  }
  if (dialog.rendered) {
    dialog.remove();
  }
};

scout.FormController.prototype._activateView = function(view) {
  this.session.desktop.bench.activateView(view);
};

scout.FormController.prototype._activateDialog = function(dialog) {
  if (this.displayParent.inFront() && !dialog.attached) {
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
  this.displayParent.dialogs.forEach(function(dialog) {
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
  this.displayParent.dialogs.forEach(function(dialog) {
    dialog.detach();
  }, this);
};

scout.FormController.prototype._layoutDialog = function(dialog) {
  var cacheBounds, position;
  dialog.htmlComp.pixelBasedSizing = true;
  dialog.htmlComp.validateLayout();

  cacheBounds = dialog.readCacheBounds();
  if (cacheBounds) {
    position = cacheBounds.point();
  } else {
    position = scout.DialogLayout.positionContainerInWindow(dialog.$container);
  }

  dialog.$container.cssPosition(position);
  dialog.trigger('move', {
    left: position.x,
    top: position.y
  });

  dialog.updateCacheBounds();

  // If not validated anew, focus on single-button forms is not gained.
  // Maybe, this is the same problem as in BusyIndicator.js
  this.session.focusManager.validateFocus();
};
