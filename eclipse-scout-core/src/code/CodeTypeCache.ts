/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, CodeType, CodeTypeCacheEventMap, CodeTypeChangeEvent, CodeTypeRemoveEvent, DoEntity, EventEmitter, ObjectModel, ObjectOrModel, objects, systems, UiNotificationEvent, uiNotifications} from '../index';

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

  constructor() {
    super();
    this.events.registerSubTypePredicate('codeTypeChange', (event: CodeTypeChangeEvent, codeTypeId) => event.codeType.id === codeTypeId); // only works if the CodeTypeId can be converted to a string
    this.events.registerSubTypePredicate('codeTypeRemove', (event: CodeTypeRemoveEvent, codeTypeId) => event.id === codeTypeId); // only works if the CodeTypeId can be converted to a string
    this.registry = new Map();
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
  bootstrap(url: string): JQuery.Promise<any> {
    if (!url) {
      return $.resolvedPromise();
    }
    uiNotifications.subscribe('codeTypeUpdate', event => this._onCodeTypeUpdateNotify(event));
    return $.ajaxJson(url).then(this._handleBootstrapResponse.bind(this, url));
  }

  protected _handleBootstrapResponse(url: string, data: any) {
    if (!data) {
      return;
    }
    if (data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: url
      };
    }
    this.add(data);
  }

  protected _onCodeTypeUpdateNotify(event: UiNotificationEvent) {
    let message = event.message as CodeTypeUpdateMessageDo;
    let codeTypes = message.codeTypes;
    if (!codeTypes?.length) {
      return; // nothing to update
    }
    this.add(codeTypes).forEach(codeType => $.log.info(`CodeType with id '${codeType.id}' updated.`));
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
  codeTypes: ObjectOrModel<CodeType<any, any, any>>[];
}

export const codes = objects.createSingletonProxy(CodeTypeCache);
