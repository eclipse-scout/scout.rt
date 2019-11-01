/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {widgets} from '../../index';
import {TreeVisitResult} from '../../index';
import {FormField} from '../../index';
import {fields} from '../../index';

export default class CompositeField extends FormField {

constructor() {
  super();
}


/**
 * @returns {FormField[]} an array of child-fields.
 */
getFields() {
  throw new Error('Not implemented');
}

/**
 *
 * @param {FormField[]} fields
 */
setFields(fields) {
  throw new Error('Not implemented');
}

/**
 * @override FormField.js
 */
visitFields(visitor) {
  var treeVisitResult = super.visitFields( visitor);
  if (treeVisitResult === TreeVisitResult.TERMINATE) {
    return TreeVisitResult.TERMINATE;
  }

  if (treeVisitResult === TreeVisitResult.SKIP_SUBTREE) {
    return TreeVisitResult.CONTINUE;
  }

  var fields = this.getFields();
  for (var i = 0; i < fields.length; i++) {
    var field = fields[i];
    treeVisitResult = field.visitFields(visitor);
    if (treeVisitResult === TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }
  }
}

/**
 * Sets the given fieldStyle recursively on all fields of the composite field.
 * @override FormField.js
 */
setFieldStyle(fieldStyle) {
  this.getFields().forEach(function(field) {
    field.setFieldStyle(fieldStyle);
  });
  super.setFieldStyle( fieldStyle);
}

/**
 * @override
 */
activate() {
  fields.activateFirstField(this, this.getFields());
}

/**
 * @override
 */
getFocusableElement() {
  var field = widgets.findFirstFocusableWidget(this.getFields(), this);
  if (field) {
    return field.getFocusableElement();
  }
  return null;
}
}
