/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class CloneSpecHelper {
  constructor(session) {
    this.session = session;
  }

  validateClone(original, clone, localProperties) {
    var properties = original._cloneProperties.filter(function(prop) {
        return original._widgetProperties.indexOf(prop) < 0;
      }),
      widgetProperties = original._cloneProperties.filter(function(prop) {
        return original._widgetProperties.indexOf(prop) > -1;
      });

    // simple properties to be cloned
    properties.forEach(function(prop) {
      expect(clone).definedProperty(original, prop);
      expect(original).sameProperty(clone, prop);
    });

    // widget properties to be cloned
    widgetProperties.forEach(function(prop) {
      expect(clone).definedProperty(original, prop);

      expect(original).widgetCloneProperty(clone, prop);
    });
  }
}
