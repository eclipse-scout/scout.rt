/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {GlassPaneRenderer, GlassPaneTarget, Widget} from '../index';

/**
 * Is used to render glasspane after the glasspane targets are set. This case occurs when a child is rendered before a parent is rendered-> on reload page.
 */
export class DeferredGlassPaneTarget {

  glassPaneRenderer: GlassPaneRenderer;
  $glassPaneTargets: GlassPaneTarget[];

  constructor() {
    this.$glassPaneTargets = null;
    this.glassPaneRenderer = null;
  }

  ready($glassPaneTargets: GlassPaneTarget[]) {
    this.$glassPaneTargets = $glassPaneTargets;
    this.renderWhenReady();
  }

  rendererReady(glassPaneRenderer: GlassPaneRenderer) {
    this.glassPaneRenderer = glassPaneRenderer;
    this.renderWhenReady();
  }

  removeGlassPaneRenderer(glassPaneRenderer: GlassPaneRenderer) {
    if (this.glassPaneRenderer === glassPaneRenderer) {
      this.glassPaneRenderer = null;
    }
  }

  renderWhenReady() {
    if (this.glassPaneRenderer && this.$glassPaneTargets && this.$glassPaneTargets.length > 0) {
      this.$glassPaneTargets.forEach(glassPaneTarget => this.glassPaneRenderer.renderGlassPane(glassPaneTarget));
    }
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * @param widget a not rendered Widget
   * @param findGlassPaneTargets function which returns the targets
   */
  static createFor(widget: Widget, findGlassPaneTargets: () => GlassPaneTarget[]): DeferredGlassPaneTarget[] {
    if (widget.rendered) {
      throw new Error('Don\'t call this function if widget is already rendered.');
    }

    let deferred = new DeferredGlassPaneTarget();
    let renderedHandler = event => {
      let elements = findGlassPaneTargets();
      deferred.ready(elements);
    };

    widget.one('render', renderedHandler);
    widget.one('destroy', () => {
      widget.off('render', renderedHandler);
    });
    return [deferred];
  }
}
