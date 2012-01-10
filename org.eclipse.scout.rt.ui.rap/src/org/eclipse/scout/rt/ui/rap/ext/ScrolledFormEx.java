/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

public class ScrolledFormEx extends SharedScrolledComposite {
  private static final long serialVersionUID = 1L;

  private Form m_content;

  public ScrolledFormEx(Composite parent, int style) {
    super(parent, style);
    setExpandHorizontal(true);
    setExpandVertical(true);
    m_content = new Form(this, SWT.NONE);
    super.setContent(m_content);
  }

  public Composite getBody() {
    return m_content.getBody();
  }

  /**
   * Returns the instance of the form owned by the scrolled form.
   * 
   * @return the form instance
   */
  public Form getForm() {
    return m_content;
  }

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    Point size = getBody().computeSize(wHint, hHint, changed);
    return size;
  }

  @Override
  public void reflow(boolean flushCache) {
    super.reflow(flushCache);
  }

}
