/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, DeferredGlassPaneTarget, DisplayParent, Event, EventHandler, Form, GlassPaneTarget, PopupWindow, scout, Session, Widget} from '../index';
import $ from 'jquery';

/**
 * Renders glassPanes over the 'glassPaneTargets' of a widget.
 */
export class GlassPaneRenderer {
  session: Session;
  protected _widget: Widget & { displayParent?: DisplayParent };
  protected _enabled: boolean;
  protected _$glassPanes: JQuery[];
  protected _deferredGlassPanes: DeferredGlassPaneTarget[];
  protected _resolvedDisplayParent: DisplayParent;
  protected _registeredDisplayParent: DisplayParent;
  protected _displayParentRenderHandler: EventHandler;
  protected _glassPaneRemoveHandler: (event: JQuery.TriggeredEvent<HTMLDivElement>) => void;
  protected _glassPaneRendererRegistered: boolean;

  /**
   * @param widget Required
   * @param enabled Optional. Default is true
   */
  constructor(widget: Widget, enabled?: boolean) {
    this._widget = widget;
    this.session = widget.session;
    this._enabled = scout.nvl(enabled, true);
    this._$glassPanes = [];
    this._deferredGlassPanes = [];
    this._resolvedDisplayParent = null;
    this._registeredDisplayParent = null;
    this._displayParentRenderHandler = this._onDisplayParentRender.bind(this);
    this._glassPaneRemoveHandler = this._onGlassPaneRemove.bind(this);
    this._glassPaneRendererRegistered = false;
  }

  renderGlassPanes() {
    this.findGlassPaneTargets().forEach(glassPaneTarget => this.renderGlassPane(glassPaneTarget));
    if (!this._glassPaneRendererRegistered) {
      this.session.focusManager.registerGlassPaneRenderer(this);
      this._glassPaneRendererRegistered = true;
    }
  }

  renderGlassPane(target: GlassPaneTarget) {
    if (target instanceof DeferredGlassPaneTarget) {
      target.rendererReady(this);
      this._deferredGlassPanes.push(target);
      return;
    }

    let $glassPaneTarget = $.ensure(target);
    if (this._widget.$container && this._widget.$container[0] === $glassPaneTarget[0]) {
      // Don't render a glass pane on the widget itself (necessary if glass pane is added after the widget is rendered)
      return;
    }

    // If glassPaneTarget already has a glasspane added by this renderer, don't add another one
    // May happen if a part of the display parent is removed and rendered again while covered by a glass pane
    // E.g. display parent is set to outline and navigation is made invisible but bench is still there.
    // When navigation is made visible again, renderGlassPanes is called but only the glass panes of the navigation need to be added and not the ones of the bench (because they are already there)
    let alreadyRendered = this._$glassPanes.some($pane => $pane.parent()[0] === $glassPaneTarget[0]);
    if (alreadyRendered) {
      return;
    }

    // Render glasspanes onto glasspane targets.
    let $glassPane = $glassPaneTarget
      .appendDiv('glasspane')
      .on('mousedown', this._onMouseDown.bind(this));

    this._adjustGlassPaneSize($glassPane, $glassPaneTarget);

    // Glasspanes in popup-windows must be visible, otherwise the user cannot recognize that the popup
    // is blocked, since the widget that blocks (e.g a message-box) may be opened in the main-window.
    let window = $glassPane.window(true);
    if (window && window[PopupWindow.PROP_POPUP_WINDOW]) {
      $glassPane.addClass('dark');
    }
    this._$glassPanes.push($glassPane);

    // Register 'glassPaneTarget' in focus manager.
    this.session.focusManager.registerGlassPaneTarget($glassPaneTarget);

    // Ensure glass pane is removed properly on remove, especially necessary when display parent is removed while glass pane renderer is still active (navigation collapse case)
    $glassPane.one('remove', this._glassPaneRemoveHandler);

    this._registerDisplayParent();
  }

  protected _adjustGlassPaneSize($glassPane: JQuery, $glassPaneTarget: JQuery) {
    // The glasspane must cover the border and overlapping children
    let top = -$glassPaneTarget.cssBorderTopWidth(),
      bottom = -$glassPaneTarget.cssBorderBottomWidth(),
      left = -$glassPaneTarget.cssBorderLeftWidth(),
      right = -$glassPaneTarget.cssBorderRightWidth();

    $glassPaneTarget.children().each((idx, elem) => {
      let element = $(elem);
      top = Math.min(top, (element.cssTop() || 0) + (element.cssMarginTop() || 0));
      bottom = Math.min(bottom, (element.cssBottom() || 0) + (element.cssMarginBottom() || 0));
      left = Math.min(left, (element.cssLeft() || 0) + (element.cssMarginLeft() || 0));
      right = Math.min(right, (element.cssRight() || 0) + (element.cssMarginRight() || 0));
    });

    $glassPane.cssTop(top)
      .cssBottom(bottom)
      .cssLeft(left)
      .cssRight(right);
  }

  removeGlassPanes() {
    // Remove glass-panes
    this._$glassPanes.slice().forEach($glassPane => this._removeGlassPane($glassPane));

    // Unregister all deferredGlassPaneTargets
    this._deferredGlassPanes.forEach(glassPaneTarget => glassPaneTarget.removeGlassPaneRenderer(this));
    this._deferredGlassPanes = [];

    this._unregisterDisplayParent();
    this.session.focusManager.unregisterGlassPaneRenderer(this);
    this._glassPaneRendererRegistered = false;
  }

  protected _removeGlassPane($glassPane: JQuery) {
    let $glassPaneTarget = $glassPane.parent();
    $glassPane.off('remove', this._glassPaneRemoveHandler);
    $glassPane.remove();
    arrays.$remove(this._$glassPanes, $glassPane);

    $glassPaneTarget.removeClass('glasspane-parent');
    this.session.focusManager.unregisterGlassPaneTarget($glassPaneTarget);
  }

  eachGlassPane(func: ($glassPane: JQuery) => void) {
    this._$glassPanes.forEach($glassPane => func($glassPane));
  }

  findGlassPaneTargets(): GlassPaneTarget[] {
    if (!this._enabled) {
      return []; // No glasspanes to be rendered, e.g. for none-modal dialogs.
    }

    let displayParent = this._resolveDisplayParent();
    if (!displayParent || !displayParent.glassPaneTargets) {
      return []; // Parent is not a valid display parent.
    }

    return displayParent.glassPaneTargets(this._widget);
  }

  protected _resolveDisplayParent(): DisplayParent {
    // Note: This has to be done after rendering, because otherwise session.desktop could be undefined!
    if (!this._resolvedDisplayParent) {
      this._resolvedDisplayParent = this._widget.displayParent || this.session.desktop;
    }
    return this._resolvedDisplayParent;
  }

  protected _registerDisplayParent() {
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

  protected _unregisterDisplayParent() {
    // if this._registeredDisplayParent is defined, unregister it
    if (this._registeredDisplayParent) {
      this.session.focusManager.unregisterGlassPaneDisplayParent(this._registeredDisplayParent);
      this._registeredDisplayParent.off('render', this._displayParentRenderHandler);
      this._registeredDisplayParent = null;
    }
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent<HTMLDivElement>) {
    let $animationTarget = null;

    // notify the display parent to handle the mouse down on the glass pane.
    let displayParent = this._resolveDisplayParent();
    if (displayParent.onGlassPaneMouseDown) {
      displayParent.onGlassPaneMouseDown(this._widget, $(event.target));
    }

    if (this._widget instanceof Form && this._widget.isView()) {
      // If the blocking widget is a view, the $container cannot be animated (this only works for dialogs). Instead,
      // highlight the view tab (or the overflow item, if the view tab is not visible).

      let viewTab = this.session.desktop.bench.getViewTab(this._widget);
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
      // If the animation target itself is covered by a glasspane, the event is passed on
      let $glassPane = this._widget.$container.children('.glasspane');
      if ($glassPane.length) {
        $glassPane.trigger('mousedown');
      } else {
        $animationTarget.addClassForAnimation('animate-modality-highlight', {
          // remove animate-open as well, user may click the glasspane before the widget itself was able to remove the animate-open class
          classesToRemove: 'animate-modality-highlight animate-open'
        });
      }
    }

    $.suppressEvent(event);
  }

  protected _onDisplayParentRender(event: Event<DisplayParent>) {
    this.renderGlassPanes();
  }

  protected _onGlassPaneRemove(event: JQuery.TriggeredEvent<HTMLDivElement>) {
    let $glassPane = $(event.target);
    this._removeGlassPane($glassPane);
  }
}
