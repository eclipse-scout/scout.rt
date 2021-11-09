/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Desktop, Form, Outline, scout} from '../index';

/**
 * Controller with functionality to register and render views and dialogs.
 *
 * The forms are put into the list 'views' and 'dialogs' contained in 'displayParent'.
 */
export default class FormController {

  constructor(model) {
    this.displayParent = model.displayParent;
    this.session = model.session;
  }

  /**
   * Adds the given view or dialog to this controller and renders it.
   * position is only used if form is a view. this position determines at which position the tab is placed.
   * if select view is set the view rendered in _renderView is also selected.
   * @param {Form} form
   */
  registerAndRender(form, position, selectView) {
    scout.assertProperty(form, 'displayParent');
    if (form.isPopupWindow()) {
      this._renderPopupWindow(form);
    } else if (form.isView()) {
      this._renderView(form, true, position, selectView);
    } else {
      this._renderDialog(form, true);
    }
  }

  /**
   * @param {Form} form
   */
  isFormShown(form) {
    if (form.isView()) {
      return this.displayParent.views.indexOf(form) > -1;
    }
    return this.displayParent.dialogs.indexOf(form) > -1;
  }

  _renderPopupWindow(formAdapterId, position) {
    throw new Error('popup window only supported by DesktopFormController');
  }

  /**
   * Removes the given view or dialog from this controller and DOM. However, the form's adapter is not destroyed. That only happens once the Form is closed.
   * @param {Form} form
   */
  unregisterAndRemove(form) {
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
  }

  _removePopupWindow(form) {
    throw new Error('popup window only supported by DesktopFormController');
  }

  /**
   * Renders all dialogs and views registered with this controller.
   */
  render() {
    this._renderViews();

    this._renderDialogs();
  }

  _renderViews() {
    this.displayParent.views.forEach((view, position) => {
      view.setDisplayParent(this.displayParent);
      this._renderView(view, false, position, false);
    });
  }

  _renderDialogs() {
    this.displayParent.dialogs.forEach(dialog => {
      dialog.setDisplayParent(this.displayParent);
      this._renderDialog(dialog, false);
    });
  }

  /**
   * Removes all dialogs and views registered with this controller.
   */
  remove() {
    this.displayParent.dialogs.forEach(dialog => {
      this._removeDialog(dialog, false);
    });
    this.displayParent.views.forEach((view, position) => {
      this._removeView(view, false);
    });
  }

  /**
   * Activates the given view or dialog.
   */
  activateForm(form) {
    let displayParent = this.displayParent;
    while (displayParent) {
      if (displayParent instanceof Outline) {
        this.session.desktop.setOutline(displayParent);
        break;
      }
      displayParent = displayParent.displayParent;
    }

    if (form.displayHint === Form.DisplayHint.VIEW) {
      this._activateView(form);
    } else {
      this._activateDialog(form);
    }
  }

  acceptView(view, register, position, selectView) {
    // Only render view if 'displayParent' is rendered yet; if not, the view will be rendered once 'displayParent' is rendered.
    return this.displayParent.rendered;
  }

  _renderView(view, register, position, selectView) {
    if (register) {
      if (position !== undefined) {
        arrays.insert(this.displayParent.views, view, position);
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

    // Prevent "Already rendered" errors --> TODO [7.0] bsh: Remove this hack! Fix it on model if possible. See #162954.
    if (view.rendered) {
      return false;
    }
    let desktop = this.session.desktop;
    if (desktop.displayStyle === Desktop.DisplayStyle.COMPACT && !desktop.bench) {
      // Show bench and hide navigation if this is the first view to be shown
      desktop.sendOutlineToBack();
      // Don't show header if the view itself already has a header. Additionally, DesktopTabBoxController takes care of not rendering a tab if there is a view header.
      desktop.switchToBench(!view.headerVisible);
    } else if (desktop.bench.removalPending) {
      // If a new form should be shown while the bench is being removed because the last form was closed, schedule the rendering to make sure the bench and the new form will be opened right after the bench has been removed
      setTimeout(this._renderView.bind(this, view, register, position, selectView));
      return;
    }
    desktop.bench.addView(view, selectView);
  }

  acceptDialog(dialog) {
    // Only render dialog if 'displayParent' is rendered yet; if not, the dialog will be rendered once 'displayParent' is rendered.
    return this.displayParent.rendered;
  }

  _renderDialog(dialog, register) {
    let desktop = this.session.desktop;
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

    // Prevent "Already rendered" errors --> TODO [7.0] bsh: Remove this hack! Fix it on model if possible. See #162954.
    if (dialog.rendered) {
      return false;
    }

    dialog.on('remove', () => {
      let formToActivate = this._findFormToActivateAfterDialogRemove();
      if (formToActivate) {
        desktop._setFormActivated(formToActivate);
      } else {
        desktop._setOutlineActivated();
      }
    });

    if (dialog.isPopupWindow()) {
      this._renderPopupWindow(dialog);
    } else {
      // start focus tracking if not already started.
      dialog.setTrackFocus(true);
      dialog.render(desktop.$container);
      this._layoutDialog(dialog);
      desktop._setFormActivated(dialog);

      // Only display the dialog if its 'displayParent' is visible to the user.
      if (!this.displayParent.inFront()) {
        dialog.detach();
      }
    }
  }

  _findFormToActivateAfterDialogRemove() {
    if (this.displayParent.dialogs.length > 0) {
      return this.displayParent.dialogs[this.displayParent.dialogs.length - 1];
    }
    if (this.displayParent instanceof Form && !this.displayParent.detailForm) {
      // activate display parent, but not if it is the detail form
      return this.displayParent;
    }
    let desktop = this.session.desktop;
    if (desktop.bench) {
      let form = desktop.bench.activeViews()[0];
      if (form instanceof Form && !form.detailForm) {
        return form;
      }
    }
  }

  _removeView(view, unregister) {
    unregister = scout.nvl(unregister, true);
    if (unregister) {
      arrays.remove(this.displayParent.views, view);
    }
    // in COMPACT case views are already removed.
    if (this.session.desktop.bench) {
      this.session.desktop.bench.removeView(view);
    }
  }

  _removeDialog(dialog, unregister) {
    unregister = scout.nvl(unregister, true);
    if (unregister) {
      arrays.remove(this.displayParent.dialogs, dialog);
    }
    if (dialog.rendered) {
      dialog.remove();
    }
  }

  _activateView(view) {
    let bench = this.session.desktop.bench;
    if (bench) {
      // Bench may be null (e.g. in mobile mode). This may probably only happen if the form is not really a view, because otherwise the bench would already be open.
      // Example: form of a FormToolButton has display style set to view but is opened as menu popup rather than in the bench.
      // So this null check is actually a workaround because a better solution would be to never call this function for fake views, but currently it is not possible to identify them easily.
      bench.activateView(view);
    }
  }

  _activateDialog(dialog) {
    // If the display-parent is a view-form --> activate it always.
    // If it is another dialog --> activate it only if the dialog to activate is modal
    if (dialog.displayParent instanceof Form &&
      (dialog.displayParent.displayHint === Form.DisplayHint.VIEW ||
        (dialog.displayParent.displayHint === Form.DisplayHint.DIALOG && dialog.modal))) {
      this.activateForm(dialog.displayParent);
    }

    if (!dialog.rendered) {
      return;
    }

    let siblings = dialog.$container.nextAll().toArray();

    // Now the approach is to move all eligible siblings that are in the DOM after the given dialog.
    // It is important not to move the given dialog itself, because this would interfere with the further handling of the
    // mousedown-DOM-event that triggerd this function.
    let movableSiblings = siblings.filter(function(sibling) {
      // siblings of a dialog are movable if they meet the following criteria:
      // - they are forms (sibling forms of a dialog are always dialogs)
      // - they are either
      //     - not modal
      //     - modal
      //         - and not a descendant of the dialog to activate
      //         - and their display parent is not the desktop
      let siblingWidget = scout.widget(sibling);
      return siblingWidget instanceof Form &&
        (!siblingWidget.modal ||
          (!dialog.has(siblingWidget) && siblingWidget.displayParent !== this.session.desktop));
    }, this);

    // All descendants of the so far determined movableSiblings are movable as well. (E.g. MessageBox, FileChooser)
    let movableSiblingsDescendants = siblings.filter(sibling => {
      return arrays.find(movableSiblings, movableSibling => {
        let siblingWidget = scout.widget(sibling);
        return !(siblingWidget instanceof Form) && // all movable forms are already captured by the filter above
          scout.widget(movableSibling).has(siblingWidget);
      });
    });
    movableSiblings = movableSiblings.concat(movableSiblingsDescendants);

    this.session.desktop.moveOverlaysBehindAndFocus(movableSiblings, dialog.$container);
  }

  /**
   * Attaches all dialogs to their original DOM parents.
   * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
   *
   * This method has no effect if already attached.
   */
  attachDialogs() {
    this.displayParent.dialogs.forEach(dialog => {
      dialog.attach();
    }, this);
  }

  /**
   * Detaches all dialogs from their DOM parents. Thereby, modality glassPanes are not detached.
   * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
   *
   * This method has no effect if already detached.
   */
  detachDialogs() {
    this.displayParent.dialogs.forEach(dialog => {
      dialog.detach();
    }, this);
  }

  _layoutDialog(dialog) {
    dialog.htmlComp.validateLayout();
    dialog.position();

    // If not validated anew, focus on single-button forms is not gained.
    // Maybe, this is the same problem as in BusyIndicator.js
    this.session.focusManager.validateFocus();

    // Animate _after_ the layout is valid (otherwise, the position would be wrong, because
    // HtmlComponent defers the layout when a component is currently being animated)
    if (dialog.animateOpening) {
      dialog.$container.addClassForAnimation('animate-open');
    }
  }
}
