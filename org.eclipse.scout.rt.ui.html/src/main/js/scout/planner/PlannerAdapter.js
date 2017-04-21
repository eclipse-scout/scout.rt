/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.PlannerAdapter = function() {
  scout.PlannerAdapter.parent.call(this);
  this._addRemoteProperties(['displayMode', 'viewRange', 'selectionRange', 'selectedActivity']);
};
scout.inherits(scout.PlannerAdapter, scout.ModelAdapter);

scout.PlannerAdapter.prototype._sendViewRange = function(viewRange) {
  this._send('property', {
    viewRange: scout.dates.toJsonDateRange(viewRange)
  });
};

scout.PlannerAdapter.prototype._sendSelectedActivity = function() {
  var activityId = null;
  if (this.widget.selectedActivity) {
    activityId = this.widget.selectedActivity.id;
  }
  this._send('property', {
    selectedActivity: activityId
  });
};

scout.PlannerAdapter.prototype._sendSelectionRange = function() {
  var selectionRange = scout.dates.toJsonDateRange(this.widget.selectionRange);
  this._send('property', {
    selectionRange: selectionRange
  });
};

scout.PlannerAdapter.prototype._onWidgetResourcesSelected = function(event) {
  this._sendResourcesSelected();
};

scout.PlannerAdapter.prototype._sendResourcesSelected = function() {
  var resourceIds = this.widget.selectedResources.map(function(r) {
    return r.id;
  });
  this._send('resourcesSelected', {
    resourceIds: resourceIds
  });
};

scout.PlannerAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'resourcesSelected') {
    this._onWidgetResourcesSelected(event);
  } else {
    scout.PlannerAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.PlannerAdapter.prototype._onResourcesInserted = function(resources) {
  this.widget.insertResources(resources);
};

scout.PlannerAdapter.prototype._onResourcesDeleted = function(resourceIds) {
  var resources = this.widget._resourcesByIds(resourceIds);
  this.addFilterForWidgetEventType('resourcesSelected');
  this.addFilterForProperties({
    selectionRange: new scout.DateRange()
  });
  this.widget.deleteResources(resources);
};

scout.PlannerAdapter.prototype._onAllResourcesDeleted = function() {
  this.addFilterForWidgetEventType('resourcesSelected');
  this.addFilterForProperties({
    selectionRange: new scout.DateRange()
  });
  this.widget.deleteAllResources();
};

scout.PlannerAdapter.prototype._onResourcesSelected = function(resourceIds) {
  var resources = this.widget._resourcesByIds(resourceIds);
  this.addFilterForWidgetEventType('resourcesSelected');
  this.widget.selectResources(resources, false);
};

scout.PlannerAdapter.prototype._onResourcesUpdated = function(resources) {
  this.widget.updateResources(resources);
};

scout.PlannerAdapter.prototype.onModelAction = function(event) {
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
    scout.PlannerAdapter.parent.prototype.onModelAction.call(this, event);
  }
};
