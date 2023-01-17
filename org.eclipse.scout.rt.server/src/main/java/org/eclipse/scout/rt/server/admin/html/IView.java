/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.html;

import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;

public interface IView {

  void produceTitle(HtmlComponent p);

  void produceBody(HtmlComponent p);

  boolean isVisible();

  void activated();
}
