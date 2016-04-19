/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractFormMenu;
import org.eclipse.scout.rt.platform.Order;

public class DesktopWithNonDisplayableActions extends AbstractDesktop {

  @Override
  protected List<Class<? extends IOutline>> getConfiguredOutlines() {
    List<Class<? extends IOutline>> outlines = new ArrayList<Class<? extends IOutline>>();
    outlines.add(OutlineWithOneNode.class);
    return outlines;
  }

  @Order(10)
  public class DisplayableMenu extends AbstractMenu {
  }

  @Order(20)
  public class NonDisplayableMenu extends AbstractMenu {

    @Override
    protected void execInitAction() {
      setVisibleGranted(false);
    }
  }

  @Order(30)
  public class DisplayableFormMenu extends AbstractFormMenu {
  }

  @Order(40)
  public class NonDisplayableFormMenu extends AbstractFormMenu {

    @Override
    protected void execInitAction() {
      setVisibleGranted(false);
    }
  }

  @Order(50)
  public class DisplayableOutlineViewButton extends AbstractOutlineViewButton {

    public DisplayableOutlineViewButton() {
      this(OutlineWithOneNode.class);
    }

    public DisplayableOutlineViewButton(Class<? extends IOutline> outlineType) {
      super(DesktopWithNonDisplayableActions.this, outlineType);
    }
  }

  @Order(60)
  public class NonDisplayableOutlineViewButton extends AbstractOutlineViewButton {

    public NonDisplayableOutlineViewButton() {
      this(OutlineWithOneNode.class);
    }

    public NonDisplayableOutlineViewButton(Class<? extends IOutline> outlineType) {
      super(DesktopWithNonDisplayableActions.this, outlineType);
    }

    @Override
    protected void execInitAction() {
      super.execInitAction();
      setVisibleGranted(false);
    }
  }
}
