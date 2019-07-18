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
package org.eclipse.texlipse.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.texlipse.TexlipsePlugin;
import org.eclipse.texlipse.model.OutlineNode;
import org.eclipse.texlipse.properties.TexlipseProperties;


/**
 * Updates code folding marks into the given editor.
 * 
 * @author Oskar Ojala
 */
public class TexCodeFolder {

    private TexEditor editor;
    private ProjectionAnnotationModel model;
    
//  private ArrayList<TexProjectionAnnotation> oldNodes;
    private ArrayList<TexProjectionAnnotation> oldNodes;

    private boolean firstRun;

    private HashSet<String> environments;
    private boolean preamble;
    private boolean part;
    private boolean chapter;
    private boolean section;
    private boolean subs;
    private boolean subsubs;
    private boolean paragraph;
    
    /**
     * Creates a new code folder.
     * 
     * @param editor The editor to which this folder is associated
     */
    public TexCodeFolder(TexEditor editor) {
        this.editor = editor;
        firstRun = true;
    }
    
    /**
     * Updates the code folds of the editor.
     * 
     * @param outline The document outline data structure containing the document positions
     */
    public void update(ArrayList<OutlineNode> outline) {
        model = (ProjectionAnnotationModel)editor.getAdapter(ProjectionAnnotationModel.class);

        if (model != null) {
            this.addMarks(outline);
        }
    }
    
    /**
     * Adds the folding marks to the editor and removes redundant marks.
     * 
     * @param outline The document outline data structure containing the document positions
     */
	private void addMarks(ArrayList<OutlineNode> outline)
	{
		if (firstRun)
		{
			String[] envs = TexlipsePlugin.getPreferenceArray(TexlipseProperties.CODE_FOLDING_ENVS);
			environments = new HashSet<String>(envs.length + 1);
			for (int i = 0; i < envs.length; i++)
				environments.add(envs[i]);

			preamble = TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.CODE_FOLDING_PREAMBLE);
			part = TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.CODE_FOLDING_PART);
			chapter = TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.CODE_FOLDING_CHAPTER);
			section = TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.CODE_FOLDING_SECTION);
			subs = TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.CODE_FOLDING_SUBSECTION);
			subsubs = TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.CODE_FOLDING_SUBSUBSECTION);
			paragraph = TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.CODE_FOLDING_PARAGRAPH);

			Map<TexProjectionAnnotation, Position> map = new HashMap<>();
			fillAnnotationMap(outline, map);
			model.modifyAnnotations(null, map, null);
			firstRun = false;
			environments = null; // frees up the memory
		} 
		else
		{
			// save old nodes
			oldNodes = new ArrayList<TexProjectionAnnotation>();
			for (Iterator iter = model.getAnnotationIterator(); iter.hasNext();)
			{
				oldNodes.add((TexProjectionAnnotation) iter.next());
			}

			markTreeNodes(outline);

			TexProjectionAnnotation[] deletes = new TexProjectionAnnotation[oldNodes.size()];
			
			/* Add elements in oldNodes into deletes*/
			oldNodes.toArray(deletes);
			model.modifyAnnotations(deletes, null, null);
		}
	}

    /**
     * Traverses the <code>documentTree</code> and updates each node's
     * corresponding marker.
     * 
     * @param documentTree The document outline data structure containing the document positions
     */
    private void markTreeNodes(ArrayList<OutlineNode> documentTree) {
        for (ListIterator<OutlineNode> iter = documentTree.listIterator(); iter.hasNext();) {
            OutlineNode on = (OutlineNode) iter.next();

            // Here, call the appropriate method on the node
            inspectAndAddMark(on);
            
            // ...and recurse over the children...
            if (on.getChildren() != null)
                markTreeNodes(on.getChildren());
        }
    }

    /**
     * Inspects a folding mark and if necessary adds a new mark.
     * 
     * @param node The node to inspect
     */
    private void inspectAndAddMark(OutlineNode node) {
        Position pos = node.getPosition();
        for (ListIterator<TexProjectionAnnotation> iter = oldNodes.listIterator(); iter.hasNext();) {
            TexProjectionAnnotation cAnnotation = (TexProjectionAnnotation) iter.next();
            if (cAnnotation.likelySame(node)) {
                oldNodes.remove(cAnnotation);
                //model.modifyAnnotationPosition(cAnnotation, pos);
                return;
            }
        }
        model.addAnnotation(new TexProjectionAnnotation(node), pos);
    }

    /**
     * Creates an annotation of every node in <code>documentTree</code> and
     * puts the annotations into the given map. Annotations of certain types
     * defined in the preferences are automatically folded.
     * 
     * @param documentTree The document outline tree
     * @param map A <code>Map</code> where to put the annotations
     */
	private void fillAnnotationMap(List<OutlineNode> documentTree, Map<TexProjectionAnnotation, Position> map)
	{
		for (ListIterator<OutlineNode> iter = documentTree.listIterator(); iter.hasNext();)
		{
			OutlineNode node = (OutlineNode) iter.next();

			Position pos = node.getPosition();

			boolean folding = false;
			// strictly speaking object-oriented code should not need switch-statements,
			// but this a lot faster than some object-approach
			switch (node.getType()) {
				case OutlineNode.TYPE_PREAMBLE:
					folding = this.preamble;
					break;
				case OutlineNode.TYPE_PART:
					folding = this.part;
					break;
				case OutlineNode.TYPE_CHAPTER:
					folding = this.chapter;
					break;
				case OutlineNode.TYPE_SECTION:
					folding = this.section;
					break;
				case OutlineNode.TYPE_SUBSECTION:
					folding = this.subs;
					break;
				case OutlineNode.TYPE_SUBSUBSECTION:
					folding = this.subsubs;
					break;
				case OutlineNode.TYPE_PARAGRAPH:
					folding = this.paragraph;
					break;
				case OutlineNode.TYPE_ENVIRONMENT:
					if (environments.contains(node.getName()))
						folding = true;
					break;
				default:
					break;
			}

			TexProjectionAnnotation tpa = new TexProjectionAnnotation(node, folding);
			map.put(tpa, pos);

			if (node.getChildren() != null)
				fillAnnotationMap(node.getChildren(), map);
		}
	}
}
