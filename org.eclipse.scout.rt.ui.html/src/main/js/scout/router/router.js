/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.router = {

  routes: [], // array with Routes
  events: new scout.EventSupport(),
  currentRoute: null,
  defaultLocation: null,

  /**
   * Default location is used, when no route is set in the URL when routes are activated initially.
   * Typically this points to your 'home' page.
   */
  setDefaultLocation: function(location) {
    this.defaultLocation = '#' + location;
  },

  prepare: function($a, location) {
    if (!location.startsWith('#')) {
      location = '#' + location;
    }
    $a
      .attr('href', location)
      .on('mousedown', function(event) {
        scout.router.activate(location);
        return false; // prevent default
      });

  },

  activate: function(location) {
    if (!location) {
      var regexp = new RegExp('[^/]*$'); // match everything after last slash
      var matches = regexp.exec(document.location.href);
      location = matches[0];
    }

    // no route is set in the URL
    if (scout.strings.empty(location) || '/' === location) {
      location = this.defaultLocation;
    }

    var i, route = null;
    for (i = 0; i < this.routes.length; i++) {
      route = this.routes[i];
      if (route.matches(location)) {

        if (route === this.currentRoute && route.location === location) {
          $.log.debug('Route has not changed - do not activate route');
          return;
        }

        // deactivate old route
        if (this.currentRoute) {
          this.currentRoute.deactivate();
        }

        // activate new route
        this.currentRoute = route;
        this.currentRoute.activate(location);

        window.location.replace(location);
        $.log.info('router: activated route for location=', location);

        this.events.trigger('routeChange', {
          route: route
        });
        return;
      }
    }
    $.log.warn('router: no route registered for location=', location);
  },

  register: function(route) {
    this.routes.push(route);
  },

  on: function(event, handler) {
    this.events.on(event, handler);
  },

  /**
   * Updates the location (URL) field in the browser.
   *
   * @param {string} routeRef a string which identifies a route.
   */
  updateLocation: function(routeRef) {
    var location = '#' + routeRef;
    window.location.replace(location);
  }

};

window.addEventListener('popstate', function(event) {
  scout.router.activate(null);
  return false;
});
