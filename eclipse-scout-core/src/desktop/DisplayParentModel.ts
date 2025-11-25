/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FileChooser, FileChooserController, Form, FormController, MessageBox, MessageBoxController, ObjectOrChildModel} from '../index';

export interface DisplayParentModel {
  /**
   * Defines the dialogs that should be rendered right after this widget is rendered.
   * The given dialogs will be linked to the current widget and use it as display parent.
   *
   * *Note*: the dialogs are only rendered, not opened. If you need the lifecycle (load, save etc.) to be run, you'll have to open the dialog programmatically.
   *
   * Default is [].
   */
  dialogs?: ObjectOrChildModel<Form>[];
  /**
   * Defines the views that should be rendered right after this widget is rendered.
   * The given views will be linked to the current widget and use it as display parent.
   *
   * *Note*: the views are only rendered, not opened. If you need the lifecycle (load, save etc.) to be run, you'll have to open the views programmatically.
   *
   * Default is [].
   */
  views?: ObjectOrChildModel<Form>[];
  /**
   * Defines the message boxes that should be rendered right after this widget is rendered.
   * The given message boxes will be linked to the current widget and use it as display parent.
   *
   * Default is [].
   */
  messageBoxes?: ObjectOrChildModel<MessageBox>[];
  /**
   * Defines the file choosers that should be rendered right after this widget is rendered.
   * The given file choosers will be linked to the current widget and use it as display parent.
   *
   * Default is [].
   */
  fileChoosers?: ObjectOrChildModel<FileChooser>[];
  formController?: FormController;
  messageBoxController?: MessageBoxController;
  fileChooserController?: FileChooserController;
}
