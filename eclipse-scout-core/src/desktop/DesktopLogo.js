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
import {Widget} from '../index';

export default class DesktopLogo extends Widget {

constructor() {
  super();
}


_init(model) {
  super._init( model);
  this.url = model.url;
}

_render() {
  this.$container = this.$parent.appendDiv('desktop-logo');
}

_renderProperties() {
  super._renderProperties();
  this._renderUrl();
}

_renderUrl() {
  this.$container.css('backgroundImage', 'url(' + this.url + ')');
}

setUrl(url) {
  this.setProperty('url', url);
}
}
