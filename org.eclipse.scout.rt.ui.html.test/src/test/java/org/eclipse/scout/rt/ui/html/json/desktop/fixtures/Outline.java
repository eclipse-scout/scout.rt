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

@ClassId("51fc8662-6d1e-4276-b660-2ab46c072826")
public class Outline extends AbstractOutline {
  private List<IPage<?>> m_pages;

  public Outline(List<IPage<?>> pages) {
    super(false);
    m_pages = pages;
    callInitializer();
  }

  @Override
  protected void execCreateChildPages(List<IPage<?>> pageList) {
    for (IPage page : m_pages) {
      pageList.add(page);
    }
  }
}
