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
/**
 * This class provides loading support functionality for widgets that simple want to add a CSS class 'loading'
 * when they're in loading state.
 */
scout.SimpleLoadingSupport = function(options) {
  scout.SimpleLoadingSupport.parent.call(this, options);
};
scout.inherits(scout.SimpleLoadingSupport, scout.LoadingSupport);

scout.SimpleLoadingSupport.prototype._renderLoadingIndicator = function() {
  if (this.widget.rendered || this.widget.rendering) {
    this.$container.addClass('loading');
  }
};

scout.SimpleLoadingSupport.prototype._removeLoadingIndicator = function() {
  if (this.widget.rendered || this.widget.rendering) {
    this.$container.removeClass('loading');
  }
};
