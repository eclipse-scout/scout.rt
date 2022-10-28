/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Widget} from '../../../index';

export default class CloneSpecHelper {

  validateClone(original: Widget, clone: Widget) {
    let properties = original._cloneProperties.filter(prop => {
        return original._widgetProperties.indexOf(prop) < 0;
      }),
      widgetProperties = original._cloneProperties.filter(prop => {
        return original._widgetProperties.indexOf(prop) > -1;
      });

    // simple properties to be cloned
    properties.forEach(prop => {
      expect(clone[prop]).toBeDefined();
      expect(clone[prop]).toBe(original[prop]);
    });

    // widget properties to be cloned
    widgetProperties.forEach(prop => {
      expect(clone[prop]).toBeDefined();
      expect(clone).toHaveClonedWidgetProperty(original, prop);
    });
  }
}
