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
package com.bsiag.scout.rt.client.ui.form.fields.chartfield;

import java.math.BigDecimal;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

/**
 *
 */
public class ChartEvent extends EventObject implements IModelEvent {

	public static final int TYPE_VALUE_CLICKED = 1;

	private static final long serialVersionUID = 1L;

	private final int m_type;

	private int m_axisPosition[];

	private BigDecimal m_value;

	public ChartEvent(IChart source, int type) {
		super(source);
		m_type = type;
	}

	@Override
	public IChart getSource() {
		return (IChart) super.getSource();
	}

	@Override
	public int getType() {
		return m_type;
	}

	/**
	 * Used for TYPE_VALUE_CLICKED
	 */
	public int[] getAxisPosition() {
		return m_axisPosition;
	}

	/**
	 * Used for TYPE_VALUE_CLICKED
	 */
	public void setAxesPosition(int axisPosition[]) {
		m_axisPosition = axisPosition;
	}

	public BigDecimal getValue() {
		return m_value;
	}

	public void setValue(BigDecimal m_value) {
		this.m_value = m_value;
	}

}
