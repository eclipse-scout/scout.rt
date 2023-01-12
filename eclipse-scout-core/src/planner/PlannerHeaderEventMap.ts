/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PlannerDisplayMode, PlannerHeader, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface PlannerHeaderDisplayModeClickEvent<P extends PlannerHeader = PlannerHeader> extends Event<P> {
  displayMode: PlannerDisplayMode;
}

export interface PlannerHeaderEventMap extends WidgetEventMap {
  'displayModeClick': PlannerHeaderDisplayModeClickEvent;
  'nextClick': Event<PlannerHeader>;
  'previousClick': Event<PlannerHeader>;
  'todayClick': Event<PlannerHeader>;
  'yearClick': Event<PlannerHeader>;
  'propertyChange:availableDisplayModes': PropertyChangeEvent<PlannerDisplayMode[]>;
  'propertyChange:displayMode': PropertyChangeEvent<PlannerDisplayMode>;

}
