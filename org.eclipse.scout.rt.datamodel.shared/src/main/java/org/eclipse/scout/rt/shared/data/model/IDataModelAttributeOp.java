/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
   * {@link IComposerAttribute#TYPE_INHERITED} when the type of the attribute should be used see
   * {@link IComposerAttribute}.TYPE_* values
   */
  int getType();

  /**
   * @return the unique operator type see {@link ComposerConstants#OPERATOR_*} values
   */
  int getOperator();

  /**
   * @return the lookupCall to be used when of type {@link IComposerAttribute#TYPE_SMART} and another lookupCall than
   * that of the attribute is needed.
   * @since 10.0
   */
  default ILookupCall<?> getLookupCall() {
    return null;
  }
}
