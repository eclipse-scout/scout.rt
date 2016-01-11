package org.eclipse.scout.rt.platform.nls;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * A Provider for {@link Collator}s to simplify replacements.
 * <h3>{@link CollatorProvider}</h3>
 *
 * @author jgu
 */
@ApplicationScoped
public class CollatorProvider {

  public Collator getInstance() {
    return getInstance(NlsLocale.get());
  }

  /**
   * @param desiredLocale
   * @return a collator for the desired locale
   */
  public Collator getInstance(Locale desiredLocale) {
    return Collator.getInstance(desiredLocale);
  }

}
