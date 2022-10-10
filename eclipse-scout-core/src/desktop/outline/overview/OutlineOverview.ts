/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, Outline, scout, Widget} from '../../../index';

export default class OutlineOverview extends Widget {

  constructor() {
    super();
    this.outline = null;
  }

  _init(model) {
    super._init(model);
    if (!this.outline && this.parent instanceof Outline) {
      this.outline = this.parent;
    }
    scout.assertProperty(this, 'outline', Outline);
  }

  _render() {
    this.$container = this.$parent.appendDiv('outline-overview');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.$content = this.$container.appendDiv('outline-overview-content');
    this.$content.appendDiv('outline-overview-icon').icon(this.outline.iconId);
    this.$content.appendDiv('outline-overview-title').text(this.outline.title);
  }

  /**
   * @override Widget.js
   */
  _attach() {
    this.$parent.append(this.$container);
    let htmlParent = this.htmlComp.getParent();
    this.htmlComp.setSize(htmlParent.size());
    super._attach();
  }

  /**
   * @override Widget.js
   */
  _detach() {
    this.$container.detach();
    super._detach();
  }
}
