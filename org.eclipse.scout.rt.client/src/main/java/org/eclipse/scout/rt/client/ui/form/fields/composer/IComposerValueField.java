/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;

/**
 * interface for all composer related generic values fields
 */
public interface IComposerValueField extends IFormField {

  void addValueChangeListenerToTarget(PropertyChangeListener listener);

  void removeValueChangeListenerFromTarget(PropertyChangeListener listener);

  void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List<?> values);

  void clearSelectionContext();

  /**
   * @return array of field value(s), null when field has invalid or missing values
   *         <p>
   *         Note: listbox and treebox have <b>one</b> value that is an array, so returning an array with the first
   *         element being an array: [[a,b,c,d] ]
   *         <p>
   *         Note: "between a and b" field has <b>two</b> values so returning an array with two elements: [a, b ]
   */
  List<Object> getValues();

  /**
   * @return the texts of the values of the selected field
   */
  List<String> getTexts();

}
