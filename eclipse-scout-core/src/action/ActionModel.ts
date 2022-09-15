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
  actionStyle: ActionStyle;
  compact: boolean;
  iconId: string;
  horizontalAlignment: -1 | 0 | 1;
  keyStroke: string;
  keyStrokeFirePolicy: KeyStrokeFirePolicy;
  selected: boolean;
  preventDoubleClick: boolean;
  /**
   * This property decides whether or not the tabindex attribute is set in the DOM.
   */
  tabbable: boolean;
  text: string;
  textPosition: ActionTextPosition;
  htmlEnabled: boolean;
  textVisible: boolean;
  toggleAction: boolean;
  tooltipText: string;
  showTooltipWhenSelected: boolean;
}
