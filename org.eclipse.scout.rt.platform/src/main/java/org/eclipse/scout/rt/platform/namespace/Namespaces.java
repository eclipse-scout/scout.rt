/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.namespace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Namespaces {

  private static final Logger LOG = LoggerFactory.getLogger(Namespaces.class);

  private final LinkedHashMap<String, INamespace> m_namespaces = new LinkedHashMap<>();

  @PostConstruct
  protected void init() {
    List<INamespace> namespaces = ClassInventory.get().getAllKnownSubClasses(INamespace.class).stream()
        .filter(IClassInfo::isInstanciable)
        .map(classInfo -> (INamespace) BeanInstanceUtil.createBean(classInfo.resolveClass()))
        .sorted(Comparator.comparing(INamespace::getOrder).thenComparing(namespace -> namespace.getClass().getName())) // FQN fallback in case of identical orders
        .collect(Collectors.toList());

    // Validate presence of ID
    namespaces.stream()
        .filter(namespace -> StringUtility.isNullOrEmpty(namespace.getId()))
        .forEach(namespace -> LOG.error("Namespace without an ID detected: {}", namespace.getClass()));

    // Validate ID uniqueness
    namespaces.stream()
        .filter(namespace -> !StringUtility.isNullOrEmpty(namespace.getId())) // ignore those logged before
        .collect(Collectors.groupingBy(INamespace::getId))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().size() > 1) // only those IDs with more than one namespace class
        .forEach(entry -> LOG.error("Non-unique namespace detected id={}, values=[{}]", entry.getKey(), entry.getValue().stream().map(namespace -> namespace.getClass().getName()).collect(Collectors.joining(", "))));

    // If there are non-unique IDs, only keep the first namespace
    namespaces.forEach(namespace -> getNamespaces().putIfAbsent(namespace.getId(), namespace));
  }

  public static Namespaces get() {
    return BEANS.get(Namespaces.class);
  }

  protected LinkedHashMap<String, INamespace> getNamespaces() {
    return m_namespaces;
  }

  /**
   * @return All {@link INamespace} sorted by their order.
   */
  public List<INamespace> all() {
    return new ArrayList<>(getNamespaces().values());
  }

  /**
   * @return Namespace with the given id or <code>null</code> if none is found.
   */
  public INamespace byId(String id) {
    return getNamespaces().get(id);
  }
}
