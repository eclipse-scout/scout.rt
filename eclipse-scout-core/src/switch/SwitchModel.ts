/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Switch, SwitchDisplayStyle, WidgetModel} from '../index';

export interface SwitchModel extends WidgetModel {
  /**
   * Specifies the state of the switch.
   *
   * Default is false.
   */
  activated?: boolean;
  /**
   * Configures the label of the switch.
   */
  label?: string;
  /**
   * Defines whether HTML code in the {@link label} property should be interpreted. If set to false, the HTML will be encoded.
   *
   * Default is false.
   */
  labelHtmlEnabled?: boolean;
  /**
   * Specifies whether the label should be visible.
   *
   * A value of `null` means "automatic", i.e. the label is automatically shown when the {@link label} property contains text.
   *
   * Default is `null`.
   */
  labelVisible?: boolean;
  /**
   * Configures the text to be displayed when the switch is hovered.
   */
  tooltipText?: string;
  /**
   * Configures whether an icon on the button should be shown indicating the state of the switch.
   *
   * Default is false.
   */
  iconVisible?: boolean;
  /**
   * Configures the display style of the switch.
   *
   * Default is {@link Switch.DisplayStyle.DEFAULT}.
   */
  displayStyle?: SwitchDisplayStyle;
  /**
   * Defines whether the slider can be focused using keyboard.
   *
   * Default is true.
   *
   * @see WidgetModel.tabbable.
   */
  tabbable?: boolean;
}
