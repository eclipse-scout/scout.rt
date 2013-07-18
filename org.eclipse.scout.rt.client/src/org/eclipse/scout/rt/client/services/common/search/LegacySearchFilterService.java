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
package org.eclipse.scout.rt.client.services.common.search;

import java.util.Date;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSessionThreadLocal;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.LegacyComposerStatementBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractUTCDateField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.services.common.jdbc.LegacySearchFilter;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * @deprecated
 */
@SuppressWarnings("deprecation")
@Deprecated
public class LegacySearchFilterService extends DefaultSearchFilterService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LegacySearchFilterService.class);

  @Override
  public SearchFilter createNewSearchFilter() {
    return new LegacySearchFilter();
  }

  public int resolveTokenIdByClass(Class<?> source) {
    throw new IllegalArgumentException("subclass should implement this method the resolve type to tokenId");
  }

  @Override
  public void applySearchDelegate(IFormField field, SearchFilter search, boolean includeChildren) {
    super.applySearchDelegate(field, search, false);
    if (search instanceof LegacySearchFilter) {
      if (applyInterceptor(field, (LegacySearchFilter) search)) {
        return;
      }
    }
    if (includeChildren) {
      applySearchDelegateForChildren(field, search);
    }
  }

  protected boolean applyInterceptor(IFormField field, LegacySearchFilter search) {
    //composer
    if (field instanceof AbstractComposerField) {
      AbstractComposerField composerField = (AbstractComposerField) field;
      ITreeNode rootNode = composerField.getTree().getRootNode();
      if (rootNode != null) {
        Object specialConstraint = new LegacyComposerStatementBuilder(search.getBindMap()).build(rootNode);
        if (specialConstraint != null) {
          try {
            search.addSpecialWhereToken(specialConstraint);
          }
          catch (ProcessingException e) {
            LOG.error("adding legacy search filter", e);
          }
        }
      }
      return true;
    }
    //string field
    if (field instanceof AbstractStringField) {
      AbstractStringField valueField = (AbstractStringField) field;
      String value = valueField.getValue();
      if (value != null && valueField.getLegacySearchTerm() != null) {
        if (ClientJob.getCurrentSession().getDesktop().isAutoPrefixWildcardForTextSearch()) {
          value = "*" + value;
        }
        try {
          search.addSpecialWhereToken(new LegacySearchFilter.StringLikeConstraint(valueField.getLegacySearchTerm(), value));
        }
        catch (ProcessingException e) {
          LOG.error("adding legacy search filter", e);
        }
      }
      return true;
    }
    //value
    if (field instanceof AbstractValueField<?>) {
      AbstractValueField<?> valueField = (AbstractValueField<?>) field;
      if (valueField.getValue() != null && valueField.getLegacySearchTerm() != null) {
        // shift date
        search.addWhereToken(valueField.getLegacySearchTerm(), shiftDateTimeFromTimeZone(valueField));
      }
      return true;
    }
    return false;
  }

  private Object shiftDateTimeFromTimeZone(AbstractValueField<?> valueField) {
    if (valueField.getValue() != null && valueField instanceof AbstractUTCDateField && ((AbstractUTCDateField) valueField).isHasTime()) {
      return ClientSessionThreadLocal.get().client2ServerDate(((Date) valueField.getValue()));
    }
    else {
      return valueField.getValue();
    }
  }

}
