/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateRange, Event, Menu, Planner, PlannerActivity, PlannerDisplayMode, PlannerDisplayModeOptions, PlannerResource, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface PlannerResourcesSelectedEvent<P extends Planner = Planner> extends Event<P> {
  resources: PlannerResource[];
}

export interface PlannerEventMap extends WidgetEventMap {
  'resourcesSelected': PlannerResourcesSelectedEvent;
  'propertyChange:availableDisplayModes': PropertyChangeEvent<PlannerDisplayMode[]>;
  'propertyChange:displayMode': PropertyChangeEvent<PlannerDisplayMode>;
  'propertyChange:displayModeOptions': PropertyChangeEvent<Partial<Record<PlannerDisplayMode, PlannerDisplayModeOptions>>>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:selectedActivity': PropertyChangeEvent<PlannerActivity>;
  'propertyChange:selectedResources': PropertyChangeEvent<PlannerResource[]>;
  'propertyChange:selectionRange': PropertyChangeEvent<DateRange>;
  'propertyChange:viewRange': PropertyChangeEvent<DateRange>;
  'propertyChange:yearPanelVisible': PropertyChangeEvent<boolean>;
}
