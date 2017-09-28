/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.outline.pages.TablePageExtensionTest;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;

/**
 * Desktop used for {@link TablePageExtensionTest#testAllPersonTablePageAlongWithDesktopOutlineAndExtendedSearchForm()}
 *
 * @since 6.0
 */
public class PersonPageTestDesktop extends AbstractDesktop {

  @Override
  protected List<Class<? extends IOutline>> getConfiguredOutlines() {
    List<Class<? extends IOutline>> outlines = new ArrayList<>();
    outlines.add(PersonPageTestOutline.class);
    return outlines;
  }

  @Order(1000)
  public class DevMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return "DEV";
    }

    @Override
    protected boolean getConfiguredVisible() {
      return Platform.get().inDevelopmentMode();
    }
  }

  public static class PersonPageTestOutline extends AbstractOutline {

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new AllPersonTablePage());
    }
  }
}
