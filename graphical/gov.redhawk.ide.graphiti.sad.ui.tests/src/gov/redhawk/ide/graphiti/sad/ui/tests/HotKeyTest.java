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
package gov.redhawk.ide.graphiti.sad.ui.tests;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.eclipse.jface.bindings.keys.ParseException;
import org.junit.Assert;
import org.junit.Test;

import gov.redhawk.ide.swtbot.WaveformUtils;
import gov.redhawk.ide.swtbot.diagram.AbstractGraphitiTest;
import gov.redhawk.ide.swtbot.diagram.DiagramTestUtils;
import gov.redhawk.ide.swtbot.diagram.RHBotGefEditor;

public class HotKeyTest extends AbstractGraphitiTest {

	private static final String SIG_GEN = "rh.SigGen";
	private static final String SIG_GEN_1 = "SigGen_1";
	private static final String SIG_GEN_2 = "SigGen_2";

	private String waveformName;

	/**
	 * IDE-85
	 * Users should be able to delete a component from a waveform
	 * using the delete key on the keyboard
	 * @throws ParseException
	 * @throws AWTException
	 */
	@Test
	public void deleteHotKeyTest() throws ParseException, AWTException {
		waveformName = "Delete_HotKey";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Add component to the diagram
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 0, 0);

		// Confirm component was added to waveform
		Assert.assertNotNull("SigGen component not found", editor.getEditPart(SIG_GEN_1));

		// Select component and delete with hotkey
		editor.select(SIG_GEN_1);
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_DELETE);
		robot.keyRelease(KeyEvent.VK_DELETE);

		// Confirm component was removed from the waveform
		Assert.assertNull("Unexpected component found", editor.getEditPart(SIG_GEN_1));
	}

	/**
	 * IDE-1865
	 * Users should be able to delete multiple components from a waveform
	 * using the delete key on the keyboard
	 * @throws ParseException
	 * @throws AWTException
	 */
	@Test
	public void deleteMultipleHotKeyTest() throws ParseException, AWTException {
		waveformName = "Delete_HotKey";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Add component to the diagram
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 0, 0);
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 300, 300);

		// Confirm component was added to waveform
		Assert.assertNotNull("SigGen component not found", editor.getEditPart(SIG_GEN_1));
		Assert.assertNotNull("SigGen component not found", editor.getEditPart(SIG_GEN_2));

		// Select component and delete with hotkey
		editor.select(editor.getEditPart(SIG_GEN_1), editor.getEditPart(SIG_GEN_2));

		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_DELETE);
		robot.keyRelease(KeyEvent.VK_DELETE);

		// Confirm component was removed from the waveform
		Assert.assertNull("Unexpected component found", editor.getEditPart(SIG_GEN_1));
		Assert.assertNull("Unexpected component found", editor.getEditPart(SIG_GEN_2));
	}

	/**
	 * IDE-678
	 * Undo/Redo key bindings
	 * @throws AWTException
	 */
	@Test
	public void undoRedoHotkeyTest() throws AWTException {
		waveformName = "UndoRedo_Hotkey";
		final String HARDLIMIT = "rh.HardLimit";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Add component to the diagram
		DiagramTestUtils.addFromPaletteToDiagram(editor, HARDLIMIT, 0, 0);

		// Confirm component was added to waveform
		Assert.assertNotNull("HardLimit component not found", editor.getEditPart(HARDLIMIT));

		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_Z);
		robot.keyRelease(KeyEvent.VK_Z);
		robot.keyRelease(KeyEvent.VK_CONTROL);

		// Confirm add was undone
		Assert.assertNull("Unexpected component found", editor.getEditPart(HARDLIMIT));

		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(KeyEvent.VK_Z);
		robot.keyRelease(KeyEvent.VK_Z);
		robot.keyRelease(KeyEvent.VK_SHIFT);
		robot.keyRelease(KeyEvent.VK_CONTROL);

		// Confirm add was redone
		Assert.assertNotNull("HardLimit component not found", editor.getEditPart(HARDLIMIT));
	}

}
