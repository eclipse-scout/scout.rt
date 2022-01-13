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
import {icons, Menu} from '../index';

export default class EllipsisMenu extends Menu {

  constructor() {
    super();
    this.hidden = true;
    this.ellipsis = true;
    this.stackable = false;
    this.horizontalAlignment = 1;
    this.iconId = icons.ELLIPSIS_V;
    this.tabbable = false;
    this.rightAligned = false;
    this._addPreserveOnPropertyChangeProperties(['childActions']);
  }

  _render() {
    super._render();
    this.$container.addClass('ellipsis');
  }

  setChildActions(childActions) {
    super.setChildActions(childActions);

    if (childActions) {
      // close all actions that have been added to the ellipsis
      childActions.forEach(ca => {
        ca.setSelected(false);
      });
    }
  }

  _renderProperties() {
    super._renderProperties();
    this._renderHidden();
  }

  // add the set hidden function to the ellipsis
  setHidden(hidden) {
    this.setProperty('hidden', hidden);
  }

  _renderHidden() {
    this.$container.setVisible(!this.hidden);
  }

  isTabTarget() {
    return super.isTabTarget() && !this.hidden;
  }

  _childrenForEnabledComputed() {
    return this.childActions;
  }
}
