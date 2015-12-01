package org.eclipse.scout.rt.client.extension.ui.desktop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.index.IIndex;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Represents an index for which multiple elements can result in the very same index value.
 *
 * @since 5.1
 */
public abstract class AbstractDisplayParentViewIndex implements IIndex<IDisplayParent, IForm> {

  private final Map<IDisplayParent, List<IForm>> m_mapByIndex = new HashMap<>();
  private final Map<IForm, IDisplayParent> m_mapByElement = new LinkedHashMap<>(); // LinkedHashMap to preserve insertion-order.

  @Override
  public boolean addToIndex(final IForm element) {
    if (m_mapByElement.containsKey(element)) {
      removeFromIndex(element);
    }

    final IDisplayParent index = calculateIndexFor(element);
    if (index == null) {
      return false;
    }

    List<IForm> elements = m_mapByIndex.get(index);
    if (elements == null) {
      elements = new ArrayList<>();
      m_mapByIndex.put(index, elements);
    }
    int position = calculatePositionForElement(element);
    elements.add(position, element);
    m_mapByElement.put(element, index);

    return true;
  }

  /**
   * returns position where element should be inserted in form list.
   *
   * @param element
   *          element to calculate position.
   * @return
   */
  protected abstract int calculatePositionForElement(IForm element);

  @Override
  public boolean removeFromIndex(final IForm element) {
    final IDisplayParent index = m_mapByElement.remove(element);
    if (index == null) {
      return false;
    }

    final List<IForm> elements = m_mapByIndex.get(index);
    elements.remove(element);
    if (elements.isEmpty()) {
      m_mapByIndex.remove(index);
    }
    return true;
  }

  @Override
  public Set<IDisplayParent> indexValues() {
    return new HashSet<>(m_mapByIndex.keySet());
  }

  @Override
  public List<IForm> values() {
    return new ArrayList<>(m_mapByElement.keySet()); // ordered as inserted because LinkedHashMap is used
  }

  @Override
  public void clear() {
    m_mapByIndex.clear();
    m_mapByElement.clear();
  }

  @Override
  public boolean contains(final IForm element) {
    return m_mapByElement.containsKey(element);
  }

  @Override
  public Iterator<IForm> iterator() {
    return values().iterator();
  }

  /**
   * Returns the elements that correspond to the given index value in the order as inserted.
   *
   * @param index
   *          the index to look elements for.
   * @return elements ordered as inserted, or an empty {@link List} if no found.
   */
  public List<IForm> get(final IDisplayParent index) {
    return CollectionUtility.arrayList(m_mapByIndex.get(index));
  }

  /**
   * Method invoked to calculate the index value for the given element.
   *
   * @param element
   *          the element to calculate its index value.
   * @return the index value, or <code>null</code> to not add to the index.
   */
  protected IDisplayParent calculateIndexFor(IForm form) {
    if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
      return form.getDisplayParent();
    }
    else {
      return null;
    }
  }
}
