/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FocusRule, ObjectOrChildModel, ScoutKeyboardEvent, TooltipModel, Widget} from '../index';

export interface WidgetTooltipModel extends TooltipModel {
  /**
   * The default interceptor stops the propagation for all keystrokes except ESCAPE and ENTER.
   * Otherwise, the tooltip would be destroyed for all keystrokes that bubble up to the
   * root (see global document listener in {@link Tooltip}).
   */
  keyStrokeStopPropagationInterceptor?: (event: ScoutKeyboardEvent) => void;
  /**
   * Default is true.
   */
  withFocusContext?: boolean;
  /**
   * Default returns {@link FocusRule.AUTO}.
   */
  initialFocus?: () => FocusRule;
  /**
   * Default is false.
   */
  focusableContainer?: boolean;
  /**
   * The {@link Widget} rendered inside the tooltip.
   */
  content?: ObjectOrChildModel<Widget>;
}
