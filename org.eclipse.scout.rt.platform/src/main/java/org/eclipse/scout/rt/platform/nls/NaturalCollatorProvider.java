/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.nls;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
 *      "https://bugs.eclipse.org/bugs/show_bug.cgi?id=390097">https://bugs.eclipse.org/bugs/show_bug.cgi?id=390097</a>
 */
@ApplicationScoped
public class NaturalCollatorProvider {

  private final ConcurrentMap<Locale, Collator> m_cache = new ConcurrentHashMap<>();

  public Collator getInstance(Locale locale) {
    Collator result = m_cache.get(locale);
    if (result == null) {
      result = create(locale);
      Collator tmp = m_cache.putIfAbsent(locale, result);
      if (tmp != null) {
        // always used same shared instance
        result = tmp;
      }
    }
    // always return a cloned instance
    return (Collator) result.clone();
  }

  /**
   * Create a patched collator for a given locale, if the collator is a {@link RuleBasedCollator}. Otherwise the default
   * collator.
   *
   * @param locale
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
    return c;
  }

  /**
   * Extends the rules of an original collator in a way that it sorts spaces (<code>\u0020</code>) and hyphens
   * (<code>\u002D</code>) before underscores (<code>\u005f</code>).
   *
   * @param origRules
   *          original rules
   * @return replaced rules
   */
  protected String replaceRules(String origRules) {
    return origRules.replaceAll("<'\u005f'", "<'\u0020','\u002D'<'\u005f'");
  }
}
