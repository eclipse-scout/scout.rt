interface JQuery {
  oneAnimationEnd(handler: () => void): JQuery;

  appendDiv(cssClass?: string, text?: string): JQuery;

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
}

interface JQueryStatic {
  log: NullLogger; // Importing this function will break extension, why? Module must not have any imports and exports, why?

  ensure(elem: JQuery | HTMLElement);

  abc(): void;

  resolvedPromise(): JQuery.Promise<any>;
}
