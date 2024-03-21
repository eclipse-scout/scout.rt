/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventHandler, EventSupport, Route, strings} from '../index';
import $ from 'jquery';

export const router = {
  routes: [] as Route[],
  events: new EventSupport(),
  currentRoute: null as Route,
  defaultLocation: null as string,

  /**
   * Default location is used, when no route is set in the URL when routes are activated initially.
   * Typically, this points to your 'home' page.
   */
  setDefaultLocation(location: string) {
    router.defaultLocation = '#' + location;
  },

  prepare($a: JQuery, location: string) {
    if (!strings.startsWith(location, '#')) {
      location = '#' + location;
    }
    $a
      .attr('href', location)
      .on('mousedown', event => {
        router.activate(location);
        return false; // prevent default
      });
  },

  activate(location?: string) {
    if (!location) {
      let regexp = new RegExp('[^/]*$'); // match everything after last slash
      let matches = regexp.exec(document.location.href);
      location = matches[0];
    }

    // no route is set in the URL
    if (strings.empty(location) || '/' === location) {
      location = router.defaultLocation;
    }

    for (let i = 0; i < router.routes.length; i++) {
      let route = router.routes[i];
      if (route.matches(location)) {
        if (route === router.currentRoute && route.location === location) {
          $.log.isDebugEnabled() && $.log.debug('Route has not changed - do not activate route');
          return;
        }

        // deactivate old route
        if (router.currentRoute) {
          router.currentRoute.deactivate();
        }

        // activate new route
        router.currentRoute = route;
        router.currentRoute.activate(location);

        window.location.replace(location);
        $.log.isInfoEnabled() && $.log.info('router: activated route for location ', location);

        router.events.trigger('routeChange', new RouteChangeEvent(route));
        return;
      }
    }

    $.log.isInfoEnabled() && $.log.info('router: no route registered for location ' + location);
  },

  register(route: Route) {
    router.routes.push(route);
  },

  on(event: 'routeChange', handler: EventHandler<RouteChangeEvent>) {
    router.events.on(event, handler);
  },

  /**
   * Updates the location (URL) field in the browser.
   *
   * @param routeRef a string which identifies a route.
   */
  updateLocation(routeRef: string) {
    let location = '#' + routeRef;
    window.location.replace(location);
  }
};

export class RouteChangeEvent extends Event {
  route: Route;

  constructor(route: Route) {
    super({route: route});
  }
}

window.addEventListener('popstate', event => {
  router.activate(null);
  return false;
});
