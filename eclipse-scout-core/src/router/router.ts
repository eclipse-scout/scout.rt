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
import {Event, EventHandler, EventSupport, Route, router, strings} from '../index';
import $ from 'jquery';

let routes: Route[] = [];
let events = new EventSupport();
let currentRoute: Route = null;
let defaultLocation: string = null;

/**
 * Default location is used, when no route is set in the URL when routes are activated initially.
 * Typically this points to your 'home' page.
 */
export function setDefaultLocation(location: string) {
  defaultLocation = '#' + location;
}

export function prepare($a: JQuery, location: string) {
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

export function activate(location?: string) {
  if (!location) {
    let regexp = new RegExp('[^/]*$'); // match everything after last slash
    let matches = regexp.exec(document.location.href);
    location = matches[0];
  }

  // no route is set in the URL
  if (strings.empty(location) || '/' === location) {
    location = defaultLocation;
  }

  let i, route: Route = null;
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

      events.trigger('routeChange', new RouteChangeEvent(route));
      return;
    }
  }
  $.log.warn('router: no route registered for location=', location);
}

export function register(route: Route) {
  routes.push(route);
}

export function on(event: 'routeChange', handler: EventHandler<RouteChangeEvent>) {
  events.on(event, handler);
}

export class RouteChangeEvent extends Event {
  route: Route;

  constructor(route: Route) {
    super({route: route});
  }
}

/**
 * Updates the location (URL) field in the browser.
 *
 * @param routeRef a string which identifies a route.
 */
export function updateLocation(routeRef: string) {
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
