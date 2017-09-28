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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

public interface IQueryParam<T> {

  public enum QueryBy {
    ALL,
    TEXT,
    KEY,
    REC
  }

  QueryBy getQueryBy();

  boolean is(QueryBy parentKey);

  T getKey();

  String getText();

}
