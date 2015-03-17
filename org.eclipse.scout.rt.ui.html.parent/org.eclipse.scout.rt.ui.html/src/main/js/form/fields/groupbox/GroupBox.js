// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this.fields = [];
  this._addAdapterProperties('fields');
  this.$body;
  this._$groupBoxTitle;

  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];
};
scout.inherits(scout.GroupBox, scout.CompositeField);

scout.GroupBox.prototype._render = function($parent) {
  var env = scout.HtmlEnvironment,
    htmlComp = this.addContainer($parent, this.mainBox ? 'root-group-box' : 'group-box', new scout.GroupBoxLayout(this)),
    htmlBody;

  if (this.mainBox) {
    htmlComp.layoutData = null;
  }

  this.$label = $('<span>').html(this.label);
  this._$groupBoxTitle = this.$container
    .appendDiv('group-box-title')
    .append(this.$label);

  this.$body = this.$container.appendDiv('group-box-body');
  htmlBody = new scout.HtmlComponent(this.$body, this.session);
  htmlBody.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap));
  if (this.scrollable) {
    scout.scrollbars.install(this.$body);
    this.session.detachHelper.pushScrollable(this.$body);
  }
  this._prepareFields();
  for (var i = 0; i < this.controls.length; i++) {
    this.controls[i].render(this.$body);
  }
};

scout.GroupBox.prototype._renderProperties = function() {
  scout.GroupBox.parent.prototype._renderProperties.call(this);

  this._renderBorderVisible(this.borderVisible);
  this._renderExpandable(this.expandable);
  this._renderExpanded(this.expanded);
};

scout.GroupBox.prototype._remove = function() {
  if (this.scrollable) {
    this.session.detachHelper.removeScrollable(this.$body);
  }
};

scout.GroupBox.prototype._prepareFields = function() {
  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];

  var i, field, res;
  for (i = 0; i < this.fields.length; i++) {
    res = undefined;
    field = this.fields[i];
    if (field.label !== scout.strings.removeAmpersand(field.label)) {
      //Add mnemonic keyStrokevar
      var mnemonic = field.label.match(/(^|[^&]|&&)&($|[^&]|&&)/g)[0].replace('&', '');
      res = mnemonic.charAt(mnemonic.length - 1);
    }

    if (field instanceof scout.Button) {
      if (field.processButton) {
        this.processButtons.push(field);
        if (field.systemType !== scout.Button.SYSTEM_TYPE.NONE) {
          this.systemButtons.push(field);
        } else {
          this.customButtons.push(field);
        }
      } else {
        this.controls.push(field);
      }
      if(res){
        this.keyStrokeAdapter.registerKeyStroke(new scout.ButtonMnemonicKeyStroke(res, field));
      }

    } else {
      this.controls.push(field);
      if(res){
        this.keyStrokeAdapter.registerKeyStroke(new scout.MnemonicKeyStroke(res, field));
      }
    }
  }
};

/**
 * @override CompositeField.js
 */
scout.GroupBox.prototype.getFields = function() {
  return this.controls;
};

scout.GroupBox.prototype._renderBorderVisible = function(borderVisible) {
  if (this.borderDecoration === 'auto') {
    borderVisible = this._computeBorderVisible(borderVisible);
  }

  if (!borderVisible) {
    this.$body.addClass('y-padding-invisible');
  }
};

//Don't include in renderProperties, it is not necessary to execute it initially because renderBorderVisible is executed already
scout.GroupBox.prototype._renderBorderDecoration = function() {
  this._renderBorderVisible(this.borderVisible);
};

/**
 *
 * @returns false if it is the mainbox. Or if the groupbox contains exactly one tablefield which has an invisible label
 */
scout.GroupBox.prototype._computeBorderVisible = function(borderVisible) {
  var fields = this.getFields();
  if (this.mainBox) {
    borderVisible = false;
  } else if (fields.length === 1 && fields[0].objectType === 'TableField' && !fields[0].labelVisible) {
    fields[0].$container.addClass('single');
    borderVisible = false;
  } else if (this.parent instanceof scout.GroupBox &&
      this.parent.parent instanceof scout.Form &&
      this.parent.parent.parent instanceof scout.WrappedFormField &&
      this.parent.parent.parent.parent instanceof scout.SplitBox &&
      this.parent.getFields().length === 1) {
    // Special case for wizard: wrapped form in split box with a single group box
    borderVisible = false;
  }
  return borderVisible;
};

scout.GroupBox.prototype._renderExpandable = function(expandable) {
  var $control = this._$groupBoxTitle.children('.group-box-control');

  if (expandable) {
    if ($control.length === 0) {
      // Create control if necessary
      $control = $.makeDiv('group-box-control')
        .on('click', this._onGroupBoxControlClick.bind(this))
        .prependTo(this._$groupBoxTitle);
    }
    this._$groupBoxTitle
      .addClass('expandable')
      .on('click.group-box-control', this._onGroupBoxControlClick.bind(this));
  }
  else {
    $control.remove();
    this._$groupBoxTitle
      .removeClass('expandable')
      .off('.group-box-control');
  }
};

scout.GroupBox.prototype._renderExpanded = function(expanded) {
  this.$container.toggleClass('collapsed', !expanded);
  if (this.rendered) {
    scout.HtmlComponent.get(this.$container).invalidateTree();
  }
};

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._renderLabelVisible = function(visible) {
  // TODO AWE: (concept) discuss with C.GU -> auf dem GUI server korrigieren oder im Browser UI?
  // --> kein hack für main-box, wenn die auf dem model ein label hat, hat es im UI auch eins
  this._$groupBoxTitle.setVisible(visible && this.label && !this.mainBox);
};

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._registerKeyStrokeAdapter = function() {
  this.keyStrokeAdapter = new scout.GroupBoxKeyStrokeAdapter(this);
};

scout.GroupBox.prototype._onGroupBoxControlClick = function(event) {
  if (this.expandable) {
    this.setGroupBoxExpanded(!this.expanded);
  }
  $.suppressEvent(event); // otherwise, the event would be triggered twice sometimes (by group-box-control and group-box-title)
};

scout.GroupBox.prototype.setGroupBoxExpanded = function(expanded) {
  if (this.expanded !== expanded) {
    this.expanded = expanded;
    this.session.send(this.id, 'expanded', {
      expanded: expanded
    });
  }
  if (this.rendered) {
    this._renderExpanded(expanded);
  }
};
