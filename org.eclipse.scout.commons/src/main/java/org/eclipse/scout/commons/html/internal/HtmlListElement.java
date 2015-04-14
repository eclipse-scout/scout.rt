/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.html.internal;

import org.eclipse.scout.commons.html.IHtmlListElement;

public class HtmlListElement extends HtmlNodeBuilder implements IHtmlListElement {

  public HtmlListElement(CharSequence text) {
    super("li", text);
  }

}
