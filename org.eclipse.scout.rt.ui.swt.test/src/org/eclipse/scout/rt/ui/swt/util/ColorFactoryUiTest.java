/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ColorFactory}
 * 
 * @since 3.9.0
 */
public class ColorFactoryUiTest {

	private ColorFactory m_colorFactory;

	@Before
	public void before() {
		m_colorFactory = new ColorFactory(Display.getDefault());
	}

	@After
	public void after() {
		m_colorFactory.dispose();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.scout.rt.ui.swt.util.ColorFactory#getColor(org.eclipse.swt.graphics.RGB)}
	 * .
	 */
	@Test
	public void testGetColorRGB() {
		runGetColorRGB(0, 0, 0);
		runGetColorRGB(128, 7, 0);
		runGetColorRGB(200, 255, 45);
	}

	private void runGetColorRGB(int red, int green, int blue) {
		Color color = m_colorFactory.getColor(new RGB(red, green, blue));
		assertColorEquals(red, green, blue, color);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.scout.rt.ui.swt.util.ColorFactory#getColor(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetColorString() {
		runGetColorString("000000", 0, 0, 0);
		runGetColorString("C9F7a2", 201, 247, 162);
		runGetColorString("FFFFFF", 255, 255, 255);
		runGetColorString("ffffff", 255, 255, 255);
	}

	private void runGetColorString(String scoutColor, int red, int green,
			int blue) {
		Color color = m_colorFactory.getColor(scoutColor);
		assertColorEquals(red, green, blue, color);
	}

	private static void assertColorEquals(int red, int green, int blue,
			Color color) {
		Assert.assertEquals("Color red", red, color.getRed());
		Assert.assertEquals("Color green", green, color.getGreen());
		Assert.assertEquals("Color blue", blue, color.getBlue());
	}
}
