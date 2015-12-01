/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.TriState;

@Bean
public interface ILookupCall<KEY_TYPE> extends Serializable, Cloneable {

  /**
   * @return
   */
  KEY_TYPE getKey();

  /**
   * @param object
   */
  void setKey(KEY_TYPE object);

  /**
   * @param object
   */
  void setText(String object);

  /**
   * @param s
   */
  void setAll(String s);

  /**
   * @return
   */
  String getAll();

  /**
   * @param parent
   */
  void setRec(KEY_TYPE parent);

  /**
   * @return
   */
  KEY_TYPE getRec();

  /**
   * @param master
   */
  void setMaster(Object master);

  /**
   * @return
   */
  Object getMaster();

  /**
   * @param activeState
   */
  void setActive(TriState activeState);

  /**
   * @return
   */
  TriState getActive();

  /**
   * @return
   */
  String getText();

  /**
   * @return
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByKey();

  /**
   * @param caller
   * @return
   */
  IFuture<?> getDataByKeyInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByText();

  /**
   * @param caller
   * @return
   */
  IFuture<?> getDataByTextInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByAll();

  /**
   * @param caller
   * @return
   */
  IFuture<?> getDataByAllInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByRec();

  /**
   * @param caller
   * @return
   */
  IFuture<?> getDataByRecInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   */
  int getMaxRowCount();

  /**
   * @param n
   */
  void setMaxRowCount(int n);

  String getWildcard();

  void setWildcard(String wildcard);

}
