/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlField, ScrollbarInstallOptions} from '../../../index';

export class TileHtmlField extends HtmlField {

  protected override _render() {
    super._render();
    this.$container.addClass('scrollbar-y-outside');
  }

  protected override _installScrollbars(options: ScrollbarInstallOptions) {
    super._installScrollbars($.extend(true, {}, options, {scrollShadow: 'gradient'}));
  }
}
