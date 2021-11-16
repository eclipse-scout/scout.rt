/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, LookupCall, objects, scout} from '../index';
import $ from 'jquery';

/**
 * A lookup call that can load lookup rows from a REST service.
 *
 * API:
 * ----
 * By default, the REST service is expected to listen for POST requests at the URL defined by
 * this.resourceUrl. It receives a restriction object and must return a list of matching lookup rows.
 * The serialization format is JSON.
 *
 * Lookup rows:
 * ------------
 * The standard lookup row properties defined by Scout are usually sufficient (see AbstractLookupRowDo.java).
 *
 * Restriction:
 * ------------
 * The restriction object consists of a number of 'well-known' properties (e.g. 'text' in QueryBy.TEXT
 * mode, see AbstractLookupRestrictionDo.java for details) and additional, service-dependent properties
 * that can either be predefined in the model or added programmatically at runtime. Since all of those
 * properties are sent in the same restriction object, some care must be taken to prevent accidental
 * overwriting of properties.
 *
 * Order of precedence (lowest to highest):
 * 1. Restrictions automatically applied to all clones after their creation in the respective cloneFor method.
 *    These are: 'active' (ALL, TEXT, REC) and 'maxRowCount' (ALL, TEXT, REC)
 * 2. Restrictions predefined in the model property 'restriction', shared by all clones.
 * 3. Restrictions applied to clones programmatically, e.g. during a 'prepareLookupCall' event.
 * 4. Hard-coded properties that are fundamental to the respective queryBy mode (cannot be overridden).
 *    These are: 'ids' (KEY, KEYS) and 'text' (TEXT)
 */
export default class RestLookupCall extends LookupCall {

  constructor() {
    super();
    this.resourceUrl = null;
    this.maxTextLength = null;

    // for predefined restrictions only (e.g. in JSON or sub-classes), don't change this attribute! this instance is shared with all clones!
    this.restriction = null;

    // dynamically added restrictions. after setting this attribute, this instance is shared with all following clones!
    this._restriction = null;

    this._ajaxCall = null;
    this._deferred = null;

    // RestLookupCall implements getByKeys
    this.batch = true;
  }

  /**
   * Use this function with caution! Added restrictions will be shared among cloned instances
   * and the current instance if this function was also called before cloning!
   */
  addRestriction(key, value) {
    if (!this._restriction) {
      this._restriction = {};
    }
    this._restriction[key] = value;
  }

  /**
   * Adds the given key-value pair to 'this._restriction', but only if there is no predefined
   * value for this key in 'this.restriction'. This prevents unintentional overriding of
   * user-defined model restrictions.
   */
  _addRestrictionIfAbsent(key, value) {
    if (!this.restriction || objects.isNullOrUndefined(this.restriction[key])) {
      this.addRestriction(key, value);
    }
  }

  _getAll() {
    return this._call();
  }

  _getByText(text) {
    this.addRestriction('text', text);
    return this._call();
  }

  _getByKey(key) {
    this.addRestriction('ids', arrays.ensure(key));
    return this._call();
  }

  _getByKeys(keys) {
    this.addRestriction('ids', arrays.ensure(keys));
    return this._call();
  }

  cloneForAll() {
    let clone = super.cloneForAll();
    clone._addRestrictionIfAbsent('active', true);
    clone._addRestrictionIfAbsent('maxRowCount', this.maxRowCount);
    return clone;
  }

  cloneForText(text) {
    let clone = super.cloneForText(text);
    clone._addRestrictionIfAbsent('active', true);
    clone._addRestrictionIfAbsent('maxRowCount', this.maxRowCount);
    return clone;
  }

  cloneForRec(parentKey) {
    let clone = super.cloneForRec(parentKey);
    clone._addRestrictionIfAbsent('active', true);
    clone._addRestrictionIfAbsent('maxRowCount', this.maxRowCount);
    return clone;
  }

  _acceptLookupRow(lookupRowDo) {
    return true;
  }

  _createLookupRowFromDo(lookupRowDo) {
    // propagate all properties from lookup row do to scout lookup row (there might be custom ones on specific lookup row dos)
    let clonedLookupRowDo = $.extend({}, lookupRowDo);
    // text, enabled and active are the same for Scout LookupRow and Studio LookupRowDo

    // id -> key
    clonedLookupRowDo.key = clonedLookupRowDo.id;
    delete clonedLookupRowDo.id;

    // parentId -> parentKey
    clonedLookupRowDo.parentKey = clonedLookupRowDo.parentId;
    delete clonedLookupRowDo.parentId;

    // unused on Scout LookupRow
    delete clonedLookupRowDo._type;

    if (this.maxTextLength) {
      let text = clonedLookupRowDo.text;
      if (text.length > this.maxTextLength) {
        clonedLookupRowDo.text = text.substr(0, this.maxTextLength) + '...';
        clonedLookupRowDo.tooltipText = text;
      }
    }

    return scout.create('LookupRow', clonedLookupRowDo, {ensureUniqueId: false});
  }

  _call() {
    this._deferred = $.Deferred();
    this._ajaxCall = this._createAjaxCall();

    this._ajaxCall.call()
      .then((data, textStatus, jqXHR) => {
        let lookupRows = arrays.ensure(data ? data.rows : null)
          .filter(this._acceptLookupRow.bind(this))
          .map(this._createLookupRowFromDo.bind(this));
        this._deferred.resolve({
          queryBy: this.queryBy,
          text: this.searchText,
          key: this.key,
          lookupRows: lookupRows
        });
      })
      .catch(ajaxError => {
        this._deferred.resolve({
          queryBy: this.queryBy,
          text: this.searchText,
          key: this.key,
          lookupRows: [],
          exception: this.session.text('ErrorWhileLoadingData')
        });
      });

    return this._deferred.promise();
  }

  abort() {
    this._deferred.reject({
      canceled: true
    });
    this._ajaxCall.abort();
    super.abort();
  }

  _getCallUrl() {
    return this.resourceUrl;
  }

  _getRestrictionForAjaxCall() {
    if (!this.restriction && !this._restriction) {
      return null;
    }

    let resolveValue = value => {
      if (typeof value === 'function') {
        // Dynamic evaluation of the restriction value
        return value(this);
      }
      return value;
    };

    let resolvedRestriction = {};
    let restriction = $.extend({}, this.restriction, this._restriction);
    Object.keys(restriction).forEach(key => {
      let value = restriction[key];
      let newValue;
      if (Array.isArray(value)) {
        // Resolve each array element individually, remove null values
        newValue = value.map(resolveValue).filter(Boolean);
        newValue = newValue.length ? newValue : null;
      } else {
        newValue = resolveValue(value);
      }
      // Only add non-null restrictions
      if (!objects.isNullOrUndefined(newValue)) {
        resolvedRestriction[key] = newValue;
      }
    });
    return resolvedRestriction;
  }

  _createAjaxCall() {
    let url = this._getCallUrl();
    let restriction = this._getRestrictionForAjaxCall();
    let data = restriction ? JSON.stringify(restriction) : null;
    let ajaxOptions = {
      type: 'POST',
      data: data,
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8',
      cache: false,
      url: url,
      timeout: 0
    };
    return scout.create('AjaxCall', {
      ajaxOptions: ajaxOptions,
      name: 'RestLookupCall',
      retryIntervals: [100, 500, 500, 500]
    }, {
      ensureUniqueId: false
    });
  }
}
