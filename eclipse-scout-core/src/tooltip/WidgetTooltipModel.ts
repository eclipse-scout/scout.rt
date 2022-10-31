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
import {FocusRule, RefModel, ScoutKeyboardEvent, TooltipModel, Widget, WidgetModel} from '../index';

export default interface WidgetTooltipModel extends TooltipModel {
  /**
   * The default interceptor stops the propagation for all key strokes except ESCAPE and ENTER.
   * Otherwise, the tooltip would be destroyed for all key strokes that bubble up to the
   * root (see global document listener in {@link Tooltip}).
   */
  keyStrokeStopPropagationInterceptor: (event: ScoutKeyboardEvent) => void;
  /**
   * Default is true.
   */
  withFocusContext: boolean;
  /**
   * Default returns {@link FocusRule.AUTO}.
   */
  initialFocus: () => FocusRule;
  /**
   * Default is false.
   */
  focusableContainer: boolean;
  /**
   * The {@link Widget} rendered inside the tooltip.
   */
  content: Widget | RefModel<WidgetModel>;
}
