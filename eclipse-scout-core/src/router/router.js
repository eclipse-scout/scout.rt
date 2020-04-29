/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {EventSupport, router, strings} from '../index';
import $ from 'jquery';

let routes = []; // array with Routes
let events = new EventSupport();
let currentRoute = null;
let defaultLocation = null;

/**
 * Default location is used, when no route is set in the URL when routes are activated initially.
 * Typically this points to your 'home' page.
 */
export function setDefaultLocation(location) {
  defaultLocation = '#' + location;
}

export function prepare($a, location) {
  if (!strings.startsWith(location, '#')) {
    location = '#' + location;
  }
  $a
    .attr('href', location)
    .on('mousedown', event => {
      activate(location);
      return false; // prevent default
    });

}

export function activate(location) {
  if (!location) {
    let regexp = new RegExp('[^/]*$'); // match everything after last slash
    let matches = regexp.exec(document.location.href);
    location = matches[0];
  }

  // no route is set in the URL
  if (strings.empty(location) || '/' === location) {
    location = defaultLocation;
  }

  let i, route = null;
  for (i = 0; i < routes.length; i++) {
    route = routes[i];
    if (route.matches(location)) {

      if (route === currentRoute && route.location === location) {
        $.log.isDebugEnabled() && $.log.debug('Route has not changed - do not activate route');
        return;
      }

      // deactivate old route
      if (currentRoute) {
        currentRoute.deactivate();
      }

      // activate new route
      currentRoute = route;
      currentRoute.activate(location);

      window.location.replace(location);
      $.log.isInfoEnabled() && $.log.info('router: activated route for location=', location);

      events.trigger('routeChange', {
        route: route
      });
      return;
    }
  }
  $.log.warn('router: no route registered for location=', location);
}

export function register(route) {
  routes.push(route);
}

export function on(event, handler) {
  events.on(event, handler);
}

/**
 * Updates the location (URL) field in the browser.
 *
 * @param {string} routeRef a string which identifies a route.
 */
export function updateLocation(routeRef) {
  let location = '#' + routeRef;
  window.location.replace(location);
}

export default {
  activate,
  currentRoute,
  defaultLocation,
  events,
  on,
  prepare,
  register,
  routes,
  setDefaultLocation,
  updateLocation
};

window.addEventListener('popstate', event => {
  router.activate(null);
  return false;
});
