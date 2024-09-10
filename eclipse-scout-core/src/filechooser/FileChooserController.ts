/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayChildController, FileChooser} from '../index';

/**
 * Controller with functionality to register and render file choosers.
 *
 * The file choosers are put into the list fileChoosers contained in 'displayParent'.
 */
export class FileChooserController extends DisplayChildController {
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

  protected override _register(fileChooser: FileChooser) {
    this._registerChild(fileChooser, this.displayParent.fileChoosers, 'fileChoosers');
  }

  protected override _unregister(fileChooser: FileChooser) {
    this._unregisterChild(fileChooser, this.displayParent.fileChoosers, 'fileChoosers');
  }
}
