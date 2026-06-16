/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, ObjectIdProvider, objects, strings} from '../index';

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
  | 'caption'
  | 'cell'
  | 'checkbox'
  | 'code'
  | 'columnheader'
  | 'combobox'
  | 'command'
  | 'comment'
  | 'complementary'
  | 'contentinfo'
  | 'definition'
  | 'deletion'
  | 'dialog'
  | 'directory'
  | 'document'
  | 'emphasis'
  | 'feed'
  | 'figure'
  | 'form'
  | 'generic'
  | 'grid'
  | 'gridcell'
  | 'group'
  | 'heading'
  | 'img'
  | 'insertion'
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
  | 'paragraph'
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
  | 'strong'
  | 'subscript'
  | 'suggestion'
  | 'superscript'
  | 'switch'
  | 'tab'
  | 'table'
  | 'tablist'
  | 'tabpanel'
  | 'term'
  | 'textbox'
  | 'time'
  | 'timer'
  | 'toolbar'
  | 'tooltip'
  | 'tree'
  | 'treegrid'
  | 'treeitem';

export type AriaHasPopup = 'menu' | 'listbox' | 'tree' | 'grid' | 'dialog' | 'true' | 'false' | boolean;
export type AriaLive = 'assertive' | 'polite' | 'off';
export type AriaCurrent = 'page' | 'step' | 'location' | 'date' | 'time' | 'true' | 'false' | boolean;
export type AriaOrientation = 'horizontal' | 'vertical';

/**
 * @see https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-labelledby#associated_roles
 */
const unsupportedLabelRoles = new Set(['caption', 'code', 'deletion', 'emphasis', 'generic', 'insertion', 'mark', 'none', 'paragraph', 'presentation', 'strong', 'subscript', 'suggestion', 'superscript', 'term', 'time']);

/**
 * Contains tag names and their implicit role, but only those tags that map to an unsupported label role according to {@link unsupportedLabelRoles}.
 * This means that tag names whose role supports a label are not included (article, button, details, ...).
 * Also, the tag names that don't map to an exact role are not included (a, area, footer, header, li, section).
 *
 * @see https://www.w3.org/TR/html-aria/#docconformance
 */
const roleByTagForUnsupportedLabelRoles: Partial<Record<keyof HTMLElementTagNameMap, AriaRole>> = {
  b: 'generic',
  bdi: 'generic',
  bdo: 'generic',
  body: 'generic',
  caption: 'caption',
  code: 'code',
  data: 'generic',
  del: 'deletion',
  dfn: 'term',
  div: 'generic',
  em: 'emphasis',
  html: 'generic',
  i: 'generic',
  ins: 'insertion',
  p: 'paragraph',
  pre: 'generic',
  q: 'generic',
  s: 'deletion',
  samp: 'generic',
  small: 'generic',
  span: 'generic',
  strong: 'strong',
  sub: 'subscript',
  sup: 'superscript',
  time: 'time',
  u: 'generic'
};

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
    let targetId = aria.ensureId($targetElement);
    if (!replace) {
      let attributeValue = $elem.attr(ariaAttribute) || '';
      if (attributeValue) {
        if (strings.contains(attributeValue, targetId)) {
          return;
        }

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
   * @returns the value of the id attribute. If the element doesn't have an id, a new one will be created and assigned.
   */
  ensureId($element: JQuery<Element>): string {
    let id = $element.attr('id');
    if (!id) {
      // Create an id if the element does not have one yet
      id = ObjectIdProvider.get().createUiSeqId();
      $element.attr('id', id);
    }
    return id;
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

  removeLabelledby($elem: JQuery<Element>) {
    if (!$elem) {
      return;
    }
    $elem.removeAttr('aria-labelledby');
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
   * Links the given element with the given error message by setting aria-errormessage.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-errormessage">ARIA: aria-errormessage</a>
   */
  linkElementWithErrorMessage($elem: JQuery<Element>, $errorMessage: JQuery<Element>) {
    aria._linkElementWithTargetElement($elem, $errorMessage, 'aria-errormessage', AriaLabelledByInsertPosition.FRONT, true);
  },

  removeErrorMessage($elem: JQuery<Element>) {
    if (!$elem) {
      return;
    }
    $elem.removeAttr('aria-errormessage');
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
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-invalid">ARIA: aria-invalid</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  invalid($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-invalid', strings.asString(value));
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
   * @returns true if the element supports aria-label or aria-labelledby, false if not
   */
  supportsLabel($elem: JQuery<Element>): boolean {
    if (!$elem) {
      return false;
    }

    let role = $elem.attr('role');
    if (!role) {
      role = roleByTagForUnsupportedLabelRoles[$elem[0].tagName.toLowerCase()];
    }

    return !unsupportedLabelRoles.has(role);
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
  hasPopup($elem: JQuery<Element>, type: AriaHasPopup) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-haspopup', strings.asString(type));
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
  live($elem: JQuery<Element>, value: AriaLive) {
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
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  disabled($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-disabled', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-modal">ARIA: aria-modal</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  modal($elem: JQuery<Element>, value: boolean) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-modal', strings.asString(value));
  },

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-current">ARIA: aria-current</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  current($elem: JQuery<Element>, value: AriaCurrent) {
    if (!$elem) {
      return;
    }
    $elem.attr('aria-current', strings.asString(value));
  },

  /**
   * Sets the aria-orientation attribute.
   *
   * If `$elem` has a role and the given orientation is the default orientation for that role, the attribute aria-orientation won't be set / will be removed.
   * Also, if the role does not support the aria-orientation attribute, it won't be set / will be removed.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Attributes/aria-orientation">ARIA: aria-orientation</a>
   *
   * @param $elem element to add/remove the attribute. If null, nothing is changed.
   * @param value value of the attribute to set. If null, attribute is removed.
   */
  orientation($elem: JQuery<Element>, orientation: AriaOrientation) {
    if (!$elem) {
      return;
    }
    let role = $elem.attr('role');
    if (role) {
      if (!Object.keys(aria.orientationDefault()).includes(role)) {
        // Don't set orientation if the role doesn't support it
        orientation = null;
      } else {
        // Don't set orientation if it is the role's default
        orientation = aria.orientationDefault()[role] === orientation ? null : orientation;
      }
    }
    $elem.attr('aria-orientation', strings.asString(orientation));
  },

  /**
   * @returns the orientation defaults per role according to https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-orientation and https://www.w3.org/TR/wai-aria/#aria-orientation
   */
  orientationDefault(): Partial<Record<AriaRole, AriaOrientation>> {
    return {
      scrollbar: 'vertical',
      tree: 'vertical',
      treegrid: 'vertical',
      listbox: 'vertical',
      menu: 'vertical',
      slider: 'horizontal',
      separator: 'horizontal',
      tablist: 'horizontal',
      toolbar: 'horizontal',
      menubar: 'horizontal',
      radiogroup: undefined // inherits the orientation attribute from the abstract select role whose default is undefined
    };
  }
};

