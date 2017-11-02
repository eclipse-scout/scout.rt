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
  this.slot;
};

scout.ContentElement.prototype.init = function(model) {
  this.contentEditor = model.contentEditor;
  this.$container = model.$container;
  this.content = scout.nvl(this.$container[0].outerHTML, '');
  $(window).on('resize', this._onResize.bind(this));
};

scout.ContentElement.prototype.dropInto = function(slot, $placeholder) {
  this.$container.addClass('ce-element ce-element-hover');
  this.contentEditor.addElement(this);

  this.slot = slot;

  $placeholder.after(this.$container);
  if ($placeholder.hasClass('ce-mandatory-placeholder')) {
    scout.arrays.remove(this.contentEditor._iframeDragTargetList, $placeholder);
    $placeholder.remove();
  }

  this.$container
    .on('mouseenter', this._onElementMouseEnter.bind(this))
    .on('mouseleave', this._onElementMouseLeave.bind(this))
    .on('click', this._onElementClick.bind(this));

  setTimeout(function() {
    this.$container.removeClass('ce-element-hover');
  }.bind(this), 200);
};

scout.ContentElement.prototype._onResize = function(event) {
  if (this._controlsVisible()) {
    this._showGlasspanes();
  }
};

scout.ContentElement.prototype._onElementMouseEnter = function(event) {
  if (!this._controlsVisible()) {
    this._showEditIndicator();
  }
};

scout.ContentElement.prototype._onElementMouseLeave = function(event) {
  this._hideEditIndicator();
};

scout.ContentElement.prototype._onElementClick = function(event) {
  if (this.doubleClicking) {
    this.doubleClicking = false;
    this.editElement();
  }

  if (!this._controlsVisible()) {
    this._hideEditIndicator();
    this._showControls();
    this.doubleClicking = true;
    setTimeout(function() {
      this.doubleClicking = false;
    }.bind(this), 300);
  }
};

scout.ContentElement.prototype._showEditIndicator = function() {
  this.$container.addClass('ce-element-hover').appendDiv('ce-element-edit-indicator');
};

scout.ContentElement.prototype._hideEditIndicator = function() {
  this.$container.removeClass('ce-element-hover');
  this.$container.find('.ce-element-edit-indicator').remove();
};

scout.ContentElement.prototype._showControls = function() {
  this.$container.find('.ce-element-button-group').remove();
  var $buttonGroup = this.$container.appendDiv('ce-element-button-group');

  this._showGlasspanes();

  $buttonGroup
    .appendDiv('ce-element-button ce-remove-button')
    .on('click', this._onRemove.bind(this));

  $buttonGroup
    .appendDiv('ce-element-button ce-edit-button')
    .on('click', this._onEdit.bind(this));

  if (this.$container.prev('.ce-element').length >= 1) {
    $buttonGroup
      .appendDiv('ce-element-button ce-move-up-button')
      .on('click', this._onMoveUp.bind(this));
  }

  if (this.$container.next('.ce-element').length >= 1) {
    $buttonGroup
      .appendDiv('ce-element-button ce-move-down-button')
      .on('click', this._onMoveDown.bind(this));
  }

  if (this.slot.isFree()) {
    $buttonGroup
      .appendDiv('ce-element-button ce-copy-button')
      .on('click', this._onCopy.bind(this));
  }

  $buttonGroup
    .appendDiv('ce-element-button ce-close-button')
    .on('click', this._onCloseControls.bind(this));
};

scout.ContentElement.prototype._hideControls = function() {
  this.$container.find('.ce-element-button-group').fadeOut(200, function() {
    $(this).remove();
  });
  $(this.contentEditor.doc.body).find('.ce-glasspane').fadeOut(200, function() {
    $(this).remove();
  });
};

scout.ContentElement.prototype._controlsVisible = function() {
  return this.$container.find('.ce-element-button-group').length > 0;
};

scout.ContentElement.prototype._showGlasspanes = function() {
  $(this.contentEditor.doc.body).find('.ce-glasspane').remove();

  var $buttonGroup = this.$container.find('.ce-element-button-group');

  var elementTop = $buttonGroup.offset().top;
  var elementLeft = $buttonGroup.offset().left;
  var elementBottom = this.$container.offset().top + this.$container.height();
  var elementRight = $buttonGroup.offset().left + $buttonGroup.width();

  // Top Glasspane
  $(this.contentEditor.doc.body)
    .appendDiv('ce-glasspane')
    .offset({
      top: 0,
      left: 0
    })
    .height(elementTop)
    .width('100%').on('click', this._onGlasspaneClick.bind(this));

  // Bottom Glasspane
  $(this.contentEditor.doc.body)
    .appendDiv('ce-glasspane')
    .offset({
      top: elementBottom,
      left: 0
    })
    .height($(this.contentEditor.doc).height() - elementBottom)
    .width('100%').on('click', this._onGlasspaneClick.bind(this));

  // Left Glasspane
  $(this.contentEditor.doc.body)
    .appendDiv('ce-glasspane')
    .offset({
      top: elementTop,
      left: 0
    })
    .height(elementBottom - elementTop)
    .width(elementLeft).on('click', this._onGlasspaneClick.bind(this));

  // Right Glasspane
  $(this.contentEditor.doc.body)
    .appendDiv('ce-glasspane')
    .offset({
      top: elementTop,
      left: elementRight
    })
    .height(elementBottom - elementTop)
    .css('float', 'right')
    .width($(this.contentEditor.doc).width() - elementRight).on('click', this._onGlasspaneClick.bind(this));
};

scout.ContentElement.prototype._onCloseControls = function() {
  this._hideControls();
};

scout.ContentElement.prototype._onGlasspaneClick = function() {
  this._hideControls();
};

scout.ContentElement.prototype._onRemove = function() {
  var $element = this.$container.closest('.ce-element');
  $element.animate({
    height: '0',
    opacity: 0
  }, 250, 'linear', function() {
    $element.remove();
    this.contentEditor.removeElement(this);
    this._hideControls();
    this.slot._showMandatoryPlaceholders();
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
    this._showControls();
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
    this._showControls();
  }.bind(this));
};

scout.ContentElement.prototype._onEdit = function() {
  this.editElement();
};

scout.ContentElement.prototype.editElement = function() {
  this.contentEditor.trigger('editElement', {
    elementContent: this.content,
    slot: this.slot.name(),
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
  contentElement.dropInto(this.slot, this.$container);
  this._hideControls();
  this._showControls();
};

scout.ContentElement.prototype.updateContent = function(content) {
  $(this.contentEditor.doc.body).find('.ce-glasspane').remove();
  this.content = content;
  this.$container.html($(content).html());
  this._showControls();
};
