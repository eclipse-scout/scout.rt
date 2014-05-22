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

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractBooleanTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 *
 */
public class EmptySpaceSelectionExtractor<T extends IMenu> extends AbstractBooleanTextExtractor<T> implements IDocTextExtractor<T> {

  /**
   * @param name
   */
  public EmptySpaceSelectionExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.action.emptySpace"));
  }

  @Override
  public String getText(T action) {
    Set<IMenuType> menuTypes = action.getMenuTypes();
    boolean emptySpace = menuTypes.contains(TableMenuType.EmptySpace);
    emptySpace |= menuTypes.contains(TreeMenuType.EmptySpace);
    emptySpace |= menuTypes.contains(ValueFieldMenuType.Null);
    return getBooleanText(emptySpace);
  }

}
