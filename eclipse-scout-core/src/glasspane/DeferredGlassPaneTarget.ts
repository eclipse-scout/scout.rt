/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/**
 * Is used to render glasspane after the glasspane targets are set. This case occurs when a child is rendered before a parent is rendered-> on reload page.
 */
export default class DeferredGlassPaneTarget {
  constructor() {
    this.$glassPaneTargets = null;
    this.glassPaneRenderer = null;
  }

  ready($glassPaneTargets) {
    this.$glassPaneTargets = $glassPaneTargets;
    this.renderWhenReady();
  }

  rendererReady(glassPaneRenderer) {
    this.glassPaneRenderer = glassPaneRenderer;
    this.renderWhenReady();
  }

  removeGlassPaneRenderer(glassPaneRenderer) {
    if (this.glassPaneRenderer === glassPaneRenderer) {
      this.glassPaneRenderer = null;
    }
  }

  renderWhenReady() {
    if (this.glassPaneRenderer && this.$glassPaneTargets && this.$glassPaneTargets.length > 0) {
      this.$glassPaneTargets.forEach($glassPaneTarget => {
        this.glassPaneRenderer.renderGlassPane($glassPaneTarget);
      });
    }
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * @param widget a not rendered Widget
   * @findGlassPaneTargets function which returns the targets
   */
  static createFor(widget, findGlassPaneTargets) {
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
