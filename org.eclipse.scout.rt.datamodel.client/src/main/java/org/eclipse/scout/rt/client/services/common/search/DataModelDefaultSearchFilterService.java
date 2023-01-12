/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.search;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerDisplayTextBuilder;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@Replace
public class DataModelDefaultSearchFilterService extends DefaultSearchFilterService {

  @Override
  public SearchFilter createNewSearchFilter() {
    return new SearchFilter();
  }

  @Override
  public void applySearchDelegate(IFormField field, SearchFilter search, boolean includeChildren) {
    super.applySearchDelegate(field, search, includeChildren);

    //composer
    if (field instanceof AbstractComposerField) {
      AbstractComposerField composerField = (AbstractComposerField) field;
      ITreeNode rootNode = composerField.getTree().getRootNode();
      if (rootNode != null) {
        StringBuilder buf = new StringBuilder();
        new ComposerDisplayTextBuilder().build(rootNode, buf, "");
        String s = buf.toString();
        if (StringUtility.hasText(s)) {
          search.addDisplayText(s);
        }
      }
    }
  }
}
