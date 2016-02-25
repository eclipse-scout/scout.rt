package org.eclipse.scout.rt.platform.index;

/**
 * Represents an index that is unique among all elements, e.g. an element's primary key.
 *
 * @since 6.0
 */
public interface ISingleValueIndex<INDEX, ELEMENT> extends IIndex<INDEX, ELEMENT> {

  /**
   * Returns the element that corresponds to the given index value.
   *
   * @param index
   *          the index to look the element for.
   * @return element, or <code>null</code> if not found.
   */
  ELEMENT get(INDEX index);
}
