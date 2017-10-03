/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public abstract class AbstractContributionComposite implements IContributionOwner, Serializable {

  private static final long serialVersionUID = 1L;

  private Map<Class<?> /* type of the contribution */, ArrayList<?>/* contribution instances */> m_contributionsByType;
  private Map<Class<?> /* concrete contribution class */, Object /* contribution instance */> m_contributionsByClass;

  protected AbstractContributionComposite() {
    this(null, true);
  }

  protected AbstractContributionComposite(Object o, boolean useScope) {
    IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    try {
      if (useScope) {
        extensionRegistry.pushScope(getClass());
      }
      initContributionsMap(o, extensionRegistry, null);
      initConfig();
    }
    finally {
      if (useScope) {
        extensionRegistry.popScope();
      }
    }
  }

  protected void initConfig() {
  }

  private <T> void initContributionsMap(Object o, IInternalExtensionRegistry extensionRegistry, Class<T> type) {
    Object owner = this;
    if (o != null) {
      owner = o;
    }
    List<?> contributionsForMe = extensionRegistry.createContributionsFor(owner, type);
    if (type == null) {
      m_contributionsByType = new HashMap<>();
      m_contributionsByClass = new HashMap<>(contributionsForMe.size());
    }
    if (CollectionUtility.hasElements(contributionsForMe)) {
      for (Object contribution : contributionsForMe) {
        m_contributionsByClass.put(contribution.getClass(), contribution);
      }
    }
  }

  public <T> void resetContributionsByClass(Object o, Class<T> type) {
    m_contributionsByType.remove(type);
    m_contributionsByClass.values().removeIf(obj -> type.isAssignableFrom(obj.getClass()));
    IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    initContributionsMap(o, extensionRegistry, type);
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    if (m_contributionsByClass == null || m_contributionsByType == null) {
      // ensure that the contributions have been initialized
      IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
      initContributionsMap(null, extensionRegistry, null);
    }
  }

  protected Collection<Object> geAllContributionsInternal() {
    return m_contributionsByClass.values();
  }

  @Override
  public List<Object> getAllContributions() {
    return CollectionUtility.arrayList(geAllContributionsInternal());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> getContributionsByClass(Class<T> type) {
    if (type == null) {
      throw new IllegalArgumentException("Contribution type class must be specified.");
    }

    ArrayList<T> contributionsOfType = (ArrayList<T>) m_contributionsByType.get(type);
    if (contributionsOfType == null) {
      Collection<Object> values = m_contributionsByClass.values();
      contributionsOfType = new ArrayList<>(values.size());
      for (Object o : values) {
        if (type.isAssignableFrom(o.getClass())) {
          T contribution = type.cast(o);
          contributionsOfType.add(contribution);
        }
      }
      contributionsOfType.trimToSize();
      m_contributionsByType.put(type, contributionsOfType);
    }
    return CollectionUtility.arrayList(contributionsOfType);
  }

  @Override
  public <T> T getContribution(final Class<T> contribution) {
    final T result = optContribution(contribution);
    if (result != null) {
      return result;
    }
    throw new IllegalExtensionException("No contribution of type '" + contribution.getName() + "' exists for class '" + getClass().getName() + "'.");
  }

  @Override
  public <T> T optContribution(final Class<T> contribution) {
    if (contribution == null) {
      throw new IllegalArgumentException("Contribution class must be specified.");
    }

    // check in my contributions
    final Object object = m_contributionsByClass.get(contribution);
    if (object != null) {
      return contribution.cast(object);
    }

    // check nested contributions
    for (final Object o : m_contributionsByClass.values()) {
      if (o instanceof IContributionOwner) {
        final T result = ((IContributionOwner) o).optContribution(contribution);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }
}
