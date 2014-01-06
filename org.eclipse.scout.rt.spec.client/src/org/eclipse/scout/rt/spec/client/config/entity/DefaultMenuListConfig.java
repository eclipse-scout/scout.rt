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
package org.eclipse.scout.rt.spec.client.config.entity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SimpleTypeTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.action.ActionPropertyExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.action.EmptySpaceSelectionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.action.MultiSelectionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.action.SingleSelectionExtractor;

/**
 * The default configuration for {@link IMenu}
 */
public class DefaultMenuListConfig extends DefaultEntityListConfig<IMenu> {

  /**
   * Default properties for {@link IMenu} with
   * <p>
   * Label,Type,Description,EmptySpaceAction, SingleSelectionAction,MultiselectionAction
   * </p>
   */
  @Override
  public List<IDocTextExtractor<IMenu>> getTexts() {
    List<IDocTextExtractor<IMenu>> propertyTemplate = new ArrayList<IDocTextExtractor<IMenu>>();
    propertyTemplate.add(new ActionPropertyExtractor<IMenu>(IAction.PROP_TEXT, TEXTS.get("org.eclipse.scout.rt.spec.label")));
    propertyTemplate.add(new DescriptionExtractor<IMenu>());
    propertyTemplate.add(new EmptySpaceSelectionExtractor<IMenu>());
    propertyTemplate.add(new SingleSelectionExtractor<IMenu>());
    propertyTemplate.add(new MultiSelectionExtractor<IMenu>());
    propertyTemplate.add(new SimpleTypeTextExtractor<IMenu>());
    return propertyTemplate;
  }

  @Override
  public String getTitle() {
    return TEXTS.get("org.eclipse.scout.rt.spec.menus");
  }

}
