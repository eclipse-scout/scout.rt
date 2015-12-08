/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.objectFactory = {

  // use createUniqueId() to generate a new ID
  uniqueIdSeqNo: 0,

  /**
   * Creates the object from the given model.objectType. Only the constructor is called, but not the init() method.
   * We concat 'scout.' + objectType to find the constructor function. When scout.objectFactories contains a create function
   * for the given objectType, this function is called, otherwise the default logic is applied to find a constructor.
   *
   * @param model needs to contain property objectType
   */
  _createObjectByType: function(model) {
    // check if requested objectType / variant is registered
    var objectTypeParts, scoutClass, scoutObject,
      objectType = model.objectType,
      createFunc = scout.objectFactories[objectType];

    if (createFunc) {
      // When a factory is registered for the given objectType
      scoutObject = createFunc(model);
    } else {
      // When no factory is registered for the given objectType
      objectTypeParts = objectType.split('.');
      if (objectTypeParts.length === 2) {
        // variant + objectType
        scoutClass = objectTypeParts[1] + objectTypeParts[0];
      } else {
        // only objectType
        scoutClass = objectType;
      }
      try {
        scoutObject = new scout[scoutClass]();
      } catch (e) {
        // NOP - error handling below
      }
    }

    if (!scoutObject) {
      throw new Error('Failed to create Scout object for objectType:' + objectType + '. Either file/class \'scout.' + objectType +
        '\' does not exist, or no factory is registered to create an instance for the given objectType');
    }

    return scoutObject;
  },

 /**
  * Creates and initializes a new Scout object. Depending on the type of the vararg parameter the method does this:
  *
  * <ul>
  * <li><code>string</code> objectType. The second parameter must provide the model. A lookup is performed to
  *     find the constructor function.</li>
  * <li><code>object</code> model object. The object must have a property 'objectType'. The second parameter is not
  *     required. A lookup is performed to find the constructor function.</li>
  * </ul>
  *
  * When the provided model does not contain the property '_register', the property is set to false, which means the
  * object is not registered in the adapter registry. Which is the desired default behavior when we create local objects.
  *
  * When the provided model does not contain the property 'id', the property is set to a random, unqiue value
  * having the prefix 'ui'.
  *
  * The returned Scout object is initialized, by calling the init() function.
  *
  * Note: support to pass a constructor-function as vararg has been removed because we cannot determine the name of the
  * function at runtime (ECMA 6 Function.name is not supported by all browsers currently). Thus the objectType would be
  * missing and it makes no sense to pass an additional objectType when we already have the constructor.
  *
  *  @param vararg string or object
  *  @param model (optional) must be set when vararg is a string
  */
  create: function(vararg, model) {
    var scoutObject;
    model = model || {};
    if (typeof vararg === 'string') {
      model.objectType = vararg;
      scoutObject = this._createObjectByType(model);
    } else if (typeof vararg === 'object') {
      model = vararg;
      scoutObject = this._createObjectByType(model);
    } else {
      throw new Error('parameter vararg must be an objectType string or an object having an objectType property');
    }

    if (scoutObject instanceof scout.ModelAdapter && model._register === undefined) {
      model._register = false;
    }

    if (model.id === undefined) {
      model.id = this.createUniqueId();
    }

    scoutObject.init(model);
    return scoutObject;
  },

  /**
   * Extracts the basic-objectType when objectType contains a model-variant.
   * Example: "GroupBox.FilterFields" will return "GroupBox"
   */
  _extractObjectType: function(objectType) {
    return objectType.split('.')[0];
  },

  /**
   * Returns a new unique ID to be used for Widgets/Adapters created by the UI
   * without a model delivered by the server-side client.
   * @return string ID with prefix 'ui'
   */
  createUniqueId: function() {
    return 'ui' + (++this.uniqueIdSeqNo).toString();
  }

};
