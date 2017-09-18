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
scout.ContentEditorField = function() {
  scout.ContentEditorField.parent.call(this);

  this.contentEditor = null;
};
scout.inherits(scout.ContentEditorField, scout.FormField);

scout.ContentEditorField.prototype._init = function(model) {
  scout.ContentEditorField.parent.prototype._init.call(this, model);

  this.contentEditor = scout.create('ContentEditor', {
    parent: this,
    content: model.content
  });

  this.contentEditor.on('propertyChange', this._onPropertyChange.bind(this));
};

scout.ContentEditorField.prototype._render = function() {
  this.addContainer(this.$parent, 'ce-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this._renderContentEditor();
  this.addStatus();
};

scout.ContentEditorField.prototype._renderContentEditor = function() {
  this.contentEditor.render();
  this.addField(this.contentEditor.$container);
};

scout.ContentEditorField.prototype.setContent = function(content) {
  this.setProperty('content', content);
  this.contentEditor.setContent(content);
};

scout.ContentEditorField.prototype.getContent = function() {
  return this.contentEditor.getContent();
};

scout.ContentEditorField.prototype._onPropertyChange = function(event) {
  if (event.propertyName === 'content') {
    this._setProperty('content', this.getContent());
  }
};

