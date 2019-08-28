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

public interface IImport {


  AliasedMember getDefaultMember();

  AliasedMember findAliasedMember(String name);

  void addMember(AliasedMember aliasedMember);
  String toSource(Context context);
}
