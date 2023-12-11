/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Device, DisplayParent, filters, FocusContext, FocusRule, focusUtils, GlassPaneRenderer, scout, Session} from '../index';
import $ from 'jquery';

export interface FocusManagerOptions {
  session: Session;
  active?: boolean;
  restrictedFocusGain?: boolean;
}

export interface RequestFocusOptions {
  /**
   * prevents scrolling to new focused element (defaults to false)
   */
  preventScroll?: boolean;
  /**
   * prevents focusing if not ready
   */
  onlyIfReady?: boolean;
  /**
   * automatically selects the text content of the element if supported (defaults to false)
   */
  selectText?: boolean;
}

/**
 * The focus manager ensures proper focus handling based on focus contexts.
 *
 * A focus context is bound to a $container. Once a context is activated, that container defines the tab cycle,
 * meaning that only child elements of that container can be entered by tab. Also, the context ensures proper
 * focus gaining, meaning that only focusable elements can gain focus. A focusable element is defined as an element,
 * which is natively focusable and which is not covert by a glass pane. Furthermore, if a context is uninstalled,
 * the previously active focus context is activated and its focus position restored.
 */
export class FocusManager implements FocusManagerOptions {
  session: Session;
  active: boolean;
  restrictedFocusGain: boolean;

  /** @internal */
  _glassPaneDisplayParents: DisplayParent[];
  /** @internal */
  _glassPaneTargets: JQuery[];

  protected _focusContexts: FocusContext[];
  protected _glassPaneRenderers: GlassPaneRenderer[];

  constructor(options: FocusManagerOptions) {
    let defaults: FocusManagerOptions = {
      // Auto focusing of elements is bad with on screen keyboards -> deactivate to prevent unwanted popping up of the keyboard
      active: !Device.get().supportsOnlyTouch(),
      // Preventing blur is bad on touch devices because every touch on a non input field is supposed to close the keyboard which does not happen if preventDefault is used on mouse down
      restrictedFocusGain: !Device.get().supportsOnlyTouch(),
      session: null
    };
    $.extend(this, defaults, options);

    if (!this.session) {
      throw new Error('Session expected');
    }

    this._focusContexts = [];
    this._glassPaneTargets = [];
    this._glassPaneDisplayParents = [];
    this._glassPaneRenderers = [];

    // Make $entryPoint focusable and install focus context.
    let $mainEntryPoint = this.session.$entryPoint;
    let portletPartId = $mainEntryPoint.data('partid') || '0';
    $mainEntryPoint.attr('tabindex', portletPartId);

    // Restricted focus gain means that not every click outside of the active element necessarily focuses another element but the active element stays focused
    // See _acceptFocusChangeOnMouseDown for details
    if (this.restrictedFocusGain) {
      this.installTopLevelMouseHandlers($mainEntryPoint);
    }
    this.installFocusContext($mainEntryPoint, FocusRule.AUTO);
  }

  installTopLevelMouseHandlers($container: JQuery) {
    // Install 'mousedown' on top-level $container to accept or prevent focus gain
    $container.on('mousedown', event => {
      if (!this._acceptFocusChangeOnMouseDown($(event.target))) {
        event.preventDefault();
      }
      return true;
    });
  }

  /**
   * Activates or deactivates focus management.
   *
   * If deactivated, the focus manager still validates the current focus, but never gains focus nor enforces a valid focus position.
   * Once activated, the current focus position is revalidated.
   */
  activate(activate: boolean) {
    if (this.active !== activate) {
      this.active = activate;
      if ($.log.isDebugEnabled()) {
        $.log.isDebugEnabled() && $.log.debug('Focus manager active: ' + this.active);
      }
      if (this.active) {
        this.validateFocus();
      }
    }
  }

  /**
   * Installs a new focus context for the given $container, and sets the $container's initial focus, either by
   * the given rule, or tries to gain focus for the given element.
   * @returns the installed context.
   */
  installFocusContext($container: JQuery, focusRuleOrElement?: FocusRule | HTMLElement): FocusContext {
    let elementToFocus = this.evaluateFocusRule($container, focusRuleOrElement);

    // Create and register the focus context.
    let focusContext = new FocusContext($container, this);
    if (FocusRule.PREPARE !== focusRuleOrElement) {
      focusContext.ready();
    }
    this._pushIfAbsentElseMoveTop(focusContext);

    if (elementToFocus) {
      focusContext.validateAndSetFocus(elementToFocus);
    }
    return focusContext;
  }

  /**
   * Evaluates the {@link FocusRule} or just returns the given element if focusRuleOrElement is not a focus rule.
   */
  evaluateFocusRule($container: JQuery, focusRuleOrElement?: FocusRule | HTMLElement): HTMLElement {
    let elementToFocus: HTMLElement;
    if (!focusRuleOrElement || focusRuleOrElement === FocusRule.AUTO || focusRuleOrElement === FocusRule.PREPARE) {
      elementToFocus = this.findFirstFocusableElement($container);
    } else if (focusRuleOrElement === FocusRule.NONE) {
      elementToFocus = null;
    } else {
      elementToFocus = focusRuleOrElement;
    }
    return elementToFocus;
  }

  /**
   * Uninstalls the focus context for the given $container, and activates the last active context.
   * This method has no effect, if there is no focus context installed for the given $container.
   *
   * @param options a boolean whether to prevent scrolling to focused element or not (default is true)
   */
  uninstallFocusContext($container: JQuery, options?: { preventScroll?: boolean }) {
    options = $.extend({}, {preventScroll: true}, options);
    let focusContext = this.getFocusContext($container);
    if (!focusContext) {
      return;
    }

    // Filter to exclude the current focus context's container and any of its child elements to gain focus.
    let filter = filters.outsideFilter(focusContext.$container);

    // Remove and dispose the current focus context.
    arrays.remove(this._focusContexts, focusContext);
    focusContext.dispose();

    // Activate last active focus context.
    let activeFocusContext = this._findActiveContext();
    if (activeFocusContext) {
      activeFocusContext.validateAndSetFocus(activeFocusContext.lastValidFocusedElement, filter, options);
    }
  }

  /**
   * Returns whether there is a focus context installed for the given $container.
   */
  isFocusContextInstalled($container: JQuery): boolean {
    return !!this.getFocusContext($container);
  }

  /**
   * Activates the focus context of the given $container or the given focus context and validates the focus so that the previously focused element will be focused again.
   */
  activateFocusContext(focusContextOr$Container: FocusContext | JQuery) {
    let focusContext: FocusContext;
    if (focusContextOr$Container instanceof FocusContext) {
      focusContext = focusContextOr$Container;
    } else {
      focusContext = this.getFocusContext(focusContextOr$Container);
    }
    if (!focusContext || this.isElementCovertByGlassPane(focusContext.$container)) {
      return;
    }
    this._pushIfAbsentElseMoveTop(focusContext);
    this.validateFocus();
  }

  /**
   * Checks if the given element is accessible, meaning not covert by a glasspane.
   *
   * @param filter if specified, the filter is used to filter the array of glass pane targets
   */
  isElementCovertByGlassPane(element: HTMLElement | JQuery, filter?: () => boolean): boolean {
    let targets = this._glassPaneTargets;
    if (filter) {
      targets = this._glassPaneTargets.filter(filter);
    }
    if (!targets.length) {
      return false; // no glasspanes active.
    }

    if (this._glassPaneDisplayParents.indexOf(scout.widget(element)) >= 0) {
      return true;
    }
    // Checks whether the element is a child of a glasspane target.
    // If so, the some-iterator returns immediately with true.
    return targets.some($glassPaneTarget => $(element).closest($glassPaneTarget).length !== 0);
  }

  /**
   * Registers the given glasspane target, so that the focus cannot be gained on the given target nor on its child elements.
   */
  registerGlassPaneTarget($glassPaneTarget: JQuery) {
    this._glassPaneTargets.push($glassPaneTarget);
    this.validateFocus();
  }

  registerGlassPaneDisplayParent(displayParent: DisplayParent) {
    this._glassPaneDisplayParents.push(displayParent);
  }

  registerGlassPaneRenderer(glassPaneRenderer: GlassPaneRenderer) {
    this._glassPaneRenderers.push(glassPaneRenderer);
  }

  /**
   * Unregisters the given glasspane target, so that the focus can be gained again for the target or one of its child controls.
   */
  unregisterGlassPaneTarget($glassPaneTarget: JQuery) {
    arrays.$remove(this._glassPaneTargets, $glassPaneTarget);
    this.validateFocus();
  }

  unregisterGlassPaneDisplayParent(displayParent: DisplayParent) {
    arrays.remove(this._glassPaneDisplayParents, displayParent);
  }

  unregisterGlassPaneRenderer(glassPaneRenderer: GlassPaneRenderer) {
    arrays.remove(this._glassPaneRenderers, glassPaneRenderer);
  }

  rerenderGlassPanes() {
    // create a copy of the current glassPaneRenderers
    let currGlassPaneRenderers = this._glassPaneRenderers.slice();
    // remove and rerender every glassPaneRenderer to keep them (and their members) valid.
    currGlassPaneRenderers.forEach(glassPaneRenderer => {
      glassPaneRenderer.removeGlassPanes();
      glassPaneRenderer.renderGlassPanes();
    });
  }

  /**
   * Enforces proper focus on the currently active focus context.
   *
   * @param filter Filter to exclude elements to gain focus.
   */
  validateFocus(filter?: () => boolean) {
    let activeContext = this._findActiveContext();
    if (activeContext) {
      activeContext.validateFocus(filter);
    }
  }

  requestFocusIfReady(element: HTMLElement | JQuery, filter?: () => boolean): boolean {
    return this.requestFocus(element, filter, {onlyIfReady: true});
  }

  /**
   * Requests the focus for the given element, but only if being a valid focus location.
   *
   * @param element
   *        the element to focus, or null to focus the context's first focusable element matching the given filter.
   * @param filter
   *        filter that controls which element should be focused, or null to accept all focusable candidates.
   * @param options
   *        options to customize the focus request.
   * @returns true if focus was gained, false otherwise.
   */
  requestFocus(element: HTMLElement | JQuery, filter?: () => boolean, options?: RequestFocusOptions): boolean {
    options = options || {};
    let htmlElement: HTMLElement = element instanceof $ ? element[0] : element;
    if (!htmlElement) {
      return false;
    }

    let context = this._findFocusContextFor(htmlElement);
    if (context) {
      if (scout.nvl(options.onlyIfReady, false) && !context.prepared) {
        return false;
      }
      context.validateAndSetFocus(htmlElement, filter, options);
    }

    return focusUtils.isActiveElement(htmlElement);
  }

  /**
   * Finds the first focusable element of the given $container, or null if not found.
   */
  findFirstFocusableElement($container: JQuery, filter?: () => boolean): HTMLElement {
    let firstElement, firstDefaultButton, firstButton, i, candidate, $candidate, $menuParents, $tabParents, $boxButtons,
      $entryPoint = $container.entryPoint(),
      $candidates = $container
        .find(':focusable')
        .addBack(':focusable') /* in some use cases, the container should be focusable as well, e.g. context menu without focusable children */
        .not($entryPoint) /* $entryPoint should never be a focusable candidate. However, if no focusable candidate is found, 'FocusContext.validateAndSetFocus' focuses the $entryPoint as a fallback. */
        .filter(filter || filters.returnTrue);

    for (i = 0; i < $candidates.length; i++) {
      candidate = $candidates[i];
      $candidate = $(candidate);

      // Check whether the candidate is accessible and not covert by a glass pane.
      if (this.isElementCovertByGlassPane(candidate)) {
        continue;
      }
      // Check if the element (or one of its parents) does not want to be the first focusable element
      if ($candidate.is('.prevent-initial-focus') || $candidate.closest('.prevent-initial-focus').length > 0) {
        continue;
      }

      if (!firstElement && !($candidate.hasClass('button') || $candidate.hasClass('menu-item'))) {
        firstElement = candidate;
      }

      if (!firstDefaultButton && $candidate.is('.default')) {
        firstDefaultButton = candidate;
      }

      $menuParents = $candidate.parents('.menubar');
      $tabParents = $candidate.parents('.tab-box-header');
      $boxButtons = $candidate.parents('.box-buttons');
      if (($menuParents.length > 0 || $tabParents.length > 0 || $boxButtons.length > 0) && !firstButton && ($candidate.hasClass('button') || $candidate.hasClass('menu-item'))) {
        firstButton = candidate;
      } else if (!$menuParents.length && !$tabParents.length && !$boxButtons.length && typeof candidate.focus === 'function') { // inline buttons and menus are selectable before choosing button or menu from bar
        return candidate;
      }
    }
    if (firstDefaultButton) {
      return firstDefaultButton;
    } else if (firstButton) {
      if (firstButton !== firstElement && firstElement) {
        let $tabParentsButton = $(firstButton).parents('.tab-box-header'),
          $firstItem = $(firstElement),
          $tabParentsFirstElement = $(firstElement).parents('.tab-box-header');
        if ($tabParentsFirstElement.length > 0 && $tabParentsButton.length > 0 && $firstItem.is('.tab-item')) {
          return firstElement;
        }
      }
      return firstButton;
    }
    return firstElement;
  }

  /**
   * Returns the currently active focus context, or null if not applicable.
   * @internal
   */
  _findActiveContext(): FocusContext {
    return arrays.last(this._focusContexts);
  }

  /**
   * Returns the focus context which is associated with the given $container, or null if not applicable.
   */
  getFocusContext($container: JQuery): FocusContext {
    return arrays.find(this._focusContexts, focusContext => focusContext.$container === $container);
  }

  /**
   * Focuses the next or previous valid element within the focus context of the given `activeElement`.
   */
  focusNextTabbable(activeElement: JQuery | HTMLElement, forward = true) {
    let focusContext = this._findFocusContextFor(activeElement);
    if (focusContext) {
      focusContext.focusNextTabbable(forward);
    }
  }

  protected _findFocusContextFor(element: JQuery | HTMLElement): FocusContext {
    let $element = $.ensure(element);
    let context = null;
    let distance = Number.MAX_VALUE;
    this._focusContexts.forEach(focusContext => {
      if (!focusContext.$container.isOrHas($element)) {
        return;
      }
      // Return the context which is closest to the element
      let length = $element.parentsUntil(focusContext.$container).length;
      if (length < distance) {
        context = focusContext;
      }
    });
    return context;
  }

  /**
   * Returns whether to accept a 'mousedown event'.
   */
  protected _acceptFocusChangeOnMouseDown($element: JQuery): boolean {
    // Prevent focus gain when glasspane is clicked.
    // Even if the glasspane is not focusable, this check is required because the glasspane might be contained in a focusable container
    // like table. Use case: outline modality with table-page as 'outlineContent'.
    if ($element.hasClass('glasspane')) {
      return false;
    }

    // Prevent focus gain if covert by glasspane.
    if (this.isElementCovertByGlassPane($element)) {
      return false;
    }

    // Prevent focus gain on elements excluded to gain focus by mouse, e.g. buttons.
    if (!focusUtils.isFocusableByMouse($element)) {
      return false;
    }

    // Allow focus gain on focusable elements.
    if ($element.is(':focusable')) {
      return true;
    }

    // Allow focus gain on elements with selectable content, e.g. the value of a label field.
    if (focusUtils.isSelectableText($element)) {
      return true;
    }

    // Allow focus gain on elements with a focusable parent, e.g. when clicking on a row in a table.
    if (focusUtils.containsParentFocusableByMouse($element, $element.entryPoint())) {
      return true;
    }

    // Don't prevent default action for draggable elements which is dragstart event
    if (focusUtils.isDraggable($element)) {
      // Unfortunately, preventDefault will not only prevent dragstart but also focus gain
      // If the draggable element is not focusable, we need to restore the focus later otherwise the desktop would be focused
      focusUtils.restoreFocusLater(this.session.$entryPoint);
      return true;
    }

    return false;
  }

  /**
   * Registers the given focus context, or moves it on top if already registered.
   * @internal
   */
  _pushIfAbsentElseMoveTop(focusContext: FocusContext) {
    arrays.remove(this._focusContexts, focusContext);
    this._focusContexts.push(focusContext);
  }
}
