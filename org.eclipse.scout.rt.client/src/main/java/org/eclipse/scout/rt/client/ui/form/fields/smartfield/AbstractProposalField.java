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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.IProposalFieldExtension;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * This field is similar to the smart field but also allows custom text. A proposal field is always of the value type
 * {@link String}. The proposals are delivered as lookup rows of any type.
 */
public abstract class AbstractProposalField<LOOKUP_KEY> extends AbstractContentAssistField<String, LOOKUP_KEY> implements IProposalField<LOOKUP_KEY> {

  private IContentAssistFieldUIFacade m_uiFacade;

  public AbstractProposalField() {
    this(true);
  }

  public AbstractProposalField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new ContentAssistFieldUIFacade<LOOKUP_KEY>(this), ModelContext.copyCurrent());
  }

  @Override
  public IContentAssistFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void applyLazyStyles() {
  }

  @Override
  public LOOKUP_KEY getValueAsLookupKey() {
    return null;
  }

  @Override
  public void acceptProposal(ILookupRow<LOOKUP_KEY> row) {
    setCurrentLookupRow(row);
    setValue(row.getText());
  }

  @Override
  protected void installLookupRowContext(ILookupRow<LOOKUP_KEY> row) {
    setCurrentLookupRow(row);
    super.installLookupRowContext(row);
  }

  @Override
  protected String formatValueInternal(String rawValue) {
    ILookupRow<LOOKUP_KEY> currentLookupRow = getCurrentLookupRow();
    if (currentLookupRow != null) {
      installLookupRowContext(currentLookupRow);
      String lookupRowText = StringUtility.emptyIfNull(currentLookupRow.getText());
      String rawValueText = StringUtility.emptyIfNull(rawValue);
      if (!lookupRowText.equals(rawValueText)) {
        if (isMultilineText()) {
          return lookupRowText;
        }
        else {
          return lookupRowText.replaceAll("[\\n\\r]+", " ");
        }
      }
    }
    return rawValue;
  }

  @Override
  protected String returnLookupRowAsValue(ILookupRow<LOOKUP_KEY> lookupRow) {
    return lookupRow.getText();
  }

  @Override
  protected boolean lookupRowMatchesValue(ILookupRow<LOOKUP_KEY> lookupRow, String value) {
    return CompareUtility.equals(lookupRow.getText(), value);
  }

  @Override
  protected String handleMissingLookupRow(String text) {
    return text;
  }

  @Override
  protected boolean handleAcceptByDisplayText(String text) {
    setValue(text);
    return false;
  }

  @Override
  protected IProposalChooser<?, LOOKUP_KEY> createProposalChooser() {
    return createProposalChooser(true);
  }

  @Override
  protected void filterKeyLookup(ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) {
    super.filterKeyLookup(call, result);
    /*
     * ticket 79027
     */
    if (result.size() == 0) {
      String key = "" + call.getKey();
      result.add(new LookupRow<LOOKUP_KEY>(call.getKey(), key));
    }
  }

  @Override
  protected void handleFetchResult(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
    if (result == null) {
      unregisterProposalChooserInternal();
    }
    else {
      Collection<? extends ILookupRow<LOOKUP_KEY>> rows = result.getLookupRows();
      if (rows == null || rows.isEmpty()) {
        unregisterProposalChooserInternal();
      }
      else {
        try {
          if (proposalChooser == null) {
            proposalChooser = registerProposalChooserInternal();
          }
          proposalChooser.dataFetchedDelegate(result, getBrowseMaxRowCount());
        }
        catch (RuntimeException e) {
          BEANS.get(ExceptionHandler.class).handle(e);
        }
      }
    }
  }

  protected static class LocalProposalFieldExtension<LOOKUP_KEY, OWNER extends AbstractProposalField<LOOKUP_KEY>> extends LocalContentAssistFieldExtension<String, LOOKUP_KEY, OWNER> implements IProposalFieldExtension<LOOKUP_KEY, OWNER> {

    public LocalProposalFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IProposalFieldExtension<LOOKUP_KEY, ? extends AbstractProposalField<LOOKUP_KEY>> createLocalExtension() {
    return new LocalProposalFieldExtension<LOOKUP_KEY, AbstractProposalField<LOOKUP_KEY>>(this);
  }

}
