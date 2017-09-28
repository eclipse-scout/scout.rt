/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.WidgetEventTypeFilter = function() {
  this.filters = [];
};

scout.WidgetEventTypeFilter.prototype.addFilter = function(filterFunc) {
  this.filters.push(filterFunc);
};

scout.WidgetEventTypeFilter.prototype.addFilterForEventType = function(eventType) {
  this.filters.push(function(event) {
    return event.type === eventType;
  });
};

scout.WidgetEventTypeFilter.prototype.filter = function(event) {
  return this.filters.some(function(filterFunc) {
    return filterFunc(event);
  });
};

scout.WidgetEventTypeFilter.prototype.reset = function() {
  this.filters = [];
};
