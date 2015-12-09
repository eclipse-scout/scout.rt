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
scout.TableControl = function() {
  scout.TableControl.parent.call(this);
  this.tableFooter;
  this.form;
  this._addAdapterProperties('form');
  this.contentRendered = false;
  this.height = scout.TableControl.CONTAINER_SIZE;
  this.animateDuration = scout.TableControl.CONTAINER_ANIMATE_DURATION;
  this.resizerVisible = true;
};
scout.inherits(scout.TableControl, scout.Action);

scout.TableControl.CONTAINER_SIZE = 345;
scout.TableControl.CONTAINER_ANIMATE_DURATION = 350;

scout.TableControl.prototype._init = function(model) {
  scout.TableControl.parent.prototype._init.call(this, model);
  this._syncForm(this.form);
};

/**
 * @override ModelAdapter.js
 */
scout.TableControl.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.TableControl.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  this.tableControlKeyStrokeContext = this._createKeyStrokeContextForTableControl();
};

scout.TableControl.prototype._createKeyStrokeContextForTableControl = function() {
  var keyStrokeContext = new scout.KeyStrokeContext();
  keyStrokeContext.$scopeTarget = function() {
    return this.tableFooter.$controlContent;
  }.bind(this);
  keyStrokeContext.$bindTarget = function() {
    return this.tableFooter.$controlContent;
  }.bind(this);
  keyStrokeContext.registerKeyStroke(new scout.TableControlCloseKeyStroke(this));
  return keyStrokeContext;
};

scout.TableControl.prototype._render = function($parent) {
  var classes = 'table-control ';
  if (this.cssClass) {
    classes += this.cssClass + '-table-control';
  }
  this.$container = $parent.appendDiv(classes)
    .on('mousedown', this._onMouseDown.bind(this))
    .data('control', this);
};

scout.TableControl.prototype.remove = function() {
  this.removeContent();
  scout.TableControl.parent.prototype.remove.call(this);
};

scout.TableControl.prototype._renderContent = function($parent) {
  this.form.render($parent);

  // Tab box gets a special style if it is the first field in the root group box
  var rootGroupBox = this.form.rootGroupBox;
  if (rootGroupBox.fields[0] instanceof scout.TabBox) {
    rootGroupBox.fields[0].$container.addClass('in-table-control');
  }

  this.form.$container.height($parent.height());
  this.form.$container.width($parent.width());
  //FIXME cgu: make this more easy to use
  this.form.htmlComp.pixelBasedSizing = true;
  this.form.htmlComp.validateRoot = true;
  this.form.htmlComp.revalidateLayout();
  this.session.keyStrokeManager.installKeyStrokeContext(this.tableControlKeyStrokeContext);
};

scout.TableControl.prototype._removeContent = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.tableControlKeyStrokeContext);
  this.form.remove();
};

scout.TableControl.prototype.removeContent = function() {
  if (this.contentRendered) {
    this._removeContent();
    if (this.cssClass) {
      this.tableFooter.$controlContainer.removeClass(this.cssClass + '-table-control-container');
      this.tableFooter.$controlContent.removeClass(this.cssClass + '-table-control-content');
    }
    this.contentRendered = false;
  }
};

/**
 * Renders the content if not already rendered.<br>
 * Opens the container if the container is not already open.<br>
 * Does nothing if the content is not available yet to -> don't open container if content is not rendered yet to prevent blank container or laggy opening.
 */
scout.TableControl.prototype.renderContent = function() {
  if (!this.contentRendered && !this.isContentAvailable()) {
    return;
  }

  // do not set initial focus as form, form is not visible during animation of control container, otherwise browser might scroll document
  if (this.form) {
    this.form.renderInitialFocusEnabled = false;
  }

  if (!this.tableFooter.open) {
    this.tableFooter.openControlContainer(this);
  }

  if (!this.contentRendered) {
    if (this.cssClass) {
      this.tableFooter.$controlContainer.addClass(this.cssClass + '-table-control-container');
      this.tableFooter.$controlContent.addClass(this.cssClass + '-table-control-content');
    }
    this._renderContent(this.tableFooter.$controlContent);
    this.contentRendered = true;
  }
};

scout.TableControl.prototype._removeForm = function() {
  this.removeContent();
};

scout.TableControl.prototype._renderForm = function(form) {
  this.renderContent();
};

scout.TableControl.prototype._renderSelected = function(selected, closeWhenUnselected) {
  selected = scout.nvl(selected, this.selected);
  closeWhenUnselected = closeWhenUnselected !== undefined ? closeWhenUnselected : true;

  this.$container.select(selected);

  if (selected) {
    this.tableFooter.onControlSelected(this);
    this.renderContent();
  } else {

    // Don't modify the state initially, only on property change events
    if (this.rendered) {

      if (closeWhenUnselected && this === this.tableFooter.selectedControl) {
        // Don't remove immediately, wait for the animation to finish (handled by onControlContainerClosed)
        this.tableFooter.onControlSelected(null);
        this.tableFooter.closeControlContainer(this);
      } else {
        this.removeContent();
      }

    }
  }
  this._updateTooltip();
};

/**
 * Returns true if the table control may be displayed (opened).
 */
scout.TableControl.prototype.isContentAvailable = function() {
  return !!this.form;
};

scout.TableControl.prototype.toggle = function() {
  if (this.tableFooter.selectedControl === this) {
    this.setSelected(false, true);
  } else {
    this.setSelected(true, true);
  }
};

scout.TableControl.prototype.setSelected = function(selected, closeWhenUnselected) {
  if (!this.enabled || !this.visible) {
    return;
  }
  if (selected === this.selected) {
    return;
  }

  if (this.tableFooter.selectedControl && this.tableFooter.selectedControl !== this) {
    this.tableFooter.selectedControl.setSelected(false, false);
  }

  // Instead of calling parent.setSelected(), we manually execute the required code. Otherwise
  // we would not be able to pass 'closeWhenUnselected' to _renderSelected().
  this.selected = selected;
  if (this.rendered) {
    this._renderSelected(selected, closeWhenUnselected);
  }
  this.sendSelected();
};

scout.TableControl.prototype._configureTooltip = function() {
  var options = scout.TableControl.parent.prototype._configureTooltip.call(this);
  options.cssClass = 'table-control-tooltip';
  return options;
};

scout.TableControl.prototype._syncForm = function(form) {
  if (form) {
    form.rootGroupBox.menuBar.bottom();
  }
  this.form = form;
};

scout.TableControl.prototype._goOffline = function() {
  if (!this.isContentAvailable()) {
    this._renderEnabled(false);
  }
};

scout.TableControl.prototype._goOnline = function() {
  if (!this.isContentAvailable() && this.enabled) {
    this._renderEnabled(true);
  }
};

scout.TableControl.prototype.onResize = function() {
  if (this.form && this.form.rendered) {
    this.form.onResize();
  }
};

scout.TableControl.prototype._onMouseDown = function() {
  this.toggle();
};

scout.TableControl.prototype.onControlContainerOpened = function() {
  if (this.form) {
    // TODO [5.2] dwi: temporary solution; set focus to last known position
    this.form.renderInitialFocus();
  }
};

scout.TableControl.prototype.onControlContainerClosed = function() {
  this.removeContent();
};

/**
 * @override Action.js
 */
scout.TableControl.prototype._createActionKeyStroke = function() {
  return new scout.TableControlActionKeyStroke(this);
};

/**
 * TableControlActionKeyStroke
 */
scout.TableControlActionKeyStroke = function(action) {
  scout.TableControlActionKeyStroke.parent.call(this, action);
  this.renderingHints.offset = 6;
};
scout.inherits(scout.TableControlActionKeyStroke, scout.ActionKeyStroke);

scout.TableControlActionKeyStroke.prototype.handle = function(event) {
  this.field.toggle();
};

scout.TableControlActionKeyStroke.prototype._postRenderKeyBox = function($drawingArea) {
  if (this.field.iconId) {
    var wIcon = $drawingArea.find('.icon').width();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var containerPadding = Number($drawingArea.css('padding-left').replace('px', ''));
    var leftKeyBox = wIcon / 2 - wKeybox / 2 + containerPadding;
    $drawingArea.find('.key-box').css('left', leftKeyBox + 'px');
  }
};
