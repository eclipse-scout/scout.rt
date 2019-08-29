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
package org.eclipse.scout.migration.ecma6.model.references;

import org.eclipse.scout.migration.ecma6.context.Context;

public class UnresolvedImport extends AbstractImport<UnresolvedImport> {
  private String m_fullyQualifiedName;

  public UnresolvedImport(String fullyQualifiedName){
    m_fullyQualifiedName = fullyQualifiedName;
  }


  @Override
  public String getModuleName() {
    return "TODO (mig could not determ library for '"+m_fullyQualifiedName+"')";
  }
}
