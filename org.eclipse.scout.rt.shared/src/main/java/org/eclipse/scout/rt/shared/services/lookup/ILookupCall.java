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
package org.eclipse.scout.rt.shared.services.lookup;

import java.io.Serializable;
import java.util.List;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.TriState;

@Bean
public interface ILookupCall<KEY_TYPE> extends Serializable, Cloneable {

  /**
   * @return In case lookup call is a "by key" query, getAll returns the key
   */
  KEY_TYPE getKey();

  void setKey(KEY_TYPE object);

  void setText(String object);

  void setAll(String s);

  /**
   * @return In case lookup call is a "by all" query, getAll returns the wildcard character
   */
  String getAll();

  void setRec(KEY_TYPE parent);

  /**
   * @return In case lookup call is a "by rec" query (which is a lookup by parent key), getRec returns the parent key
   */
  KEY_TYPE getRec();

  void setMaster(Object master);

  Object getMaster();

  void setActive(TriState activeState);

  TriState getActive();

  /**
   * @return In case lookup call is a "by text" query , getText returns the search text
   */
  String getText();

  List<? extends ILookupRow<KEY_TYPE>> getDataByKey();

  IFuture<Void> getDataByKeyInBackground(RunContext runContext, ILookupRowFetchedCallback<KEY_TYPE> callback);

  List<? extends ILookupRow<KEY_TYPE>> getDataByText();

  IFuture<Void> getDataByTextInBackground(RunContext runContext, ILookupRowFetchedCallback<KEY_TYPE> callback);

  List<? extends ILookupRow<KEY_TYPE>> getDataByAll();

  IFuture<Void> getDataByAllInBackground(RunContext runContext, ILookupRowFetchedCallback<KEY_TYPE> callback);

  List<? extends ILookupRow<KEY_TYPE>> getDataByRec();

  IFuture<Void> getDataByRecInBackground(RunContext runContext, ILookupRowFetchedCallback<KEY_TYPE> callback);

  int getMaxRowCount();

  void setMaxRowCount(int n);

  String getWildcard();

  void setWildcard(String wildcard);

  void setMultilineText(boolean b);

  boolean isMultilineText();
}
