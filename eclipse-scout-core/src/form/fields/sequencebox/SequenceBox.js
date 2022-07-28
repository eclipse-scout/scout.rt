/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CheckBoxField, CompositeField, DateField, dates, FormField, HtmlComponent, LogicalGridData, LogicalGridLayoutConfig, scout, SequenceBoxGridConfig, SequenceBoxLayout} from '../../../index';

export default class SequenceBox extends CompositeField {

  constructor() {
    super();
    this._addWidgetProperties('fields');
    this._addCloneProperties(['layoutConfig']);
    this.logicalGrid = scout.create('scout.HorizontalGrid');
    this.layoutConfig = null;
    this.fields = [];
    this._fieldPropertyChangeHandler = this._onFieldPropertyChange.bind(this);
    this._lastVisibleFieldSuppressStatusHandler = this._onLastVisibleFieldSuppressStatusChange.bind(this);
  }

  _init(model) {
    super._init(model);

    this._setLayoutConfig(this.layoutConfig);

    this._initDateFields();

    this.setErrorStatus(this.errorStatus);
    this.setTooltipText(this.tooltipText);
    this.setMenus(this.menus);
    this.setMenusVisible(this.menusVisible);
  }

  /**
   * Initialize all DateFields in this SequenceBox with a meaningful autoDate, except fields which already have an autoDate provided by the model.
   */
  _initDateFields() {
    let dateFields = this._getDateFields();
    let newAutoDate = null;
    for (let i = 0; i < dateFields.length; i++) {
      let currField = dateFields[i];
      if (currField.autoDate) {
        // is the autoDate already set by the field's model remember to not change this value.
        currField.hasModelAutoDateSet = true;
      }
      if (!currField.hasModelAutoDateSet) {
        currField.setAutoDate(newAutoDate);
      }
      newAutoDate = this._getAutoDateProposal(currField);
    }
  }

  _render() {
    let field, i;
    this.addContainer(this.$parent, 'sequence-box');
    this.addLabel();
    this.addField(this.$parent.makeDiv());
    this.addStatus();
    this._handleStatus();
    this.htmlBody = HtmlComponent.install(this.$field, this.session);
    this.htmlBody.setLayout(this._createBodyLayout());
    for (i = 0; i < this.fields.length; i++) {
      field = this.fields[i];
      field.labelUseUiWidth = true;
      field.on('propertyChange', this._fieldPropertyChangeHandler);
      field.render(this.$field);
      this._modifyLabel(field);

      // set each children layout data to logical grid data
      field.setLayoutData(new LogicalGridData(field));
    }
  }

  _renderProperties() {
    super._renderProperties();
    this._renderLayoutConfig();
  }

  _createBodyLayout() {
    return new SequenceBoxLayout(this, this.layoutConfig);
  }

  _remove() {
    this.fields.forEach(f => f.off('propertyChange', this._fieldPropertyChangeHandler));
    if (this._lastVisibleField) {
      this._lastVisibleField.off('propertyChange:suppressStatus', this._lastVisibleFieldSuppressStatusHandler);
    }
    super._remove();
  }

  /**
   * @override
   */
  invalidateLogicalGrid(invalidateLayout) {
    super.invalidateLogicalGrid(false);
    if (scout.nvl(invalidateLayout, true) && this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  /**
   * @override
   */
  _setLogicalGrid(logicalGrid) {
    super._setLogicalGrid(logicalGrid);
    if (this.logicalGrid) {
      this.logicalGrid.setGridConfig(new SequenceBoxGridConfig());
    }
  }

  setLayoutConfig(layoutConfig) {
    this.setProperty('layoutConfig', layoutConfig);
  }

  _setLayoutConfig(layoutConfig) {
    if (!layoutConfig) {
      layoutConfig = new LogicalGridLayoutConfig();
    }
    this._setProperty('layoutConfig', LogicalGridLayoutConfig.ensure(layoutConfig));
  }

  _renderLayoutConfig() {
    this.layoutConfig.applyToLayout(this.htmlBody.layout);
    if (this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  _onFieldPropertyChange(event) {
    let visibiltyChanged = (event.propertyName === 'visible');
    if (scout.isOneOf(event.propertyName, ['errorStatus', 'tooltipText', 'visible', 'menus', 'menusVisible'])) {
      this._handleStatus(visibiltyChanged);
    }
    if (event.propertyName === 'value') {
      this._onFieldValueChange(event);
    }
  }

  /**
   * Moves the status relevant properties from the last visible field to the sequencebox. This makes sure that the fields inside the sequencebox have the same size.
   */
  _handleStatus(visibilityChanged) {
    if (visibilityChanged && this._lastVisibleField) {
      // if there is a new last visible field, make sure the status is shown on the previously last one
      this._lastVisibleField.off('propertyChange:suppressStatus', this._lastVisibleFieldSuppressStatusHandler);
      this._lastVisibleField.setSuppressStatus(null);
      if (this._lastVisibleField.rendered) {
        this._lastVisibleField._renderErrorStatus();
        this._lastVisibleField._renderTooltipText();
        this._lastVisibleField._renderMenus();
      }
    }
    this._lastVisibleField = this._getLastVisibleField();
    if (!this._lastVisibleField) {
      return;
    }

    // Update the sequencebox with the status relevant flags
    this._isOverwritingStatusFromField = true;
    if (this._lastVisibleField.errorStatus) {
      this.setErrorStatus(this._lastVisibleField.errorStatus);
      this._isErrorStatusOverwritten = true;
    } else {
      this.setErrorStatus(this.boxErrorStatus);
      this._isErrorStatusOverwritten = false;
    }

    if (this._lastVisibleField.hasStatusTooltip()) {
      this.setTooltipText(this._lastVisibleField.tooltipText);
      this._isTooltipTextOverwritten = true;
    } else {
      this.setTooltipText(this.boxTooltipText);
      this._isTooltipTextOverwritten = false;
    }

    let menuItems = this._lastVisibleField.getContextMenuItems(false);
    if (menuItems && menuItems.length > 0) {
      // Change owner to make sure menu won't be destroyed when setMenus is called
      this._updateBoxMenuOwner(this.fieldStatus);
      this.setMenus(menuItems);
      this.setMenusVisible(this._lastVisibleField.menusVisible);
      this._isMenusOverwritten = true;
    } else {
      this._updateBoxMenuOwner(this);
      this.setMenus(this.boxMenus);
      this.setMenusVisible(this.boxMenusVisible);
      this._isMenusOverwritten = false;
    }
    this._isOverwritingStatusFromField = false;

    // Make sure the last field won't display a status (but shows status CSS class)
    this._lastVisibleField.setSuppressStatus(FormField.SuppressStatus.ICON);
    this._lastVisibleField.on('propertyChange:suppressStatus', this._lastVisibleFieldSuppressStatusHandler);
    if (visibilityChanged) {
      // If the last field got invisible, make sure the new last field does not display a status anymore (now done by the seq box)
      if (this._lastVisibleField.rendered) {
        this._lastVisibleField._renderErrorStatus();
        this._lastVisibleField._renderTooltipText();
        this._lastVisibleField._renderMenus();
      }
    }
  }

  _onLastVisibleFieldSuppressStatusChange(e) {
    // do not change suppressStatus
    e.preventDefault();
  }

  setErrorStatus(errorStatus) {
    if (this._isOverwritingStatusFromField && !this._isErrorStatusOverwritten) {
      // was not overwritten, will be overwritten now -> backup old value
      this.boxErrorStatus = this.errorStatus;
    } else if (!this._isOverwritingStatusFromField) {
      // directly changed on seq box -> update backed-up value
      this.boxErrorStatus = errorStatus;
    }
    if (this._isOverwritingStatusFromField || !this._isErrorStatusOverwritten) {
      // prevent setting value if directly changed on seq box and is already overwritten
      super.setErrorStatus(errorStatus);
    }
  }

  setTooltipText(tooltipText) {
    if (this._isOverwritingStatusFromField && !this._isTooltipTextOverwritten) {
      // was not overwritten, will be overwritten now -> backup old value
      this.boxTooltipText = this.tooltipText;
    } else if (!this._isOverwritingStatusFromField) {
      // directly changed on seq box -> update backed-up value
      this.boxTooltipText = tooltipText;
    }
    if (this._isOverwritingStatusFromField || !this._isTooltipTextOverwritten) {
      // prevent setting value if directly changed on seq box and is already overwritten
      super.setTooltipText(tooltipText);
    }
  }

  setMenus(menus) {
    // ensure menus are real and not just model objects
    menus = this._createChildren(menus);

    if (this._isOverwritingStatusFromField && !this._isMenusOverwritten) {
      // was not overwritten, will be overwritten now -> backup old value
      this.boxMenus = this.menus;
    } else if (!this._isOverwritingStatusFromField) {
      // directly changed on seq box -> update backed-up value
      this.boxMenus = menus;
    }
    if (this._isOverwritingStatusFromField || !this._isMenusOverwritten) {
      // prevent setting value if directly changed on seq box and is already overwritten
      super.setMenus(menus);
    }
  }

  _updateBoxMenuOwner(newOwner) {
    this.boxMenus.forEach(menu => {
      menu.setOwner(newOwner);
    });
  }

  setMenusVisible(menusVisible) {
    if (this._isOverwritingStatusFromField && !this._isMenusOverwritten) {
      // was not overwritten, will be overwritten now -> backup old value
      this.boxMenusVisible = this.menusVisible;
    } else if (!this._isOverwritingStatusFromField) {
      // directly changed on seq box -> update backed-up value
      this.boxMenusVisible = menusVisible;
    }
    if (this._isOverwritingStatusFromField || !this._isMenusOverwritten) {
      // prevent setting value if directly changed on seq box and is already overwritten
      super.setMenusVisible(menusVisible);
    }
  }

  _getLastVisibleField() {
    let visibleFields = this.fields.filter(field => {
      return field.visible;
    });
    if (visibleFields.length === 0) {
      return;
    }

    return visibleFields[visibleFields.length - 1];
  }

  _onFieldValueChange(event) {
    if (event.source instanceof DateField) {
      this._onDateFieldValueChange(event);
    }
  }

  _onDateFieldValueChange(event) {
    // For a better user experience preselect a meaningful date on all following DateFields in the sequence box.
    let field = event.source;
    let dateFields = this._getDateFields();
    let newAutoDate = this._getAutoDateProposal(field);
    for (let i = dateFields.indexOf(field) + 1; i < dateFields.length; i++) {
      let currField = dateFields[i];
      if (!currField.hasModelAutoDateSet) {
        currField.setAutoDate(newAutoDate);
      }
      if (currField.value) {
        // only update fields in between the current field and the next field with a value set. Otherwise already set autoDates would be overwritten.
        break;
      }
    }
  }

  _getDateFields() {
    return this.fields.filter(field => {
      return field instanceof DateField;
    });
  }

  _getAutoDateProposal(field) {
    let newAutoDate = null;
    // if it's only a time field, add one hour, otherwise add one day
    if (field && field.value) {
      if (!field.hasDate && field.hasTime) {
        newAutoDate = dates.shiftTime(field.value, 1, 0, 0);
      } else {
        newAutoDate = dates.shift(field.value, 0, 0, 1);
      }
    }
    return newAutoDate;
  }

  // The new sequence-box sets the label to invisible on the model.
  _modifyLabel(field) {
    if (field instanceof CheckBoxField) {
      field.labelVisible = false;
    }

    if (field instanceof DateField) {
      // The DateField has two inputs ($dateField and $timeField), field.$field refers to the composite which is irrelevant here
      // In order to support aria-labelledby for date fields also, the individual inputs have to be linked with the label rather than the composite
      if (field.$dateField) {
        this._linkWithLabel(field.$dateField);
      }
      if (field.$timeField) {
        this._linkWithLabel(field.$timeField);
      }
    } else if (field.$field) { // If $field is set depends on the concrete field e.g. a group box does not have a $field
      this._linkWithLabel(field.$field);
    }
  }

  setFields(fields) {
    if (this.rendered) {
      throw new Error('Setting fields is not supported if sequence box is already rendered.');
    }
    this.setProperty('fields', fields);
  }

  /**
   * @override CompositeField.js
   */
  getFields() {
    return this.fields;
  }

  clone(model, options) {
    let clone = super.clone(model, options);
    this._deepCloneProperties(clone, 'fields', options);
    return clone;
  }
}
