/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, objects, ObjectUuidProvider, strings} from './index';

/**
 * Determines whether a labelledby id is inserted at the front or the back of current aria-labelledby value.
 */
export enum AriaLabelledByInsertPosition {
  FRONT = 'front',
  BACK = 'back'
}

/**
 * List of all available ARIA roles.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Roles">WAI-ARIA Roles</a>
 */
export type AriaRole =
  'alert'
  | 'alertdialog'
  | 'application'
  | 'article'
  | 'banner'
  | 'button'
  | 'cell'
  | 'checkbox'
  | 'columnheader'
  | 'combobox'
  | 'command'
  | 'comment'
  | 'complementary'
  | 'contentinfo'
  | 'definition'
  | 'dialog'
  | 'directory'
  | 'document'
  | 'feed'
  | 'figure'
  | 'form'
  | 'grid'
  | 'gridcell'
  | 'group'
  | 'heading'
  | 'img'
  | 'link'
  | 'list'
  | 'listbox'
  | 'listitem'
  | 'log'
  | 'main'
  | 'mark'
  | 'marquee'
  | 'math'
  | 'menu'
  | 'menubar'
  | 'menuitem'
  | 'menuitemcheckbox'
  | 'menuitemradio'
  | 'meter'
  | 'navigation'
  | 'none'
  | 'note'
  | 'option'
  | 'presentation'
  | 'progressbar'
  | 'radio'
  | 'radiogroup'
  | 'region'
  | 'row'
  | 'rowgroup'
  | 'rowheader'
  | 'scrollbar'
  | 'search'
  | 'searchbox'
  | 'separator'
  | 'slider'
  | 'spinbutton'
  | 'status'
  | 'suggestion'
  | 'switch'
  | 'tab'
  | 'table'
  | 'tablist'
  | 'tabpanel'
  | 'term'
  | 'textbox'
  | 'timer'
  | 'toolbar'
  | 'tooltip'
  | 'tree'
  | 'treegrid'
  | 'treeitem';

export const aria = {

  /******************************************************************************************************************
   * Roles
   ******************************************************************************************************************/

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Roles">WAI-ARIA Roles</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  role($elem: JQuery<Element>, role: AriaRole) {
    if (!$elem) {
      return;
    }
    $elem.attr('role', role);

    // Alert should have aria-live set to assertive, except for iOS.
    // In iOS this is not recommended because of double speaking issues in VoiceOver
    // see https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Live_Regions#roles_with_implicit_live_region_attributes
    if (role === 'alert' && !Device.get().isIos()) {
      aria.live($elem, 'assertive');
    }

    // Log and status should have aria-live set to polite.
    // see https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Live_Regions#roles_with_implicit_live_region_attributes
    if (role === 'log' || role === 'status') {
      aria.live($elem, 'polite');
    }
  },

  /******************************************************************************************************************
   * Attributes
   ******************************************************************************************************************/

  /**
   * Links the element to the given target element by giving an id to the target element (if needed) and prepending this id to the
   * given aria attribute. If replace is set to true, completely replaces the attribute.
   *
   * For insert position see {@param position}, insert position has no effect if the labelledby property is replaced.
   */
  _linkElementWithTargetElement($elem: JQuery<Element>, $targetElement: JQuery<Element>, ariaAttribute: string, position = AriaLabelledByInsertPosition.FRONT, replace = false) {
    if (!$elem || !$targetElement || strings.empty(ariaAttribute)) {
      return;
    }
    let targetId = $targetElement.attr('id') as string;
    if (!targetId) {
      // Create an id if the element does not have one yet
      targetId = ObjectUuidProvider.createUiId();
      $targetElement.attr('id', targetId);
    }
    if (!replace) {
      let attributeValue = $elem.attr(ariaAttribute) || '';
      if (attributeValue && !strings.contains(attributeValue, targetId)) {
        // Add to the existing value if there is one
        if (objects.isNullOrUndefined(position) || position === AriaLabelledByInsertPosition.FRONT) {
          targetId += ' ' + attributeValue;
        } else if (position === AriaLabelledByInsertPosition.BACK) {
          targetId = attributeValue + ' ' + targetId;
        } else {
          // unknown position
          return;
        }
      }
    }
    $elem.attr(ariaAttribute, targetId);
  },

  /**
   * Links the given element with the given label by setting aria-labelledby.
   * This allows screen readers to build a catalog of the elements on the screen and their relationships, for example, to read the label when the input is focused.
   *
   * Per default linked labels are added to existing linked labels separated by space. If you want to completely replace the linked label, set replace to true.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-labelledby">ARIA: aria-labelledby</a>
   */
  linkElementWithLabel($elem: JQuery<Element>, $label: JQuery<Element>, position = AriaLabelledByInsertPosition.FRONT, replace = false) {
    aria._linkElementWithTargetElement($elem, $label, 'aria-labelledby', position, replace);
  },

  /**
   * Links the given element with the given description by setting aria-describedBy.
   *
   * Per default linked descriptions are added to existing linked descriptions separated by space. If you want to completely replace the linked description, set replace to true.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-describedby">ARIA: aria-describedby</a>
   */
  linkElementWithDescription($elem: JQuery<Element>, $description: JQuery<Element>, position = AriaLabelledByInsertPosition.FRONT, replace = false) {
    aria._linkElementWithTargetElement($elem, $description, 'aria-describedby', position, replace);
  },

  /**
   * Adds {@param description} to {@param $elem} by adding and linking a screen reader only text to {@param $elem}
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-describedby">ARIA: aria-describedby</a>
   */
  addHiddenDescriptionAndLinkToElement($elem: JQuery<Element>, id: string, description: string, position = AriaLabelledByInsertPosition.FRONT, replace = false): JQuery<Element> {
    if (!$elem || strings.empty(description)) {
      return;
    }
    let $descriptionElement = $elem.beforeDiv().addClass('text').attr('id', 'desc' + id).text(description);
    aria.hidden($descriptionElement, true); // hide the element in the accessibility tree, or it may be read twice
    aria.screenReaderOnly($descriptionElement);
    aria.linkElementWithDescription($elem, $descriptionElement, position, replace);
    return $descriptionElement;
  },

  /**
   * Adds aria heading semantics to {@param $header} and correctly assigns heading level information to the heading as well as the surrounding container {@param $elem}.
   * Avoid using empty {@param $header} objects because a screen reader may ignore them in the heading structure leading to inconsistent heading levels.
   * Default aria-level for headers is level 2.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Roles/heading_role">ARIA: heading role</a>
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-level">ARIA: aria-level</a>
   */
  linkElementWithHeader($elem: JQuery<Element>, $header: JQuery<Element>, defaultLevel = 2) {
    if (!$elem || !$header) {
      return;
    }
    let currentLevel = aria._computeHeaderLevel($elem);
    if (currentLevel) {
      currentLevel = currentLevel + 1;
    } else if (defaultLevel) {
      currentLevel = defaultLevel;
    }

    if (currentLevel) {
      aria.role($header, 'heading');
      aria.level($header, currentLevel);
      aria._addHeaderLevelToElement($elem, currentLevel);
    }
  },

  /**
   * In most cases you should just use {@link linkElementWithHeader} which automatically creates an aria heading for you and assigns levels correctly.
   *
   * Use this to implicitly link your heading with its container by adding the header level to the container.
   * This should ensure the heading structure is consistent. Normally your DOM structure looks something like this:
   *
   * <pre>
   * <groupbox>
   *   <label role="heading" aria-level="1">...<\label>
   *   <groupbox>
   *    <label role="heading" aria-level="2">...<\label>
   *    <groupbox>
   *      <label role="heading" aria-level="3">...<\label>
   *      ...
   *    <\groupbox>
   *   <\groupbox>
   * <\groupbox>
   * </pre>
   *
   * After linking your headers to their containers it will look something like this:
   *
   * <pre>
   * <groupbox data-aria-header-level="1">
   *   <label role="heading" aria-level="1">...<\label>
   *   <groupbox data-aria-header-level="2">
   *    <label role="heading" aria-level="2">...<\label>
   *    <groupbox data-aria-header-level="3">
   *      <label role="heading" aria-level="3">...<\label>
   *      ...
   *    <\groupbox>
   *   <\groupbox>
   * <\groupbox>
   * </pre>
   *
   * This allows us to go upwards in the DOM structure, find the last header level used, and pick a header level that fits the structure.
   * Consequently, when adding a heading to your container and before calling this method, you should use {@link _computeHeaderLevel} on
   * your container to find the last header level used and derive your header level accordingly.
   * In most cases, this means adding 1 to the derived header level.
   */
  _addHeaderLevelToElement($elem: JQuery<Element>, level: number) {
    if (!$elem) {
      return;
    }
    $elem.attr('data-aria-header-level', level);
  },

  /**
   * In most cases you should just use {@link linkElementWithHeader} which automatically creates an aria heading for you and assigns levels correctly.
   * Derives the current header level by going upwards in the DOM structure and finding the last header level used.
   * If no parent with a heading is found, returns null.
   */
  _computeHeaderLevel($elem: JQuery<Element>): number {
    if (!$elem) {
      return null;
    }
    let $parentHeader = $elem.parents('[data-aria-header-level]');
    if ($parentHeader.length > 0) {
      return parseInt($parentHeader.eq(0).attr('data-aria-header-level'));
    }
    return null;
  },

  /**
   * Links the given element with the given controlled element by setting aria-controls.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-controls">ARIA: aria-controls</a>
   */
  linkElementWithControls($elem: JQuery<Element>, $controls: JQuery<Element>, position = AriaLabelledByInsertPosition.FRONT, replace = false) {
    aria._linkElementWithTargetElement($elem, $controls, 'aria-controls', position, replace);
  },

  removeControls($elem: JQuery<Element>) {
    if (!$elem) {
      return;
    }
    $elem.removeAttr('aria-controls');
  },

  /**
   * Links the active descendant with the given element by setting aria-activedescendant.
   *
   * When an element does not receive focus when navigating, setting the active descendant property of the field that has focus to the element that has "implied" focus
   * helps screen readers to announce elements as if they had focus. E.g. a selected sub menu item that is rendered selected, but focus remains on the main menu item.
   * Setting the active descendant of the main menu item to the sub menu item will tell the screen reader to announce the currently selected sub item.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-activedescendant">ARIA: aria-activedescendant</a>
   */
  linkElementWithActiveDescendant($elem: JQuery<Element>, $activeDescendant: JQuery<Element>) {
    aria._linkElementWithTargetElement($elem, $activeDescendant, 'aria-activedescendant', AriaLabelledByInsertPosition.FRONT, true);
  },

  removeActiveDescendant($elem: JQuery<Element>) {
    if (!$elem) {
      return;
    }
    $elem.removeAttr('aria-activedescendant');
  },

  /**
   * Adds the screen reader only css class to the element, which hides it from seeing users, but is still visible to the screen reader. This can be useful to
   * e.g. add hidden description elements and link them to field, or replacing visual content (like charts) with tables that make more sense to screen reader users.
   */
  screenReaderOnly($elem: JQuery<Element>) {
    if (!$elem) {
      return;
    }
    $elem.addClass('sr-only');
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-required">ARIA: aria-required</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  required($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-required', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-label">ARIA: aria-label</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed (if not overridden by allowEmpty).
   * @param allowEmpty if set to true, setting label to null/undefined will add an empty string.
   */
  label($elem: JQuery<Element>, label: string, allowEmpty = false) {
    if (!$elem) {
      return;
    }
    if (strings.hasText(label)) {
      $elem.attr('aria-label', label);
    } else if (allowEmpty) {
      $elem.attr('aria-label', '');
    } else {
      $elem.removeAttr('aria-label');
    }
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-description">ARIA: aria-description</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed (if not overridden by allowEmpty).
   * @param allowEmpty if set to true, setting label to null/undefined will add an empty string.
   */
  description($elem: JQuery<Element>, description: string, allowEmpty = false) {
    if (!$elem) {
      return;
    }
    if (strings.hasText(description)) {
      $elem.attr('aria-description', description);
    } else if (allowEmpty) {
      $elem.attr('aria-description', '');
    } else {
      $elem.removeAttr('aria-description');
    }
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-checked">ARIA: aria-checked</a>
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed (if not overridden by triStateAllowed).
   * @param triStateAllowed if set to true, null/undefined will set the checked property to 'mixed'.
   */
  checked($elem: JQuery<Element>, value: boolean, triStateAllowed = false) {
    if (!$elem) {
      return;
    }
    if (triStateAllowed && value !== true && value !== false) {
      $elem.attr('aria-checked', 'mixed');
    } else {
      $elem.attr('aria-checked', strings.asString(value));
    }
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-haspopup">ARIA: aria-haspopup</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param type value of the attribute to set. If null, attribute is removed.
   */
  hasPopup($elem: JQuery<Element>, type: string) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-haspopup', type);
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-expanded">ARIA: aria-expanded</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  expanded($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-expanded', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-selected">ARIA: aria-selected</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  selected($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-selected', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-pressed">ARIA: aria-pressed</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  pressed($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-pressed', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-live">ARIA: aria-live</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  live($elem: JQuery<Element>, value: string) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-live', value);
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-level">ARIA: aria-level</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  level($elem: JQuery<Element>, value: number) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-level', value);
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-hidden">ARIA: aria-hidden</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  hidden($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-hidden', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-multiselectable">ARIA: aria-multiselectable</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  multiselectable($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-multiselectable', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-posinset">ARIA: aria-posinset</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  posinset($elem: JQuery<Element>, value: number) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-posinset', value);
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-setsize">ARIA: aria-setsize</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  setsize($elem: JQuery<Element>, value: number) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-setsize', value);
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-disabled">ARIA: aria-disabled</a>
   */
  disabled($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-disabled', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-modal">ARIA: aria-modal</a>
   */
  modal($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-modal', strings.asString(value));
  }
};
