/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * A Collator provider that patches the default java {@link Collator} in order to handle the underscore character the
 * same as space and hyphen. The default ignores space and hyphen characters, but not underscores. E.g. the word "ABC
 * PQR" is treated as "ABCPQR".
 * <p>
 * <b>Please note:</b> This bean is <i>not</i> activated automatically when comparing Strings using scout (e.g. via
 * {@link StringUtility}). It uses the {@link CollatorProvider} that defines whether this bean is used or not. From
 * Scout 9.0.x onwards, this bean is used by default. Prior versions used the JVM default.
 *
 * <pre>
 * &#64;Replace
 * public class MyCollactorProvider extends CollatorProvider {
 *
 *   &#64;Override
 *   public Collator getInstance(Locale locale) {
 *     return BEANS.get(NaturalCollatorProvider.class).getInstance(locale);
 *   }
 * }
 * </pre>
 *
 * @see CollatorProvider
 * @see <a href=
 * "https://bugs.eclipse.org/bugs/show_bug.cgi?id=390097">https://bugs.eclipse.org/bugs/show_bug.cgi?id=390097</a>
 */
@ApplicationScoped
public class NaturalCollatorProvider {
  private static final ThreadLocal<Map<Locale, Collator>> THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);

  public Collator getInstance(Locale locale) {
    // always return a cloned instance
    return (Collator) THREAD_LOCAL.get()
        .computeIfAbsent(locale, this::create)
        .clone();
  }

  /**
   * Create a patched collator for a given locale, if the collator is a {@link RuleBasedCollator}. Otherwise the default
   * collator.
   *
   * @return {@link Collator} not <code>null</code>
   */
  protected Collator create(Locale locale) {
    Collator c = Collator.getInstance(locale);
    if (c instanceof RuleBasedCollator) {
      try {
        String origRules = ((RuleBasedCollator) c).getRules();
        return new RuleBasedCollator(replaceRules(origRules));
      }
      catch (ParseException e) {
        throw new ProcessingException("Collation rules cannot be parsed", e);
      }
    }
    return (Collator) c.clone();
  }

  /**
   * Extends the rules of an original collator in a way that it sorts spaces (<code> </code>, U+0020) and hyphens
   * (<code>-</code>, U+002D) before underscores (<code>_</code>, U+005F).
   *
   * @param origRules
   *     original rules
   * @return replaced rules
   */
  protected String replaceRules(String origRules) {
    return origRules.replaceAll("<'_'", "<' ','-'<'_'");
  }
}
