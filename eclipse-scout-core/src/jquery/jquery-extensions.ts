import {IconDesc} from '../index';
import * as $ from "jquery";

declare global {

  interface JQuery {
    nvl($element: JQuery);

    /**
     * Creates a new HTMLElement based on the given html snippet and creates a new JQuery object.
     * The element is created using the current document.
     *
     * @param element HTML snippet, example = &lt;input&gt;
     * @param cssClass class attribute
     * @param text adds a child text-node with given text (no HTML content)
     */
    makeElement(element: string, cssClass?: string, text?: string);

    /**
     * Creates a new DIV element using the current document.
     *
     * @param cssClass string added to the 'class' attribute
     * @param text string used as inner text
     */
    makeDiv(cssClass?: string, text?: string);

    /**
     * Creates a new SPAN element using the current document.
     *
     * @param cssClass string added to the 'class' attribute
     * @param text string used as inner text
     */
    makeSpan(cssClass?: string, text?: string);

    oneAnimationEnd(handler: () => void): JQuery;

    cssAnimated(fromValues, toValues, opts);

    appendDiv(cssClass?: string, text?: string): JQuery;

    appendSpan(cssClass?: string, text?: string): JQuery;

    appendIcon(iconId: IconDesc | string, cssClass?: string);

    isDisplayNone(): boolean;

    isVisible(): boolean;

    setVisible(visible: boolean): void;

    isEveryParentVisible(): boolean;

    isAttached(): boolean;

    setEnabled(enabled: boolean): void;

    window(domElement: boolean): JQuery | Window;

    document(domElement: boolean): JQuery | Document;

    isOrHas(JQuery): boolean;

    scrollParent(): JQuery;

    addDeviceClass(): void;

    setTabbable(tabbable: boolean);

    unfocusable();
  }

  interface JQueryStatic {
    log: Logger; // Importing this function will break extension, why? Module must not have any imports and exports, why?

    ensure(elem: JQuery | HTMLElement);

    abc(): void;

    /**
     * Use this function as shorthand of this:
     * <code>$.Deferred().resolve([arguments]).promise();</code>
     *
     * @param args of this function are passed to the resolve function of the deferred
     * @returns a promise for an already resolved jQuery.Deferred object.
     */
    resolvedPromise(...args): JQuery.Promise<any>;
  }

}

interface Logger {
  trace(...logArgs): void

  debug(...logArgs): void

  info(...logArgs): void

  warn(...logArgs): void

  error(...logArgs): void

  fatal(...logArgs): void

  isEnabledFor(): boolean

  isTraceEnabled(): boolean

  isDebugEnabled(): boolean

  isInfoEnabled(): boolean

  isWarnEnabled(): boolean

  isErrorEnabled(): boolean

  isFatalEnabled(): boolean
}
