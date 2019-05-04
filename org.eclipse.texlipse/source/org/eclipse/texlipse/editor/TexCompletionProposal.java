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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.texlipse.TexlipsePlugin;
import org.eclipse.texlipse.model.TexCommandEntry;
import org.eclipse.texlipse.properties.TexlipseProperties;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * This class is a wrapper class to create an ICompletionProposal
 * from a TexCommandEntry. It also creates "smart" braces.
 * 
 * @author Boris von Loesch
 * @author Oskar Ojala
 *
 */
public class TexCompletionProposal implements ICompletionProposal {
    private TexCommandEntry fentry;
    private int fReplacementOffset;
    private int fReplacementLength;
    private ISourceViewer fviewer;
    
    /**
     * Constructs a new completion proposal for a (La)TeX command
     * 
     * @param entry The command entry
     * @param replacementOffset Offset of where it is to be replaced
     * @param replacementLength The length of the replacement
     * @param viewer The current viewer
     */
    public TexCompletionProposal(TexCommandEntry entry, int replacementOffset, int replacementLength, ISourceViewer viewer) {
        fentry = entry;
        fReplacementOffset = replacementOffset;
        fReplacementLength = replacementLength;
        fviewer = viewer;
    }
    
    /**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.
	 * jface.text.IDocument)
	 */
	public void apply(IDocument document)
	{
		try
		{
			/**
			 * TODO How to change the offset of cursor?
			 */
			if (fentry.arguments > 0)
			{
				IRegion region = document.getLineInformationOfOffset(fReplacementOffset);
				/** the content of existing command ("\\" exclusive) */
				String existCommand = document.get(region.getOffset(), region.getLength());
				existCommand = existCommand.substring(fReplacementOffset - region.getOffset());
				
				int length = fentry.key.length();
				int commandLength = existCommand.length();
				int fromIndex = (length < commandLength ? length : commandLength);
				//the <code>fromIndex</code> here is just used to avoid unnecessary calculation
				for (int i = 0; i < fromIndex; i++)
				{
					if (existCommand.charAt(i) != fentry.key.charAt(i))
					{
						fromIndex = i;
						break;
					}
				}
				document.replace(fReplacementOffset + fromIndex, /*length - fromIndex*/0, fentry.key.substring(fromIndex));
					
				//Insert brackets here
				int argumentNumber = fentry.arguments;
				int[] arguments = new int[argumentNumber];
				int[] argumentsIndex = new int[argumentNumber];
				int index = 0;
				int fromTemp = 0;
				int insertIndex = length;
				for (int i = fromIndex; i < commandLength; i++)
				{
					if (existCommand.charAt(i) == '{')
					{
						argumentsIndex[index] = i + 1;
						fromTemp = i;
					}
					else if (existCommand.charAt(i) == '}')
					{
						argumentNumber--;
						insertIndex = i + 1;
						arguments[index] = i - fromTemp - 1;
						index++;
					}
				}
				int temp;
				for (int i = 0; i < argumentNumber; i++)
				{
					temp = insertIndex + i + i;
					document.replace(fReplacementOffset + temp, 0, "{}");
					argumentsIndex[index] = temp + 1;
				}
				
				if (TexlipsePlugin.getDefault().getPreferenceStore().getBoolean(TexlipseProperties.SMART_PARENS))
				{
					LinkedModeModel model = new LinkedModeModel();
					
					for (int j = 0; j < fentry.arguments; j++)
					{
						LinkedPositionGroup group = new LinkedPositionGroup();
						group.addPosition(new LinkedPosition(document, fReplacementOffset + argumentsIndex[j], arguments[j], LinkedPositionGroup.NO_STOP));
						model.addGroup(group);
					}
					model.forceInstall();
					LinkedModeUI ui = new EditorLinkedModeUI(model, fviewer);
					ui.setSimpleMode(false);
//					ui.setExitPolicy(new ExitPolicy('}', fviewer));
					ui.setExitPosition(fviewer, fReplacementOffset + insertIndex + 2 * argumentNumber, 0, Integer.MAX_VALUE);
					ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
					ui.enter();
				}
			} 
			else
				document.replace(fReplacementOffset, fReplacementLength, fentry.key);
		} catch (BadLocationException x) {}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
     */
	public Point getSelection(IDocument document)
	{
		if (fentry.arguments > 0)
			return new Point(fReplacementOffset + fentry.key.length() + 1, 0);
		return new Point(fReplacementOffset + fentry.key.length(), 0);
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        return (fentry.info.length() > TexCompletionProcessor.assistLineLength ?
                TexCompletionProcessor.wrapString(fentry.info, TexCompletionProcessor.assistLineLength)
                : fentry.info);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
        return fentry.key;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage() {
        return fentry.getImage();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
        return null;
    }
    
    protected static class ExitPolicy implements IExitPolicy {
        
        final char fExitCharacter;
        ISourceViewer fviewer;
        
        public ExitPolicy(char exitCharacter, ISourceViewer viewer) {
            fExitCharacter= exitCharacter;
            fviewer = viewer;
        }
        
        /*
         * @see org.eclipse.jdt.internal.ui.text.link.LinkedPositionUI.ExitPolicy#doExit(org.eclipse.jdt.internal.ui.text.link.LinkedPositionManager, org.eclipse.swt.events.VerifyEvent, int, int)
         */
        public ExitFlags doExit(LinkedModeModel environment, VerifyEvent event, int offset, int length) {
            if (event.character == fExitCharacter) {
                try {
                    if (fviewer.getDocument().getChar(offset) == fExitCharacter) {
                        event.doit = false;
                    }
                    try {
                        if (fviewer.getDocument().getChar(offset + 1) == '{') {
                            fviewer.setSelectedRange(offset + 2, 0);
                        } else {
                            fviewer.setSelectedRange(offset + 1, 0);
                        }
                        return null;
                    } catch (BadLocationException e) {
                    }
                    fviewer.setSelectedRange(offset + 1, 0);
                } catch (BadLocationException e1) {
                    // Should not happen
                }
            } else if (event.character == '{') {
                try {
                    if (fviewer.getDocument().getChar(offset - 1) == '{')
                        event.doit = false;
                } catch (BadLocationException e) {
                }
            }
            return null;
        }
    }
    
}
