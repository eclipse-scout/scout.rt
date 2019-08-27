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
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NamedElement implements INamedElement {

  private Type m_type;
  private String m_name;
  private INamedElement m_parent;
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
  public String getFullyQuallifiedName() {
    if (getType() == Type.Constructor) {
      return getParent().getFullyQuallifiedName();
    }
    StringBuilder nameBuilder = new StringBuilder();
    if (getParent() != null) {
      nameBuilder.append(getParent().getFullyQuallifiedName())
          .append(".");

    }
    nameBuilder.append(getName());
    return nameBuilder.toString();
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
    m_children.addAll(children);
  }

  public List<INamedElement> getElements(INamedElement.Type type) {
    return getElements(type, null);
  }

  @Override
  public List<INamedElement> getElements(Type type, Predicate<INamedElement> filter) {
    List<INamedElement> result = new ArrayList<>();
    this.visit(new INamedElementVisitor() {
      @Override
      public void visit(INamedElement element) {
        if (element.getType() == type && (filter == null || filter.test(element))) {
          result.add(element);
        }
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
