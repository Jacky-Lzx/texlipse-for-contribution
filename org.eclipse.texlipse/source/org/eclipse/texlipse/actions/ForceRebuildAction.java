/*******************************************************************************
 * Copyright (c) 2017 the TeXlipse team and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The TeXlipse team - initial API and implementation
 *******************************************************************************/
/*
 * $Id
 */
package org.eclipse.texlipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.texlipse.TexlipsePlugin;
import org.eclipse.texlipse.properties.TexlipseProperties;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * Forces a (partial) rebuild of the current project 
 * @author Boris von Loesch
 *
 */
public class ForceRebuildAction implements IEditorActionDelegate {

    private IEditorPart editor;

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = targetEditor;
    }

    public void run(IAction action) {
        IResource res = (IResource) editor.getEditorInput().getAdapter(IResource.class);
        if (res == null) return;
        
        IProject project = res.getProject();
        TexlipseProperties.setSessionProperty(project, TexlipseProperties.FORCED_REBUILD, true);
        try {
            project.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        } catch (CoreException e) {
            TexlipsePlugin.log("Force rebuild CoreException", e);
        }        
        TexlipseProperties.setSessionProperty(project, TexlipseProperties.FORCED_REBUILD, null);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        //Nothing to do
    }

}
