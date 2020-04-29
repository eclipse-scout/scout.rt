/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, Menu} from '../index';

export default class ComboMenu extends Menu {

  constructor() {
    super();
  }

  _render() {
    this.$container = this.$parent.appendDiv('menu-item combo-menu');
    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }
    this.$container.unfocusable();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this.childActions.forEach(childAction => {
      childAction.addCssClass('combo-menu-child');
      childAction.render();
    });
  }

  // @override
  _togglesSubMenu() {
    return false;
  }
}
