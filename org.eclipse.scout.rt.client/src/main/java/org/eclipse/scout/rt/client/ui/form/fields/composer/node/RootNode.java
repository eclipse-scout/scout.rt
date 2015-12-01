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
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public class RootNode extends AbstractComposerNode {

  public RootNode(IComposerField composerField) {
    super(composerField, true);
  }

  @Override
  protected void execInitTreeNode() {
    List<IMenu> menus = new ArrayList<IMenu>();
    for (IMenu m : getMenus()) {
      if (m.getClass() == AddEntityPlaceholderOnRootMenu.class) {
        attachAddEntityMenus(menus);
      }
      else {
        menus.add(m);
      }
    }
    setMenus(menus);
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
    protected void execAction() {
      ITreeNode node = getComposerField().addEitherNode(RootNode.this, false);
      getComposerField().addAdditionalOrNode(node, false);
    }
  }

  @Order(30)
  public class Separator1Menu extends AbstractMenuSeparator {
  }

  @Order(40)
  public class AddEntityPlaceholderOnRootMenu extends AbstractMenuSeparator {
  }

}
