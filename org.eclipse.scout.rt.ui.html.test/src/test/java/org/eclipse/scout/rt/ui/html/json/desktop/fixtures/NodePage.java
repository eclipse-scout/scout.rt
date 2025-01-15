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

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("445b5748-c577-4cf6-9b37-bfa9b20d01b7")
public class NodePage extends AbstractPageWithNodes {

  @Override
  protected String getConfiguredTitle() {
    return "Node";
  }
}
