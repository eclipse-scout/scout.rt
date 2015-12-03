/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.search;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.TokenBasedComposerStatementBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.AndNodeToken;

@Order(5200)
public class TokenBasedSearchFilterService extends DefaultSearchFilterService {

  @Override
  public SearchFilter createNewSearchFilter() {
    return new TokenBasedSearchFilter();
  }

  public Integer resolveTokenIdByClass(Class<?> source) {
    throw new IllegalArgumentException("subclass should implement this method the resolve type to tokenId");
  }

  @Override
  public void applySearchDelegate(IFormField field, SearchFilter search, boolean includeChildren) {
    if (search instanceof TokenBasedSearchFilter) {
      applyInterceptor(field, (TokenBasedSearchFilter) search);
    }
    super.applySearchDelegate(field, search, includeChildren);
  }

  protected void applyInterceptor(IFormField field, TokenBasedSearchFilter search) {
    //composer
    if (field instanceof AbstractComposerField) {
      AbstractComposerField composerField = (AbstractComposerField) field;
      ITreeNode rootNode = composerField.getTree().getRootNode();
      if (rootNode != null) {
        AndNodeToken rootTok = new TokenBasedComposerStatementBuilder(this).build(rootNode);
        if (rootTok != null) {
          search.addTreeToken(rootTok);
        }
      }
      return;
    }
    //string field
    if (field instanceof AbstractStringField) {
      AbstractStringField valueField = (AbstractStringField) field;
      String value = valueField.getValue();
      if (value != null) {
        if (ClientSessionProvider.currentSession().getDesktop().isAutoPrefixWildcardForTextSearch()) {
          value = "*" + value;
        }
        search.addWildcardStringToken(resolveTokenIdByClass(field.getClass()), value);
      }
      return;
    }
    //value
    if (field instanceof AbstractValueField<?>) {
      AbstractValueField<?> valueField = (AbstractValueField<?>) field;
      if (valueField.getValue() != null) {
        search.addToken(resolveTokenIdByClass(field.getClass()), valueField.getValue());
      }
      return;
    }
  }

}
