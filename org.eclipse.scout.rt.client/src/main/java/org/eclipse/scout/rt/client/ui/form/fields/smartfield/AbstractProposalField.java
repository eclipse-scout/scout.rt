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

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.IProposalFieldExtension;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * This field is similar to the smart field but also allows custom text. A proposal field is always of the value type
 * {@link String}. The proposals are delivered as lookup rows of any type.
 */
public abstract class AbstractProposalField<LOOKUP_KEY> extends AbstractContentAssistField<String, LOOKUP_KEY> implements IProposalField<LOOKUP_KEY> {

  @SuppressWarnings("deprecation")
  private IContentAssistFieldUIFacadeLegacy m_uiFacadeLegacy;
  private IContentAssistFieldUIFacade m_uiFacade;

  public AbstractProposalField() {
    this(true);
  }

  public AbstractProposalField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected String getConfiguredIconId() {
    return AbstractIcons.ProposalFieldBrowse;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacadeLegacy = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacadeLegacy(), ModelContext.copyCurrent());
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new ContentAssistFieldUIFacade<LOOKUP_KEY>(this), ModelContext.copyCurrent());
  }

  @Override
  @SuppressWarnings("deprecation")
  public IContentAssistFieldUIFacadeLegacy getUIFacadeLegacy() {
    return m_uiFacadeLegacy;
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
  protected P_HandleResult handleNoCurrentLookupRowSet(String text) throws VetoException {
    return new P_HandleResult(text);
  }

  @Override
  protected IProposalChooser<?, LOOKUP_KEY> createProposalChooser() throws ProcessingException {
    return createProposalChooser(true);
  }

  @Override
  protected void filterKeyLookup(ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
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
        catch (ProcessingException e) {
          BEANS.get(ExceptionHandler.class).handle(e);
        }
      }
    }
  }

  @SuppressWarnings("deprecation")
  private class P_UIFacadeLegacy implements IContentAssistFieldUIFacadeLegacy {

    @Override
    public boolean setTextFromUI(String text) {
      String currentValidText = getValue();
      IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
      // accept proposal form if either input text matches search text or
      // existing display text is valid
      if (proposalChooser != null && proposalChooser.getAcceptedProposal() != null) {
        // a proposal was selected
        return acceptProposalFromUI();
      }
      if (proposalChooser != null && (StringUtility.equalsIgnoreNewLines(text, proposalChooser.getSearchText()) || StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(currentValidText)))) {
        /*
         * empty text means null
         */
        if (text == null || text.length() == 0) {
          return parseValue(text);
        }
        else {
          // no proposal was selected...
          if (!StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(currentValidText))) {
            return parseValue(text);
          }
          else {
            // ... and current display is unchanged from model value -> nop
            unregisterProposalChooserInternal();
            return true;
          }
        }

      }
      else {
        /*
         * ticket 88359
         * check if changed at all
         */
        if (CompareUtility.equals(text, currentValidText)) {
          return true;
        }
        else {
          return parseValue(text);
        }
      }
    }

    @Override
    public void openProposalFromUI(String searchText, boolean selectCurrentValue) {
      if (searchText == null) {
        searchText = BROWSE_ALL_TEXT;
      }
      IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
      if (proposalChooser == null) {
        setActiveFilter(TriState.TRUE);
        doSearch(searchText, selectCurrentValue, false);
      }
      else {
        if (!StringUtility.equalsIgnoreNewLines(getLookupRowFetcher().getLastSearchText(), searchText)) {
          doSearch(searchText, false, false);
        }
      }
    }

    @Override
    public boolean acceptProposalFromUI() {
      try {
        IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
        if (proposalChooser != null) {
          if (proposalChooser.getAcceptedProposal() != null) {
            acceptProposal();
            return true;
          }
          else {
            // allow with null text traverse
            if (StringUtility.isNullOrEmpty(getDisplayText())) {
              return true;
            }
            else {
              // select first
              proposalChooser.forceProposalSelection();
              return false;
            }
          }
        }
      }
      catch (ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
      return false;
    }

    @Override
    public void closeProposalFromUI() {
      unregisterProposalChooserInternal();
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
