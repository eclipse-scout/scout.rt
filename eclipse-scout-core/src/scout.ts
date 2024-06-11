/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {
  AdapterData, App, Device, GroupBox, Locale, locales, LogicalGridLayout, ModelAdapterLike, ObjectCreator, ObjectFactory, ObjectFactoryOptions, objects, ObjectType, ObjectUuidProvider, Session, strings, TileGrid, ValueField, Widget, widgets
} from './index';
import $ from 'jquery';

let $activeElements = null;

/**
 * The minimal model declaration (usually extends {@link ObjectModel}) as it would be used in a nested declaration (e.g. a {@link FormField} within a {@link GroupBox}).
 * The {@link objectType} is optional as sometimes it might be already given by the context (e.g. when passing a {@link MenuModel} to a method {@link insertMenu()} where the method sets a default {@link objectType} if missing).
 */
export type ModelOf<TObject> = TObject extends { model: infer TModel } ? TModel : object;
/**
 * Model used to initialize an object instance. Usually the same as {@link ModelOf} but with some minimal required properties (mandatory properties).
 * Typically, adds an e.g. {@link parent} or {@link session} property which needs to be present when initializing an already created instance.
 */
export type InitModelOf<TObject> = TObject extends { initModel: infer TInitModel } ? TInitModel : ModelOf<TObject>;
/**
 * Model required to create a new object as child of an existing. To identify the object an {@link objectType} is mandatory.
 * But as the properties required to initialize the instance are derived from the parent, no other mandatory properties are required.
 */
export type ChildModelOf<TObject> = ModelOf<TObject> & { objectType: ObjectType<TObject> };
/**
 * A full object model declaring all mandatory properties. Such models contain all information to create ({@link objectType}) and initialize (e.g. {@link parent}) a new object.
 */
export type FullModelOf<TObject> = InitModelOf<TObject> & ChildModelOf<TObject>;
/**
 * Represents an instance of an object or its minimal model ({@link ModelOf}).
 */
export type ObjectOrModel<T> = T | ModelOf<T>;
/**
 * Represents an instance of an object or its child model ({@link ChildModelOf}).
 */
export type ObjectOrChildModel<T> = T | ChildModelOf<T>;

export type Constructor<T = object> = new(...args: any[]) => T;
export type AbstractConstructor<T = object> = abstract new(...args: any[]) => T;

export interface ObjectWithType {
  objectType: string;
}

/**
 * Represents an object having an uuid.
 */
export interface ObjectWithUuid {
  /**
   * The identifier property of the object. This property alone may not be unique within a widget tree (e.g. if template widgets are used).
   * To get a more unique id use {@link uuidPath} instead.
   */
  uuid: string;

  /**
   * Computes a string consisting of the id of this object and its parent objects (if existing) to get a unique identifier within an object tree.
   *
   * Note: The returned id may not be unique within the application! E.g. if the same form is opened twice, its children will share the same ids.
   * @param useFallback Optional boolean specifying if a fallback identifier may be used or created in case an object has no specific identifier set. The fallback may be less stable. Default is true.
   */
  uuidPath(useFallback?: boolean): string;
}

export interface ObjectModel<TObject = object, TId = string> {
  objectType?: ObjectType<TObject>;
  id?: TId;
}

export interface ObjectWithUuidModel<TObject = object, TId = string> extends ObjectModel<TObject, TId> {
  /**
   * A unique identifier for the object. Typically, a new random UUID can be used.
   */
  uuid?: string;
}

export interface ReloadPageOptions {
  /**
   * If true, the page reload is not executed in the current thread but scheduled using setTimeout().
   * This is useful if the caller wants to execute some other code before reload. The default is false.
   */
  schedule?: boolean;
  /**
   * If true, the body is cleared first before reload. This is useful to prevent
   * showing "old" content in the browser until the new content arrives. The default is true.
   */
  clearBody?: boolean;
  /**
   * The new URL to load. If not specified, the current location is used (window.location).
   */
  redirectUrl?: string;
}

function create<T>(objectType: Constructor<T>, model?: InitModelOf<T>, options?: ObjectFactoryOptions): T;
function create<T>(model: FullModelOf<T>, options?: ObjectFactoryOptions): T;
function create(objectType: string, model?: object, options?: ObjectFactoryOptions): any;
function create(model: { objectType: string; [key: string]: any }, options?: ObjectFactoryOptions): any;
function create<T>(objectType: ObjectType<T> | FullModelOf<T>, model?: InitModelOf<T>, options?: ObjectFactoryOptions): T;

/**
 * Creates a new object instance.
 *
 * Delegates the create call to {@link ObjectFactory.create}.
 */
function create<T extends object>(objectType: ObjectType<T> | FullModelOf<T>, model?: InitModelOf<T>, options?: ObjectFactoryOptions): T {
  return ObjectFactory.get().create(objectType, model, options);
}

function widget<T extends Widget>(widgetIdOrElement: string | number | HTMLElement | JQuery, partIdOrType?: string | Constructor<T>): T;
function widget(widgetIdOrElement: string | number | HTMLElement | JQuery, partId?: string): Widget;

/**
 * Resolves the widget using the given widget id or HTML element.
 *
 * If the argument is a string or a number, it will search the widget hierarchy for the given id using {@link Widget.widget}.
 * If the argument is a {@link HTMLElement} or {@link JQuery} element, it will use {@link widgets.get(elem)} to get the widget which belongs to the given element.
 *
 * @param widgetIdOrElement
 *          a widget ID or an HTML or jQuery element
 * @param partId
 *          partId of the session the widget belongs to (optional, only relevant if the
 *          argument is a widget ID). If omitted, the first session is used.
 * @returns the widget for the given element or id
 */
function widget<T extends Widget>(widgetIdOrElement: string | number | HTMLElement | JQuery, partIdOrType?: string | Constructor<T>): T {
  if (objects.isNullOrUndefined(widgetIdOrElement)) {
    return null;
  }
  let $elem = widgetIdOrElement;
  if (typeof widgetIdOrElement === 'string' || typeof widgetIdOrElement === 'number') {
    // Find widget for ID
    let partId = typeof partIdOrType === 'string' ? partIdOrType : null;
    let session = scout.getSession(partId);
    if (session) {
      let id = strings.asString(widgetIdOrElement);
      return session.root.widget(id) as T;
    }
  }
  return widgets.get($elem as (HTMLElement | JQuery)) as T;
}

export const scout = {
  objectFactories: new Map<string | Constructor, ObjectCreator>(),

  /**
   * Returns the first of the given arguments that is not null or undefined. If no such element
   * is present, the last argument is returned. If no arguments are given, undefined is returned.
   */
  nvl(...args: any[]): any {
    let result;
    for (let i = 0; i < args.length; i++) {
      result = args[i];
      if (result !== undefined && result !== null) {
        break;
      }
    }
    return result;
  },

  /**
   * Use this method in your functions to assert that a mandatory parameter is passed
   * to the function. Throws an error when value is not set.
   *
   * @param type if this optional parameter is set, the given value must be of this type (instanceof check)
   * @returns the value (for direct assignment)
   */
  assertParameter<T>(parameterName: string, value?: T, type?: AbstractConstructor | Constructor): T {
    if (objects.isNullOrUndefined(value)) {
      throw new Error('Missing required parameter \'' + parameterName + '\'');
    }
    if (type && !(value instanceof type)) {
      throw new Error('Parameter \'' + parameterName + '\' has wrong type');
    }
    return value;
  },

  /**
   * Use this method to assert that a mandatory property is set. Throws an error when value is not set.
   *
   * @param type if this parameter is set, the value must be of this type (instanceof check)
   * @returns the value (for direct assignment)
   */
  assertProperty(object: object, propertyName: string, type?: AbstractConstructor) {
    let value = object[propertyName];
    if (objects.isNullOrUndefined(value)) {
      throw new Error('Missing required property \'' + propertyName + '\'');
    }
    if (type && !(value instanceof type)) {
      throw new Error('Property \'' + propertyName + '\' has wrong type');
    }
    return value;
  },

  /**
   * Throws an error if the given value is null or undefined. Otherwise, the value is returned.
   *
   * @param value value to check
   * @param msg optional error message when the assertion fails
   */
  assertValue<T>(value: T, msg?: string): T {
    if (objects.isNullOrUndefined(value)) {
      throw new Error(msg || 'Missing value');
    }
    return value;
  },

  /**
   * Throws an error if the given value is not an instance of the given type. Otherwise, the value is returned.
   *
   * @param value value to check
   * @param type type to check against with "instanceof"
   * @param msg optional error message when the assertion fails
   */
  assertInstance<T>(value: any, type: AbstractConstructor<T>, msg?: string): T {
    if (!(value instanceof type)) {
      throw new Error(msg || 'Value has wrong type');
    }
    return value;
  },

  /**
   * Checks if one of the arguments from 1-n is equal to the first argument.
   * @param args to check against the value, may be an array or a variable argument list.
   */
  isOneOf(value: any, ...args /* explicit any produces warning at calling js code */): boolean {
    if (args.length === 0) {
      return false;
    }
    let argsToCheck = args;
    if (args.length === 1 && Array.isArray(args[0])) {
      argsToCheck = args[0];
    }
    return argsToCheck.indexOf(value) !== -1;
  },

  create,

  /**
   * Prepares the DOM for scout in the given document. This should be called once while initializing scout.
   * If the target document is not specified, the global "document" variable is used instead.
   *
   * This is used by apps (App, LoginApp, LogoutApp)
   *
   * Currently, it does the following:
   * - Remove the <noscript> tag (obviously there is no need for it).
   * - Remove <scout-text> tags (they must have been processed before, see texts.readFromDOM())
   * - Remove <scout-version> tag (it must have been processed before, see App._initVersion())
   * - Add a device / browser class to the body tag to allow for device specific CSS rules.
   * - Add browser locale to DOM so screen readers read text correctly (may get replaced if actual locale of user is loaded)
   * - If the browser is Google Chrome, add a special meta header to prevent automatic translation.
   */
  prepareDOM(targetDocument: Document) {
    targetDocument = targetDocument || document;
    // Cleanup DOM
    $('noscript', targetDocument).remove();
    $('scout-text', targetDocument).remove();
    $('scout-version', targetDocument).remove();
    $('body', targetDocument).addDeviceClass();

    // Set locale of the document so screen readers read text correctly
    scout.setDocumentLocale(locales.getNavigatorLocale());

    // Prevent "Do you want to translate this page?" in Google Chrome
    if (Device.get().browser === Device.Browser.CHROME) {
      let metaNoTranslate = '<meta name="google" content="notranslate" />';
      let $title = $('head > title', targetDocument);
      if ($title.length === 0) {
        // Add to end of head
        $('head', targetDocument).append(metaNoTranslate);
      } else {
        $title.after(metaNoTranslate);
      }
    }
  },

  /**
   * Installs a global 'mousedown' interceptor to invoke 'aboutToBlurByMouseDown' on value field before anything else gets executed.
   */
  installGlobalMouseDownInterceptor(myDocument: Document) {
    myDocument.addEventListener('mousedown', event => {
      ValueField.invokeValueFieldAboutToBlurByMouseDown(event.target as Element);
    }, true); // true=the event handler is executed in the capturing phase
  },

  /**
   * Because Firefox does not set the active state of a DOM element when the mousedown event
   * for that element is prevented, we set an 'active' CSS class instead. This means in the
   * CSS we must deal with :active and with .active, where we need same behavior for the
   * active state across all browsers.
   *
   * Typically, you'd write something like this in your CSS:
   *   button:active, button.active { ... }
   */
  installSyntheticActiveStateHandler(myDocument: Document) {
    if (Device.get().requiresSyntheticActiveState()) {
      $activeElements = [];
      $(myDocument)
        .on('mousedown', event => {
          let $element = $(event.target);
          while ($element.length) {
            $activeElements.push($element.addClass('active'));
            $element = $element.parent();
          }
        })
        .on('mouseup', () => {
          $activeElements.forEach($element => {
            $element.removeClass('active');
          });
          $activeElements = [];
        });
    }
  },

  /**
   * Sets locale of the document, important for screen readers
   * @param locale
   */
  setDocumentLocale(locale: Locale) {
    if (!objects.isNullOrUndefined(locale)) {
      document.documentElement.lang = locale.languageTag;
    }
  },

  widget,

  /**
   * Helper to get the model adapter for a given adapterId. If there is more than one
   * session, e.g. in case of portlets, the second argument specifies the partId of the session
   * to be queried. If not specified explicitly, the first session is used. If the session or
   * the adapter could not be found, null is returned.
   */
  adapter(adapterId: string, partId: string): ModelAdapterLike {
    if (objects.isNullOrUndefined(adapterId)) {
      return null;
    }
    let session = scout.getSession(partId);
    if (session && session.modelAdapterRegistry) {
      return session.modelAdapterRegistry[adapterId];
    }
    return null;
  },

  /**
   * @returns the session for the given partId. If the partId is omitted, the first session is returned.
   */
  getSession(partId?: string): Session {
    let sessions = App.get().sessions;
    if (!sessions) {
      return null;
    }
    if (objects.isNullOrUndefined(partId)) {
      return sessions[0];
    }
    for (let i = 0; i < sessions.length; i++) {
      let session = sessions[i];
      // eslint-disable-next-line eqeqeq
      if (session.partId == partId) { // <-- compare with '==' is intentional! (NOSONAR)
        return session;
      }
    }
    return null;
  },

  /**
   * This method exports the adapter with the given ID as JSON, it returns a plain object containing the
   * configuration of the adapter. You can transform that object into JSON by calling <code>JSON.stringify</code>.
   * This method can only be called through the browser JavaScript console.
   * Here's an example of how to call the method:
   *
   * JSON.stringify(exportAdapter(4))
   */
  exportAdapter(adapterId: string, partId: string): AdapterData {
    let session = scout.getSession(partId);
    if (session && session.modelAdapterRegistry) {
      let adapter = session.getModelAdapter(adapterId);
      if (!adapter) {
        return null;
      }
      let adapterData = cloneAdapterData(adapterId);
      resolveAdapterReferences(adapter, adapterData);
      adapterData.type = 'model'; // property 'type' is required for models.ts
      return adapterData;
    }

    // ----- Helper functions -----

    function cloneAdapterData(adapterId: string): AdapterData {
      let adapterData = session.getAdapterData(adapterId);
      adapterData = $.extend(true, {}, adapterData);
      return adapterData;
    }

    function resolveAdapterReferences(adapter: ModelAdapterLike, adapterData: AdapterData) {
      let tmpAdapter: ModelAdapterLike, tmpAdapterData: AdapterData;
      adapter.widget.widgetProperties.forEach(widgetPropertyName => {
        let widgetPropertyValue = adapterData[widgetPropertyName];
        if (!widgetPropertyValue) {
          return; // nothing to do when property is null
        }
        if (Array.isArray(widgetPropertyValue)) {
          // value is an array of adapter IDs
          let adapterDataArray = [];
          widgetPropertyValue.forEach(adapterId => {
            tmpAdapter = session.getModelAdapter(adapterId);
            tmpAdapterData = cloneAdapterData(adapterId);
            resolveAdapterReferences(tmpAdapter, tmpAdapterData);
            adapterDataArray.push(tmpAdapterData);
          });
          adapterData[widgetPropertyName] = adapterDataArray;
        } else {
          // value is an adapter ID
          tmpAdapter = session.getModelAdapter(widgetPropertyValue);
          tmpAdapterData = cloneAdapterData(widgetPropertyValue);
          resolveAdapterReferences(tmpAdapter, tmpAdapterData);
          adapterData[widgetPropertyName] = tmpAdapterData;
        }
      });
      adapterData = adapter.exportAdapterData(adapterData);
    }

    return null;
  },

  /**
   * Reloads the entire browser window.
   */
  reloadPage(options?: ReloadPageOptions) {
    options = options || {} as ReloadPageOptions;
    if (options.schedule) {
      setTimeout(reloadPageImpl);
    } else {
      reloadPageImpl();
    }

    // ----- Helper functions -----

    function reloadPageImpl() {
      // Hide everything (on entire page, not only $entryPoint)
      if (scout.nvl(options.clearBody, true)) {
        $('body').html('');
      }

      if (options.redirectUrl) {
        window.location.href = options.redirectUrl;
      } else {
        window.location.reload();
      }
    }
  },

  /**
   * @param factories Object that contains the object type as key and the that constructs the object as value.
   *          If you prefer using a class reference as object type rather than a string, please use {@link addObjectFactory} to register your factory.
   * @see create
   */
  addObjectFactories(factories: Record<string, ObjectCreator>) {
    for (let [objectType, factory] of Object.entries(factories)) {
      scout.addObjectFactory(objectType, factory);
    }
  },

  /**
   * @param objectType ObjectType to register the factory for.
   * @param factory Function that constructs the object.
   * @see create
   */
  addObjectFactory(objectType: ObjectType, factory: ObjectCreator) {
    scout.objectFactories.set(objectType, factory);
  },

  cloneShallow(template: object, properties?: object, createUniqueId?: boolean): object {
    scout.assertParameter('template', template);
    let clone = Object.create(Object.getPrototypeOf(template));
    Object.getOwnPropertyNames(template)
      .forEach(key => {
        clone[key] = template[key];
      });
    if (properties) {
      for (let key in properties) {
        clone[key] = properties[key];
      }
    }
    if (scout.nvl(createUniqueId, true)) {
      clone.id = ObjectUuidProvider.createUiId();
    }
    if (clone.cloneOf === undefined) {
      clone.cloneOf = template;
    }
    return clone;
  },

  /**
   * Enables or disables the layout spy that visualizes the cell bounds of the logical grid for debugging purposes.
   * The spy can be enabled on elements that belong to a widget using a {@link LogicalGridLayout}, e.g. {@link GroupBox}, {@link TileGrid}, etc.
   */
  setLogicalGridSpyEnabled(elem: HTMLElement, enabled: boolean) {
    let layout;
    let widget = scout.widget(elem);
    if (widget instanceof GroupBox) {
      layout = widget.htmlBody.layout;
      widget.htmlBody.invalidateLayoutTree(false);
    } else {
      layout = widget.htmlComp.layout;
      widget.htmlComp.invalidateLayoutTree(false);
    }
    if (!(layout instanceof LogicalGridLayout)) {
      throw new Error('Layout needs to be a LogicalGridLayout');
    }
    layout.setSpyEnabled(enabled);
  }
};
