/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, GlassPane, LoadingSupportOptions, scout, WidgetSupport} from '../index';

export class LoadingSupport extends WidgetSupport {
  loadingIndicatorDelay: number;
  withGlassPane: boolean;
  abortable = false;
  abortHandler: () => void = null;
  protected _$loadingIndicator: JQuery;
  protected _loadingIndicatorTimeoutId: number;
  protected _glassPane: GlassPane;
  protected _cancelAction: Action;
  protected _containerScrollHandler: (event: JQuery.ScrollEvent) => void;

  /**
   * @param options a mandatory options object
   */
  constructor(options: LoadingSupportOptions) {
    super(options);
    this.loadingIndicatorDelay = scout.nvl(options.loadingIndicatorDelay, 250); // ms
    this.withGlassPane = scout.nvl(options.withGlassPane, false);
    this.abortable = scout.nvl(options.abortable, this.abortable);
    this.abortHandler = scout.nvl(options.abortHandler, this.abortHandler);

    this._$loadingIndicator = null;
    this._loadingIndicatorTimeoutId = null;
    this._containerScrollHandler = this._onContainerScroll.bind(this);
  }

  setLoadingIndicatorDelay(loadingIndicatorDelay: number) {
    this.loadingIndicatorDelay = loadingIndicatorDelay;
  }

  setAbortable(abortable: boolean) {
    this.abortable = abortable;
  }

  setAbortHandler(abortHandler: () => void) {
    this.abortHandler = abortHandler;
  }

  protected override _ensure$Container() {
    if (typeof this.options$Container === 'function') {
      // resolve function provided by options.$container that returns a jQuery element
      this.$container = this.options$Container();
    } else if (this.options$Container) {
      // use jQuery element provided by options.$container
      this.$container = this.options$Container;
    } else {
      // default: when no options.$container is not set, use jQuery element of widget
      this.$container = this.widget.$container;
    }
  }

  /**
   * @param immediate whether the {@link loadingIndicatorDelay} should be ignored and the indicator rendered immediately.
   */
  renderLoading(immediate = false) {
    // Clear any pending loading function
    clearTimeout(this._loadingIndicatorTimeoutId);
    this._ensure$Container();

    if (this.widget.isLoading()) {
      // add loading indicator
      if (!immediate && this.loadingIndicatorDelay && !this.widget.rendering) {
        this._loadingIndicatorTimeoutId = setTimeout(
          this._renderLoadingIndicator.bind(this), this.loadingIndicatorDelay);
      } else {
        this._renderLoadingIndicator();
      }
    } else {
      // remove loading indicator
      this._removeLoadingIndicator();
    }
  }

  protected _renderLoadingIndicator() {
    if (this._$loadingIndicator || !this.widget.rendered && !this.widget.rendering) {
      return;
    }

    // Hide widget content
    this.$container.addClass('loading');
    if (this.withGlassPane) {
      this._glassPane = scout.create(GlassPane, {
        parent: this.widget,
        cssClass: 'loading-glasspane'
      });
      this._glassPane.render(this.$container);
      if (this.$container.data('scrollable')) {
        // If $container is scrollable, glasspane needs to be moved into the viewport
        this._updateGlassPanePosition();
        this.$container.on('scroll', this._containerScrollHandler);
      }
    }

    // Create loading indicator
    let $indicatorParent = this._glassPane ? this._glassPane.$container : this.$container;
    this._$loadingIndicator = $indicatorParent.appendDiv('loading-indicator');
    if (this.abortable) {
      // LoadingSupport is abortable -> add cancel button
      this._cancelAction = scout.create(Action, {
        parent: this.widget,
        text: this.widget.session.text('Cancel'),
        cssClass: 'button cancel-button'
      });
      this._cancelAction.one('action', event => {
        // button was clicked -> remove button after a short delay and call abortHandler
        setTimeout(() => this._cancelAction.remove(), 100);
        this.abortHandler?.();
      });
      this._cancelAction.render(this._$loadingIndicator);
    }
  }

  protected _removeLoadingIndicator() {
    if (!this._$loadingIndicator) {
      return;
    }

    this._glassPane?.deactivate(); // Ensure glasspane does not prevent focusing an element underneath while loading indicator is being removed.
    this._$loadingIndicator.css('opacity', this._$loadingIndicator.css('opacity'));
    this._$loadingIndicator.addClass('animate-remove');
    this._$loadingIndicator.oneAnimationEnd(() => {
      this.remove();
      if (this.widget.rendered) {
        // Show widget's content (layout if necessary)
        this.$container.removeClass('loading');
        this.widget.invalidateLayoutTree();
      }
    });
  }

  remove() {
    if (this._$loadingIndicator) {
      this._$loadingIndicator.remove();
      this._$loadingIndicator = null;
    }
    this.$container?.off('scroll', this._containerScrollHandler);
    this._glassPane?.destroy();
    this._cancelAction?.destroy();
  }

  protected _onContainerScroll(event: JQuery.ScrollEvent) {
    this._updateGlassPanePosition();
  }

  protected _updateGlassPanePosition() {
    if (!this._glassPane?.rendered || !this.$container) {
      return;
    }
    this._glassPane.$container
      .cssTop(this.$container.scrollTop())
      .cssLeft(this.$container.scrollLeft());
  }
}
