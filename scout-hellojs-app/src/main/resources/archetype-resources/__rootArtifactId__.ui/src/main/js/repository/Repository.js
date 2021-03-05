import {ajax, scout} from '@eclipse-scout/core';
import * as $ from 'jquery';

/**
 * Subclasses of Repository must set the resourceType property.
 *
 * @abstract
 * @class
 */
export default class Repository {

  constructor() {
    this.entityType = null;
  }

  getJson(url, opts) {
    return Repository.map(ajax.getJson(url, this._ensureConverter(opts)));
  }

  postJson(url, data, opts) {
    return Repository.map(ajax.postJson(url, data, this._ensureConverter(opts)));
  }

  removeJson(url, opts) {
    return Repository.map(ajax.removeJson(url, this._ensureConverter(opts)));
  }

  putJson(url, data, opts) {
    return Repository.map(ajax.putJson(url, data, this._ensureConverter(opts)));
  }

  _ensureConverter(opts) {
    return ${symbol_dollar}.extend({}, {
      converters: {
        'text json': this._mapJson
      }
    }, opts);
  }

  _mapJson(data) {
    return JSON.parse(data, function(key, value) {
      if (key === '_type') {
        this.objectType = value;
      }
      return value;
    });
  }

  _first(items) {
    return items[0];
  }

  // ---- Static Objects ---- //

  static repositories = {};

  /**
   * @static
   * @param {string} objectName
   * @returns {Repository}
   */
  static register(objectName) {
    let repository = scout.create(objectName);
    Repository.repositories[repository.entityType] = repository;
    return repository;
  }

  static map(promise) {
    return promise
      .then(response => {
        if (!response || !response.items) {
          return response;
        }

        return response.items
          .map(item => {
            return scout.create(item, {
              ensureUniqueId: false
            });
          });
      });
  }

  /**
   * @static
   * @param {string} resourceType
   * @returns {Repository} a repository for the given resourceType
   */
  static get(resourceType) {
    let repository = Repository.repositories[resourceType];
    if (!repository) {
      throw new Error('no repository found for resourceType ' + resourceType);
    }
    return repository;
  }
}
