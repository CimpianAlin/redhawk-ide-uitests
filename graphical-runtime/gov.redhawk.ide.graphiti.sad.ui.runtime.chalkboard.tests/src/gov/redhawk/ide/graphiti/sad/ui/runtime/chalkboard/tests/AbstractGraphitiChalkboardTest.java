/*******************************************************************************
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package gov.redhawk.ide.graphiti.sad.ui.runtime.chalkboard.tests;

import org.junit.After;
import org.junit.Before;

import gov.redhawk.ide.swtbot.UIRuntimeTest;
import gov.redhawk.ide.swtbot.diagram.RHSWTGefBot;
import gov.redhawk.ide.swtbot.scaExplorer.ScaExplorerTestUtils;

/**
 * 
 */
public abstract class AbstractGraphitiChalkboardTest extends UIRuntimeTest {

	static final String[] CHALKBOARD_PARENT_PATH = { "Sandbox" };
	static final String[] CHALKBOARD_PATH = { "Sandbox", "Chalkboard" };
	static final String CHALKBOARD = "Chalkboard";

	// Common Test Component Names
	protected static final String HARD_LIMIT = "rh.HardLimit";
	protected static final String HARD_LIMIT_1 = "HardLimit_1";
	protected static final String SIGGEN = "rh.SigGen";
	protected static final String SIGGEN_1 = "SigGen_1";
	protected static final String NAME_SPACE_COMP = "name.space.comp";
	protected static final String NAME_SPACE_COMP_1 = "comp_1";

	protected RHSWTGefBot gefBot; // SUPPRESS CHECKSTYLE VisibilityModifier

	@Before
	public void beforeTest() throws Exception {
		gefBot = new RHSWTGefBot();
	}

	@After
	public void afterTest() {
		ScaExplorerTestUtils.releaseFromScaExplorer(gefBot, CHALKBOARD_PARENT_PATH, CHALKBOARD);
		ScaExplorerTestUtils.waitUntilScaExplorerWaveformEmpty(gefBot, CHALKBOARD_PARENT_PATH, CHALKBOARD);
	}
}
