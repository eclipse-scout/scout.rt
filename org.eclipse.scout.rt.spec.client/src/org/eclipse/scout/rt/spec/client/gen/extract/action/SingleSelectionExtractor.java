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

import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.utility.SpecUtility;

/**
 * Extracts the value for {@link IAction#isSingleSelectionAction()} and returns the text for
 * ({@link SpecUtility#DOC_ID_TRUE} or {@link SpecUtility#DOC_ID_FALSE}.
 */
public class SingleSelectionExtractor<T extends IMenu> extends AbstractNamedTextExtractor<T> {

  public SingleSelectionExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.action.singleSelection"));
  }

  /**
   * Extracts the value for {@link IAction#isSingleSelectionAction()} and returns the text
   * 
   * @param action
   *          {@link IAction}
   * @return translated text for {@link SpecUtility#DOC_ID_TRUE} or {@link SpecUtility#DOC_ID_FALSE}
   */
  @Override
  public String getText(T action) {
    Set<IMenuType> menuTypes = action.getMenuTypes();
    boolean singleSeleciton = menuTypes.contains(TableMenuType.SingleSelection);
    singleSeleciton |= menuTypes.contains(TreeMenuType.SingleSelection);
    singleSeleciton |= menuTypes.contains(ValueFieldMenuType.NotNull);
    return SpecUtility.getBooleanText(singleSeleciton);
  }
}
