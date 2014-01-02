/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.gen.extract.action;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractBooleanTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 * Extracts the value for {@link IAction#isMultiSelectionAction()} and returns the text for
 * ({@link AbstractBooleanTextExtractor#DOC_ID_TRUE} or {@link AbstractBooleanTextExtractor#DOC_ID_FALSE}.
 */
public class MultiSelectionExtractor<T extends IAction> extends AbstractBooleanTextExtractor<T> implements IDocTextExtractor<T> {

  public MultiSelectionExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.action.multiSelection"));
  }

  /**
   * Extracts the value for {@link IAction#isMultiSelectionAction()} and returns the text
   * 
   * @param action
   *          {@link IAction}
   * @return translated text for {@link AbstractBooleanTextExtractor#DOC_ID_TRUE} or
   *         {@link AbstractBooleanTextExtractor#DOC_ID_FALSE}
   */
  @Override
  public String getText(T object) {
    return getBooleanText(object.isMultiSelectionAction());
  }

}
