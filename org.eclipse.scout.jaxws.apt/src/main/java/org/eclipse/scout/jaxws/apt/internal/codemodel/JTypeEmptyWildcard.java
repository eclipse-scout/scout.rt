/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.jaxws.apt.internal.codemodel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JTypeVar;

public final class JTypeEmptyWildcard extends JClass {

  JTypeEmptyWildcard(JCodeModelWrapper model) {
    super(model.getModel());
  }

  @Override
  public String fullName() {
    return name();
  }

  @Override
  public String name() {
    return "?";
  }

  @Override
  public JPackage _package() {
    return owner()._package("");
  }

  @Override
  public JClass _extends() {
    return null;
  }

  @Override
  public Iterator<JClass> _implements() {
    return Collections.emptyIterator();
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  @Override
  protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings) {
    return this;
  }

  @Override
  public void generate(JFormatter f) {
    f.p(name());
  }

}
