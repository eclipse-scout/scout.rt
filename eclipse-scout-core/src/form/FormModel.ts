/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FileChooser, FileChooserController, FileChooserModel, Form, FormController, GroupBox, GroupBoxModel, KeyStroke, MessageBox, MessageBoxController, MessageBoxModel, Status, Widget, WidgetModel} from '../index';
import {DisplayHint} from './Form';
import {RefModel} from '../types';
import StatusModel from '../status/StatusModel';

export default interface FormModel extends WidgetModel {
  /**
   * Default is true.
   */
  animateOpening?: boolean;
  /**
   * AskIfNeedSave defines if a message box with yes, no and cancel option is shown to the user for confirmation after
   * having made at least one change in the form and then having pressed the cancel button.
   *
   * Default is true.
   */
  askIfNeedSave?: boolean;
  /**
   * if not set, a default text is used (see Lifecycle.js)
   */
  askIfNeedSaveText?: string;
  /**
   * Default is {}.
   */
  data?: object;
  displayViewId?: string;
  /**
   * Default is Form.DisplayHint.DIALOG.
   */
  displayHint?: DisplayHint;
  /**
   * Default is false.
   */
  maximized?: boolean;
  /**
   * The header contains the title, subtitle, icon, save needed status and close action.
   * true, to show a header, false to not show a header. Null, to let the UI decide what to do, which means: show a header if it is a dialog, otherwise don't show one.
   */
  headerVisible?: boolean;
  /**
   * Default is true.
   */
  modal?: boolean;
  /**
   * Default is [].
   */
  dialogs?: Form[] | RefModel<FormModel>[];
  /**
   * Default is [].
   */
  views?: Form[] | RefModel<FormModel>[];
  /**
   * Default is [].
   */
  messageBoxes?: MessageBox[] | RefModel<MessageBoxModel>[];
  /**
   * Default is [].
   */
  fileChoosers?: FileChooser[] | RefModel<FileChooserModel>[];
  focusedElement?: Widget;
  /**
   * Defines whether the form should display a close button [X] in the form header resp. view tab.
   *
   * Default is true.
   */
  closable?: boolean;
  /**
   * Default is false.
   */
  cacheBounds?: boolean;
  cacheBoundsKey?: string;
  /**
   * Default is true.
   */
  resizable?: boolean;
  /**
   * Default is true.
   */
  movable?: boolean;
  rootGroupBox?: GroupBox | RefModel<GroupBoxModel>;
  /**
   * Default is false.
   */
  saveNeeded?: boolean;
  /**
   * Whether or not a changed form should display the save needed state (dirty) in the dialog or view header.
   * true to display the save needed state, false otherwise.
   *
   * Default is false.
   */
  saveNeededVisible?: boolean;
  formController?: FormController;
  messageBoxController?: MessageBoxController;
  fileChooserController?: FileChooserController;
  closeKeyStroke?: KeyStroke;
  /**
   * Configure whether to show this {@link Form} once started.
   * If set to true and this {@link Form} is started, it is added to the {@link Desktop} in order to be
   * displayed.
   *
   * Default is true.
   */
  showOnOpen?: boolean;
  /**
   * The widget to be focused initially when the form renders. If not set, the first focusable element will be focused.
   * If a string is provided, the widget will be resolved automatically in the context of the form.
   *
   * Default is null.
   */
  initialFocus?: Widget | string;
  /**
   * Whether this form should render its initial focus
   *
   * Default is true.
   */
  renderInitialFocusEnabled?: boolean;
  title?: string;
  subTitle?: string;
  iconId?: string;
  status?: Status | StatusModel;
}
