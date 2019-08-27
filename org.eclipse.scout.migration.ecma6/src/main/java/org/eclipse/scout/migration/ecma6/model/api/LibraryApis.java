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
import java.util.List;

public class LibraryApis {

  private List<INamedElement> m_libraries;


  public LibraryApis(List<INamedElement> libraries){
    m_libraries = libraries;
  }


  public List<INamedElement> getElements(INamedElement.Type type, INamedElement parent){
    List<INamedElement> roots = new ArrayList<>();
    if(parent != null){
      roots.add(parent);
    }else{
      roots.addAll(m_libraries);
    }
    List<INamedElement> result = new ArrayList<>();
    roots.forEach(node -> node.visit(new INamedElementVisitor() {
      @Override
      public void visit(INamedElement element) {
        if(element.getType() == type){
          result.add(element);
        }
      }
    }));
    return result;
  }


}
