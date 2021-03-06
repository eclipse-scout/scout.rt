/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
