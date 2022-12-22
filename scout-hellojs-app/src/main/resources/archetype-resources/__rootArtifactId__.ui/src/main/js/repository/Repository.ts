import {ajax, ObjectWithType, scout} from '@eclipse-scout/core';
import * as $ from 'jquery';

/**
 * Subclasses of Repository must set the 'entityType' property.
 */
export abstract class Repository implements ObjectWithType {
  objectType: string;

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
        'text json': this._mapJson
      }
    }, opts);
  }

  protected _mapJson(data: any): any {
    return JSON.parse(data, function(key: string, value: any): any {
      if (key === '_type') {
        this.objectType = value;
      }
      return value;
    });
  }

  protected _first<T>(items: T[]): T {
    return items[0];
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
}
