/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, CodeType, CodeTypeAddEvent, CodeTypeCacheEventMap, CodeTypeRemoveEvent, DoEntity, EventEmitter, ObjectModel, ObjectOrModel, UiNotificationEvent, uiNotifications} from '../index';

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
    this.events.registerSubTypePredicate('codeTypeAdd', (event: CodeTypeAddEvent, codeTypeId) => event?.codeType?.id === codeTypeId); // only works if the CodeTypeId can be converted to a string
    this.events.registerSubTypePredicate('codeTypeRemove', (event: CodeTypeRemoveEvent, codeTypeId) => event?.id === codeTypeId); // only works if the CodeTypeId can be converted to a string
    this.registry = new Map();
  }

  /**
   * @see codes.bootstrap
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

  protected _triggerCodeTypeAdd(codeType: CodeType<any, any, any>) {
    this.trigger('codeTypeAdd', {codeType});
  }

  protected _triggerCodeTypeRemove(id: any) {
    this.trigger('codeTypeRemove', {id});
  }

  /**
   * @see codes.add
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
    registeredCodeTypes.forEach(codeType => this._triggerCodeTypeAdd(codeType));
    return registeredCodeTypes;
  }

  /**
   * @see codes.remove
   */
  remove(codeTypes: string | CodeType<any, any, any> | (string | CodeType<any, any, any>)[]) {
    const ids = arrays.ensure(codeTypes)
      .map(codeTypeOrId => typeof codeTypeOrId === 'string' ? codeTypeOrId : codeTypeOrId.id);
    ids.forEach(id => this.registry.delete(id));
    ids.forEach(id => this._triggerCodeTypeRemove(id));
  }

  /**
   * @see codes.get
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
