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
scout.Code = function() {
  this.id;
};

scout.Code.prototype.init = function(model) {
  scout.assertParameter('id', model.id);
  this.id = model.id;
  this._text = model.text;
  this.modelClass = model.modelClass;
};

scout.Code.prototype.text = function(locale) {
  if (!scout.objects.isPlainObject(this._text)) {
    return this._text; // FIXME [awe] 6.1 support for (plain) text or text like '${textKey:foo.bar}' - should we pass session instead of locale?
  }

  var i, text,
    tags = scout.texts.splitLanguageTag(locale.languageTag);
  for (i = 0; i < tags.length; i++) {
    text = this._text[tags[i]];
    if (text) {
      break;
    }
  }
  if (!text) { // FIXME [awe] 6.1 - review this with C.GU. Can we reuse parts of TextMap for this? How to define 'default' language?
    text = this._text['default'];
  }
  if (!text) {
    text = '[missing text for code ' + this.id + ']';
  }
  return text;
};

