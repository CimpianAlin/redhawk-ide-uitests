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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.Assert;
import org.junit.Test;

import gov.redhawk.core.graphiti.ui.ext.RHContainerShape;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.ide.swtbot.WaveformUtils;
import gov.redhawk.ide.swtbot.condition.WaitForWidgetEnablement;
import gov.redhawk.ide.swtbot.diagram.AbstractGraphitiTest;
import gov.redhawk.ide.swtbot.diagram.DiagramTestUtils;
import gov.redhawk.ide.swtbot.diagram.FETunerControl;
import gov.redhawk.ide.swtbot.diagram.RHBotGefEditor;
import gov.redhawk.ide.swtbot.diagram.UsesDeviceTestUtils;
import mil.jpeojtrs.sca.partitioning.UsesDeviceStub;

public class UsesDeviceTest extends AbstractGraphitiTest {

	private static final String SIG_GEN = "rh.SigGen";
	private static final String SIG_GEN_1 = "SigGen_1";
	private static final String DATA_CONVERTER = "rh.DataConverter";
	private static final String DATA_CONVERTER_1 = "DataConverter_1";

	private String waveformName;

	/**
	 * Add a uses device FrontEnd tuner to the diagram. Verify the shape, its ports, and the XML. Delete and verify.
	 * IDE-124 SAD-level uses device support
	 * IDE-1821 Check for model error when using deviceusedbyapplication
	 */
	@Test
	public void usesDevice_frontEndTuner_listenMode() {
		waveformName = "usesDevice_frontEndTuner_listenMode";

		// Create an empty waveform project
		WaveformUtils.createNewWaveform(gefBot, waveformName, "rh.SigGen");
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);
		editor.setFocus();

		// generate usesdevice FrontEnd tuner with listen by id
		String usesDeviceId = "FrontEndTuner_1"; // auto generated by wizard
		String existingAllocationId = "12345";
		String newAllocationId = "678910";

		DiagramTestUtils.addUseFrontEndTunerDeviceToDiagram(gefBot, editor, 0, 150);
		String[] providesPorts = new String[] { "dataDouble_in", "dataDouble2_in" };
		String[] usesPorts = new String[] { "dataDouble_out", "dataDouble2_out" };
		UsesDeviceTestUtils.completeUsesFEDeviceWizard(gefBot, existingAllocationId, newAllocationId, providesPorts, usesPorts);

		editor.setFocus();

		// Confirm created component truly is a usesdevice FrontEnd tuner
		SWTBotGefEditPart frontEndTunerGefEditPart = editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE);
		assertFrontEndTuner(frontEndTunerGefEditPart);

		RHContainerShape rhContainerShape = (RHContainerShape) frontEndTunerGefEditPart.part().getModel();

		// two provides ports, two uses ports
		Assert.assertTrue(rhContainerShape.getUsesPortStubs().size() == 2 && rhContainerShape.getProvidesPortStubs().size() == 2);

		// Both ports are of type dataDouble
		Assert.assertEquals(rhContainerShape.getUsesPortStubs().get(0).getName(), "dataDouble_out");
		Assert.assertEquals(rhContainerShape.getUsesPortStubs().get(1).getName(), "dataDouble2_out");
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(0).getName(), "dataDouble_in");
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(1).getName(), "dataDouble2_in");

		// Save to trigger validation. Ensure there are no errors (IDE-1821)
		KeyboardFactory.getSWTKeyboard().pressShortcut(SWT.CTRL, 's');
		try {
			bot.shell("Invalid Model").bot().button("No").click();
			Assert.fail("Error dialog appeared while saving");
		} catch (WidgetNotFoundException e) {
			// PASS
		}

		// Check to see if xml is correct in the sad.xml
		final String usesDeviceXML = regexStringForGenericUseFrontEndTunerDeviceListenById(usesDeviceId, existingAllocationId, newAllocationId);
		DiagramTestUtils.openTabInEditor(editor, waveformName + ".sad.xml");
		String editorText = editor.toTextEditor().getText();
		Assert.assertTrue("The sad.xml should include UsesDevice", editorText.matches(usesDeviceXML));
		DiagramTestUtils.openTabInEditor(editor, "Diagram");

		// delete
		DiagramTestUtils.deleteFromDiagram(editor, editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE));

		// verify deleted
		Assert.assertNull(editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE));
	}

	/**
	 * IDE-124
	 * Add a usesdevice for a FrontEnd tuner in control mode. Use sim_RX_DIGITIZER device in SDRROOT as template.
	 */
	@Test
	public void usesDevice_frontEndTuner_controlMode_simRxDigitizer() {
		waveformName = "IDE-124-CreateAndDeleteUse_im_rx_digitizer_FrontEndTunerDeviceTest";

		// Create an empty waveform project
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);
		editor.setFocus();

		// Add a usesdevice for a FrontEnd tuner in control mode
		String usesDeviceId = "sim_RX_DIGITIZER_1"; // auto generated by wizard
		String tunerType = "RX_DIGITIZER";
		String newAllocationId = "678910";
		String centerFrequency = "5";
		String bandwidth = "1";
		String sampleRate = "1";
		boolean deviceControl = true;
		String groupID = "group1";
		String rfFlowID = "";

		DiagramTestUtils.addUseFrontEndTunerDeviceToDiagram(gefBot, editor);
		FETunerControl tunerControl = new FETunerControl(tunerType, newAllocationId, centerFrequency, bandwidth, sampleRate, deviceControl, rfFlowID, groupID);
		UsesDeviceTestUtils.completeUsesFEDeviceWizard(bot, "sim_RX_DIGITIZER (/devices/sim_RX_DIGITIZER/)", tunerControl, null, null);

		editor.setFocus();

		// Confirm created component truly is a usesdevice FrontEnd Tuner
		SWTBotGefEditPart frontEndTunerGefEditPart = editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE);
		assertFrontEndTuner(frontEndTunerGefEditPart);

		// two provides ports, one uses ports
		RHContainerShape rhContainerShape = (RHContainerShape) frontEndTunerGefEditPart.part().getModel();
		Assert.assertTrue(rhContainerShape.getProvidesPortStubs().size() == 2 && rhContainerShape.getUsesPortStubs().size() == 1);
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(0).getName(), "RFInfo_in");
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(1).getName(), "DigitalTuner_in");
		Assert.assertEquals(rhContainerShape.getUsesPortStubs().get(0).getName(), "dataShort_out");

		// Check to see if xml is correct in the sad.xml
		final String usesDeviceXML = regexStringFor_sim_rx_digitizer_UseFrontEndTunerDeviceControlTuner(usesDeviceId, tunerControl);
		DiagramTestUtils.openTabInEditor(editor, waveformName + ".sad.xml");
		String editorText = editor.toTextEditor().getText();
		Assert.assertTrue("The sad.xml should include UsesDevice", editorText.matches(usesDeviceXML));
		DiagramTestUtils.openTabInEditor(editor, "Diagram");

		// delete
		DiagramTestUtils.deleteFromDiagram(editor, editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE));

		// verify deleted
		Assert.assertNull(editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE));
	}

	/**
	 * IDE-124
	 * Edit existing usesdevice FrontEnd tuner name, model and ports
	 * Change names, add & remove ports
	 */
	@Test
	public void editDiagram_usesDevice_frontEndTuner() {
		waveformName = "IDE-124-editGenericFrontEndTunerDevice";

		// Create an empty waveform project
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);
		editor.setFocus();

		// generate generic use frontent tuner device with listen by id
		final String usesDeviceId = "FrontEndTuner_1"; // auto generated by wizard
		final String existingAllocationId = "12345";
		final String newAllocationId = "678910";

		// Add components to diagram
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 0, 0);
		DiagramTestUtils.addFromPaletteToDiagram(editor, DATA_CONVERTER, 500, 0);

		// Add Uses Generic FrontEnd Device
		DiagramTestUtils.addUseFrontEndTunerDeviceToDiagram(gefBot, editor, 225, 0);
		String[] providesPorts = new String[] { "dataFloat_in", "dataFloat2_in" };
		String[] usesPorts = new String[] { "dataFloat_out", "dataFloat2_out" };
		UsesDeviceTestUtils.completeUsesFEDeviceWizard(gefBot, existingAllocationId, newAllocationId, providesPorts, usesPorts);

		editor.setFocus();

		// Get a handle on ports
		SWTBotGefEditPart sigGenUsesPart = DiagramTestUtils.getDiagramUsesPort(editor, SIG_GEN_1);
		SWTBotGefEditPart dataConverterProvidesPart1 = DiagramTestUtils.getDiagramProvidesPort(editor, DATA_CONVERTER_1, "dataShort");
		SWTBotGefEditPart dataConverterProvidesPart2 = DiagramTestUtils.getDiagramProvidesPort(editor, DATA_CONVERTER_1, "dataFloat");
		SWTBotGefEditPart usesDeviceProvidesDoublePart = DiagramTestUtils.getDiagramProvidesPort(editor, SadTestUtils.USE_FRONTEND_TUNER_DEVICE, "dataFloat_in");
		SWTBotGefEditPart usesDeviceProvidesDouble2Part = DiagramTestUtils.getDiagramProvidesPort(editor, SadTestUtils.USE_FRONTEND_TUNER_DEVICE,
			"dataFloat2_in");
		SWTBotGefEditPart usesDeviceUsesDoublePart = DiagramTestUtils.getDiagramUsesPort(editor, SadTestUtils.USE_FRONTEND_TUNER_DEVICE, "dataFloat_out");
		SWTBotGefEditPart usesDeviceUsesDouble2Part = DiagramTestUtils.getDiagramUsesPort(editor, SadTestUtils.USE_FRONTEND_TUNER_DEVICE, "dataFloat2_out");

		// maximize window
		DiagramTestUtils.maximizeActiveWindow(gefBot);

		// draw 4 connections
		Assert.assertTrue("Failed to make connection to Uses Device provides port 1",
			DiagramTestUtils.drawConnectionBetweenPorts(editor, sigGenUsesPart, usesDeviceProvidesDoublePart));
		Assert.assertTrue("Failed to make connection to Uses Device provides port 2",
			DiagramTestUtils.drawConnectionBetweenPorts(editor, sigGenUsesPart, usesDeviceProvidesDouble2Part));
		Assert.assertTrue("Failed to make connection from Uses Device uses port 1",
			DiagramTestUtils.drawConnectionBetweenPorts(editor, usesDeviceUsesDoublePart, dataConverterProvidesPart1));
		Assert.assertTrue("Failed to make connection from Uses Device uses port 2",
			DiagramTestUtils.drawConnectionBetweenPorts(editor, usesDeviceUsesDouble2Part, dataConverterProvidesPart2));

		// Open USE_FRONTEND_TUNER_DEVICE edit wizard and change name, remove existing port, and add a new one
		editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE).select();
		editor.clickContextMenu("Edit Use FrontEnd Tuner Device");

		// Change Name
		gefBot.textWithLabel("Uses Device ID").setText(usesDeviceId + "x");

		// set model
		gefBot.textWithLabel("Device Model (optional)").setText("someModel");
		gefBot.button("&Next >").click();

		// change existing tuner allocation ID
		gefBot.textWithLabel("Existing Tuner Allocation ID").setText(existingAllocationId + "x");
		gefBot.button("&Next >").click();

		// Delete existing provides port
		gefBot.list(0).select(0); // dataDouble_in
		gefBot.button(1).click();

		// Add new provides port
		gefBot.textInGroup("Port(s) to use for connections", 0).setText("newProvides");
		gefBot.button(0).click();

		// Delete existing provides port
		gefBot.list(1).select(0); // dataDouble_out
		gefBot.button(3).click();

		// Add new uses port
		gefBot.textInGroup("Port(s) to use for connections", 1).setText("newUses");
		gefBot.button(2).click();

		gefBot.button("Finish").click();

		// Confirm that changes were made
		// Confirm created component truly is Generic FrontEnd Tuner
		SWTBotGefEditPart frontEndTunerGefEditPart = editor.getEditPart(SadTestUtils.USE_FRONTEND_TUNER_DEVICE);
		assertFrontEndTuner(frontEndTunerGefEditPart);

		RHContainerShape rhContainerShape = (RHContainerShape) frontEndTunerGefEditPart.part().getModel();

		// two provides ports, two uses ports
		Assert.assertTrue(rhContainerShape.getUsesPortStubs().size() == 2 && rhContainerShape.getProvidesPortStubs().size() == 2);

		// Verify ports
		Assert.assertEquals(rhContainerShape.getUsesPortStubs().get(0).getName(), "dataFloat2_out");
		Assert.assertEquals(rhContainerShape.getUsesPortStubs().get(1).getName(), "newUses");
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(0).getName(), "dataFloat2_in");
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(1).getName(), "newProvides");

		// Check to see if xml is correct in the sad.xml
		final String usesDeviceXML = regexStringForGenericUseFrontEndTunerDeviceListenById(usesDeviceId + "x", existingAllocationId + "x", newAllocationId);
		DiagramTestUtils.openTabInEditor(editor, waveformName + ".sad.xml");
		String editorText = editor.toTextEditor().getText();
		Assert.assertTrue("The sad.xml should include UsesDevice", editorText.matches(usesDeviceXML));
		DiagramTestUtils.openTabInEditor(editor, "Diagram");

		// Confirm that connections properly removed
		sigGenUsesPart = DiagramTestUtils.getDiagramUsesPort(editor, SIG_GEN_1);
		List<SWTBotGefConnectionEditPart> connections = DiagramTestUtils.getSourceConnectionsFromPort(editor, sigGenUsesPart);
		Assert.assertTrue("SigGen should only have a single connection", connections.size() == 1);

		dataConverterProvidesPart1 = DiagramTestUtils.getDiagramProvidesPort(editor, DATA_CONVERTER_1, "dataShort");
		dataConverterProvidesPart2 = DiagramTestUtils.getDiagramProvidesPort(editor, DATA_CONVERTER_1, "dataFloat");
		connections = DiagramTestUtils.getTargetConnectionsFromPort(editor, dataConverterProvidesPart1);
		Assert.assertTrue("DataConverter should only have a single connection", connections.size() == 0);
		connections = DiagramTestUtils.getTargetConnectionsFromPort(editor, dataConverterProvidesPart2);
		Assert.assertTrue("DataConverter should only have a single connection", connections.size() == 1);
	}

	/**
	 * IDE-124
	 * Modify XML of a usesdevice to change it from a FrontEnd allocation to just a generic usesdevice. Re-invoke the
	 * wizard (now just the generic usesdevice one), makes some edits and verify them.
	 */
	@Test
	public void editXml_usesDevice_FrontEndTuner() {
		waveformName = "IDE-124-editUsesDevice";

		// Create an empty waveform project
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);
		editor.setFocus();

		// Create a uses device for a FrontEnd tuner using listen by id
		final String usesDeviceId = "FrontEndTuner_1"; // auto generated by wizard
		final String existingAllocationId = "12345";
		final String newAllocationId = "678910";

		// Add uses device for a FrontEnd tune
		DiagramTestUtils.addUseFrontEndTunerDeviceToDiagram(gefBot, editor);
		String[] providesPorts = new String[] { "dataDouble_in", "dataDouble2_in" };
		String[] usesPorts = new String[] { "dataDouble_out", "dataDouble2_out" };
		UsesDeviceTestUtils.completeUsesFEDeviceWizard(gefBot, existingAllocationId, newAllocationId, providesPorts, usesPorts);

		editor.setFocus();

		// Remove FRONTENT::TUNER from sad.xml
		DiagramTestUtils.openTabInEditor(editor, waveformName + ".sad.xml");
		String editorText = editor.toTextEditor().getText();
		editorText = editorText.replace("<propertyref refid=\"DCE:cdc5ee18-7ceb-4ae6-bf4c-31f983179b4d\" value=\"FRONTEND::TUNER\"/>", "");
		editor.toTextEditor().setText(editorText);

		// display diagram tab
		DiagramTestUtils.openTabInEditor(editor, "Diagram");

		// Open edit wizard and change name, remove existing port, and add a new one
		editor.getEditPart(SadTestUtils.USE_DEVICE).select();
		editor.clickContextMenu("Edit Use Device");

		// Change Name
		gefBot.textWithLabel("Uses Device Id").setText(usesDeviceId + "x");
		gefBot.button("&Next >").click();

		// Delete existing provides port
		gefBot.list(0).select(0); // dataDouble_in
		gefBot.button(1).click();

		// Add new provides port
		gefBot.textInGroup("Port(s) to use for connections", 0).setText("newProvides");
		gefBot.button(0).click();

		// Delete existing provides port
		gefBot.list(1).select(0); // dataDouble_out
		gefBot.button(3).click();

		// Add new uses port
		gefBot.textInGroup("Port(s) to use for connections", 1).setText("newUses");
		gefBot.button(2).click();

		gefBot.button("Finish").click();

		// Confirm that changes were made
		SWTBotGefEditPart useDeviceEditPart = editor.getEditPart(SadTestUtils.USE_DEVICE);
		SadTestUtils.assertUsesDevice(useDeviceEditPart);

		RHContainerShape rhContainerShape = (RHContainerShape) useDeviceEditPart.part().getModel();

		// two provides ports, two uses ports
		Assert.assertTrue(rhContainerShape.getUsesPortStubs().size() == 2 && rhContainerShape.getProvidesPortStubs().size() == 2);

		// Verify ports
		Assert.assertEquals(rhContainerShape.getUsesPortStubs().get(0).getName(), "dataDouble2_out");
		Assert.assertEquals(rhContainerShape.getUsesPortStubs().get(1).getName(), "newUses");
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(0).getName(), "dataDouble2_in");
		Assert.assertEquals(rhContainerShape.getProvidesPortStubs().get(1).getName(), "newProvides");
	}

	/**
	 * Make sure it looks like validation is working in the FrontEnd uses device wizard
	 */
	@Test
	public void usesDevice_WizardValidation() {
		waveformName = "IDE-1266-feUsesDeviceWizardValidation";

		// Create an empty waveform project
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);
		editor.setFocus();

		// Add uses device for a FrontEnd tuner
		DiagramTestUtils.addUseFrontEndTunerDeviceToDiagram(gefBot, editor);

		SWTBotShell allocateTunerShell = bot.shell("Allocate Tuner");
		allocateTunerShell.setFocus();

		// click next, Generic FrontEnd Device already selected
		bot.button("&Next >").click();

		// Allocate Tuner page
		SWTBotText botText = bot.textWithLabel("Uses Device ID");
		botText.setText("");
		bot.waitUntil(new WaitForWidgetEnablement(bot.button("&Next >"), false));
		botText.setText("abc");
		bot.button("&Next >").click();

		// Tuner Allocation Page
		bot.waitUntil(new WaitForWidgetEnablement(bot.button("&Next >"), false));
		SWTBotCombo comboField = bot.comboBox(0); // Allocation
		comboField.setFocus();
		comboField.setSelection("Listen to Existing Tuner by ID");
		bot.waitUntil(new WaitForWidgetEnablement(bot.button("&Next >"), false));
		SWTBotText existingTunerAllocationIdText = bot.textWithLabel("Existing Tuner Allocation ID");
		existingTunerAllocationIdText.setText("abc");
		bot.waitUntil(new WaitForWidgetEnablement(bot.button("&Next >"), true));
		comboField.setFocus();
		comboField.setSelection("Control New Tuner");
		bot.waitUntil(new WaitForWidgetEnablement(bot.button("&Next >"), false));
		comboField.setFocus();
		comboField.setSelection("Listen to Existing Tuner by ID");
		bot.waitUntil(new WaitForWidgetEnablement(bot.button("&Next >"), true));

		bot.button("Cancel").click();
	}

	/**
	 * Assert FrontEnd Tuner
	 * @param gefEditPart
	 */
	private void assertFrontEndTuner(SWTBotGefEditPart gefEditPart) {
		Assert.assertNotNull(gefEditPart);
		// Drill down to graphiti component shape
		RHContainerShape rhContainerShape = (RHContainerShape) gefEditPart.part().getModel();

		// Grab the associated business object and confirm it is a UsesDeviceStub
		Object bo = DUtil.getBusinessObject(rhContainerShape);
		Assert.assertTrue("business object should be of type UsesDeviceStub", bo instanceof UsesDeviceStub);
		UsesDeviceStub usesDeviceStub = (UsesDeviceStub) bo;

		// Run assertions on expected properties
		Assert.assertEquals("outer text should match shape type", SadTestUtils.USE_FRONTEND_TUNER_DEVICE, rhContainerShape.getOuterText().getValue());
		Assert.assertEquals("inner text should match usesdevice id", usesDeviceStub.getUsesDevice().getId(), rhContainerShape.getInnerText().getValue());
		Assert.assertNotNull("component supported interface graphic should not be null", rhContainerShape.getLollipop());

	}

	/**
	 * Checks sad.xml for generic uses device code listen by id
	 * @param componentShape
	 * @return
	 */
	private String regexStringForGenericUseFrontEndTunerDeviceListenById(String usesDeviceId, String existingAllocationId, String allocationId) {
		String usesDevice = "<usesdevice id=\"" + usesDeviceId + "\">";
		String propertyRef = "<propertyref refid=\"DCE:cdc5ee18-7ceb-4ae6-bf4c-31f983179b4d\" value=\"FRONTEND::TUNER\"/>";
		String structRef = "<structref refid=\"FRONTEND::listener_allocation\">";
		String simpleRef1 = "<simpleref refid=\"FRONTEND::listener_allocation::existing_allocation_id\" value=\"" + existingAllocationId + "\"/>";
		String simpleRef2 = "<simpleref refid=\"FRONTEND::listener_allocation::listener_allocation_id\" value=\"" + allocationId + "\"/>";

		return "(?s).*" + usesDevice + ".*" + propertyRef + ".*" + structRef + ".*" + simpleRef1 + ".*" + simpleRef2 + ".*";
	}

	/**
	 * Checks sad.xml for generic uses device code listen by id
	 * @param componentShape
	 * @return
	 */
	private String regexStringFor_sim_rx_digitizer_UseFrontEndTunerDeviceControlTuner(final String usesDeviceId, final FETunerControl tunerControl) {
		String usesDevice = "<usesdevice id=\"" + usesDeviceId + "\">";
		String propertyRef = "<propertyref refid=\"DCE:cdc5ee18-7ceb-4ae6-bf4c-31f983179b4d\" value=\"FRONTEND::TUNER\"/>";
		String deviceModel = "<propertyref refid=\"DCE:0f99b2e4-9903-4631-9846-ff349d18ecfb\" value=\"RX_DIGITIZER simulator\"/>";
		String structRef = "<structref refid=\"FRONTEND::tuner_allocation\">";
		String simpleRef1 = "<simpleref refid=\"FRONTEND::tuner_allocation::tuner_type\" value=\"" + tunerControl.getTunerType() + "\"/>";
		String simpleRef2 = "<simpleref refid=\"FRONTEND::tuner_allocation::allocation_id\" value=\"" + tunerControl.getNewAllocationID() + "\"/>";
		String simpleRef3 = "<simpleref refid=\"FRONTEND::tuner_allocation::center_frequency\" value=\"" + tunerControl.getCenterFrequency() + "000000.0\"/>";
		String simpleRef4 = "<simpleref refid=\"FRONTEND::tuner_allocation::bandwidth\" value=\"" + tunerControl.getBandwidth() + "000000.0\"/>";
		String simpleRef5 = "<simpleref refid=\"FRONTEND::tuner_allocation::bandwidth_tolerance\" value=\"20.0\"/>";
		String simpleRef6 = "<simpleref refid=\"FRONTEND::tuner_allocation::sample_rate\" value=\"" + tunerControl.getSampleRate() + "000000.0\"/>";
		String simpleRef7 = "<simpleref refid=\"FRONTEND::tuner_allocation::sample_rate_tolerance\" value=\"20.0\"/>";
		String simpleRef8 = "<simpleref refid=\"FRONTEND::tuner_allocation::device_control\" value=\"" + tunerControl.getDeviceControl() + "\"/>";
		String simpleRef9 = "<simpleref refid=\"FRONTEND::tuner_allocation::group_id\" value=\"" + tunerControl.getGroupID() + "\"/>";
		String simpleRef10 = "<simpleref refid=\"FRONTEND::tuner_allocation::rf_flow_id\" value=\"" + tunerControl.getRFFlowID() + "\"/>";

		return "(?s).*" + usesDevice + ".*" + propertyRef + ".*" + deviceModel + ".*" + structRef + ".*" + simpleRef1 + ".*" + simpleRef2 + ".*" + simpleRef3
			+ ".*" + simpleRef4 + ".*" + simpleRef5 + ".*" + simpleRef6 + ".*" + simpleRef7 + ".*" + simpleRef8 + ".*" + simpleRef9 + ".*" + simpleRef10 + ".*";
	}

}
