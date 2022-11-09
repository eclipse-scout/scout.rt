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
