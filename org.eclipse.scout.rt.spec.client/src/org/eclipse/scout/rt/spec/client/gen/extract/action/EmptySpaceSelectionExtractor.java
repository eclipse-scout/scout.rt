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
 *
 */
public class EmptySpaceSelectionExtractor<T extends IAction> extends AbstractBooleanTextExtractor<T> implements IDocTextExtractor<T> {

  /**
   * @param name
   */
  public EmptySpaceSelectionExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.action.emptySpace"));
  }

  @Override
  public String getText(T object) {
    return getBooleanText(object.isEmptySpaceAction());
  }

}
