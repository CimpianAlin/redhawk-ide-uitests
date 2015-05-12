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
package gov.redhawk.ide.ui.tests.scd;

import gov.redhawk.ide.spd.internal.ui.editor.ComponentEditor;
import gov.redhawk.ide.swtbot.ComponentUtils;
import gov.redhawk.ide.swtbot.UITest;
import gov.redhawk.ide.swtbot.condition.WaitForEditorCondition;
import mil.jpeojtrs.sca.scd.Ports;
import mil.jpeojtrs.sca.spd.SoftPkg;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PortsTabTest extends UITest {
	static final String PORTS_TAB_NAME = "Ports";
	static final String PROG_LANG_CPP  = "C++";
	static final String FIELD_NAME     = "Name*:";
	static final String BUTTON_ADD     = "Add";
	static final String BUTTON_REMOVE  = "Remove";
	static final String IDL_MODULE     = "IDETEST";
	static final String EXPECTED_IDL_SAMPLE1 = "IDL:IDETEST/SampleInterface:1.0";
	static final String EXPECTED_IDL_SAMPLE2 = "IDL:IDETEST/SampleInterface2:1.0";

	private SWTBot editorBot;

	private SoftPkg spd;

	private DefaultCondition selectTestIDLCondition = new DefaultCondition() {

		@Override
		public boolean test() throws Exception {
			SWTBotTree idlTree = bot.tree(2);
			SWTBotTreeItem treeItem = idlTree.getTreeItem(IDL_MODULE).expand().getNode("SampleInterface");
			treeItem.select();
			return true;
		}

		@Override
		public String getFailureMessage() {
			return "Failed to find and select IDL: SampleInterface from " + IDL_MODULE;
		}

	};

	@Before
	public void before() throws Exception {
		super.before();

		ComponentUtils.createComponentProject(bot, "TestCppComponent", PROG_LANG_CPP);
		bot.waitUntil(new WaitForEditorCondition());

		SWTBotEditor editor = bot.activeEditor();
		editor.setFocus();
		this.editorBot = editor.bot();
		this.editorBot.cTabItem(PORTS_TAB_NAME).activate();

		ComponentEditor spdEditor = (ComponentEditor) editor.getReference().getEditor(false);
		this.spd = SoftPkg.Util.getSoftPkg(spdEditor.getMainResource());
	}

	@Test
	public void testAddPort() {
		editorBot.button(BUTTON_ADD).click();

		bot.textWithLabel(FIELD_NAME).setText("inTestPort");
		bot.comboBoxWithLabel("Direction:").setSelection("in <provides>");
		bot.waitUntil(selectTestIDLCondition);

		// Check type table
		SWTBotTable typeTable = bot.tableWithLabel("Type:");
		Assert.assertEquals(3, typeTable.rowCount());
		typeTable.getTableItem("data").check();
		typeTable.getTableItem("responses").check();
		typeTable.getTableItem("control").check();
		typeTable.getTableItem("control").uncheck();
		typeTable.getTableItem("responses").uncheck();
		typeTable.getTableItem("data").uncheck();

		editorBot.button(BUTTON_ADD).click();
		bot.textWithLabel(FIELD_NAME).setText("inTestPort");
		// TODO: validate that Duplicate Port name should result in error on editor

		bot.textWithLabel(FIELD_NAME).setText("outTestPort");
		bot.comboBox().setSelection("out <uses>");
		bot.waitUntil(selectTestIDLCondition);

		bot.button(BUTTON_ADD).click();
		bot.textWithLabel(FIELD_NAME).setText("bidirTestPort");
		bot.comboBox().setSelection("bidir <uses/provides>");
		bot.waitUntil(selectTestIDLCondition);

		bot.saveAllEditors();
		Assert.assertEquals("Number of Ports in Ports Table (UI)", 3, editorBot.table().rowCount());
		Ports ports = spd.getDescriptor().getComponent().getComponentFeatures().getPorts();

		Assert.assertEquals("Number of Ports in scd.xml", 4, ports.getAllPorts().size()); // bidir direction creates two Ports
	}

	@Test
	public void testEditPort() {
		editorBot.button(BUTTON_ADD).click();
		bot.textWithLabel(FIELD_NAME).setText("inTestPort");
		bot.comboBoxWithLabel("Direction:").setSelection("in <provides>");
		bot.waitUntil(selectTestIDLCondition);

		// Test double-clicking on the port in the table, then bailing out
//		bot.table().click(0, 0); // TODO: why does it blank out right side?

		SWTBotTableItem item = bot.table().getTableItem(0);
		Assert.assertEquals("<provides> inTestPort", item.getText());
		Assert.assertEquals(EXPECTED_IDL_SAMPLE1, item.getText(1));

		// Test clicking the Port and making a change
//		bot.table().select(0); // TODO: why does it blank out right side?

		bot.textWithLabel(FIELD_NAME).setText("outTestPort");
		bot.comboBox().setSelection("out <uses>");
		bot.waitUntil(selectTestIDLCondition);
		bot.tree(2).getTreeItem(IDL_MODULE).expand().getNode("SampleInterface2").select();

		item = bot.table().getTableItem(0);
		Assert.assertEquals("<uses> outTestPort", item.getText());
		Assert.assertEquals(EXPECTED_IDL_SAMPLE2, item.getText(1));

		// Test clicking to edit and NOT making a change (IDE-1230)
		bot.table().select(0);
		Assert.assertEquals("<uses> outTestPort", item.getText());
		Assert.assertEquals(EXPECTED_IDL_SAMPLE2, item.getText(1));

		item = bot.table().getTableItem(0);
		Assert.assertEquals("<uses> outTestPort", item.getText());
		Assert.assertEquals(EXPECTED_IDL_SAMPLE2, item.getText(1));
	}

	@Test
	public void testRemovePort() {
		Assert.assertFalse("Remove button should be disabled", bot.button(BUTTON_REMOVE).isEnabled());

		editorBot.button(BUTTON_ADD).click();
		// fill in required Port details
		bot.textWithLabel(FIELD_NAME).setText("inTestPortToRemove");
		bot.comboBoxWithLabel("Direction:").setSelection("in <provides>");

		bot.table().select(0);
		bot.button(BUTTON_REMOVE).click();

		Assert.assertEquals("Removed Port", 0, bot.table().rowCount());
	}

}