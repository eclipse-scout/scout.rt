/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayParentModel, FileChooser, FileChooserController, Form, FormController, MessageBox, MessageBoxController, Widget} from '../index';

export interface DisplayParent extends Widget, DisplayParentModel {
  views: Form[];
  dialogs: Form[];
  formController: FormController;

  messageBoxes: MessageBox[];
  messageBoxController: MessageBoxController;

  fileChoosers: FileChooser[];
  fileChooserController: FileChooserController;

  inFront(): boolean;

  acceptView?(view: Widget): boolean;

  acceptDialog?(dialog: Widget): boolean;

  onGlassPaneMouseDown?(glassPaneOwner: Widget, $glassPane: JQuery);
}
