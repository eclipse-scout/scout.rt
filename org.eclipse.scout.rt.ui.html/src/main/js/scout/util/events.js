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
scout.events = {

  /**
   * @returns the x coordinate where the event happened, works for touch events as well.
   */
  pageX: function(event) {
    if (!scout.objects.isNullOrUndefined(event.pageX)) {
      return event.pageX;
    }
    return event.originalEvent.touches[0].pageX;
  },

  /**
   * @returns the y coordinate where the event happened, works for touch events as well.
   */
  pageY: function(event) {
    if (!scout.objects.isNullOrUndefined(event.pageY)) {
      return event.pageY;
    }
    return event.originalEvent.touches[0].pageY;
  },

  touchdown: function(touch, suffix) {
    return this.touchOrMouse(touch, 'touchstart', 'mousedown', suffix);
  },

  touchmove: function(touch, suffix) {
    return this.touchOrMouse(touch, 'touchmove', 'mousemove', suffix);
  },

  touchendcancel: function(touch, suffix) {
    return this.touchOrMouse(touch, 'touchend touchcancel', 'mouseup', suffix);
  },

  touchOrMouse: function(touch, touchevent, mouseevent, suffix) {
    suffix = suffix || '';
    if (suffix) {
      suffix = '.' + suffix;
    }
    if (touch) {
      return touchevent + suffix;
    }
    return mouseevent + suffix;
  }
};
