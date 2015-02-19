/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.basic.activitymap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapActivityCellSelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapCellActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapCreateTimeScaleChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDecorateActivityCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDecorateMajorTimeColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDecorateMinorTimeColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDisposeActivityMapChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapInitActivityMapChain;
import org.eclipse.scout.rt.client.ui.basic.activitymap.AbstractActivityMap;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.MajorTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.activitymap.MinorTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale;
import org.eclipse.scout.rt.shared.extension.IExtension;

/**
 *
 */
public interface IActivityMapExtension<RI, AI, OWNER extends AbstractActivityMap<RI, AI>> extends IExtension<OWNER> {

  void execDecorateMinorTimeColumn(ActivityMapDecorateMinorTimeColumnChain<RI, AI> chain, TimeScale scale, MajorTimeColumn majorColumn, MinorTimeColumn minorColumn) throws ProcessingException;

  void execActivityCellSelected(ActivityMapActivityCellSelectedChain<RI, AI> chain, ActivityCell<RI, AI> cell) throws ProcessingException;

  void execDisposeActivityMap(ActivityMapDisposeActivityMapChain<RI, AI> chain) throws ProcessingException;

  TimeScale execCreateTimeScale(ActivityMapCreateTimeScaleChain<RI, AI> chain) throws ProcessingException;

  void execDecorateActivityCell(ActivityMapDecorateActivityCellChain<RI, AI> chain, ActivityCell<RI, AI> cell) throws ProcessingException;

  void execInitActivityMap(ActivityMapInitActivityMapChain<RI, AI> chain) throws ProcessingException;

  void execCellAction(ActivityMapCellActionChain<RI, AI> chain, RI resourceId, MinorTimeColumn column, ActivityCell<RI, AI> activityCell) throws ProcessingException;

  void execDecorateMajorTimeColumn(ActivityMapDecorateMajorTimeColumnChain<RI, AI> chain, TimeScale scale, MajorTimeColumn columns) throws ProcessingException;

}
