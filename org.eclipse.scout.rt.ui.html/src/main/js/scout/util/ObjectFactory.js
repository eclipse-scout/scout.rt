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
scout.ObjectFactory = function(session) {
  this.session = session;
};

/**
 * @param model needs to contain property objectType
 * @param register (optional) when set to true the adapter instance is un-/registered in the modelAdapterRegistry of the session
 *   when not set, the default-value is true. When working with local objects (see LocalObject.js) the register flag is set to false.
 */
scout.ObjectFactory.prototype.create = function(model) {
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
    scoutObject = new scout[scoutClass]();
  }

  if (!scoutObject) {
    throw new Error('Failed to create Scout object for objectType:' + objectType + '. Either file/class \'scout.' + objectType +
        '\' does not exist, or no factory is registered to create an instance for the given objectType');
  }

  model.session = this.session;
  scoutObject.init(model);
  return scoutObject;
};
