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
import {FormFieldModel, Widget} from '../../../index';
import {ButtonDisplayStyle, ButtonSystemType} from './Button';

export default interface ButtonModel extends FormFieldModel {
  /**
   * Configures whether this button is a default button.
   * Default buttons typically have a dedicated look.
   * Default is false.
   */
  defaultButton?: boolean;
  /**
   * Configures the display style of this button.
   * See {@link Button.DisplayStyle} constants for valid values.
   * Default is {@link Button.DisplayStyle.DEFAULT}.
   */
  displayStyle?: ButtonDisplayStyle;
  iconId?: string;
  keyStroke?: string;
  /**
   * Configures the scope where the keystroke of this button is registered. If nothing is configured the Keystroke is set on the form.
   */
  keyStrokeScope?: Widget | string;
  /**
   * Configures whether this button is a process button.
   * Process buttons are typically displayed on a dedicated button bar at the bottom of a form.
   * Non-process buttons can be placed anywhere on a form.
   * Default is {@code true}.
   */
  processButton?: boolean;
  selected?: boolean;
  /**
   * Configures the system type of this button.
   * See {@link Button.SystemType} constants for valid values.
   * System buttons are buttons with pre-defined behavior (such as an 'Ok' button or a 'Cancel' button).
   * Default is {@link Button.SystemType.NONE}.
   */
  systemType?: ButtonSystemType;
  /**
   * Configures whether two or more consecutive clicks on the button within a short period of time (e.g. double click) should be prevented.
   * The default is false.
   */
  preventDoubleClick?: boolean;
  /**
   * A stackable button will be stacked in a dropdown menu if there is not enough space in the menubar.
   * Default is true.
   */
  stackable?: boolean;
  /**
   * A shrinkable button will be displayed without label but only with its configured icon if there is not enough space in the menubar.
   * Default is false.
   */
  shrinkable?: boolean;
}
