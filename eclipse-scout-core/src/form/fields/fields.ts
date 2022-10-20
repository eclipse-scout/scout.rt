/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Device, FormField, scout, strings, TabItem, widgets} from '../../index';

/**
 * @param $parent used to determine which HTML document is used to create the new HTML element
 * @returns an INPUT element as used in Scout forms.
 */
export function makeTextField($parent, cssClass) {
  return $parent.makeElement('<input>', cssClass)
    .attr('type', 'text')
    .attr('autocomplete', 'NoAutocomplete') /* off and false are currently ignored in Chrome */
    .disableSpellcheck();
}

export function appendIcon($field, cssClass) {
  /*
   * Note: the field usually does $field.focus() when the icon is clicked.
   * Unfocusable is required because when the icon is clicked the browser is in the middle of setting
   * a new focus target, so we cannot simply change the focus target, because at the end the wrong target would be
   * focused and the popup would be closed immediately. With 'unfocusable' the focus manager will prevent the default focus
   */
  let $icon = $field.appendSpan('icon unfocusable text-field-icon');
  if (cssClass) {
    $icon.addClass(cssClass);
  }
  return $icon;
}

export function initTouch(field, model) {
  field.embedded = scout.nvl(model.embedded, false);
  // when 'touchMode' is not set explicitly, check the device
  field.touchMode = scout.nvl(model.touchMode, Device.get().supportsOnlyTouch());
}

/**
 * Calls JQuery $.text() for touch-devices and $.val() for all other devices, used together with #makeInputOrDiv().
 * Works as setter when called with 2 arguments, works a getter when called with 1 arguments.
 *
 * @return when called with 1 argument: $field.text() or $field.val()
 */
export function valOrText($field, text) {
  let isDiv = $field.is('div');
  if (arguments.length === 2) {
    if (isDiv) {
      $field.text(text);
    } else {
      $field.val(text);
    }
  } else {
    return isDiv ? $field.text() : $field.val();
  }
}

/**
 * Creates a DIV element for touch-devices and an INPUT element for all other devices,
 * depending on the touch flag of the given field.
 *
 * @param {FormField} field
 * @param {string} [cssClass]
 */
export function makeInputOrDiv(field, cssClass) {
  if (field.touchMode) {
    return makeInputDiv(field, cssClass);
  }
  return makeTextField(field.$container, cssClass);
}

/**
 * Creates a DIV element that looks like an INPUT element.
 *
 * @param {FormField} field
 * @param {string} [cssClass]
 */
export function makeInputDiv(field, cssClass) {
  return field.$container.makeDiv(strings.join(' ', 'input-field', cssClass));
}

// note: the INPUT element does not process the click event when the field is disabled
// however, the DIV element used in touch-mode does process the event anyway, that's
// why this check is required.
export function handleOnClick(field) {
  return field.enabledComputed && !field.embedded && !field.popup;
}

/**
 * Calls activate() on the first focusable field of the given fields. Does nothing if the widget is disabled or not rendered.
 *
 * @param {Widget} field
 * @param {FormField[]} fields
 */
export function activateFirstField(widget, fields) {
  let firstField = widgets.findFirstFocusableWidget(fields, widget);
  if (firstField) {
    firstField.activate();
  }
}

/**
 * Links the given element with the given label by setting aria-labelledby.<br>
 * This allows screen readers to build a catalog of the elements on the screen and their relationships, for example, to read the label when the input is focused.
 */
export function linkElementWithLabel($elem, $label) {
  let labelId = $label.attr('id');
  if (!labelId) {
    // Create an id if the element does not have one yet
    labelId = widgets.createUniqueId('lbl');
    $label.attr('id', labelId);
  }
  let labelledBy = $elem.attr('aria-labelledby') || '';
  if (labelledBy) {
    // Add to the existing value if there is one
    labelId += ' ' + labelledBy;
  }
  $elem.attr('aria-labelledby', labelId);
}

/**
 * @param field a ValueField which works like a Proposal- or SmartField. The field must have a property <i>popup</i> and
 *     a <i>_tooltip</i> function.
 * @param target
 * @returns {boolean} Whether or not the target is on the field (including popup and tooltip)
 */
export function eventOutsideProposalField(field, target) {
  let eventOnField =
    safeIsOrHas(field.$field, target) ||
    safeIsOrHas(field.$icon, target) ||
    safeIsOrHas(field.$clearIcon, target);
  let eventOnPopup = safeWidgetIsOrHas(field.popup, target);
  let eventOnTooltip = safeWidgetIsOrHas(field._tooltip(), target);

  return !eventOnField && !eventOnPopup && !eventOnTooltip;

  function safeIsOrHas($elem, target) {
    return $elem && $elem.isOrHas(target);
  }

  function safeWidgetIsOrHas(widget, target) {
    return widget && widget.rendered && widget.$container.isOrHas(target);
  }
}

/**
 * Selects the tab containing the given {@link FormField} for all parent tabBoxes.
 * This ensures that the given field could be seen (if visible itself).
 *
 * @param {FormField} field The field whose parent tabs should be selected.
 */
export function selectAllParentTabsOf(field) {
  if (!field || !(field instanceof FormField)) {
    return;
  }
  field.visitParentFields(selectIfIsTab);
}

/**
 * Selects the given tabItem if it is a {@link TabItem}.
 *
 * @param {TabItem} tabItem The tab to be selected.
 */
export function selectIfIsTab(tabItem) {
  if (!tabItem || !(tabItem instanceof TabItem)) {
    return;
  }
  tabItem.select(tabItem);
}

/**
 * Toggles FormField.statusPosition based on the given predicate in order to enlarge or reset the box header line while scrolling.
 * The header line is enlarged to match the width of the scroll shadow.
 * @param {GroupBox|TabBox} box
 * @param {function} predicate
 */
export function adjustStatusPositionForScrollShadow(box, predicate) {
  if (box.htmlComp) {
    // Suppress layout invalidation to prevent dialogs from resetting the height.
    // The box itself must not change its size while scrolling anyway, so there is no need to propagate the invalidation.
    box.htmlComp.suppressInvalidate = true;
  }
  if (predicate()) {
    widgets.preserveAndSetProperty(() => box.setStatusPosition(FormField.StatusPosition.TOP), () => box.statusPosition, box, '_statusPositionOrig');
  } else {
    widgets.resetProperty(preservedValue => box.setStatusPosition(preservedValue), box, '_statusPositionOrig');
  }
  if (box.htmlComp) {
    box.htmlComp.suppressInvalidate = false;
  }
}

export default {
  activateFirstField,
  appendIcon,
  eventOutsideProposalField,
  handleOnClick,
  initTouch,
  linkElementWithLabel,
  makeInputDiv,
  makeInputOrDiv,
  makeTextField,
  selectAllParentTabsOf,
  selectIfIsTab,
  valOrText,
  adjustStatusPositionForScrollShadow
};
