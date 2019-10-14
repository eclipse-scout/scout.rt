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

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("445b5748-c577-4cf6-9b37-bfa9b20d01b7")
public class NodePage extends AbstractPageWithNodes {

  @Override
  protected String getConfiguredTitle() {
    return "Node";
  }

}
