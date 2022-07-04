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
import {LoadingSupport} from '../index';

/**
 * This class provides loading support functionality for widgets that simple want to add a CSS class 'loading'
 * when they're in loading state.
 */
export default class SimpleLoadingSupport extends LoadingSupport {

  constructor(options) {
    super(options);
  }

  _renderLoadingIndicator() {
    if (this.widget.rendered || this.widget.rendering) {
      this.$container.addClass('loading');
    }
  }

  _removeLoadingIndicator() {
    if (this.widget.rendered || this.widget.rendering) {
      this.$container.removeClass('loading');
    }
  }
}
