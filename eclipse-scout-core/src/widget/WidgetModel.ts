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
import {DisabledStyle, DisplayParent, LogicalGrid, ObjectModel, ObjectType, Session, Widget} from '../index';

export interface WidgetModel extends ObjectModel<Widget> {
  /**
   * The parent widget.
   */
  parent?: Widget;
  owner?: Widget;
  /**
   * If not specified, the session of the parent widget is used
   */
  session?: Session;
  enabled?: boolean;
  trackFocus?: boolean;
  scrollTop?: number;
  scrollLeft?: number;
  inheritAccessibility?: boolean;
  disabledStyle?: DisabledStyle;
  visible?: boolean;
  cssClass?: string;
  loading?: boolean;
  logicalGrid?: LogicalGrid | ObjectType<LogicalGrid>;
  displayParent?: DisplayParent;
  animateRemoval?: boolean;

  [property: string]: any; // allow custom properties
}
