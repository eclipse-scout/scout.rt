import * as $ from 'jquery';
import * as scout from '../scout';

var modelMap = {};

export default class Models {

  static bootstrap(url) {
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

  static init(data) {
      modelMap = data;
  }*/

  static getModel(model, parent) {
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
}
