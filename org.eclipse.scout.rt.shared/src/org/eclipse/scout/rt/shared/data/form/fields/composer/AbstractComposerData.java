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
package org.eclipse.scout.rt.shared.data.form.fields.composer;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;

/**
 * Data representation for a composer tree field
 */
public abstract class AbstractComposerData extends AbstractTreeFieldData {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractComposerData.class);
  private static final long serialVersionUID = 1L;

  public AbstractComposerData() {
  }

}
