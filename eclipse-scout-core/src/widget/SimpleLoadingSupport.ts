/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LoadingSupport} from '../index';

/**
 * This class provides loading support functionality for widgets that simple want to add a CSS class 'loading'
 * when they're in loading state.
 */
export class SimpleLoadingSupport extends LoadingSupport {

  protected override _renderLoadingIndicator() {
    if (this.widget.rendered || this.widget.rendering) {
      this.$container.addClass('loading');
    }
  }

  protected override _removeLoadingIndicator() {
    if (this.widget.rendered || this.widget.rendering) {
      this.$container.removeClass('loading');
    }
  }
}
