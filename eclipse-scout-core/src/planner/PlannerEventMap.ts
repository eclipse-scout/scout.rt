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
import {DateRange, Event, Menu, Planner, PropertyChangeEvent, WidgetEventMap} from '../index';
import {PlannerActivity, PlannerDisplayMode, PlannerDisplayModeOptions, PlannerResource} from './Planner';

export interface PlannerResourcesSelectedEvent<P extends Planner = Planner> extends Event<P> {
  resources: PlannerResource[];
}

export default interface PlannerEventMap extends WidgetEventMap {
  'resourcesSelected': PlannerResourcesSelectedEvent;
  'propertyChange:availableDisplayModes': PropertyChangeEvent<PlannerDisplayMode[], Planner>;
  'propertyChange:displayMode': PropertyChangeEvent<PlannerDisplayMode, Planner>;
  'propertyChange:displayModeOptions': PropertyChangeEvent<Partial<Record<PlannerDisplayMode, PlannerDisplayModeOptions>>, Planner>;
  'propertyChange:menus': PropertyChangeEvent<Menu[], Planner>;
  'propertyChange:selectedActivity': PropertyChangeEvent<PlannerActivity, Planner>;
  'propertyChange:selectedResources': PropertyChangeEvent<PlannerResource[], Planner>;
  'propertyChange:selectionRange': PropertyChangeEvent<DateRange, Planner>;
  'propertyChange:viewRange': PropertyChangeEvent<DateRange, Planner>;
  'propertyChange:yearPanelVisible': PropertyChangeEvent<boolean, Planner>;
}
