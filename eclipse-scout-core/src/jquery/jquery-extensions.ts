import {IconDesc} from '../index';

declare global {

  interface JQuery {
    oneAnimationEnd(handler: () => void): JQuery;

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

    setTabbable(tabbable: boolean)
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
