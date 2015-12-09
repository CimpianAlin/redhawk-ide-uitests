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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.Assert;
import org.junit.Test;

import gov.redhawk.ide.graphiti.sad.ui.diagram.patterns.HostCollocationPattern;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.ide.swtbot.MenuUtils;
import gov.redhawk.ide.swtbot.WaveformUtils;
import gov.redhawk.ide.swtbot.diagram.AbstractGraphitiTest;
import gov.redhawk.ide.swtbot.diagram.DiagramTestUtils;
import gov.redhawk.ide.swtbot.diagram.FindByUtils;
import gov.redhawk.ide.swtbot.diagram.RHBotGefEditor;
import gov.redhawk.ide.swtbot.diagram.UsesDeviceTestUtils;
import mil.jpeojtrs.sca.sad.HostCollocation;
import mil.jpeojtrs.sca.sad.SadComponentPlacement;

public class HostCollocationTest extends AbstractGraphitiTest {

	private static final String SIG_GEN = "rh.SigGen";
	private static final String SIG_GEN_1 = "SigGen_1";
	private static final String HARD_LIMIT = "rh.HardLimit";
	private static final String HARD_LIMIT_1 = "HardLimit_1";

	private String waveformName;
	static final String UNEXPECTED_NUM_COMPONENTS = "Incorrect number of components in Host Collocation";
	static final String UNEXPECTED_SHAPE_LOCATION = "Shape location unexpected";

	/**
	 * IDE-746
	 * Create the pictogram shape in the waveform diagram that represents the Host Collocation object.
	 * This includes the ContainerShape for the component a label for the object name.
	 */
	@Test
	public void checkHostCollocationPictogramElements() {
		waveformName = "HC_Pictogram";
		final String HOST_CO_NAME = "HC1";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Add host collocation to the waveform
		DiagramTestUtils.addHostCollocationToDiagram(gefBot, editor, HOST_CO_NAME);

		// Add component to the host collocation
		DiagramTestUtils.addFromPaletteToDiagram(editor, HARD_LIMIT, 20, 20);

		MenuUtils.save(editor);

		// Check pictogram elements
		SWTBotGefEditPart hostCoEditPart = editor.getEditPart(HOST_CO_NAME);
		Assert.assertNotNull(hostCoEditPart);
		ContainerShape containerShape = (ContainerShape) hostCoEditPart.part().getModel();
		String shapeType = Graphiti.getPeService().getPropertyValue(containerShape, DUtil.SHAPE_TYPE);
		Assert.assertEquals("Host Collocation property is missing or wrong", HostCollocationPattern.HOST_COLLOCATION_OUTER_CONTAINER_SHAPE, shapeType);

		// Check model object values
		Object bo = DUtil.getBusinessObject(containerShape);
		Assert.assertTrue("Business object should be instance of HostCollocation", bo instanceof HostCollocation);

		HostCollocation hostCo = (HostCollocation) bo;
		Assert.assertEquals("Name value does not match expected value: " + HOST_CO_NAME, HOST_CO_NAME, hostCo.getName());
		EList<SadComponentPlacement> components = hostCo.getComponentPlacement();
		Assert.assertEquals("Expected component \'" + HARD_LIMIT_1 + "\' was not found", HARD_LIMIT_1,
			components.get(0).getComponentInstantiation().get(0).getId());
	}

	/**
	 * IDE-749
	 * Elements contained within a Host Collocation container should should stay in their relative locations when the
	 * parent container is moved.
	 */
	@Test
	public void hostCollocationRelativePosition() {
		waveformName = "HC_Component_Position";
		final String HOST_CO_NAME = "HC1";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Add host collocation to the waveform
		DiagramTestUtils.addHostCollocationToDiagram(gefBot, editor, HOST_CO_NAME);

		// Add component to the host collocation
		DiagramTestUtils.addFromPaletteToDiagram(editor, HARD_LIMIT, 20, 20);
		MenuUtils.save(editor);

		// Store host collocation and component relative location
		ContainerShape hostCoShape = DiagramTestUtils.getHostCollocationShape(editor, HOST_CO_NAME);
		int hostCoX = hostCoShape.getGraphicsAlgorithm().getX();
		int hostCoY = hostCoShape.getGraphicsAlgorithm().getY();
		Shape child = hostCoShape.getChildren().get(0);
		int childX = child.getGraphicsAlgorithm().getX();
		int childY = child.getGraphicsAlgorithm().getY();

		// Drag host collocation
		editor.drag(HOST_CO_NAME, 50, 50);

		// Check that host collocation has moved, but component relative location is the same
		hostCoShape = DiagramTestUtils.getHostCollocationShape(editor, HOST_CO_NAME);
		Assert.assertNotEquals("Host Collocation x-coord did not change, and it should have", hostCoX, hostCoShape.getGraphicsAlgorithm().getX());
		Assert.assertNotEquals("Host Collocation y-coord did not change, and it should have", hostCoY, hostCoShape.getGraphicsAlgorithm().getY());
		child = hostCoShape.getChildren().get(0);
		Assert.assertEquals("Child component relative x-coord should not have changed", childX, child.getGraphicsAlgorithm().getX());
		Assert.assertEquals("Child component relative y-coord should not have changed", childY, child.getGraphicsAlgorithm().getY());
	}

	/**
	 * IDE-747
	 * User should be able to add and remove components to a Host Collocation container by dragging them in and out of
	 * the pictogram element.
	 */
	@Test
	public void hostCollocationDnDComponents() {
		waveformName = "HC_DragAndDrop";
		final String HOST_CO_NAME = "HC1";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Add host collocation to the waveform
		DiagramTestUtils.addHostCollocationToDiagram(gefBot, editor, HOST_CO_NAME);

		// Add component to the host collocation
		DiagramTestUtils.addFromPaletteToDiagram(editor, HARD_LIMIT, 20, 20);
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 20, 150);

		MenuUtils.save(editor);

		// Check that component was added
		HostCollocation hostCo = DiagramTestUtils.getHostCollocationObject(editor, HOST_CO_NAME);
		Assert.assertNotNull("Host collocation must not be null", hostCo);
		Assert.assertFalse("Component was not added to the Host Collocation", hostCo.getComponentPlacement().isEmpty());
		Assert.assertTrue("Number of components should be 2, instead there are " + hostCo.getComponentPlacement().size(),
			hostCo.getComponentPlacement().size() == 2);
		Assert.assertEquals("Expected component \'" + HARD_LIMIT_1 + "\' was not found", HARD_LIMIT_1,
			hostCo.getComponentPlacement().get(0).getComponentInstantiation().get(0).getId());

		// Drag component outside of host collocation and confirm that it was removed
		editor.drag(editor.getEditPart(HARD_LIMIT_1), 350, 0);
		MenuUtils.save(editor);
		hostCo = DiagramTestUtils.getHostCollocationObject(editor, HOST_CO_NAME);
		Assert.assertTrue("Number of components should be 1, instead there are " + hostCo.getComponentPlacement().size(),
			hostCo.getComponentPlacement().size() == 1);
		Assert.assertEquals("Expected component \'" + SIG_GEN_1 + "\' was not found", SIG_GEN_1,
			hostCo.getComponentPlacement().get(0).getComponentInstantiation().get(0).getId());
	}

	/**
	 * IDE-696
	 * Ensure deletion of host collocation does not remove contained components and instead leaves
	 * them in the diagram.
	 */
	@Test
	public void hostCollocationContextMenuDelete() {
		waveformName = "HC_Pictogram";
		final String HOST_CO_NAME = "HC1";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Add host collocation to the waveform
		DiagramTestUtils.addHostCollocationToDiagram(gefBot, editor, HOST_CO_NAME);

		// Add component to the host collocation
		editor.setFocus();
		DiagramTestUtils.addFromPaletteToDiagram(editor, HARD_LIMIT, 20, 20);

		MenuUtils.save(editor);

		// Check pictogram elements
		SWTBotGefEditPart hostCoEditPart = editor.getEditPart(HOST_CO_NAME);
		Assert.assertNotNull(hostCoEditPart);
		ContainerShape hostCollocationContainerShape = (ContainerShape) hostCoEditPart.part().getModel();
		String shapeType = Graphiti.getPeService().getPropertyValue(hostCollocationContainerShape, DUtil.SHAPE_TYPE);
		Assert.assertTrue("Host Collocation property is missing or wrong", shapeType.equals(HostCollocationPattern.HOST_COLLOCATION_OUTER_CONTAINER_SHAPE));

		// Check model object values
		Object bo = DUtil.getBusinessObject(hostCollocationContainerShape);
		Assert.assertTrue("Business object should be instance of HostCollocation", bo instanceof HostCollocation);

		HostCollocation hostCo = (HostCollocation) bo;
		EList<SadComponentPlacement> components = hostCo.getComponentPlacement();
		Assert.assertEquals("Expected component \'" + HARD_LIMIT_1 + "\' was not found", HARD_LIMIT_1,
			components.get(0).getComponentInstantiation().get(0).getId());

		// delete host collocation
		SWTBotGefEditPart gefEditPart = editor.getEditPart(HOST_CO_NAME);
		DiagramTestUtils.deleteFromDiagram(editor, gefEditPart);
		// ensure host collocation is deleted
		Assert.assertNull(editor.getEditPart(HOST_CO_NAME));
		// ensure component still exists
		Assert.assertNotNull(editor.getEditPart(HARD_LIMIT_1));
	}

	/**
	 * IDE-748
	 * Upon resizing Host Collocation components should be added/removed to/from Host Collocation if they are
	 * contained within the boundaries of the new host collocation shape. Most of the location checking appears to be
	 * the same which is what we expect. There is code in place that maintains the absolute position of components
	 * within the graph as they are transitioned to the hostCollocation and we don't want them shifting.
	 */
	@Test
	public void hostCollocationResize() {
		waveformName = "HC_Resize";
		final String HOST_CO_NAME = "HC1";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// Maximize window
		DiagramTestUtils.maximizeActiveWindow(gefBot);

		// Add host collocation to the waveform
		DiagramTestUtils.addHostCollocationToDiagram(gefBot, editor, HOST_CO_NAME);

		// Add component to the host collocation
		DiagramTestUtils.addFromPaletteToDiagram(editor, HARD_LIMIT, 20, 20);
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 20, 150);

		MenuUtils.save(editor);

		// Check that component was added
		HostCollocation hostCo = DiagramTestUtils.getHostCollocationObject(editor, HOST_CO_NAME);
		Assert.assertFalse("Component was not added to the Host Collocation", hostCo.getComponentPlacement().isEmpty());
		Assert.assertTrue("Number of components should be 2, instead there are " + hostCo.getComponentPlacement().size(),
			hostCo.getComponentPlacement().size() == 2);
		Assert.assertEquals("Expected component \'" + HARD_LIMIT_1 + "\' was not found", HARD_LIMIT_1,
			hostCo.getComponentPlacement().get(0).getComponentInstantiation().get(0).getId());

		// ** Get hold of the shapes involved **//
		// HostCOllocation
		SWTBotGefEditPart hostCollocationEditPart = editor.getEditPart(HOST_CO_NAME);
		GraphicsAlgorithm hostCollocationGa = DiagramTestUtils.getHostCollocationShape(editor, HOST_CO_NAME).getGraphicsAlgorithm();
		// HardLimit
		ContainerShape hardLimitShape = (ContainerShape) editor.getEditPart(HARD_LIMIT_1).part().getModel();
		// SigGen
		ContainerShape sigGenShape = (ContainerShape) editor.getEditPart(SIG_GEN_1).part().getModel();

		// Expand Host Collocation, verify components are still part of Host Collocation and
		// absolute position of components (in relation to diagram) has not changed
		hostCollocationEditPart.select();
		int newWidth = hostCollocationGa.getWidth() + 500;
		int newHeight = hostCollocationGa.getHeight() + 200;
		hostCollocationEditPart.resize(PositionConstants.SOUTH_WEST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 2);

		// Contract Host Collocation from the left thereby excluding the two components
		newWidth -= 500;
		hostCollocationEditPart.resize(PositionConstants.EAST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 0);

		// Expand Host Collocation from the left thereby including the two components
		newWidth += 500;
		hostCollocationEditPart.resize(PositionConstants.EAST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 2);

		// Contract Host Collocation from the top-left coming down and to the right thereby excluding the two components
		newWidth -= 500;
		newHeight -= 200;
		hostCollocationEditPart.resize(PositionConstants.NORTH_EAST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 0);

		// Expand Host Collocation from the top-left thereby including the two components
		newWidth += 500;
		newHeight += 200;
		hostCollocationEditPart.resize(PositionConstants.NORTH_EAST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 2);

		// Contract Host Collocation from the bottom-left to the right and up thereby excluding the two components
		newWidth -= 500;
		newHeight /= 2;
		hostCollocationEditPart.resize(PositionConstants.SOUTH_EAST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 0);

		// Expand Host Collocation from the bottom-left to the left and down thereby including the two components
		newWidth += 500;
		newHeight *= 2;
		hostCollocationEditPart.resize(PositionConstants.SOUTH_EAST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 2);

		// Collapse Host Collocation from the top downwards thereby excluding the two components
		newHeight = newHeight / 2 - 50;
		hostCollocationEditPart.resize(PositionConstants.NORTH, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 0);

		// Expand Host Collocation from the top upwards thereby including the two components
		newHeight = (newHeight + 50) * 2;
		hostCollocationEditPart.resize(PositionConstants.NORTH, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 2);

		// Collapse Host Collocation from the bottom upwards thereby including the one component (topmost)
		newHeight = 130;
		hostCollocationEditPart.resize(PositionConstants.SOUTH, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 10, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 1);

		// Drag HardLimit to right
		editor.drag(editor.getEditPart(HARD_LIMIT_1), 350, 15);

		// Collapse Host Collocation from the right towards the left thereby excluding the all components
		hostCollocationEditPart.select();
		newWidth = 300;
		hostCollocationEditPart.resize(PositionConstants.WEST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 340, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 0);

		// Expand Host Collocation from the right towards the right thereby including one component
		newWidth = 750;
		hostCollocationEditPart.resize(PositionConstants.WEST, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 340, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 1);

		// Expand Host Collocation from the bottom downward thereby adding one more component
		newHeight = 400;
		hostCollocationEditPart.resize(PositionConstants.SOUTH, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 340, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 2);

		// Collapse Host Collocation from the bottom upward thereby removing one component
		newHeight = 130;
		hostCollocationEditPart.resize(PositionConstants.SOUTH, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 340, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 1);

		// Drag Host Collocation Down below SigGenComponent and check that it still contains one component
		editor.drag(hostCollocationEditPart, 0, 300);
		assertShapeLocationAndNumber(editor, hardLimitShape, 340, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 1);

		// Expand Host Collocation from the top upwards thereby adding one more component
		newHeight += 300;
		hostCollocationEditPart.resize(PositionConstants.NORTH, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 340, 310, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 2);

		// Collapse Host Collocation from the top downwards thereby removing one component
		newHeight -= 300;
		hostCollocationEditPart.resize(PositionConstants.NORTH, newWidth, newHeight);
		assertShapeLocationAndNumber(editor, hardLimitShape, 340, 10, sigGenShape, 10, 140, hostCo, HOST_CO_NAME, 1);
	}

	private void assertShapeLocationAndNumber(SWTBotGefEditor editor, ContainerShape shape1, int shape1X, int shape1Y, // SUPPRESS CHECKSTYLE INLINE
		ContainerShape shape2, int shape2X, int shape2Y, HostCollocation hostCo, String hostCoName, int numContainedShapes) {

		MenuUtils.save(editor);
		hostCo = DiagramTestUtils.getHostCollocationObject(editor, hostCoName);

		// Test how many components are contained in the host collocation bounds
		Assert.assertEquals(UNEXPECTED_NUM_COMPONENTS, numContainedShapes, hostCo.getComponentPlacement().size());

		// Test Position of component shapes
		Assert.assertTrue(UNEXPECTED_SHAPE_LOCATION, DiagramTestUtils.verifyShapeLocation(shape1, shape1X, shape1Y));
		Assert.assertTrue(UNEXPECTED_SHAPE_LOCATION, DiagramTestUtils.verifyShapeLocation(shape2, shape2X, shape2Y));
	}

	/**
	 * IDE-698
	 * Host Collocation resize should not execute if a Find By object would end up inside the contained
	 */
	@Test
	public void hostCollocationResizeOverFindBy() {
		waveformName = "HC_Resize_FindBy";
		final String HOST_CO_NAME = "HC1";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// maximize window
		DiagramTestUtils.maximizeActiveWindow(gefBot);

		// Add host collocation to the waveform
		DiagramTestUtils.addHostCollocationToDiagram(gefBot, editor, HOST_CO_NAME);

		// Add component/findby to the host collocation
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 20, 150);
		DiagramTestUtils.addFromPaletteToDiagram(editor, FindByUtils.FIND_BY_DOMAIN_MANAGER, 450, 150);
		MenuUtils.save(editor);

		// HostCOllocation objects
		HostCollocation hostCo = DiagramTestUtils.getHostCollocationObject(editor, HOST_CO_NAME);
		GraphicsAlgorithm hostCollocationGa = DiagramTestUtils.getHostCollocationShape(editor, HOST_CO_NAME).getGraphicsAlgorithm();

		// Attempt to expand host collocation right to cover the FindBy object
		editor.getEditPart(HOST_CO_NAME).click();
		int oldX = hostCollocationGa.getX() + hostCollocationGa.getWidth();
		int oldY = (hostCollocationGa.getY() + hostCollocationGa.getHeight()) / 2;
		int newX = oldX + 1000;
		int newY = oldY;
		editor.drag(oldX + 5, oldY + 2, newX, newY);

		// Assert that the host collocation resize was rejected
		Assert.assertFalse(editor.isDirty());
		Assert.assertEquals("Host collocation width not have changed size", oldX, hostCollocationGa.getX() + hostCollocationGa.getWidth());

		// Assert that FindBy Element is not contained within host collocation
		Assert.assertTrue("Number of components should be 1, instead there are " + hostCo.getComponentPlacement().size(),
			hostCo.getComponentPlacement().size() == 1);
		Assert.assertEquals("Expected component \'" + SIG_GEN_1 + "\' was not found", SIG_GEN_1,
			hostCo.getComponentPlacement().get(0).getComponentInstantiation().get(0).getId());
	}

	/**
	 * IDE-698
	 * Host Collocation resize should not execute if a Find By object would end up inside the contained
	 */
	@Test
	public void hostCollocationResizeOverUsesDevice() {
		waveformName = "HC_Resize_FindBy";
		final String HOST_CO_NAME = "HC1";

		// Create a new empty waveform
		WaveformUtils.createNewWaveform(gefBot, waveformName, null);
		RHBotGefEditor editor = gefBot.rhGefEditor(waveformName);

		// maximize window
		DiagramTestUtils.maximizeActiveWindow(gefBot);

		// Add host collocation to the waveform
		DiagramTestUtils.addHostCollocationToDiagram(gefBot, editor, HOST_CO_NAME);

		// Add component/findby to the host collocation
		DiagramTestUtils.addFromPaletteToDiagram(editor, SIG_GEN, 20, 150);
		DiagramTestUtils.addUseFrontEndTunerDeviceToDiagram(gefBot, editor, 450, 150);
		UsesDeviceTestUtils.completeUsesFEDeviceWizard(gefBot, "existingAllocId", "newAllocId", new String[] { "provides" }, new String[] { "uses" });
		MenuUtils.save(editor);

		// HostCOllocation objects
		HostCollocation hostCo = DiagramTestUtils.getHostCollocationObject(editor, HOST_CO_NAME);
		GraphicsAlgorithm hostCollocationGa = DiagramTestUtils.getHostCollocationShape(editor, HOST_CO_NAME).getGraphicsAlgorithm();

		// Attempt to expand host collocation right to cover the FindBy object
		editor.getEditPart(HOST_CO_NAME).click();
		int oldX = hostCollocationGa.getX() + hostCollocationGa.getWidth();
		int oldY = (hostCollocationGa.getY() + hostCollocationGa.getHeight()) / 2;
		int newX = oldX + 1000;
		int newY = oldY;
		editor.drag(oldX + 5, oldY + 2, newX, newY);

		// Assert that the host collocation resize was rejected
		Assert.assertFalse(editor.isDirty());
		Assert.assertEquals("Host collocation width not have changed size", oldX, hostCollocationGa.getX() + hostCollocationGa.getWidth());

		// Assert that Uses Device Element is not contained within host collocation
		Assert.assertTrue("Number of components should be 1, instead there are " + hostCo.getComponentPlacement().size(),
			hostCo.getComponentPlacement().size() == 1);
		Assert.assertEquals("Expected component \'" + SIG_GEN_1 + "\' was not found", SIG_GEN_1,
			hostCo.getComponentPlacement().get(0).getComponentInstantiation().get(0).getId());
	}
}
