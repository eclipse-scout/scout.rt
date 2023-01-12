/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, FormField, FormFieldStatusPosition, Popup, scout, strings, TabItem, ValueField, Widget, widgets} from '../../index';

/**
 * Calls JQuery $.text() for touch-devices and $.val() for all other devices, used together with #makeInputOrDiv().
 * Works as setter when called with 2 arguments, works a getter when called with 1 arguments.
 *
 * @returns when called with 1 argument: $field.text() or $field.val()
 */
function valOrText($field: JQuery, text: string);
function valOrText($field: JQuery): string;
function valOrText($field: JQuery, text?: string): string {
  let isDiv = $field.is('div');
  if (arguments.length === 2) {
    if (isDiv) {
      $field.text(text);
    } else {
      $field.val(text);
    }
  } else {
    return isDiv ? $field.text() : $field.val() as string;
  }
}

export const fields = {
  /**
   * @param $parent used to determine which HTML document is used to create the new HTML element
   * @returns an INPUT element as used in Scout forms.
   */
  makeTextField($parent: JQuery, cssClass?: string): JQuery<HTMLInputElement> {
    return $parent.makeElement('<input>', cssClass)
      .attr('type', 'text')
      .attr('autocomplete', 'NoAutocomplete') /* off and false are currently ignored in Chrome */
      .disableSpellcheck() as JQuery<HTMLInputElement>;
  },

  appendIcon($field: JQuery, cssClass?: string): JQuery {
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
  },

  initTouch(field: { embedded: boolean; touchMode: boolean }, model: { embedded?: boolean; touchMode?: boolean }) {
    field.embedded = scout.nvl(model.embedded, false);
    // when 'touchMode' is not set explicitly, check the device
    field.touchMode = scout.nvl(model.touchMode, Device.get().supportsOnlyTouch());
  },

  valOrText,

  /**
   * Creates a DIV element for touch-devices and an INPUT element for all other devices,
   * depending on the touch flag of the given field.
   */
  makeInputOrDiv(field: Widget & { touchMode?: boolean }, cssClass?: string): JQuery {
    if (field.touchMode) {
      return fields.makeInputDiv(field, cssClass);
    }
    return fields.makeTextField(field.$container, cssClass);
  },

  /**
   * Creates a DIV element that looks like an INPUT element.
   */
  makeInputDiv(field: Widget, cssClass?: string): JQuery {
    return field.$container.makeDiv(strings.join(' ', 'input-field', cssClass));
  },

  /**
   * The INPUT element does not process the click event when the field is disabled.
   * However, the DIV element used in touch-mode does process the event anyway, that's why this check is required.
   */
  handleOnClick(field: Widget & { popup?: Popup; embedded?: boolean }): boolean {
    return field.enabledComputed && !field.embedded && !field.popup;
  },

  /**
   * Calls activate() on the first focusable field of the given fields. Does nothing if the widget is disabled or not rendered.
   */
  activateFirstField(widget: Widget, fieldArr: FormField[]) {
    let firstField = widgets.findFirstFocusableWidget(fieldArr, widget) as FormField;
    if (firstField) {
      firstField.activate();
    }
  },

  /**
   * Links the given element with the given label by setting aria-labelledby.<br>
   * This allows screen readers to build a catalog of the elements on the screen and their relationships, for example, to read the label when the input is focused.
   */
  linkElementWithLabel($elem: JQuery, $label: JQuery) {
    let labelId = $label.attr('id') as string;
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
  },

  /**
   * @param field a ValueField which works like a Proposal- or SmartField.
   * @returns Whether or not the target is on the field (including popup and tooltip)
   */
  eventOutsideProposalField(field: ValueField<any> & { popup: Popup }, target: JQuery | Element): boolean {
    let eventOnField = safeIsOrHas(field.$field, target)
      || safeIsOrHas(field.$icon, target)
      || safeIsOrHas(field.$clearIcon, target);
    let eventOnPopup = safeWidgetIsOrHas(field.popup, target);
    let eventOnTooltip = safeWidgetIsOrHas(field.tooltip(), target);

    return !eventOnField && !eventOnPopup && !eventOnTooltip;

    function safeIsOrHas($elem: JQuery, target: JQuery | Element): boolean {
      return $elem && $elem.isOrHas(target);
    }

    function safeWidgetIsOrHas(widget: Widget, target: JQuery | Element): boolean {
      return widget && widget.rendered && widget.$container.isOrHas(target);
    }
  },

  /**
   * Selects the tab containing the given {@link FormField} for all parent tabBoxes.
   * This ensures that the given field could be seen (if visible itself).
   *
   * @param field The field whose parent tabs should be selected.
   */
  selectAllParentTabsOf(field: FormField) {
    if (!field || !(field instanceof FormField)) {
      return;
    }
    field.visitParentFields(fields.selectIfIsTab);
  },

  /**
   * Selects the given tabItem if it is a {@link TabItem}.
   *
   * @param tabItem The tab to be selected.
   */
  selectIfIsTab(tabItem: FormField) {
    if (!tabItem || !(tabItem instanceof TabItem)) {
      return;
    }
    tabItem.select();
  },

  /**
   * Toggles {@link FormField.statusPosition} based on the given predicate in order to enlarge or reset the box header line while scrolling.
   * The header line is enlarged to match the width of the scroll shadow.
   */
  adjustStatusPositionForScrollShadow(box: FormField, predicate: () => boolean) {
    if (box.htmlComp) {
      // Suppress layout invalidation to prevent dialogs from resetting the height.
      // The box itself must not change its size while scrolling anyway, so there is no need to propagate the invalidation.
      box.htmlComp.suppressInvalidate = true;
    }
    if (predicate()) {
      widgets.preserveAndSetProperty(() => box.setStatusPosition(FormField.StatusPosition.TOP), () => box.statusPosition, box, '_statusPositionOrig');
    } else {
      widgets.resetProperty((preservedValue: FormFieldStatusPosition) => box.setStatusPosition(preservedValue), box, '_statusPositionOrig');
    }
    if (box.htmlComp) {
      box.htmlComp.suppressInvalidate = false;
    }
  }
};
