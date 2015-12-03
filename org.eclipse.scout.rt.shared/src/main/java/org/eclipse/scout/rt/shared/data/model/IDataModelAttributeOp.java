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
package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

public interface IDataModelAttributeOp {

  String createVerboseText(Integer aggregationType, String attributeText, List<String> valueTexts);

  /**
   * example: "is bewteen {0} and {1}"
   */
  String getText();

  /**
   * example: "bewteen"
   */
  String getShortText();

  /**
   * @return the field type to display to select a value for usiong this operation, use
   *         {@link IComposerAttribute#TYPE_INHERITED} when the type of the attribute should be used see
   *         {@link IComposerAttribute}.TYPE_* values
   */
  int getType();

  /**
   * @return the unique operator type see {@link ComposerConstants#OPERATOR_*} values
   */
  int getOperator();
}
