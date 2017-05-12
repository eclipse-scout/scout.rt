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
scout.Route = function() {
  this.location = null;
};

/**
 * @returns {boolean} Whether or not this instance can handle the given location.
 *
 * @param {string} location
 */
scout.Route.prototype.matches = function(location) {
  return false;
};

/**
 * Called when the route is activated, stores the given location as instance variable.
 * This is useful for the case where a single instance of scout.Route handles multiple locations.
 *
 * @param {string} location
 */
scout.Route.prototype.activate = function(location) {
  this.location = location;
};

/**
 * Called when route is deactivated because another route is activated. This is the place
 * to perform clean-up operations.
 */
scout.Route.prototype.deactivate = function() {

};
