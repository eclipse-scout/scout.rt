/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("67078cc1-abec-4063-b6b4-2d87ed39c657")
public class NonDisplayableOutlineWithOneNode extends AbstractOutline {

  @Override
  protected void execCreateChildPages(List<IPage<?>> pageList) {
    NodePage nodePage = new NodePage();
    pageList.add(nodePage);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setVisibleGranted(false);
  }
}
