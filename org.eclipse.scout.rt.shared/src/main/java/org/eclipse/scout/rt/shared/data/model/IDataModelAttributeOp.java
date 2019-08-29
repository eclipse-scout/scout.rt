/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public interface IDataModelAttributeOp {

  String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts);

  /**
   * example: "is between {0} and {1}"
   */
  String getText();

  /**
   * example: "between"
   */
  String getShortText();

  /**
   * example: "Operator does not search for part terms use '*' to do so."
   */
  String getExplanationText();

  /**
   * @return the field type to display to select a value for using this operation, use
   *         {@link IComposerAttribute#TYPE_INHERITED} when the type of the attribute should be used see
   *         {@link IComposerAttribute}.TYPE_* values
   */
  int getType();

  /**
   * @return the unique operator type see {@link ComposerConstants#OPERATOR_*} values
   */
  int getOperator();

  /**
   * @since 10.0
   * @return the lookupCall to be used when of type {@link IComposerAttribute#TYPE_SMART} and another lookupCall than
   *         that of the attribute is needed.
   */
  default ILookupCall<?> getLookupCall() {
    return null;
  }
}
