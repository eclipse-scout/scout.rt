/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, IconDesc, Logger, OldWheelEvent, Point, Predicate, ResizableModel} from '../index';
import $ from 'jquery';
import Deferred = JQuery.Deferred;

export interface InjectOptions {
  /**
   * Which document to inject the script to. Default is window.document
   */
  document: Document;
}

export interface InjectScriptOptions extends InjectOptions {
  /**
   * Whether to remove the script tag again from the DOM after the script has been loaded. Default is false.
   */
  removeTag: boolean;
}

export type AppLinkBeanArgument = { ref: string; name: string } | string;

export type AppLinkFuncArgument<T> = JQuery.TypeEventHandler<T, undefined, T, T, 'click'> | { _onAppLinkAction: JQuery.TypeEventHandler<T, undefined, T, T, 'click'> };

export type JQueryMouseWheelEvent<TDelegateTarget = any, TData = any, TCurrentTarget = any, TTarget = any> = JQuery.TriggeredEvent<TDelegateTarget, TData, TCurrentTarget, TTarget> & { originalEvent: OldWheelEvent };

declare global {

  interface DebounceOptions {
    /**
     * Waiting time in milliseconds before the function is executed. Default is 250.
     */
    delay?: number;
    /**
     * Defines whether subsequent call to the debounced function within the waiting time cause the timer to be reset or not.
     * If the reschedule option is 'false', subsequent calls within the waiting time will just be ignored.
     * Default is true.
     */
    reschedule: boolean;
  }

  interface JQueryStatic {
    log: Logger; // Importing this function will break extension, why? Module must not have any imports and exports, why?

    /**
     * Ensures the given parameter is a jQuery object.<p>
     * If it is a jQuery object, it will be returned as it is.
     * If it isn't, it will be passed to $() in order to create one.
     * <p>
     * Just using $() on an existing jQuery object would clone it which would work but is unnecessary.
     */
    ensure(elem: JQuery | HTMLElement): JQuery;

    /**
     * Convenience function that can be used as an jQuery event handler, when this
     * event should be "swallowed". Technically, this function calls preventDefault(),
     * stopPropagation() and stopImmediatePropagation() on the event.
     *
     * Note: "return false" is equal to preventDefault() and stopPropagation(), but
     * not stopImmediatePropagation().
     */
    suppressEvent(event: JQuery.Event): void;

    /**
     * Implements the 'debounce' pattern. The given function fx is executed after a certain delay
     * (in milliseconds), but if the same function is called a second time within the waiting time,
     * depending on the 'reschedule' option, the timer is reset or the second call is ignored.
     * The default value for the 'delay' option is 250 ms.
     *
     * The resulting function has a function member 'cancel' that can be used to clear any scheduled
     * calls to the original function. If no such call was scheduled, cancel() returns false,
     * otherwise true.
     *
     * @param fx the function to wrap
     * @param options an optional options object. Short-hand version: If a number is passed instead
     *          of an object, the value is automatically converted to the option 'delay'.
     */
    debounce(fx: (...args: any[]) => void, options?: DebounceOptions | number): ((...args: any[]) => void) & { cancel(): boolean };

    /**
     * Executes the given function. Further calls to the same function are delayed by the given delay.
     * This is similar to $.debounce() but ensures that the function is called at least
     * every 'delay' milliseconds. Can be useful to prevent too many function calls, e.g. from UI events.
     * @param fx the function to wrap
     * @param delay how much the function calls should be delayed. Default is 250.
     */
    throttle(fx: (...args: any[]) => unknown, delay?: number): (...args: any[]) => unknown;

    /**
     * Returns a function which negates the return value of the given function when called.
     */
    negate(fx: (...args: any[]) => unknown): ((...args: any[]) => boolean);

    /**
     * CSP-safe method to dynamically load and execute a script from server.
     *
     * A new <script> tag is added to the document's head element. The methods returns
     * a promise which can be used to execute code after the loading has been completed.
     * A jQuery object referring to the new script tag is passed to the promise's
     * callback functions.
     *
     *   $.injectScript('http://server/path/script.js')
     *     .done(function($scriptTag) { ... });
     */
    injectScript(url: string, options?: InjectScriptOptions): JQuery.Promise<JQuery>;

    /**
     * CSP-safe method to dynamically load a style sheet from server.
     *
     * A new <link> tag is added to the document's head element. The methods returns
     * a promise which can be used to execute code after the loading has been completed.
     * A jQuery object referring to the new link tag is passed to the promise's
     * callback functions.
     *
     *   $.injectStyleSheet('http://server/path/style.css')
     *     .done(function($linkTag) { ... });
     */
    injectStyleSheet(url: string, options?: InjectOptions): JQuery.Promise<JQuery>;

    /**
     * Dynamically adds styles to the document.
     *
     * A new <style> tag is added to the document's head element. The methods returns
     * a jQuery object referring to the new style tag.
     *
     *   $styleTag = $.injectStyle('p { text-color: orange; }');
     *
     */
    injectStyle(data: string, options?: InjectOptions): JQuery.Promise<JQuery>;

    /**
     * Converts a string containing 'px' to a number by removing the 'px'.
     * Returns 0 if the string is empty or undefined.
     */
    pxToNumber(pixel: string): number;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve(...args);</code>
     *
     * @param args arguments of this function are passed to the resolve function of the {@link Deferred}.
     * @returns an already resolved {@link Deferred}.
     */
    resolvedDeferred(...args: any[]): Deferred<any>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve(arg);</code>
     *
     * @param arg the argument to pass to the resolve function of the {@link Deferred}.
     * @returns an already resolved {@link Deferred}.
     */
    resolvedDeferred<TR>(arg: TR): Deferred<TR, never, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve(arg).promise();</code>
     *
     * @param arg passed to the resolve function of the deferred
     * @returns a {@link JQuery.Promise} for an already resolved {@link Deferred} object.
     */
    resolvedPromise<TR>(arg: TR): JQuery.Promise<TR, never, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve(arg1, arg2).promise();</code>
     *
     * @param arg1 passed to the resolve function of the deferred
     * @param arg2 passed to the resolve function of the deferred
     * @returns a {@link JQuery.Promise} for an already resolved {@link Deferred} object.
     */
    resolvedPromise<TR, UR>(arg1: TR, arg2: UR): JQuery.Promise2<TR, never, never, UR, never, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve(arg1, arg2, arg3).promise();</code>
     *
     * @param arg1 passed to the resolve function of the deferred
     * @param arg2 passed to the resolve function of the deferred
     * @param arg3 passed to the resolve function of the deferred
     * @returns a {@link JQuery.Promise} for an already resolved {@link Deferred} object.
     */
    resolvedPromise<TR, UR, VR>(arg1: TR, arg2: UR, arg3: VR): JQuery.Promise3<TR, never, never, UR, never, never, VR, never, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve(arg1, arg2, arg3, ...args).promise();</code>
     *
     * @param arg1 passed to the resolve function of the deferred
     * @param arg2 passed to the resolve function of the deferred
     * @param arg3 passed to the resolve function of the deferred
     * @param args remaining arguments passed to the resolve function of the deferred
     * @returns a {@link JQuery.Promise} for an already resolved {@link Deferred} object.
     */
    resolvedPromise<TR, UR, VR, SR>(arg1: TR, arg2: UR, arg3: VR, ...args: SR[]): JQuery.PromiseBase<TR, never, never, UR, never, never, VR, never, never, SR, never, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve(...args).promise();</code>
     *
     * @param args arguments passed to the resolve function of the deferred
     * @returns a {@link JQuery.Promise} for an already resolved {@link Deferred} object.
     */
    resolvedPromise(...args: any[]): JQuery.Promise<any, never, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().reject(arg).promise();</code>
     *
     * @param arg passed to the reject function of the {@link Deferred}.
     * @returns a {@link JQuery.Promise} for an already rejected {@link Deferred}.
     */
    rejectedPromise<TJ>(arg: TJ): JQuery.Promise<never, TJ, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().reject(arg1, arg2).promise();</code>
     *
     * @param arg1 passed to the reject function of the {@link Deferred}.
     * @param arg2 passed to the reject function of the {@link Deferred}.
     * @returns a {@link JQuery.Promise} for an already rejected {@link Deferred}.
     */
    rejectedPromise<TJ, UJ>(arg1: TJ, arg2: UJ): JQuery.Promise2<never, TJ, never, never, UJ, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().reject(arg1, arg2, arg3).promise();</code>
     *
     * @param arg1 passed to the reject function of the {@link Deferred}.
     * @param arg2 passed to the reject function of the {@link Deferred}.
     * @param arg3 passed to the reject function of the {@link Deferred}.
     * @returns a {@link JQuery.Promise} for an already rejected {@link Deferred}.
     */
    rejectedPromise<TJ, UJ, VJ>(arg1: TJ, arg2: UJ, arg3: VJ): JQuery.Promise3<never, TJ, never, never, UJ, never, never, VJ, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().reject(arg1, arg2, arg3, ...args).promise();</code>
     *
     * @param arg1 passed to the reject function of the {@link Deferred}.
     * @param arg2 passed to the reject function of the {@link Deferred}.
     * @param arg3 passed to the reject function of the {@link Deferred}.
     * @param args remaining arguments passed to the reject function of the {@link Deferred}.
     * @returns a {@link JQuery.Promise} for an already rejected {@link Deferred}.
     */
    rejectedPromise<TJ, UJ, VJ, SJ>(arg1: TJ, arg2: UJ, arg3: VJ, ...args: SJ[]): JQuery.PromiseBase<never, TJ, never, never, UJ, never, never, VJ, never, never, SJ, never>;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().reject(...args).promise();</code>
     *
     * @param args arguments passed to the reject function of the {@link Deferred}.
     * @returns a {@link JQuery.Promise} for an already rejected {@link Deferred}.
     */
    rejectedPromise(...args: any[]): JQuery.Promise<any>;

    /**
     * Creates a new promise which resolves when all promises resolve and fails when the first promise fails.
     *
     * @param promises the promises to wait for.
     * @param asArray when set to true, the resolve function will transform the
     *    flat arguments list containing the results into an array. The arguments of the reject function won't be touched. Default is false.
     */
    promiseAll(promises: JQuery.PromiseBase<any, any, any, any, any, any, any, any, any, any, any, any>[], asArray?: boolean): JQuery.Promise<any>;

    /**
     * Shorthand for an AJAX request for a JSON file with UTF8 encoding.
     * Errors are caught and converted to a rejected promise with the following
     * arguments: jqXHR, textStatus, errorThrown, requestOptions.
     *
     * @returns a promise from JQuery function $.ajax
     */
    ajaxJson(url: string): JQuery.jqXHR;

    /**
     * Helper function to determine if an object is of type "jqXHR" (http://api.jquery.com/jQuery.ajax/#jqXHR)
     */
    isJqXHR(obj: unknown): obj is JQuery.jqXHR;
  }

  interface JQuery<TElement = HTMLElement> extends Array<TElement | JQuery> {
    /**
     * @param $element returns the given element if the current jquery object does not contain any elements.
     * Otherwise returns the current jquery object.
     */
    nvl($element: JQuery): JQuery;

    /**
     * Creates a new HTMLElement based on the given html snippet and creates a new JQuery object.
     * The element is created using the current document.
     *
     * @param element HTML snippet, example = &lt;input&gt;
     * @param cssClass class attribute
     * @param text adds a child text-node with given text (no HTML content)
     */
    makeElement(element: string, cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new DIV element using the current document.
     *
     * @param cssClass string added to the 'class' attribute
     * @param text string used as inner text
     */
    makeDiv(cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new SPAN element using the current document.
     *
     * @param cssClass string added to the 'class' attribute
     * @param text string used as inner text
     */
    makeSpan(cssClass?: string, text?: string): JQuery<HTMLSpanElement>;

    /**
     * @returns document reference (ownerDocument) of the HTML element.
     */
    document(): JQuery<Document>;

    /**
     * @param domElement if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
     * @returns document reference (ownerDocument) of the HTML element.
     */
    document<T extends boolean>(domElement?: T): T extends true ? Document : JQuery<Document>;

    /**
     * @returns window reference (defaultView) of the HTML element
     */
    window(): JQuery<Window>;

    /**
     * @param domElement if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
     * @returns window reference (defaultView) of the HTML element
     */
    window<T extends boolean>(domElement?: T): T extends true ? Window : JQuery<Window>;

    /**
     * @returns the BODY element of the HTML document in which the current HTML element is placed.
     */
    body(): JQuery<Body>;

    /**
     * @param domElement if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
     * @returns the BODY element of the HTML document in which the current HTML element is placed.
     */
    body<T extends boolean>(domElement?: T): T extends true ? Body : JQuery<Body>;

    /**
     * @returns the closest DOM element that has the 'scout' class.
     */
    entryPoint(): JQuery;

    /**
     * @param domElement if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
     * @returns the closest DOM element that has the 'scout' class.
     */
    entryPoint<T extends boolean>(domElement?: T): T extends true ? HTMLElement : JQuery;

    /**
     * @returns the active element of the current document
     */
    activeElement(): JQuery;

    /**
     * @param domElement if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
     * @returns the active element of the current document
     */
    activeElement<T extends boolean>(domElement?: T): T extends true ? HTMLElement : JQuery;

    /**
     * @returns size of the window (width and height)
     */
    windowSize(): Dimension;

    /**
     * Returns the element at the given point considering only child elements and elements matching the selector, if specified.
     */
    elementFromPoint(x: number, y: number, selector?: JQuery.Selector): JQuery;

    /**
     * Creates a new HTML element and prepends it to the current element.
     */
    prependElement(element: string, cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new HTML element and appends it to the current element.
     */
    appendElement(element: string, cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new DIV and prepends it to the current element.
     */
    prependDiv(cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new DIV and appends it to the current element.
     */
    appendDiv(cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new DIV and adds it after the current element.
     */
    afterDiv(cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new DIV and adds it before current element.
     */
    beforeDiv(cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new SPAN and appends it to the current element.
     */
    appendSpan(cssClass?: string, text?: string): JQuery<HTMLSpanElement>;

    /**
     * Creates a new BR and appends it to the current element.
     */
    appendBr(cssClass?: string): JQuery<HTMLBRElement>;

    /**
     * Creates a new TABLE and appends it to the current element.
     */
    appendTable(cssClass?: string): JQuery<HTMLTableElement>;

    /**
     * Creates a new COLGROUP and appends it to the current element.
     */
    appendColgroup(cssClass?: string): JQuery;

    /**
     * Creates a new COL and appends it to the current element.
     */
    appendCol(cssClass?: string): JQuery<HTMLTableColElement>;

    /**
     * Creates a new TR and appends it to the current element.
     */
    appendTr(cssClass?: string): JQuery<HTMLTableRowElement>;

    /**
     * Creates a new TD and appends it to the current element.
     */
    appendTd(cssClass?: string, text?: string): JQuery<HTMLTableCellElement>;

    /**
     * Creates a new TH and appends it to the current element.
     */
    appendTh(cssClass?: string, text?: string): JQuery;

    /**
     * Creates a new UL and appends it to the current element.
     */
    appendUl(cssClass?: string): JQuery<HTMLUListElement>;

    /**
     * Creates a new LI and appends it to the current element.
     */
    appendLi(cssClass?: string, text?: string): JQuery<HTMLLIElement>;

    /**
     * Creates a new text node and appends it to the current element.
     */
    appendTextNode(cssClass?: string): JQuery;

    /**
     * Creates a new HTML element containing a font icon or an image and appends it to the current element.
     */
    appendIcon(iconId: IconDesc | string, cssClass?: string): JQuery<HTMLSpanElement> | JQuery<HTMLImageElement>;

    /**
     * Prepends a SPAN for font icons or an IMG for image urls or updates an existing SPAN or IMG if this method has already been called once.
     *
     * @param iconId
     * @param addToDomFunc optional function which is used to add the new icon element to the DOM
     *     When not set, this.prepend($icon) is called.
     * @see Icon as an alternative
     */
    icon(iconId: string, addToDomFunc?: ($icon: JQuery) => void): this;

    /**
     * Creates a new IMG pointing to the given imageSrc and appends it to the current element.
     */
    appendImg(imageSrc: string, cssClass?: string): JQuery<HTMLImageElement>;

    makeSVG(type: string, cssClass?: string, text?: string, id?: string): JQuery<SVGElement>;

    /**
     * Creates a new SVG element and appends it to the current element.
     */
    appendSVG(type: string, cssClass?: string, text?: string, id?: string): JQuery<SVGElement>;

    attrXLINK(attributeName: string, value: string): JQuery<TElement>;

    /**
     * Appends a span and converts it to an app link.
     * @see appLink
     */
    appendAppLink(appLinkBean: AppLinkBeanArgument, func?: AppLinkFuncArgument<TElement>): this;

    /**
     * Converts the current html element to an app link by adding the required class and attributes.
     * Also registers the given handler which will be called when the app link is activated.
     *
     * @param appLinkBean
     *          Either
     *           - an AppLinkBean with both (1) a ref attribute which will be mapped to the
     *             data-ref attribute of the element and (2) a name attribute which will be
     *             set as the text of the element.
     *           - or just a ref, which will be mapped to the data-ref attribute of the
     *             element.
     * @param func
     *          Either
     *           - a function to be called when the app link has been clicked
     *           - or an object with a method named _onAppLinkAction (e.g. an instance of
     *             BeanField)
     *          If func is not set, the _onAppLinkAction of the inner most widget relative to
     *          this element (if any) will be called when the app link has been clicked.
     */
    appLink(appLinkBean: AppLinkBeanArgument, func?: AppLinkFuncArgument<TElement>): this;

    /**
     * This function adds a device specific CSS class to the current element.
     * The current implementation adds a class 'ios' if it is an ios device.
     */
    addDeviceClass(): this;

    /**
     * Adds the class 'selected' to the current element and removes class selected from siblings.
     */
    selectOne(): this;

    /**
     * Toggles the class 'selected'.
     */
    select(selected?: boolean): this;

    /**
     * @returns true if the current element has the class 'selected', false if not.
     */
    isSelected(): boolean;

    /**
     * Toggles the class 'disabled'. Also toggles 'disabled' attribute for elements that support it (see http://www.w3.org/TR/html5/disabled-elements.html)
     */
    setEnabled(enabled: boolean): this;

    /**
     * @returns true if the current element doesn't have the class 'disabled', false otherwise.
     */
    isEnabled(): boolean;

    /**
     * Toggles the class 'hidden'. Also triggers the events 'hide' and 'show', if the state changed.
     */
    setVisible(visible: boolean): this;

    /**
     * @returns true if the current element doesn't have the class 'hidden', false otherwise.
     */
    isVisible(): boolean;

    /**
     * @returns true if the current element and every parent are visible (=don't have the class 'hidden'), false otherwise.
     */
    isEveryParentVisible(): boolean;

    /**
     * returns true of the current element has the 'display' property set to 'none'.
     */
    isDisplayNone(): boolean;

    /**
     * @param tabbable true, to make the component tabbable. False, to make it neither tabbable nor focusable.
     * @returns {$}
     */
    setTabbable(tabbable: boolean): this;

    /**
     * @param tabbable true, to make the component tabbable. False, to make it not tabbable but focusable, so the user can focus it with the mouse but not with the keyboard.
     */
    setTabbableOrFocusable(tabbable: boolean): this;

    /**
     * @returns true, if the current element has a 'tabIndex' greater than or equal to 0.
     */
    isTabbable(): boolean;

    /**
     * Sets the attribute 'placeholder' with the given text. Removes the attribute, if no text is provided.
     */
    placeholder(placeholder: string): this;

    /**
     * @returns true if the element is attached (= is in the dom tree), false if not
     */
    isAttached(): boolean;

    /**
     * @returns the current element if it is scrollable, otherwise the first parent that is scrollable.
     */
    scrollParent(): JQuery;

    /**
     * @returns every parent that is scrollable
     */
    scrollParents(): JQuery;

    /**
     * Similar to {@link JQuery.closest} but with a predicate function and the ability to stop at a given element.
     * @param predicate the search predicate
     * @param $stop if provided, the search is done until this element is reached.
     */
    findUp(predicate: Predicate<JQuery>, $stop?: JQuery): JQuery;

    /**
     * @returns whether the current element is the given element or has a child which is the given element.
     */
    isOrHas(elem: JQuery | Element): boolean;

    animateAVCSD(attr: string, value, complete?: (elem: JQuery) => void, step?: (elem: JQuery) => void, duration?: number): this;

    /**
     * Animates the attribute to the given end value. When the animation is completed, the given handler will be executed.
     * The animation will add the property <code>tabIndex</code> if it does not exist.
     *
     * @param attr The name of the attribute.
     * @param endValue The end value of the animation.
     * @param duration A number determining how long the animation will run.
     * @param complete A function that is called once the animation is complete.
     * @param withoutTabIndex Whether the property <code>tabIndex</code> should be removed after the animation or not.
     */
    animateSVG(attr: string, endValue: number, duration: number, complete: (HTMLElement) => void, withoutTabIndex: boolean): this;

    /**
     * Adds a class which will animate the element. When the animation finishes, the class will be removed again.
     * Removing the class again is necessary, otherwise the animation will be executed each time the element changes its visibility (attach/rerender).
     * and even each time when the css classes change.
     * @param className
     * @param options
     * @param options.classesToRemove The classes to remove after the animation. Default is the given class that started the animation.
     */
    addClassForAnimation(className: string, options?: { classesToRemove?: boolean }): this;

    /**
     * Adds a handler that is executed when a CSS animation ends on the current element. It will be executed
     * only once when the 'animationend' event is triggered on the current element. Bubbling events from child
     * elements are ignored.
     *
     * @param handler A function to execute when the 'animationend' event is triggered
     */
    oneAnimationEnd(handler: JQuery.TypeEventHandler<TElement, undefined, TElement, TElement, 'animationend'>): JQuery;

    /**
     * @returns true, if the current element has a class that starts with 'animate-'.
     */
    hasAnimationClass(): boolean;

    /**
     * Animates from old to new width. The default duration is set to 300ms.
     */
    cssWidthAnimated(oldWidth: number, newWidth: number, options: JQuery.EffectsOptions<TElement>);

    /**
     * Animates from old to new height. The default duration is set to 300ms.
     */
    cssHeightAnimated(oldHeight: number, newHeight: number, options: JQuery.EffectsOptions<TElement>);

    /**
     * Animates the left property from old to new left. The default duration is set to 300ms.
     */
    cssLeftAnimated(from: number, to: number, options: JQuery.EffectsOptions<TElement>): this;

    /**
     * Animates the top property from old to new top. The default duration is set to 300ms.
     */
    cssTopAnimated(from: number, to: number, options: JQuery.EffectsOptions<TElement>): this;

    /**
     * Animates the given properties from old to new. The default duration is set to 300ms.
     */
    cssAnimated<T extends JQuery.PlainObject<string | number>>(fromValues: T, toValues: T, options: JQuery.EffectsOptions<TElement>): this;

    /**
     * Animates the width of the current element to its preferred width.
     */
    cssWidthToContentAnimated(options: JQuery.EffectsOptions<TElement>);

    /**
     * Offset to a specific ancestor and not to the document as offset() would do.
     * Not the same as position() which returns the position relative to the offset parent.
     */
    offsetTo($to: JQuery): JQuery.Coordinates;

    /**
     * @returns the value of the given property converted to a number
     */
    cssPxValue(prop: string): number;

    /**
     * Sets the value of the given property. Adds 'px' if the value is a number.
     */
    cssPxValue(prop: string, value: number | string): this;

    /**
     * Sets left property to the given value.
     * @see cssPxValue
     */
    cssLeft(position: number | string): this;

    /**
     * @returns the value of the left property.
     * @see cssPxValue
     */
    cssLeft(): number;

    /**
     * Sets top property to the given value.
     * @see cssPxValue
     */
    cssTop(position: number | string): this;

    /**
     * @returns the value of the top property.
     * @see cssPxValue
     */
    cssTop(): number;

    /**
     * Sets the CSS properties 'left' and 'top' based on the x and y properties of the given point instance.
     */
    cssPosition(position: Point): this;

    /**
     * Sets bottom property to the given value.
     * @see cssPxValue
     */
    cssBottom(position: number | string): this;

    /**
     * @returns the value of the bottom property.
     * @see cssPxValue
     */
    cssBottom(): number;

    /**
     * Sets right property to the given value.
     * @see cssPxValue
     */
    cssRight(position: number | string): this;

    /**
     * @returns the value of the right property.
     * @see cssPxValue
     */
    cssRight(): number;

    /**
     * Sets width property to the given value.
     * @see cssPxValue
     */
    cssWidth(width: number | string): this;

    /**
     * @returns the value of the width property.
     * @see cssPxValue
     */
    cssWidth(): number;

    /**
     * Sets min-width property to the given value.
     * @see cssPxValue
     */
    cssMinWidth(width: number | string): this;

    /**
     * @returns the value of the min-width property. Returns 0 if min-width is set to auto or contains %
     * @see cssPxValue
     */
    cssMinWidth(): number;

    /**
     * Sets max-width property to the given value.
     * @see cssPxValue
     */
    cssMaxWidth(width: number | string): this;

    /**
     * @returns the value of the max-width property. If max-width is not set (resp. defaults to 'none') or contains %, Number.MAX_VALUE is returned.
     * @see cssPxValue
     */
    cssMaxWidth(): number;

    /**
     * @returns the value of the height property.
     * @see cssPxValue
     */
    cssHeight(): number;

    /**
     * Sets min-height property to the given value.
     * @see cssPxValue
     */
    cssHeight(height: number | string): this;

    /**
     * Sets min-height property to the given value.
     * @see cssPxValue
     */
    cssMinHeight(height: number | string): this;

    /**
     * @returns the value of the min-height property. Returns 0 if min-height is set to auto or contains %
     * @see cssPxValue
     */
    cssMinHeight(): number;

    /**
     * Sets max-height property to the given value.
     * @see cssPxValue
     */
    cssMaxHeight(height: number | string): this;

    /**
     * @returns the value of the max-height property. If max-height is not set (resp. defaults to 'none') or contains %, Number.MAX_VALUE is returned.
     * @see cssPxValue
     */
    cssMaxHeight(): number;

    /**
     * Sets lineHeight property to the given value.
     * @see cssPxValue
     */
    cssLineHeight(lineHeight: number | string): this;

    /**
     * @returns the value of the lineHeight property.
     * @see cssPxValue
     */
    cssLineHeight(): number;

    /**
     * Sets marginLeft property to the given value.
     * @see cssPxValue
     */
    cssMarginLeft(marginLeft: number | string): this;

    /**
     * @returns the value of the marginLeft property.
     * @see cssPxValue
     */
    cssMarginLeft(): number;

    /**
     * Sets marginBottom property to the given value.
     * @see cssPxValue
     */
    cssMarginBottom(marginBottom: number | string): this;

    /**
     * @returns the value of the marginBottom property.
     * @see cssPxValue
     */
    cssMarginBottom(): number;

    /**
     * Sets marginRight property to the given value.
     * @see cssPxValue
     */
    cssMarginRight(marginRight: number | string): this;

    /**
     * @returns the value of the marginRight property.
     * @see cssPxValue
     */
    cssMarginRight(): number;

    /**
     * Sets marginTop property to the given value.
     * @see cssPxValue
     */
    cssMarginTop(marginTop: number | string): this;

    /**
     * @returns the value of the marginTop property.
     * @see cssPxValue
     */
    cssMarginTop(): number;

    /**
     * Sets marginLeft and marginRight to the given value.
     * @see cssPxValue
     */
    cssMarginX(marginX: number | string): this;

    /**
     * @returns the sum of marginLeft and marginBottom.
     * @see cssPxValue
     */
    cssMarginX(): number;

    /**
     * Sets marginTop and marginBottom to the given value.
     * @see cssPxValue
     */
    cssMarginY(marginY: number | string): this;

    /**
     * @returns the sum of marginTop and marginBottom.
     * @see cssPxValue
     */
    cssMarginY(): number;

    /**
     * Sets paddingLeft property to the given value.
     * @see cssPxValue
     */
    cssPaddingLeft(paddingLeft: number | string): this;

    /**
     * @returns the value of the paddingLeft property.
     * @see cssPxValue
     */
    cssPaddingLeft(): number;

    /**
     * Sets paddingBottom property to the given value.
     * @see cssPxValue
     */
    cssPaddingBottom(paddingBottom: number | string): this;

    /**
     * @returns the value of the paddingBottom property.
     * @see cssPxValue
     */
    cssPaddingBottom(): number;

    /**
     * Sets paddingRight property to the given value.
     * @see cssPxValue
     */
    cssPaddingRight(paddingRight: number | string): this;

    /**
     * @returns the value of the paddingRight property.
     * @see cssPxValue
     */
    cssPaddingRight(): number;

    /**
     * Sets paddingTop property to the given value.
     * @see cssPxValue
     */
    cssPaddingTop(paddingTop: number | string): this;

    /**
     * @returns the value of the paddingTop property.
     * @see cssPxValue
     */
    cssPaddingTop(): number;

    /**
     * Sets paddingLeft and paddingRight to the given value.
     * @see cssPxValue
     */
    cssPaddingX(paddingX: number | string): this;

    /**
     * @returns the sum of paddingLeft and paddingBottom.
     * @see cssPxValue
     */
    cssPaddingX(): number;

    /**
     * Sets paddingTop and paddingBottom to the given value.
     * @see cssPxValue
     */
    cssPaddingY(paddingY: number | string): this;

    /**
     * @returns the sum of paddingTop and paddingBottom.
     * @see cssPxValue
     */
    cssPaddingY(): number;

    /**
     * Sets borderLeft property to the given value.
     * @see cssPxValue
     */
    cssBorderLeftWidth(borderLeft: number | string): this;

    /**
     * @returns the value of the borderLeft property.
     * @see cssPxValue
     */
    cssBorderLeftWidth(): number;

    /**
     * Sets borderBottom property to the given value.
     * @see cssPxValue
     */
    cssBorderBottomWidth(borderBottom: number | string): this;

    /**
     * @returns the value of the borderBottom property.
     * @see cssPxValue
     */
    cssBorderBottomWidth(): number;

    /**
     * Sets borderRight property to the given value.
     * @see cssPxValue
     */
    cssBorderRightWidth(borderRight: number | string): this;

    /**
     * @returns the value of the borderRight property.
     * @see cssPxValue
     */
    cssBorderRightWidth(): number;

    /**
     * Sets borderTop property to the given value.
     * @see cssPxValue
     */
    cssBorderTopWidth(borderTop: number | string): this;

    /**
     * @returns the value of the borderTop property.
     * @see cssPxValue
     */
    cssBorderTopWidth(): number;

    /**
     * Sets borderLeft and borderRight to the given value.
     * @see cssPxValue
     */
    cssBorderWidthX(borderX: number | string): this;

    /**
     * @returns the sum of borderLeft and borderBottom.
     * @see cssPxValue
     */
    cssBorderWidthX(): number;

    /**
     * Sets borderTop and borderBottom to the given value.
     * @see cssPxValue
     */
    cssBorderWidthY(borderY: number | string): this;

    /**
     * @returns the sum of borderTop and borderBottom.
     * @see cssPxValue
     */
    cssBorderWidthY(): number;

    /**
     * Bottom of a html element without margin and border relative to offset parent. Expects border-box model.
     */
    innerBottom(): number;

    /**
     * Right of a html element without margin and border relative to offset parent. Expects border-box model.
     */
    innerRight(): number;

    /**
     * Copies the properties from the other element to the current element.
     */
    copyCss($other: JQuery, props: string): this;

    /**
     * Adds the given css classes to the current element but only those that are on the other element as well.
     */
    copyCssClasses($other: JQuery, classString: string): this;

    /**
     * Sets the attribute spellcheck to false.
     */
    disableSpellcheck(): this;

    /**
     * Makes the current element resizable, which means DIVs for resize-handling are added to the DOM
     * in the E, SE and S of the element. This is primarily useful for (modal) dialogs.
     */
    resizable(model?: ResizableModel): this;

    /**
     * Removes the resize handles and event handlers in order to make the element un resizable again.
     */
    unresizable(): this;

    /**
     * Makes any element movable with the mouse. If the argument '$handle' is missing, the entire
     * element can be used as a handle.
     *
     * A callback function can be passed as second argument. The function is called for
     * every change of the draggable's position with an object as argument.
     */
    draggable($handle: JQuery, callback?: (position: { top: number; left: number }) => void): this;

    /**
     * Removes the mouse down handler which was added by draggable() in order to make it un draggable again.
     */
    undraggable($handle: JQuery): this;

    /**
     * Calls {@link JQuery.fadeOut} and then removes the element from the DOM.
     * @param duration duration of the animation. Default is 150 ms.
     * @param callback callback that is executed after the element has been removed.
     */
    fadeOutAndRemove(duration?: number, callback?: () => void): this;

    /**
     * Adds a css class that starts a CSS animation and removes the element after the animation finishes.
     *
     * @param cssClass the css class that starts the remove animation. Default is animate-remove.
     * @param callback callback that is executed after the element has been removed.
     */
    removeAnimated(cssClass?: string, callback?: () => void): void;

    removeAnimated(callback?: () => void): void;

    /**
     * Sets the given 'text' as text to the jQuery element, using the text() function (i.e. HTML is encoded automatically).
     * @see contentOrNbsp
     */
    textOrNbsp(text: string, emptyCssClass?: string): this;

    /**
     * Same as {@link textOrNbsp}, but with html (caller is responsible for encoding).
     * @see contentOrNbsp
     */
    htmlOrNbsp(html: string, emptyCssClass?: string): this;

    /**
     * Renders the given content as plain-text or HTML depending on the given htmlEnabled flag.
     * If the text does not contain any non-space characters, the text '&nbsp;' is set instead (using the html() function).
     * If an 'emptyCssClass' is provided, this CSS class is removed in the former and added in the later case.
     */
    contentOrNbsp(htmlEnabled: boolean, content: string, emptyCssClass?: string): this;

    /**
     * Like {@link JQuery.toggleClass}, this toggles a HTML attribute on a set of jquery elements.
     *
     * @param attr Name of the attribute to toggle.
     * @param state
     *          Specifies if the attribute should be added or removed (based on whether the argument is truthy or falsy).
     *          If this argument is not defined, the attribute is added when it exists, and vice-versa. If this behavior
     *          is not desired, explicitly cast the argument to a boolean using "!!".
     * @param value
     *          Value to use when adding the attribute.
     *          If this argument is not specified, 'attr' is used as value.
     */
    toggleAttr(attr: string, state?: boolean, value?): this;

    /**
     * If the given value is "truthy", it is set as attribute on the target. Otherwise, the attribute is removed.
     */
    attrOrRemove(attributeName: string, value): this;

    /**
     * Adds the class 'unfocusable' to current result set. The class is not used for styling purposes
     * but has a meaning to the FocusManager.
     */
    unfocusable(): this;

    /**
     * Select all text within an element, e.g. within a content editable div element.
     */
    selectAllText(): this;

    /**
     * Checks if content is truncated.
     */
    isContentTruncated(): boolean;

    onPassive<TType extends string>(eventType: TType, handler: JQuery.TypeEventHandler<TElement, undefined, TElement, TElement, TType>): this;

    offPassive<TType extends string>(eventType: TType, handler: JQuery.TypeEventHandler<TElement, undefined, TElement, TElement, TType>): this;
  }
}
