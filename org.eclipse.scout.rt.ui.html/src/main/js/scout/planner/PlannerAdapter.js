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
  this._addRemoteProperties(['displayMode', 'viewRange']);
};
scout.inherits(scout.PlannerAdapter, scout.ModelAdapter);

scout.PlannerAdapter.prototype._sendViewRange = function(viewRange) {
  this._send('property', {
    viewRange: scout.dates.toJsonDateRange(viewRange)
  });
};

scout.PlannerAdapter.prototype._onResourcesInserted = function(resources) {
  this.widget.insertResources(resources);
};

scout.PlannerAdapter.prototype._onResourcesDeleted = function(resourceIds) {
  var resources = this.widget._resourcesByIds(resourceIds);
  this.widget.deleteResources(resources);
};

scout.PlannerAdapter.prototype._onResourcesSelected = function(resourceIds) {
  var resources = this.widget._resourcesByIds(resourceIds);
  this.widget.selectResources(resources, false);
};

scout.PlannerAdapter.prototype._onAllResourcesDeleted = function() {
  this.widget.deleteAllResources();
};

scout.PlannerAdapter.prototype._onResourcesUpdated = function(resources) {
  this.widget._updateResources(resources);
};

scout.PlannerAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'resourcesInserted') {
    this._onResourcesInserted(event.resources);
  } else if (event.type === 'resourcesDeleted') {
    this._onResourcesDeleted(event.resourceIds);
  } else if (event.type === 'resourcesSelected') {
    this._onResourcesSelected(event.resourceIds);
  } else if (event.type === 'allResourcesDeleted') {
    this._onAllResourcesDeleted();
  } else if (event.type === 'resourcesUpdated') {
    this._onResourcesUpdated(event.resources);
  } else {
    scout.PlannerAdapter.parent.prototype.onModelAction.call(this, event);
  }
};
