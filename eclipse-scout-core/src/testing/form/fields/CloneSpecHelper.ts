/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Widget} from '../../../index';

export class CloneSpecHelper {

  validateClone(original: Widget, clone: Widget) {
    let properties = Array.from(original.cloneProperties).filter(prop => {
      return !original.isWidgetProperty(prop);
    });
    let widgetProperties = Array.from(original.cloneProperties).filter(prop => {
      return original.isWidgetProperty(prop);
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
