/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayChildController, MessageBox} from '../index';

/**
 * Controller with functionality to register and render message boxes.
 *
 * The message boxes are put into the list 'messageBoxes' contained in 'displayParent'.
 */
export class MessageBoxController extends DisplayChildController {

  /**
   * Removes all message boxes registered with this controller from DOM.
   */
  remove() {
    this.displayParent.messageBoxes.forEach(this._remove.bind(this));
  }

  /**
   * Renders all message boxes registered with this controller.
   */
  render() {
    this.displayParent.messageBoxes.forEach(msgBox => {
      msgBox.setDisplayParent(this.displayParent);
      this._render(msgBox);
    });
  }

  /**
   * Attaches all message boxes to their original DOM parents.
   * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
   *
   * This method has no effect if already attached.
   */
  attach() {
    this.displayParent.messageBoxes.forEach(messageBox => messageBox.attach());
  }

  /**
   * Detaches all message boxes from their DOM parents. Thereby, modality glassPanes are not detached.
   * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
   *
   * This method has no effect if already detached.
   */
  detach() {
    this.displayParent.messageBoxes.forEach(messageBox => messageBox.detach());
  }

  protected override _register(messageBox: MessageBox) {
    this._registerChild(messageBox, this.displayParent.messageBoxes, 'messageBoxes');
  }

  protected override _unregister(messageBox: MessageBox) {
    this._unregisterChild(messageBox, this.displayParent.messageBoxes, 'messageBoxes');
  }
}
