/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, DisplayParent, FileChooser, Form, scout, Session} from '../index';

/**
 * Controller with functionality to register and render file choosers.
 *
 * The file choosers are put into the list fileChoosers contained in 'displayParent'.
 */
export class FileChooserController {
  displayParent: DisplayParent;
  session: Session;

  constructor(displayParent: DisplayParent, session: Session) {
    this.displayParent = displayParent;
    this.session = session;
  }

  /**
   * Adds the given file chooser to this controller and renders it.
   */
  registerAndRender(fileChooser: FileChooser) {
    scout.assertProperty(fileChooser, 'displayParent');
    this._render(fileChooser, true);
  }

  /**
   * Removes the given file chooser from this controller and DOM. However, the file chooser's adapter is not destroyed. That only happens once the file chooser is closed.
   */
  unregisterAndRemove(fileChooser: FileChooser) {
    if (fileChooser) {
      arrays.remove(this.displayParent.fileChoosers, fileChooser);
      this._remove(fileChooser);
    }
  }

  /**
   * Removes all file choosers registered with this controller from DOM.
   */
  remove() {
    this.displayParent.fileChoosers.forEach(this._remove.bind(this));
  }

  /**
   * Renders all file choosers registered with this controller.
   */
  render() {
    this.displayParent.fileChoosers.forEach(chooser => {
      chooser.setDisplayParent(this.displayParent);
      this._render(chooser);
    });
  }

  protected _render(fileChooser: FileChooser, register?: boolean) {
    // missing displayParent (when render is called by reload), use displayParent of FileChooserController
    if (!fileChooser.displayParent) {
      fileChooser._setProperty('displayParent', this.displayParent);
    }
    // Prevent "Already rendered" errors (see #162954).
    if (fileChooser.rendered) {
      return;
    }
    if (register && !arrays.contains(this.displayParent.fileChoosers, fileChooser)) {
      this.displayParent.fileChoosers.push(fileChooser);
    }

    // Use parent's function or (if not implemented) our own.
    if (this.displayParent.acceptView) {
      if (!this.displayParent.acceptView(fileChooser)) {
        return;
      }
    } else if (!this.acceptView(fileChooser)) {
      return;
    }

    // Open all file choosers in the center of the desktop, except the ones that belong to a popup-window
    // Since the file chooser doesn't have a DOM element as parent when render is called, we must find the
    // entryPoint by using the model.
    let $parent;
    if (this.displayParent instanceof Form && this.displayParent.isPopupWindow()) {
      $parent = this.displayParent.popupWindow.$container;
    } else {
      $parent = this.session.desktop.$container;
    }
    // start focus tracking if not already started.
    fileChooser.setTrackFocus(true);
    fileChooser.render($parent);

    // Only display the file chooser if its 'displayParent' is visible to the user.
    if (!this.displayParent.inFront()) {
      fileChooser.detach();
    }
  }

  protected _remove(fileChooser: FileChooser) {
    fileChooser.remove();
  }

  /**
   * Attaches all file choosers to their original DOM parents.
   * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
   *
   * This method has no effect if already attached.
   */
  attach() {
    this.displayParent.fileChoosers.forEach(fileChooser => fileChooser.attach());
  }

  /**
   * Detaches all file choosers from their DOM parents. Thereby, modality glassPanes are not detached.
   * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
   *
   * This method has no effect if already detached.
   */
  detach() {
    this.displayParent.fileChoosers.forEach(fileChooser => fileChooser.detach());
  }

  acceptView(view: FileChooser): boolean {
    return this.displayParent.rendered;
  }
}
