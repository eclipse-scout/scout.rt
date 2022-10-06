/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlField} from '../../../index';

export default class TileHtmlField extends HtmlField {

  constructor() {
    super();
  }

  _render() {
    super._render();

    this.$container.addClass('scrollbar-y-outside');
  }

  _installScrollbars(options) {
    return super._installScrollbars($.extend(true, {}, options, {scrollShadow: 'gradient'}));
  }
}
