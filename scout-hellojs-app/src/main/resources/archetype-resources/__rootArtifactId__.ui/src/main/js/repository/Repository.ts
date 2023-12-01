import {ajax, dates, ObjectWithType, scout} from '@eclipse-scout/core';
import $ from 'jquery';

export abstract class Repository implements ObjectWithType {
  objectType: string;
  entityType: string;
  targetUrl: string;
  /** @see reviver on {@link JSON.parse} */
  jsonReviver: (this: any, key: string, value: any) => any;

  protected constructor() {
    this.jsonReviver = Repository.objectTypeTransformer;
  }

  getJson(url: string, opts?: JQuery.AjaxSettings): JQuery.Promise<any> {
    return Repository.map(ajax.getJson(url, this._ensureConverter(opts)));
  }

  postJson(url: string, data: any, opts?: JQuery.AjaxSettings): JQuery.Promise<any> {
    return Repository.map(ajax.postJson(url, data, this._ensureConverter(opts)));
  }

  removeJson(url: string, opts?: JQuery.AjaxSettings): JQuery.Promise<any> {
    return Repository.map(ajax.removeJson(url, this._ensureConverter(opts)));
  }

  putJson(url: string, data: any, opts?: JQuery.AjaxSettings): JQuery.Promise<any> {
    return Repository.map(ajax.putJson(url, data, this._ensureConverter(opts)));
  }

  protected _ensureConverter(opts?: JQuery.AjaxSettings): JQuery.AjaxSettings {
    return ${symbol_dollar}.extend({}, {
      converters: {
        'text json': this._mapJson.bind(this)
      }
    }, opts);
  }

  jsonStringify(data: any): string {
    return JSON.stringify(data, function(key, value) {
      if (this[key] instanceof Date) {
        return dates.toJsonDate(this[key]);
      }
      return value;
    });
  }

  protected _mapJson(data: any): any {
    if (!data) {
      return data;
    }
    return JSON.parse(data, this.jsonReviver);
  }

  protected _list<TRestriction, TData>(restriction: TRestriction): JQuery.Promise<TData> {
    return this.postJson(this.targetUrl + 'list', this.jsonStringify(restriction));
  }

  protected _load<TData>(url: string): JQuery.Promise<TData> {
    return this.getJson(url)
      .then(data => this._first(data) as TData);
  }

  protected _store<TData>(data: TData, url: string): JQuery.Promise<TData> {
    return this.putJson(url, this.jsonStringify(data))
      .then(data => this._first(data) as TData)
      .then(data => this._triggerDataChange(data));
  }

  protected _create<TData>(data: TData): JQuery.Promise<TData> {
    return this.postJson(this.targetUrl, this.jsonStringify(data))
      .then(data => this._first(data) as TData)
      .then(data => this._triggerDataChange(data));
  }

  protected _remove(id: string): JQuery.Promise<void> {
    return this.removeJson(this.targetUrl + id)
      .then(() => {
        this._triggerDataChange();
      });
  }

  protected _first<T>(items: T[]): T {
    return items[0];
  }

  protected _triggerDataChange<TData>(data?: TData): TData {
    scout.getSession().desktop.dataChange({
      dataType: this.entityType,
      data: data
    });
    return data;
  }

  static map(promise: JQuery.Promise<any>): JQuery.Promise<any> {
    return promise.then(response => {
      if (!response || !response.items) {
        return response;
      }
      return response.items.map(item => {
        return scout.create(item, {
          ensureUniqueId: false
        });
      });
    });
  }

  static objectTypeTransformer(this: any, key: string, value: any): any {
    if (key === '_type') {
      this.objectType = value;
    }
    return value;
  }
}
