/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.gen.extract.form;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Extracts the title texts for a {@link IForm}
 */
public class FormTitleExtractor extends AbstractNamedTextExtractor<IForm> {

  private boolean m_anchor;

  public FormTitleExtractor(boolean anchor) {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
    m_anchor = anchor;
  }

  /**
   * @param {@link IForm}
   * @return the title of the form
   */
  @Override
  public String getText(IForm form) {
    StringBuilder sb = new StringBuilder();
    if (m_anchor) {
      sb.append(MediawikiUtility.createAnchor(getAnchorId(form)));
    }
    sb.append(MediawikiUtility.transformToWiki(form.getTitle()));
    return sb.toString();
  }

  public static String getAnchorId(IForm form) {
    return "c_" + form.classId();
  }

}
