/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, DateRange, dates, defaultValues, Event, ModelAdapter, objects, Planner, PlannerActivity, PlannerResource, PlannerResourcesSelectedEvent} from '../index';

export class PlannerAdapter extends ModelAdapter {
  declare widget: Planner;

  constructor() {
    super();
    this._addRemoteProperties(['displayMode', 'viewRange', 'selectionRange', 'selectedActivity']);
  }

  static PROPERTIES_ORDER = ['displayMode', 'viewRange'];

  protected override _orderPropertyNamesOnSync(newProperties: Record<string, any>): string[] {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(PlannerAdapter.PROPERTIES_ORDER));
  }

  protected _sendViewRange(viewRange: DateRange) {
    this._send('property', {
      viewRange: dates.toJsonDateRange(viewRange)
    });
  }

  protected _sendSelectedActivity() {
    let activityId: string = null;
    if (this.widget.selectedActivity) {
      activityId = this.widget.selectedActivity.id;
    }
    this._send('property', {
      selectedActivity: activityId
    });
  }

  protected _sendSelectionRange() {
    let selectionRange = dates.toJsonDateRange(this.widget.selectionRange);
    this._send('property', {
      selectionRange: selectionRange
    });
  }

  protected _onWidgetResourcesSelected(event: PlannerResourcesSelectedEvent) {
    this._sendResourcesSelected();
  }

  protected _sendResourcesSelected() {
    let resourceIds = this.widget.selectedResources.map(r => r.id);
    this._send('resourcesSelected', {
      resourceIds: resourceIds
    });
  }

  protected override _onWidgetEvent(event: Event<Planner>) {
    if (event.type === 'resourcesSelected') {
      this._onWidgetResourcesSelected(event as PlannerResourcesSelectedEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onResourcesInserted(resources: PlannerResource[]) {
    this.widget.insertResources(resources);
  }

  protected _onResourcesDeleted(resourceIds: string[]) {
    let resources = this.widget.resourcesByIds(resourceIds);
    this.addFilterForWidgetEventType('resourcesSelected');
    this.addFilterForProperties({
      selectionRange: new DateRange()
    });
    this.widget.deleteResources(resources);
  }

  protected _onAllResourcesDeleted() {
    this.addFilterForWidgetEventType('resourcesSelected');
    this.addFilterForProperties({
      selectionRange: new DateRange()
    });
    this.widget.deleteAllResources();
  }

  protected _onResourcesSelected(resourceIds: string[]) {
    let resources = this.widget.resourcesByIds(resourceIds);
    this.addFilterForWidgetEventType('resourcesSelected');
    this.widget.selectResources(resources);
  }

  protected _onResourcesUpdated(resources: PlannerResource[]) {
    this.widget.updateResources(resources);
  }

  override onModelAction(event: any) {
    if (event.type === 'resourcesInserted') {
      this._onResourcesInserted(event.resources);
    } else if (event.type === 'resourcesDeleted') {
      this._onResourcesDeleted(event.resourceIds);
    } else if (event.type === 'allResourcesDeleted') {
      this._onAllResourcesDeleted();
    } else if (event.type === 'resourcesSelected') {
      this._onResourcesSelected(event.resourceIds);
    } else if (event.type === 'resourcesUpdated') {
      this._onResourcesUpdated(event.resources);
    } else {
      super.onModelAction(event);
    }
  }

  protected static _initResourceRemote(resource: PlannerResource) {
    // @ts-expect-error
    if (this.modelAdapter) {
      defaultValues.applyTo(resource, 'Resource');
    }
    // @ts-expect-error
    return this._initResourceOrig(resource);
  }

  protected static _initActivityRemote(activity: PlannerActivity) {
    // @ts-expect-error
    if (this.modelAdapter) {
      defaultValues.applyTo(activity, 'Activity');
    }
    // @ts-expect-error
    return this._initActivityOrig(activity);
  }

  static modifyPlannerPrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(Planner, '_initResource', PlannerAdapter._initResourceRemote, true);
    objects.replacePrototypeFunction(Planner, '_initActivity', PlannerAdapter._initActivityRemote, true);
  }
}

App.addListener('bootstrap', PlannerAdapter.modifyPlannerPrototype);
