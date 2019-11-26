/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.model.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NamedElement implements INamedElement {

  private Type m_type;
  private String m_name;
  private INamedElement m_parent;
  private String m_fullyQualifiedName;
  private Map<String, Object> m_customAttributes = new HashMap<>();
  private List<INamedElement> m_children = new ArrayList<>();

  public NamedElement() {
    this(null, null);
  }

  public NamedElement(Type type, String name) {
    this(type, name, null);
  }

  public NamedElement(Type type, String name, INamedElement parent) {
    m_type = type;
    m_name = name;
    m_parent = parent;
  }

  public NamedElement(Type type, String name, String fullyQualifiedName, INamedElement parent) {
    m_type = type;
    m_name = name;
    m_fullyQualifiedName = fullyQualifiedName;
    m_parent = parent;
  }

  @Override
  public Type getType() {
    return m_type;
  }

  void setType(Type type) {
    m_type = type;
  }

  @Override
  public String getName() {
    return m_name;
  }

  void setName(String name) {
    m_name = name;
  }

  @Override
  public String getFullyQualifiedName() {
    if (getType() == Type.Constructor) {
      return getParent().getFullyQualifiedName();
    }

    if (m_fullyQualifiedName != null) {
      return m_fullyQualifiedName;
    }

    StringBuilder nameBuilder = new StringBuilder();
    if (getType() != Type.Library && getParent() != null) {
      nameBuilder
          .append(getParent().getFullyQualifiedName())
          .append(".");
    }
    nameBuilder.append(getName());
    return nameBuilder.toString();
  }

  public void setFullyQualifiedName(String fqn) {
    m_fullyQualifiedName = fqn;
  }

  @Override
  public Map<String, Object> getCustomAttributes() {
    return m_customAttributes;
  }

  public void setCustomAttributes(Map<String, ?> customAttributes) {
    m_customAttributes.putAll(customAttributes);
  }

  public void addCustomAttribute(String key, Object value) {
    m_customAttributes.put(key, value);
  }

  @Override
  public Object getCustomAttribute(String key) {
    return m_customAttributes.get(key);
  }

  @Override
  public String getCustomAttributeString(String key) {
    return (String) getCustomAttribute(key);
  }

  @JsonIgnore
  @Override
  public INamedElement getParent() {
    return m_parent;
  }

  @JsonIgnore
  @Override
  public INamedElement getAncestor(Predicate<INamedElement> filter) {
    if (filter.test(this)) {
      return this;
    }
    return getParent().getAncestor(filter);
  }

  @JsonIgnore
  @Override
  public INamedElement getAncestor(Type type) {
    return getAncestor(ne -> ne.getType() == type);
  }

  @Override
  public void setParent(INamedElement parent) {
    m_parent = parent;
  }

  @Override
  public List<INamedElement> getChildren() {
    return Collections.unmodifiableList(m_children);
  }

  void setChildren(List<INamedElement> children) {
    m_children = children;
  }

  void addChildren(List<INamedElement> children) {
    Set<String> fqns = children.stream().map(c -> c.getFullyQualifiedName()).collect(Collectors.toSet());
    List<INamedElement> newKids = m_children.stream().filter(c -> !fqns.contains(c.getFullyQualifiedName())).collect(Collectors.toList());
    children.forEach(c -> c.setParent(this));
    newKids.addAll(children);
    m_children = newKids;
  }

  @Override
  public List<INamedElement> getElements(INamedElement.Type type) {
    return getElements(type, null);
  }

  @Override
  public List<INamedElement> getElements(Type type, Predicate<INamedElement> filter) {
    List<INamedElement> result = new ArrayList<>();
    this.visit(element -> {
      if (element.getType() == type && (filter == null || filter.test(element))) {
        result.add(element);
      }
    });
    return result;
  }

  @Override
  public void visit(INamedElementVisitor visitor) {
    visitor.visit(this);
    m_children.forEach(child -> child.visit(visitor));
  }

}
