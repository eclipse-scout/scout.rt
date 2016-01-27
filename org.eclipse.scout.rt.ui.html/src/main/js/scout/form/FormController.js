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
  this.displayParent = displayParent;
  this.session = session;
};

/**
 * Adds the given view or dialog to this controller and renders it.
 * position is only used if form is a view. this position determines at which position the tab is placed.
 * if select view is set the view rendered in _renderView is also selected.
 */
scout.FormController.prototype.registerAndRender = function(formAdapterId, position, selectView) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this.displayParent);
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
  this.displayParent.dialogs.forEach(function(dialog) {
    dialog._setProperty('displayParent', this.displayParent);
    this._renderDialog(dialog, false);
  }.bind(this));
  this.displayParent.views.forEach(function(view, position) {
    view._setProperty('displayParent', this.displayParent);
    this._renderView(view, false, position);
  }.bind(this));
};

/**
 * Activates the given view or dialog.
 */
scout.FormController.prototype.activateForm = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this.displayParent);
  //if form is not rendered it could not be activated.
  if (!form.rendered) {
    return;
  }
  // FIXME awe: (2nd screen) handle popupWindow?
  if (form.displayHint === scout.Form.DisplayHint.VIEW) {
    this._activateView(form);
  } else {
    this._activateDialog(form);
  }
};

scout.FormController.prototype._renderView = function(view, register, position, selectView) {
  if (register) {
    if (position !== undefined) {
      scout.arrays.insert(this.displayParent.views, view, position);
    } else {
      this.displayParent.views.push(view);
    }
  }

  // Only render view if 'displayParent' is rendered yet; if not, the view will be rendered once 'displayParent' is rendered.
  // Except when Desktop is in initial rendering-> the tab has to be rendered to exist in overview
  if (!this.displayParent.rendered && !this.session.desktop.initialFormRendering) {
    return;
  }
  // Prevent "Already rendered" errors / FIXME bsh, dwi: Remove this hack! Fix in on model if possible. See #162954.
  if (view.rendered) {
    return;
  }

  var viewTabsController = this.session.desktop.viewTabsController;

  // Create the view-tab.
  var viewTab = viewTabsController.createAndRenderViewTab(view, this.displayParent.views.indexOf(view));
  if (selectView) {
    viewTabsController.selectViewTab(viewTab);
  }
};

scout.FormController.prototype._renderDialog = function(dialog, register) {
  if (register) {
    this.displayParent.dialogs.push(dialog);
  }
  if (this.displayParent instanceof scout.Form) {
    dialog.on('remove', function() {
      if (this.displayParent.dialogs.length > 0) {
        this.session.desktop._setFormActivated(this.displayParent.dialogs[this.displayParent.dialogs.length - 1]);
      } else if (this.displayParent.parent instanceof scout.Outline) {
        // if displayParent is a page
        this.session.desktop._setOutlineActivated();
      } else {
        this.session.desktop._setFormActivated(this.displayParent);
      }
    }.bind(this));
  }

  // Only render dialog if 'displayParent' is rendered yet; if not, the dialog will be rendered once 'displayParent' is rendered.
  if (!this.displayParent.rendered) {
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
    if (!this.displayParent.inFront()) {
      dialog.detach();
    }
  }
};

scout.FormController.prototype._removeView = function(view) {
  scout.arrays.remove(this.displayParent.views, view);
  if (view.rendered) {
    view.remove();
  }
};

scout.FormController.prototype._removeDialog = function(dialog) {
  scout.arrays.remove(this.displayParent.dialogs, dialog);
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
  var left, top, opticalMiddleOffset, dialogSize,
    $window = dialog.$container.window(),
    windowSize = new scout.Dimension($window.width(), $window.height());

  dialog.htmlComp.pixelBasedSizing = true;
  dialog.htmlComp.validateLayout();

  dialogSize = dialog.htmlComp.getSize(true);
  left = (windowSize.width - dialogSize.width) / 2;
  top = (windowSize.height - dialogSize.height) / 2;

  // optical middle
  opticalMiddleOffset = Math.min(top / 5, 10);
  top -= opticalMiddleOffset;

  dialog.$container
    .cssLeft(left)
    .cssTop(top);

  dialog.trigger('move', {
    top: top,
    left: left
  });

  // FIXME dwi: If not validated anew, focus on single-button forms is not gained.
  //                 Maybe, this is the same problem as in BusyIndicator.js
  this.session.focusManager.validateFocus();
};
