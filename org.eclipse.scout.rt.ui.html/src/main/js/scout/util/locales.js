/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.locales = {

  localesMap: {},

  bootstrap: function(url) {
    url = scout.nvl(url, 'res/locales.json');
    return $.ajaxJson(url)
      .done(this.init.bind(this));
  },

  init: function(data) {
    data.forEach(function(locale) {
      this.localesMap[locale.languageTag] = locale;
    }, this);
  },

  get: function(languageTag) {
    return this.localesMap[languageTag];
  }

};
