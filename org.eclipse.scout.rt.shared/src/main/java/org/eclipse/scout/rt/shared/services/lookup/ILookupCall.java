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

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;

/**
 *
 */
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
   * @throws ProcessingException
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByKey() throws ProcessingException;

  /**
   * @param caller
   * @return
   */
  JobEx getDataByKeyInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   * @throws ProcessingException
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByText() throws ProcessingException;

  /**
   * @param caller
   * @return
   */
  JobEx getDataByTextInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   * @throws ProcessingException
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByAll() throws ProcessingException;

  /**
   * @param caller
   * @return
   */
  JobEx getDataByAllInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   * @throws ProcessingException
   */
  List<? extends ILookupRow<KEY_TYPE>> getDataByRec() throws ProcessingException;

  /**
   * @param caller
   * @return
   */
  JobEx getDataByRecInBackground(ILookupCallFetcher<KEY_TYPE> caller);

  /**
   * @return
   */
  int getMaxRowCount();

  /**
   * @param n
   */
  void setMaxRowCount(int n);

}
