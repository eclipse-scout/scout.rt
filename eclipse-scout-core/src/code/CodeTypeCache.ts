/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ajax, arrays, CodeType, CodeTypeCacheEventMap, CodeTypeChangeEvent, CodeTypeRemoveEvent, DoEntity, EventEmitter, ObjectModel, ObjectOrModel, objects, systems, UiNotificationEvent, uiNotifications} from '../index';
import $ from 'jquery';

/**
 * Cache for CodeTypes grouped by CodeType id.
 */
export class CodeTypeCache extends EventEmitter implements ObjectModel<CodeTypeCache> {
  declare self: CodeTypeCache;
  declare eventMap: CodeTypeCacheEventMap;

  id: string;
  /**
   * Map of CodeType id to CodeType instance. Do not access directly. Instead, use {@link get}.
   */
  registry: Map<any /* CodeType id */, CodeType<any, any, any>>;
  /**
   * URL to get CodeType data from
   */
  url: string;

  protected _reloadTimeoutId: number;
  protected _idsToUpdate: string[];

  constructor() {
    super();
    this.events.registerSubTypePredicate('codeTypeChange', (event: CodeTypeChangeEvent, codeTypeId) => event.codeType.id === codeTypeId); // only works if the CodeTypeId can be converted to a string
    this.events.registerSubTypePredicate('codeTypeRemove', (event: CodeTypeRemoveEvent, codeTypeId) => event.id === codeTypeId); // only works if the CodeTypeId can be converted to a string
    this.registry = new Map();
    this._reloadTimeoutId = -1;
    this._idsToUpdate = [];
    this.url = null;
  }

  /**
   * Loads code types from the main system using the url configuration of that {@link System}.
   */
  bootstrapSystem(): JQuery.Promise<void> {
    const url = systems.getOrCreate().getEndpointUrl('codes', 'codes');
    return this.bootstrap(url);
  }

  /**
   * Initializes the code type map with the result of the given REST url.
   */
  bootstrap(url: string): JQuery.Promise<void> {
    if (!url) {
      return $.resolvedPromise();
    }
    this.url = url;
    uiNotifications.subscribe('codeTypeUpdate', event => this._onCodeTypeUpdateNotify(event));
    return this.loadCodeTypes().then(codes => undefined);
  }

  /**
   * Loads the CodeTypes from the backend. The URL provided in {@link bootstrap} is used.
   * @param codeTypeIds Optional list of {@link CodeType.id} to load. If not specified, all are loaded.
   * @returns The newly loaded {@link CodeType} instances.
   */
  loadCodeTypes(codeTypeIds?: string[]): JQuery.Promise<CodeType<any, any, any>[]> {
    if (!this.url) {
      return $.resolvedPromise([]);
    }
    const request: CodeTypeRequest = codeTypeIds?.length ? {
      _type: 'scout.CodeTypeRequest',
      codeTypeIds
    } : null;
    return ajax.putJson(this.url, request).then(this._handleCodesResponse.bind(this));
  }

  protected _handleCodesResponse(data: any): CodeType<any, any, any>[] {
    if (!data) {
      return [];
    }
    if (data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: this.url
      };
    }
    return this.add(data);
  }

  protected _onCodeTypeUpdateNotify(event: UiNotificationEvent) {
    let message = event.message as CodeTypeUpdateMessageDo;
    let codeTypes = message.codeTypes;
    if (codeTypes?.length) {
      // notification message directly contained the new CodeTypes: add them to the cache
      this.add(codeTypes).forEach(codeType => $.log.info(`CodeType with id '${codeType.id}' updated.`));
    } else if (message.codeTypeIds?.length) {
      // no codeTypes received: get the changed CodeTypes in a new request
      const reloadDelay = uiNotifications.computeReloadDelay(message.reloadDelayWindow);
      this._scheduleCodeTypeUpdate(message.codeTypeIds, reloadDelay);
    }
  }

  protected _scheduleCodeTypeUpdate(idsToUpdate: string[], delay: number) {
    this._idsToUpdate = [...new Set([...this._idsToUpdate, ...idsToUpdate])]; // merge existing and new arrays removing duplicates
    if (this._reloadTimeoutId > 0) {
      // cancel current update and schedule a new one to ensure the newest changes from the backend are fetched in case the fetch has already started.
      clearTimeout(this._reloadTimeoutId);
    }
    $.log.info(`About to refresh CodeTypes with ids '${this._idsToUpdate}' after ${delay}ms.`);
    this._reloadTimeoutId = setTimeout(() => {
      this._reloadTimeoutId = -1;
      const ids = this._idsToUpdate;
      this._idsToUpdate = [];
      this.loadCodeTypes(ids)
        .then(codeTypes => codeTypes.forEach(codeType => $.log.info(`CodeType with id '${codeType.id}' updated.`)));
    }, delay);
  }

  protected _triggerCodeTypeChange(codeType: CodeType<any, any, any>) {
    this.trigger('codeTypeChange', {codeType});
  }

  protected _triggerCodeTypeRemove(id: any) {
    this.trigger('codeTypeRemove', {id});
  }

  /**
   * Adds the given CodeType models to the registry. Existing entries with the same ids are overwritten.
   * @returns The registered CodeType instances.
   */
  add(codeTypes: ObjectOrModel<CodeType<any, any, any>> | ObjectOrModel<CodeType<any, any, any>>[]): CodeType<any, any, any>[] {
    let registeredCodeTypes = [];
    arrays.ensure(codeTypes).forEach(codeTypeOrModel => {
      let codeType = CodeType.ensure(codeTypeOrModel);
      if (codeType && codeType.id) {
        this.registry.set(codeType.id, codeType);
        registeredCodeTypes.push(codeType);
      }
    });
    registeredCodeTypes.forEach(codeType => this._triggerCodeTypeChange(codeType));
    return registeredCodeTypes;
  }

  /**
   * Removes the given CodeTypes from the registry.
   *
   * @param codeTypes code types or code type ids to remove.
   */
  remove(codeTypes: string | CodeType<any, any, any> | (string | CodeType<any, any, any>)[]) {
    const ids = arrays.ensure(codeTypes)
      .map(codeTypeOrId => typeof codeTypeOrId === 'string' ? codeTypeOrId : codeTypeOrId.id);
    ids.forEach(id => this.registry.delete(id));
    ids.forEach(id => this._triggerCodeTypeRemove(id));
  }

  /**
   * Gets the CodeType with given id or Class.
   * @param codeTypeIdOrClassRef The CodeType id or Class
   * @returns The CodeType instance or undefined if not found.
   */
  get<T extends CodeType<any>>(codeTypeIdOrClassRef: string | (new() => T)): T {
    if (typeof codeTypeIdOrClassRef === 'string') {
      return this.registry.get(codeTypeIdOrClassRef) as T;
    }

    for (let codeType of this.registry.values()) {
      if (codeType instanceof codeTypeIdOrClassRef) {
        return codeType as T;
      }
    }
    return undefined; // class not found
  }
}

export interface CodeTypeUpdateMessageDo extends DoEntity {
  codeTypes?: ObjectOrModel<CodeType<any, any, any>>[];
  codeTypeIds?: string[];
  reloadDelayWindow?: number;
}

export interface CodeTypeRequest extends DoEntity {
  codeTypeIds?: string[];
}

export const codes: CodeTypeCache = objects.createSingletonProxy(CodeTypeCache);
