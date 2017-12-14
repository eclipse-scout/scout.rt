/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.tagfield;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable value containing tags, use by ITagField.
 */
public class TagFieldValue {

  private final Set<String> m_tags;

  public TagFieldValue(Set<String> tags) {
    m_tags = new HashSet<>(tags);
  }

  public Set<String> getTags() {
    return Collections.unmodifiableSet(m_tags);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_tags == null) ? 0 : m_tags.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TagFieldValue other = (TagFieldValue) obj;
    if (m_tags == null) {
      if (other.m_tags != null) {
        return false;
      }
    }
    else if (!m_tags.equals(other.m_tags)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return TagFieldValue.class.getSimpleName() + "[tags=" + m_tags + "]";
  }

}
