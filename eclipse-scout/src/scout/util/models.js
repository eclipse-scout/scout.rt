import * as $ from 'jquery';
import * as scout from '../scout';

let modelMap = {};

export function bootstrap(url) {
  /*var promise = url ? $.ajaxJson(url) : $.resolvedPromise({});
  return promise.then(this._preInit.bind(this, url));
  */
  return $.resolvedPromise({});
}

/*static _preInit(url, data) {
      if (data && data.error) {
          // The result may contain a json error (e.g. session timeout) -> abort processing
          throw {
              error: data.error,
              url: url
          };
      }
      this.init(data);
  }

export function init(data) {
      modelMap = data;
  }*/

export function getModel(model, parent) {
  //let model = Models._get(modelId, 'model');
  if (parent) {
    model.parent = parent;
  }
  return model;
}

/*static _get(id, type) {
    var model = modelMap[id];
    if (!model) {
        throw new Error('No model map entry found for id \'' + id + '\'');
    }
    if (model.type !== type) {
        throw new Error('Model \'' + id + '\' is not of type \'' + type + '\'');
    }
    return $.extend(true, {}, model);
}*/
