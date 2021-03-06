/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gov.redhawk.ide.graphiti.ui.tests.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.junit.Assert;
import org.junit.Test;

import gov.redhawk.ide.swtbot.UITest;

public abstract class AbstractXmlEditorTest extends UITest {

	/**
	 * Tests that the XML editor, and not a text editor, is used for a SAD editor opened for something in the SDRROOT,
	 * rather than the workspace.
	 */
	@Test
	public void xmlEditor() {
		openEditorFromSdrRoot();

		final IEditorPart editorPart = bot.activeEditor().getReference().getEditor(false);
		final List<IEditorPart> containedEditors = new ArrayList<IEditorPart>();
		bot.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				Collections.addAll(containedEditors, ((MultiPageEditorPart) editorPart).findEditors(editorPart.getEditorInput()));
			}
		});
		Assert.assertEquals("org.eclipse.wst.sse.ui.StructuredTextEditor", containedEditors.get(0).getClass().getName());
	}

	/**
	 * Open the editor for an item in the Target SDR, switch to the XML editor tab.
	 * @return The editor
	 */
	protected abstract SWTBotEditor openEditorFromSdrRoot();

}
