/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ContentElement = function() {
  this.elementId = scout.objectFactory.createUniqueId();
  this.contentEditor;
  this.$container;
  this.content;
  this.$slot;
  this.slot;
};

scout.ContentElement.prototype.init = function(model) {
  this.contentEditor = model.contentEditor;
  this.$container = model.$container;
  this.content = scout.nvl(this.$container[0].outerHTML, '');
};

scout.ContentElement.prototype.dropInto = function($placeholder) {
  this.$container.addClass('ce-element ce-element-hover');
  this.contentEditor.addElement(this);

  this.$slot = $placeholder.parent();
  this.slot = this.$slot.attr('data-ce-slot');

  $placeholder.after(this.$container);

  this.$container
    .on('mouseenter', this._onElementMouseEnter.bind(this))
    .on('mouseleave', this._onElementMouseLeave.bind(this));

  setTimeout(function() {
    this.$container.removeClass('ce-element-hover');
  }.bind(this), 200);
};

scout.ContentElement.prototype._onElementMouseEnter = function(event) {
  this.$container.addClass('ce-element-hover');
  var $buttonGroup = this.$container.appendDiv('ce-element-button-group');

  $buttonGroup
    .appendDiv('ce-element-button ce-remove-button')
    .on('click', this._onRemove.bind(this));

  $buttonGroup
    .appendDiv('ce-element-button ce-move-up-button')
    .on('click', this._onMoveUp.bind(this));

  $buttonGroup
    .appendDiv('ce-element-button ce-move-down-button')
    .on('click', this._onMoveDown.bind(this));

  $buttonGroup
    .appendDiv('ce-element-button ce-edit-button')
    .on('click', this._onEdit.bind(this));

  $buttonGroup
  .appendDiv('ce-element-button ce-copy-button')
  .on('click', this._onCopy.bind(this));
};

scout.ContentElement.prototype._onElementMouseLeave = function(event) {
  this.$container.removeClass('ce-element-hover');
  this.$container.find('.ce-element-button-group').remove();
};

scout.ContentElement.prototype._onRemove = function() {
  var $element = this.$container.closest('.ce-element');
  $element.animate({
    height: '0',
    opacity: 0
  }, 'fast', 'linear', function() {
    $element.remove();
    this.contentEditor.removeElement(this);
  }.bind(this));
};

scout.ContentElement.prototype._onMoveUp = function() {
  var $prev = this.$container.prev('.ce-element');
  var deltaUp = $prev.height();
  var deltaDown = this.$container.height();
  this.$container.animate({
    'top': '-=' + (deltaUp) + 'px'
  }, 'fast');
  $prev.animate({
    'top': '+=' + (deltaDown) + 'px'
  }, 'fast', 'linear', function() {
    $prev.insertAfter(this.$container);
    $prev.css('top', 'auto');
    this.$container.css('top', 'auto');
  }.bind(this));
};

scout.ContentElement.prototype._onMoveDown = function() {
  var $next = this.$container.next('.ce-element');
  var deltaDown = $next.height();
  var deltaUp = this.$container.height();
  this.$container.animate({
    'top': '+=' + (deltaDown) + 'px'
  }, 'fast');
  $next.animate({
    'top': '-=' + (deltaUp) + 'px'
  }, 'fast', 'linear', function() {
    $next.insertBefore(this.$container);
    $next.css('top', 'auto');
    this.$container.css('top', 'auto');
  }.bind(this));
};

scout.ContentElement.prototype._onEdit = function() {
  this.contentEditor.trigger('editElement', {
    elementContent: this.content,
    slot: this.slot,
    elementId: this.elementId
  });
};

scout.ContentElement.prototype._onCopy = function() {
  var $elementContent = this.$container.clone(false).removeClass('ce-element-hover');
  $elementContent.find('.ce-element-button-group').remove();
  var contentElement = scout.create('ContentElement', {
    contentEditor: this.contentEditor,
    $container: $elementContent
  });
  contentElement.dropInto(this.$container);
};

scout.ContentElement.prototype.updateContent = function(content) {
  this.content = content;
  this.$container.html($(content).html());
};
