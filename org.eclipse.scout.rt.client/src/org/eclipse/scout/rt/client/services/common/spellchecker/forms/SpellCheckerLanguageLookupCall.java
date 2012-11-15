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
package org.eclipse.scout.rt.client.services.common.spellchecker.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsUtility;
import org.eclipse.scout.rt.client.services.common.spellchecker.ISpellCheckerService;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

public class SpellCheckerLanguageLookupCall extends LocalLookupCall {

  private static final long serialVersionUID = 1L;

  @Override
  protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
    TreeMap<String, LookupRow> sortMap = new TreeMap<String, LookupRow>();
    ISpellCheckerService sc = SERVICES.getService(ISpellCheckerService.class);
    if (sc != null) {
      for (String lang : sc.getAvailableLanguages()) {
        Locale loc = NlsUtility.parseLocale(lang);
        sortMap.put(lang, new LookupRow(lang, loc.getDisplayLanguage() + " (" + loc.getDisplayCountry() + ")"));
      }
    }
    return new ArrayList<LookupRow>(sortMap.values());
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
