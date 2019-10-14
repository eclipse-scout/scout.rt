/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractFormMenu;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("9bb93504-0e4d-4492-bd9c-6432f2fea157")
public class DesktopWithNonDisplayableActions extends AbstractDesktop {

  @Override
  protected List<Class<? extends IOutline>> getConfiguredOutlines() {
    List<Class<? extends IOutline>> outlines = new ArrayList<>();
    outlines.add(OutlineWithOneNode.class);
    return outlines;
  }

  @Order(10)
  @ClassId("e3b25c35-2df9-41b5-8eb4-cdc385bfd9c0")
  public class DisplayableMenu extends AbstractMenu {
  }

  @Order(20)
  @ClassId("549f57b6-22de-430b-82b2-07481b13b286")
  public class NonDisplayableMenu extends AbstractMenu {

    @Override
    protected void execInitAction() {
      setVisibleGranted(false);
    }
  }

  @Order(30)
  @ClassId("b6102dec-df0c-4f43-b295-955c14a0dd1e")
  public class DisplayableFormMenu extends AbstractFormMenu {
  }

  @Order(40)
  @ClassId("7c287d3e-f15e-4af4-97e8-7970248de997")
  public class NonDisplayableFormMenu extends AbstractFormMenu {

    @Override
    protected void execInitAction() {
      setVisibleGranted(false);
    }
  }

  @Order(50)
  @ClassId("7c4967fc-26e6-49f5-a21e-88b34600f7ba")
  public class DisplayableOutlineViewButton extends AbstractOutlineViewButton {

    public DisplayableOutlineViewButton() {
      this(OutlineWithOneNode.class);
    }

    public DisplayableOutlineViewButton(Class<? extends IOutline> outlineType) {
      super(DesktopWithNonDisplayableActions.this, outlineType);
    }
  }

  @Order(60)
  @ClassId("10cbf377-ca4e-4c5c-97dd-da76619fdcac")
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
