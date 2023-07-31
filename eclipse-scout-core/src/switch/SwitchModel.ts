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
   * Default is false
   */
  activated?: boolean;
  label?: string;
  /**
   * Default is false
   */
  labelHtmlEnabled?: boolean;
  /**
   * Specifies if the label is visible. A value of `null` means "automatic", i.e. the label is
   * automatically shown when the 'label' property contains text.
   */
  labelVisible?: boolean;
  tooltipText?: string;
  /**
   * Default is false
   */
  iconVisible?: boolean;
  /**
   * Default is {@link Switch.DisplayStyle.DEFAULT}
   */
  displayStyle?: SwitchDisplayStyle;
  /**
   * Default is false
   */
  tabbable?: boolean;
}
