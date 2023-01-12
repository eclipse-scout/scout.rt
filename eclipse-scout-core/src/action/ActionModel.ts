/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ActionStyle, ActionTextPosition, Alignment, KeyStrokeFirePolicy, WidgetModel} from '../index';

export interface ActionModel extends WidgetModel {
  /**
   * Configures how the action looks like.
   *
   * Default is {@link Action.ActionStyle.DEFAULT}.
   */
  actionStyle?: ActionStyle;
  /**
   * Controls whether the action should be displayed more compact.
   * It only has an effect if the action is used in the desktop tool box.
   * The desktop tool box sets it dynamically based on the available size.
   *
   * Default is false
   */
  compact?: boolean;
  /**
   * Configures the icon of the action.
   *
   * It can either be a font icon identifier or an url pointing to an image.
   *
   * @see icons.parseIconId
   * @see Icon
   */
  iconId?: string;
  /**
   * Configures where the action should be positioned inside a menu bar.
   * It has no effect if the action is used outside a menu bar.
   *
   * Default is -1 (left)
   */
  horizontalAlignment?: Alignment;
  /**
   * Defines the keystroke that executes the action if pressed.
   *
   * A keystroke is built from optional modifiers (alt, control, shift) and a key (p, f11, delete).
   * The keystroke has to follow a certain pattern: The modifiers (alt, shift, control) are separated from the key by a '-'.
   *
   * *Examples*
   * - control-alt-1
   * - control-shift-alt-1
   * - f11
   * - alt-f11
   */
  keyStroke?: string;
  /**
   * Determines if the keystroke should be fired when the action itself is not accessible (e.g. covered by a modal dialog).
   *
   * Default is {@link Action.KeyStrokeFirePolicy.ACCESSIBLE_ONLY}.
   */
  keyStrokeFirePolicy?: KeyStrokeFirePolicy;
  /**
   * Specifies whether the action should look selected or not.
   *
   * Default is false.
   *
   * @see {@link toggleAction}.
   */
  selected?: boolean;
  /**
   * Configures whether two or more consecutive clicks on the action within a short period of time (e.g. double click) should be prevented by the UI.
   *
   * Default is false.
   */
  preventDoubleClick?: boolean;
  /**
   * Defines whether the action can be focused using keyboard.
   *
   * Default is false.
   */
  tabbable?: boolean;
  /**
   * Configures the text of the action.
   */
  text?: string;
  /**
   * Configures where the text should be positioned.
   *
   * Default is {@link Action.TextPosition.DEFAULT}, which is on the right of the icon.
   */
  textPosition?: ActionTextPosition;
  /**
   * Configures whether HTML code in the {@link text} property should be interpreted. If set to false, the HTML will be encoded.
   *
   * Default is false.
   */
  htmlEnabled?: boolean;
  /**
   * If set to false, only the icon is visible.
   *
   * The menu bar sets it dynamically based on the available size.
   *
   * Default is true.
   */
  textVisible?: boolean;
  /**
   * Configures whether the action should be toggleable.
   *
   * If set to true, executing the action (e.g. by click or key stroke) will toggle the {@link selected} property.
   *
   * Default is false.
   */
  toggleAction?: boolean;
  /**
   * Configures the text to be displayed when the action is hovered.
   */
  tooltipText?: string;
  /**
   * Configures whether the tooltip should be displayed when hovered even if the action is selected.
   *
   * Default is true.
   */
  showTooltipWhenSelected?: boolean;
}
