package org.eclipse.scout.rt.platform.index;

import java.util.List;

/**
 * Represents an index for which multiple elements can result in the very same index value.
 *
 * @since 6.0
 */
public interface IMultiValueIndex<INDEX, ELEMENT> extends IIndex<INDEX, ELEMENT> {

  /**
   * Returns the elements that correspond to the given index value in the order as inserted.
   *
   * @param index
   *          the index to look elements for.
   * @return elements ordered as inserted, or an empty {@link List} if no found.
   */
  List<ELEMENT> get(INDEX index);
}
