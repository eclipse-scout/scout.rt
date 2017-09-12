package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * Provides a lookup row.
 */
@FunctionalInterface
interface ILookupRowByKeyProvider<LOOKUP_KEY> {

  /**
   * @return {@link ILookupRow}, <code>null</code>, if not found
   */
  ILookupRow<LOOKUP_KEY> getLookupRow(LOOKUP_KEY key);

}
