/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlField, ScrollbarInstallOptions} from '../../../index';

export class TileHtmlField extends HtmlField {

  protected override _render() {
    super._render();
    this.$container.addClass('scrollbar-y-outside tile-html-field');
  }

  protected override _installScrollbars(options: ScrollbarInstallOptions) {
    super._installScrollbars($.extend(true, {}, options, {scrollShadow: 'gradient'}));
  }
}
