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
package org.eclipse.scout.rt.client.ui.form.fields.composer.node;

import java.util.ArrayList;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public class RootNode extends AbstractComposerNode {

  public RootNode(IComposerField composerField) {
    super(composerField, true);
  }

  @Override
  protected void execInitTreeNode() {
    ArrayList<IMenu> menus = new ArrayList<IMenu>();
    for (IMenu m : getMenus()) {
      if (m.getClass() == AddEntityPlaceholderOnRootMenu.class) {
        attachAddEntityMenus(menus);
      }
      else {
        menus.add(m);
      }
    }
    setMenus(menus.toArray(new IMenu[menus.size()]));
  }

  @Override
  protected void execDecorateCell(Cell cell) {
    if (getChildNodeCount() == 0) {
      cell.setFont(FontSpec.parse("plain"));
    }
    else {
      cell.setFont(FontSpec.parse("bold"));
    }
  }

  @Order(10)
  public class AddAttributeOnRootMenu extends AbstractAddAttributeMenu {
    public AddAttributeOnRootMenu() {
      super(getComposerField(), RootNode.this);
    }
  }

  @Order(20)
  public class AddEitherOrOnRootMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ExtendedSearchAddEitherOrMenu");
    }

    @Override
    protected void execAction() throws ProcessingException {
      ITreeNode node = getComposerField().addEitherNode(RootNode.this, false);
      getComposerField().addAdditionalOrNode(node, false);
    }
  }

  @Order(30)
  public class Separator1Menu extends MenuSeparator {
  }

  @Order(40)
  public class AddEntityPlaceholderOnRootMenu extends MenuSeparator {
  }

}
