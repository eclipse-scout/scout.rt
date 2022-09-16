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
import {WidgetModel} from '../index';
import {ActionStyle, ActionTextPosition, KeyStrokeFirePolicy} from './Action';

export default interface ActionModel extends WidgetModel {
  /**
   * Default is {@link Action.ActionStyle.DEFAULT}.
   */
  actionStyle?: ActionStyle;
  /**
   * Default is false
   */
  compact?: boolean;
  iconId?: string;
  /**
   * Default is -1 (left)
   */
  horizontalAlignment?: -1 | 0 | 1;
  keyStroke?: string;
  /**
   * Default is {@link Action.KeyStrokeFirePolicy.ACCESSIBLE_ONLY}.
   */
  keyStrokeFirePolicy?: KeyStrokeFirePolicy;
  /**
   * Default is false.
   */
  selected?: boolean;
  /**
   * Default is false.
   */
  preventDoubleClick?: boolean;
  /**
   * This property decides whether or not the tabindex attribute is set in the DOM. Default is false.
   */
  tabbable?: boolean;
  text?: string;
  /**
   * Default is {@link Action.TextPosition.DEFAULT}.
   */
  textPosition?: ActionTextPosition;
  /**
   * Default is false.
   */
  htmlEnabled?: boolean;
  /**
   * Default is true.
   */
  textVisible?: boolean;
  /**
   * Default is false.
   */
  toggleAction?: boolean;
  tooltipText?: string;
  /**
   * Default is true.
   */
  showTooltipWhenSelected?: boolean;
}
