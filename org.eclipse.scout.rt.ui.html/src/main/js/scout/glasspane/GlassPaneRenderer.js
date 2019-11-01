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
import {Device} from '../index';
import {Form} from '../index';
import {DeferredGlassPaneTarget} from '../index';
import * as $ from 'jquery';
import {arrays} from '../index';
import {scout} from '../index';

/**
 * Renders glassPanes over the 'glassPaneTargets' of a widget.
 */
export default class GlassPaneRenderer {

constructor(widget, enabled) {
  this._widget = widget;
  this.session = widget.session;
  this._enabled = scout.nvl(enabled, true);
  this._$glassPanes = [];
  this._deferredGlassPanes = [];
  this._resolvedDisplayParent = null;
  this._registeredDisplayParent = null;
  this._displayParentRenderHandler = this._onDisplayParentRender.bind(this);
  this._glassPaneRemoveHandler = this._onGlassPaneRemove.bind(this);
}

renderGlassPanes() {
  this.findGlassPaneTargets().forEach(function(glassPaneTarget) {
    if (glassPaneTarget instanceof DeferredGlassPaneTarget) {
      glassPaneTarget.rendererReady(this);
      this._deferredGlassPanes.push(glassPaneTarget);
    } else {
      this.renderGlassPane(glassPaneTarget);
    }
  }, this);
}

/**
 * @param {($|HTMLElement)} $glassPaneTarget
 */
renderGlassPane($glassPaneTarget) {
  $glassPaneTarget = $.ensure($glassPaneTarget);

  if (this._widget.$container && this._widget.$container[0] === $glassPaneTarget[0]) {
    // Don't render a glass pane on the widget itself (necessary if glass pane is added after the widget is rendered)
    return;
  }

  // If glassPaneTarget already has a glasspane added by this renderer, don't add another one
  // May happen if a part of the display parent is removed and rendered again while covered by a glass pane
  // E.g. display parent is set to outline and navigation is made invisible but bench is still there.
  // When navigation is made visible again, renderGlassPanes is called but only the glass panes of the navigation need to be added and not the ones of the bench (because they are already there)
  var alreadyRendered = this._$glassPanes.some(function($pane) {
    return $pane.parent()[0] === $glassPaneTarget[0];
  });
  if (alreadyRendered) {
    return;
  }

  // Render glasspanes onto glasspane targets.
  var $glassPane = $glassPaneTarget
    .appendDiv('glasspane')
    .on('mousedown', this._onMouseDown.bind(this));

  // This is required in touch mode, because FastClick messes up the order
  // of mouse/click events which is especially important for TouchPopups.
  if (Device.get().supportsTouch()) {
    $glassPane.addClass('needsclick');
  }

  // Glasspanes in popup-windows must be visible, otherwise the user cannot recognize that the popup
  // is blocked, since the widget that blocks (e.g a message-box) may be opened in the main-window.
  if ($glassPane.window(true).popupWindow) {
    $glassPane.addClass('dark');
  }
  this._$glassPanes.push($glassPane);

  // Register 'glassPaneTarget' in focus manager.
  this.session.focusManager.registerGlassPaneTarget($glassPaneTarget);

  // Ensure glass pane is removed properly on remove, especially necessary when display parent is removed while glass pane renderer is still active (navigation collapse case)
  $glassPane.one('remove', this._glassPaneRemoveHandler);

  this._registerDisplayParent();
}

removeGlassPanes() {
  // Remove glass-panes
  this._$glassPanes.slice().forEach(function($glassPane) {
    this._removeGlassPane($glassPane);
  }, this);

  // Unregister all deferedGlassPaneTargets
  this._deferredGlassPanes.forEach(function(glassPaneTarget) {
    glassPaneTarget.removeGlassPaneRenderer(this);
  }, this);
  this._deferredGlassPanes = [];

  this._unregisterDisplayParent();
}

_removeGlassPane($glassPane) {
  var $glassPaneTarget = $glassPane.parent();
  $glassPane.off('remove', this._glassPaneRemoveHandler);
  $glassPane.remove();
  arrays.$remove(this._$glassPanes, $glassPane);

  $glassPaneTarget.removeClass('no-hover');
  this.session.focusManager.unregisterGlassPaneTarget($glassPaneTarget);
}

eachGlassPane(func) {
  this._$glassPanes.forEach(function($glassPane) {
    func($glassPane);
  });
}

findGlassPaneTargets() {
  if (!this._enabled) {
    return []; // No glasspanes to be rendered, e.g. for none-modal dialogs.
  }

  var displayParent = this._resolveDisplayParent();
  if (!displayParent || !displayParent.glassPaneTargets) {
    return []; // Parent is not a valid display parent.
  }

  return displayParent.glassPaneTargets(this._widget);
}

_resolveDisplayParent() {
  // Note: This has to be done after rendering, because otherwise session.desktop could be undefined!
  if (!this._resolvedDisplayParent) {
    this._resolvedDisplayParent = this._widget.displayParent || this.session.desktop;
  }
  return this._resolvedDisplayParent;
}

_registerDisplayParent() {
  // if this._resolvedDisplayParent is not yet resolved, do it now
  if (!this._resolvedDisplayParent) {
    this._resolveDisplayParent();
  }
  // if this._resolvedDisplayParent is resolved, but not yet registered
  if (this._resolvedDisplayParent) {
    if (!this._registeredDisplayParent) {
      // register this._resolvedDisplayParent and remember it as this._registeredDisplayParent
      this.session.focusManager.registerGlassPaneDisplayParent(this._resolvedDisplayParent);
      this._registeredDisplayParent = this._resolvedDisplayParent;
      this._registeredDisplayParent.on('render', this._displayParentRenderHandler);
    }
  }
}

_unregisterDisplayParent() {
  // if this._registeredDisplayParent is defined, unregister it
  if (this._registeredDisplayParent) {
    this.session.focusManager.unregisterGlassPaneDisplayParent(this._registeredDisplayParent);
    this._registeredDisplayParent.off('render', this._displayParentRenderHandler);
    this._registeredDisplayParent = null;
  }
}

_onMouseDown(event) {
  var $animationTarget = null;

  // notify the display parent to handle the mouse down on the glass pane.
  var displayParent = this._resolveDisplayParent();
  if (displayParent._onGlassPaneMouseDown) {
    displayParent._onGlassPaneMouseDown(this._widget, $(event.target));
  }

  if (this._widget instanceof Form && this._widget.isView()) {
    // If the blocking widget is a view, the $container cannot be animated (this only works for dialogs). Instead,
    // highlight the view tab (or the overflow item, if the view tab is not visible).

    var viewTab = this.session.desktop.bench.getViewTab(this._widget);
    // View tab may not exist if view has neither a title nor a subtitle
    if (viewTab) {
      $animationTarget = viewTab.$container;
      if (!$animationTarget.isVisible()) {
        $animationTarget = $animationTarget.siblings('.overflow-tab-item');
      }
    }
  } else if (this._widget.$container) {
    $animationTarget = this._widget.$container;
  }

  if ($animationTarget) {
    $animationTarget.addClassForAnimation('animate-modality-highlight', {
      // remove animate-open as well, user may click the glasspane before the widget itself was able to remove the animate-open class
      classesToRemove: 'animate-modality-highlight animate-open'
    });
  }

  $.suppressEvent(event);
}

_onDisplayParentRender(event) {
  this.renderGlassPanes();
}

_onGlassPaneRemove(event) {
  var $glassPane = $(event.target);
  this._removeGlassPane($glassPane);
}
}
