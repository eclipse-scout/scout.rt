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
import {Event, PlannerHeader, PropertyChangeEvent, WidgetEventMap} from '../index';
import {PlannerDisplayMode} from './Planner';

export interface PlannerHeaderDisplayModeClickEvent<P extends PlannerHeader = PlannerHeader> extends Event<P> {
  displayMode: PlannerDisplayMode;
}

export default interface PlannerHeaderEventMap extends WidgetEventMap {
  'displayModeClick': PlannerHeaderDisplayModeClickEvent;
  'nextClick': Event<PlannerHeader>;
  'previousClick': Event<PlannerHeader>;
  'todayClick': Event<PlannerHeader>;
  'yearClick': Event<PlannerHeader>;
  'propertyChange:availableDisplayModes': PropertyChangeEvent<PlannerDisplayMode[], PlannerHeader>;
  'propertyChange:displayMode': PropertyChangeEvent<PlannerDisplayMode, PlannerHeader>;

}
