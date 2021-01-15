/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DateRange, dates, ModelAdapter} from '../index';

export default class PlannerAdapter extends ModelAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['displayMode', 'viewRange', 'selectionRange', 'selectedActivity']);
  }

  static PROPERTIES_ORDER = ['displayMode', 'viewRange'];

  _orderPropertyNamesOnSync(newProperties) {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(PlannerAdapter.PROPERTIES_ORDER));
  }

  _sendViewRange(viewRange) {
    this._send('property', {
      viewRange: dates.toJsonDateRange(viewRange)
    });
  }

  _sendSelectedActivity() {
    let activityId = null;
    if (this.widget.selectedActivity) {
      activityId = this.widget.selectedActivity.id;
    }
    this._send('property', {
      selectedActivity: activityId
    });
  }

  _sendSelectionRange() {
    let selectionRange = dates.toJsonDateRange(this.widget.selectionRange);
    this._send('property', {
      selectionRange: selectionRange
    });
  }

  _onWidgetResourcesSelected(event) {
    this._sendResourcesSelected();
  }

  _sendResourcesSelected() {
    let resourceIds = this.widget.selectedResources.map(r => {
      return r.id;
    });
    this._send('resourcesSelected', {
      resourceIds: resourceIds
    });
  }

  _onWidgetEvent(event) {
    if (event.type === 'resourcesSelected') {
      this._onWidgetResourcesSelected(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onResourcesInserted(resources) {
    this.widget.insertResources(resources);
  }

  _onResourcesDeleted(resourceIds) {
    let resources = this.widget._resourcesByIds(resourceIds);
    this.addFilterForWidgetEventType('resourcesSelected');
    this.addFilterForProperties({
      selectionRange: new DateRange()
    });
    this.widget.deleteResources(resources);
  }

  _onAllResourcesDeleted() {
    this.addFilterForWidgetEventType('resourcesSelected');
    this.addFilterForProperties({
      selectionRange: new DateRange()
    });
    this.widget.deleteAllResources();
  }

  _onResourcesSelected(resourceIds) {
    let resources = this.widget._resourcesByIds(resourceIds);
    this.addFilterForWidgetEventType('resourcesSelected');
    this.widget.selectResources(resources, false);
  }

  _onResourcesUpdated(resources) {
    this.widget.updateResources(resources);
  }

  onModelAction(event) {
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
}
