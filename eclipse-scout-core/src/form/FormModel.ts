/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayHint, DisplayParent, DisplayParentModel, DisplayViewId, Form, FormValidator, GroupBox, ObjectOrChildModel, StatusOrModel, Widget, WidgetModel} from '../index';

export interface FormModel extends WidgetModel, DisplayParentModel {
  /**
   * Specifies whether the opening of a dialog should be animated.
   *
   * If set to true, the CSS class `animate-open` is added to the form container and a default CSS animation runs.
   * It only has an effect for forms with {@link displayHint} set to {@link Form.DisplayHint.DIALOG}.
   *
   * Default is true.
   */
  animateOpening?: boolean;
  /**
   * Defines if a message box with yes, no and cancel option is shown to the user for confirmation after
   * having made at least one change in the form and then having pressed the cancel button.
   *
   * Default is true.
   */
  askIfNeedSave?: boolean;
  /**
   * Configures the text to be shown on the message box displayed when cancelling a form that has changes, see {@link askIfNeedSave}.
   *
   * If not set, a default text is used (see {@link FormLifecycle.saveChangesQuestionTextKey}.
   */
  askIfNeedSaveText?: string;
  /**
   * The header text to show in message box when the Form validation failed (e.g. on a missing or invalid value).
   *
   * If not set, a default text is used: the text with key 'FormValidationFailedTitle'
   */
  validationFailedText?: string;
  /**
   * Configures the text to show in the Form validation message box for missing mandatory fields.
   *
   * If not set, a default text is used: the text with key 'FormEmptyMandatoryFieldsMessage'.
   */
  emptyMandatoryElementsText?: string;
  /**
   * Configures the text to show in the Form validation message box for fields having invalid values.
   *
   * If not set, a default text is used: the text with key 'FormInvalidFieldsMessage'.
   */
  invalidElementsErrorText?: string;
  /**
   * Configures the text to show in the Form validation message box for fields having a warning status.
   *
   * If not set, a default text is used: the text with key 'FormInvalidFieldsWarningMessage'.
   */
  invalidElementsWarningText?: string;
  /**
   * The data property can be used to store arbitrary data on the form.
   *
   * It is typically mapped to form fields in {@link Form.importData} and exported from the form fields back to the data object in {@link Form.exportData}.
   * Instead of passing the data to the form, you can implement {@link Form.\_load} to load the data when the form opens and {@link Form._save} to store it when the form is saved.
   *
   * Default is {}.
   */
  data?: any;
  /**
   * The exclusive key is used by {@link Desktop.createFormExclusive} to check whether a similar form is already open, and if yes, activate that form instead of opening a new one.
   *
   * The exclusive key can be anything, a primitive, an object or a function returning the key.
   */
  exclusiveKey?: any | (() => any);
  /**
   * Defines where the view will be opened in the {@link DesktopBench} if the {@link displayHint} is set to {@link Form.DisplayHint.VIEW}.
   *
   * By default, views are opened in the center area ({@link DisplayViewId.C}).
   */
  displayViewId?: DisplayViewId;
  /**
   * Defines the way the form should be displayed.
   *
   * - {@link Form.DisplayHint.DIALOG}: The form will be opened as overlay.
   *   It will be as large as its content prefers and can be moved or resized by the user.
   * - {@link Form.DisplayHint.VIEW}: The form will be opened in the {@link DesktopBench} in the area specified by {@link displayViewId}.
   *   The area is in fact a tab box, so each view opened in the same area gets a tab if {@link title}, {@link subTitle} or an {@link icon} is set.
   *   Selecting a tab will activate the corresponding view.
   *   Thew will be as large as the area.
   * - {@link Form.DisplayHint.POPUP_WINDOW}: The form will be opened in a separate browser window.
   *   It will be as large as its content prefers and can be moved or resized by the user.
   *
   * Default is {@link Form.DisplayHint.DIALOG}.
   */
  displayHint?: DisplayHint;
  /**
   * Defines whether a dialog should use the whole window size and therefore cover the whole desktop.
   *
   * It only has an effect for forms with {@link displayHint} set to {@link Form.DisplayHint.DIALOG}.
   *
   * Default is false.
   */
  maximized?: boolean;
  /**
   * The header contains the {@link title}, {@link subtitle}, {@link icon}, {@link saveNeeded} status and close action (controlled by {@link closable}).
   *
   * - If set to true, a header will be shown.
   * - If set to false, no header will be shown.
   * - If set to null, the UI will decide what to do, which means: show a header if {@link displayHint} is set to {@link DisplayHint.DIALOG}, otherwise don't show one.
   *
   * Default is null (= header is visible if the form is a dialog).
   */
  headerVisible?: boolean;
  /**
   * Controls whether the user is allowed to interact with the user interface outside the form.
   *
   * - If set to true, the user can only interact with the form and the rest is blocked.
   * - If set to false, the interaction is not limited to the form.
   * - If set to null, the UI decides whether to use true or false, which means: modal will be true if {@link displayHint} is set to {@link DisplayHint.DIALOG}, otherwise it will be false.
   *
   * What parts of the desktop will be blocked depends on the used {@link displayParent}.
   *
   * Default is null (= form is modal if it is a dialog).
   */
  modal?: boolean;
  /**
   * Defines when the form should be accessible (visible) and which part of the desktop is blocked for interaction if {@link modal} is set to true.
   *
   * Possible parents are {@link Desktop}, {@link Outline} or {@link Form}:
   *
   * - Desktop: The form is always accessible; blocks the entire desktop if modal.
   * - Outline: The form is only accessible when the given outline is active; blocks only the active outline if modal, so changing the outline or using the desktop header in general is still possible.
   * - Form: The form is only accessible when the given form is active; blocks only the form used as display parent if modal.
   *
   * By default, the {@link Desktop} is used as display parent.
   */
  displayParent?: DisplayParent;
  /**
   * Defines whether the form should display a close button [X] in the form header resp. view tab.
   *
   * Default is true.
   */
  closable?: boolean;
  /**
   * If set to true, the bounds (position and size) of the form will be stored in the local storage using {@link cacheBoundsKey}.
   * When a form with the same {@link cacheBoundsKey} is opened again, the bounds will be restored.
   *
   * It only has an effect for forms with {@link displayHint} set to {@link Form.DisplayHint.DIALOG} or {@link Form.DisplayHint.POPUP_WINDOW}.
   *
   * Default is false.
   */
  cacheBounds?: boolean;
  /**
   * The key that is used to store the bounds of a form in the local storage, if {@link cacheBounds} is enabled.
   *
   * By default, the {@link objectType} is used as key.
   */
  cacheBoundsKey?: string;
  /**
   * Configures whether the form can be resized by the user.
   *
   * It only has an effect for forms with {@link displayHint} set to {@link Form.DisplayHint.DIALOG}.
   *
   * Default is true.
   */
  resizable?: boolean;
  /**
   * Configures whether the form can be moved by the user.
   *
   * It only has an effect for forms with {@link displayHint} set to {@link Form.DisplayHint.DIALOG}.
   *
   * Default is true.
   */
  movable?: boolean;
  /**
   * The root group box is the main container inside the form.
   * Every form needs exactly one root group box that can contain one or more {@link FormField}s.
   */
  rootGroupBox?: ObjectOrChildModel<GroupBox>;
  /**
   * Whether a changed form should display the save {@link saveNeeded} state in the form header or tab.
   *
   * Default is true.
   */
  saveNeededVisible?: boolean;
  /**
   * Configure whether to show this {@link Form} once started.
   *
   * If set to true and the {@link Form} is started, it is added to the {@link Desktop} in order to be displayed.
   *
   * Default is true.
   */
  showOnOpen?: boolean;
  /**
   * The widget to be focused initially when the form renders. If not set, the first focusable element will be focused.
   * If an id is provided, the widget will be resolved automatically in the context of the form.
   *
   * Default is null.
   */
  initialFocus?: Widget | string;
  /**
   * Whether this form should render its {@link initialFocus}.
   *
   * Default is true.
   */
  renderInitialFocusEnabled?: boolean;
  /**
   * If set, the title will be displayed in the form header or {@link DesktopTab} tab.
   */
  title?: string;
  /**
   * If set, the subtitle will be displayed in the form header or {@link DesktopTab} tab.
   */
  subTitle?: string;
  /**
   * If set, the icon will be displayed in the form header or {@link DesktopTab} tab.
   *
   * It can either be a font icon identifier or an url pointing to an image.
   */
  iconId?: string;
  /**
   * If set, the status will be displayed in the form header or {@link DesktopTab} tab.
   */
  status?: StatusOrModel;
  /**
   * Configures the validators to be used when the form needs to be validated (e.g. when the form is saved).
   *
   * When the form is validated, every validator is called and has to agree.
   * If one validation fails, the form is considered invalid.
   *
   * By default, the list is empty.
   */
  validators?: FormValidator[];
}
