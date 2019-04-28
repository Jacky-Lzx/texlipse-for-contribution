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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.texlipse.TexlipsePlugin;


/**
 * This class handles the line wrapping.
 * 
 * @author Antti Pirinen
 * @author Oskar Ojala
 * @author Boris von Loesch
 */
public class HardLineWrap {
    
    private TexEditorTools tools;
	private static final Pattern simpleCommandPattern = Pattern.compile("\\\\(\\w+|\\\\)\\s*(\\[.*?\\]\\s*)*(\\{.*?\\}\\s*)*");

    public HardLineWrap(){
        this.tools = new TexEditorTools();
    }

    
    /**
     * Removes all whitespaces from the beginning of the String
     * @param str The string to wrap
     * @return trimmed version of the string
     */
    private static String trimBegin (final String str) {
        int i = 0;
        while (i < str.length() && (Character.isWhitespace(str.charAt(i)))) 
            i++;
        return str.substring(i);
    }
    
    /**
     * Removes all whitespaces and the first "% " from the beginning of the 
     * String.
     * 
     * Examples:
     * "   hello world" will return "hello world"
     * "   % hello" will return "hello"
     * "   %hello" will return "hello"
     * "   % % hello" will return "% hello"
     * "   %% hello" will return "% hello"
     * 
     * @param str The string to trim
     * @return trimmed version of the string
     */
    private static String trimBeginPlusComment(final String str) {
        int i = 0;
        while (i < str.length() && (Character.isWhitespace(str.charAt(i)))) 
            i++;
        if (i < str.length() && str.charAt(i) == '%')
            i++;
        if (i < str.length() && str.charAt(i) == ' ')
            i++;
        return str.substring(i);
    }
    
    /**
     * Removes all whitespaces from the end of the String
     * @param str The string to wrap
     * @return trimmed version of the string
     */
    private static String trimEnd (final String str) {
        int i = str.length() - 1;
        //while (i >= 0 && (str.charAt(i) == ' ' || str.charAt(i) == '\t')) 
        while (i >= 0 && (Character.isWhitespace(str.charAt(i)))) 
            i--;
        return str.substring(0, i + 1);
    }

    /**
     * This method checks, whether <i>line</i> should stay alone on one line.<br />
     * Examples:
     * <ul>
     * <li>\begin{env}</li>
     * <li>% Comments</li>
     * <li>\command[...]{...}{...}</li>
     * <li>(empty line)</li>
     * <li>\\[2em]</li>
     * </ul>
     * 
     * @param line
     * @return
     */
	private static boolean isSingleLine(String line)
	{
		String trimmedLine = trimBegin(line);
		if (trimmedLine.startsWith("%"))
			return true;
		// The line starts with "\%" is not a sigle line
		if (trimmedLine.startsWith("\\") && !trimmedLine.startsWith("\\%"))
			return true; // e.g. \\ or \[ 
		if (trimmedLine.startsWith("\\item"))
			return true;
		if (trimmedLine.length() == 0)
			return true;
		Matcher m = simpleCommandPattern.matcher(line);
		if (m.matches())
			return true;
		return false;
	}
    
    /**
     * Finds the best position in the given String to make a line break
     * @param line
     * @param MAX_LENGTH
     * @return -1 if not found
     */
    private static int getLineBreakPosition(String line, int MAX_LENGTH) {
    	int offset = 0;
    	//Ignore indentation
    	while (offset < line.length() && (line.charAt(offset) == ' ' || line.charAt(offset) == '\t')) {
    		offset++;
    	}
    	
    	int breakOffset = -1;
    	while (offset < line.length()) {
    		if (offset > MAX_LENGTH && breakOffset != -1) break;
    		if (line.charAt(offset) == ' ' || line.charAt(offset) == '\t') {
    			breakOffset = offset;
    		}
    		offset++;
    	}
    	return breakOffset;
    }
    /**
     * New line wrapping strategy.    
     * The actual wrapping method. Based on the <code>IDocument d</code>
     * and <code>DocumentCommand c</code> the method determines how the
     * line must be wrapped. 
     * <p>
     * If there is more than <code>MAX_LENGTH</code>
     * characters at the line, the method tries to detect the last white
     * space before <code> MAX_LENGTH</code>. In case there is none, the 
     * method finds the first white space after <code> MAX_LENGTH</code>.
     * Normally it adds the rest of the currentline to the next line. 
     * Exceptions are empty lines, commandlines, commentlines, and special lines like \\ or \[.
     * 
     * @param d             
     * @param c             The text that will inserted into the document later on
     * @param MAX_LENGTH    How many characters are allowed at one line.
     * 
     * @add lzx
     * Every time user types a char, the function will be called.
     * 
     * Done: Delete the case of comment line: no normal one will use comment in a line. If so, don't wrap it.
     * Done: Line does not merge with the next comment line.
     * Done: Comment line wraps. 
     * Done: Newline might need wrapping as well if too long
     */
	/*public void doWrapB(IDocument d, DocumentCommand c, int MAX_LENGTH)
	{
		*//**
		 * Notes:
		 * 	1. c.caretOffset: The location of the cursor. e.g. If inputing a 'e', the caret will be right after 'e', i.e. "e^".
		 *  2. lineNr starts from 0.
		 *  3. c.offset: The offset in the document that insert the c.text.
		 *  4. If c.length == 0: insert c.text; if c.length != 0: replace the char from c.offset to c.offset + c.length with c.text.
		 *//*
		try
		{
			*//**
			 * Get the line of the command excluding delimiter
			 * commandRegion contains <code>offset</code> and <code>length</code>
			 *//*
			IRegion commandRegion = d.getLineInformationOfOffset(c.offset);

			*//** line: the content of the line before inserting <code>c.text</code> *//*
			String line = d.get(commandRegion.getOffset(), commandRegion.getLength());
			int lineNr = d.getLineOfOffset(c.offset);
			final int cursorOnLine = c.offset - commandRegion.getOffset();
			
			//Ignore texts with line endings and needn't to be wrapped 
			if (c.text.indexOf("\n") >= 0 || c.text.indexOf("\r") >= 0 || commandRegion.getLength() + c.text.length() <= MAX_LENGTH)
			{
				System.out.println(line.length() + ":" + line.charAt(line.length() - 1));
				*//** better not change the content of document *//*
				d.replace(c.offset, 2, "www");
				c.text = "";
				c.length = 1;
				c.offset -= 1;
				c.offset = commandRegion.getOffset();
				c.text = "centimeter";
				c.length = 0;
				return;
			}
			
			//If there's a comment in the current line and the normal part needn't wrap: do not wrap.
			if (tools.getIndexOfComment(tools.trimBegin(line)) > 0 && tools.getIndexOfComment(tools.trimBegin(line)) < MAX_LENGTH)
				return;

			// Create the newLine, we rewrite the whole currentline
			StringBuffer newLineBuf = new StringBuffer();

			newLineBuf.append(line.substring(0, cursorOnLine));
			newLineBuf.append(c.text);
			newLineBuf.append(trimEnd(line.substring(cursorOnLine)));

			// Special case if there are white spaces at the end of the line
			if (trimEnd(newLineBuf.toString()).length() <= MAX_LENGTH) 
				return;

			boolean isLastLine = false;
			*//** For windows the delimiter is '/n/' *//*
			String delim = d.getLineDelimiter(lineNr);
			if (delim == null)
			{
				// This is the last line in the document
				isLastLine = true;
				if (lineNr > 0)
					delim = d.getLineDelimiter(lineNr - 1);
				else
				{
					// Last chance
					delim = d.getLegalLineDelimiters()[0];
					System.out.println("Error: you are not supposed to be here.");
				}
			}
			
			int length = line.length();

			String nextline = tools.getStringAt(d, c, false, 1);
			String nextTrimLine = nextline.trim();
			boolean isWithNextline = false;

			// Figure out whether the next line should be merged with the wrapped text

			// 1st case: wrapped text ends with '.' ':' or '\\'
			if (line.trim().endsWith(".") || line.trim().endsWith(":") || line.trim().endsWith("\\\\"))
			{
				newLineBuf.append(delim); // do not merge
			} 
			else
			{
				// 2nd case: merge comment lines
				if (tools.isLineCommentLine(line) // wrapped text is a comment line,
						&& tools.isLineCommentLine(nextTrimLine) // next line is also a comment line,
						&& tools.getIndentation(line).equals(tools.getIndentation(nextline)) // with the same indentation!
						&& !isSingleLine(trimBeginPlusComment(nextTrimLine))) // but not an empty comment line, commented command line, etc.
				{
					// merge!
					newLineBuf.append(' ');
					newLineBuf.append(trimBeginPlusComment(nextline));
					length += nextline.length();
					isWithNextline = true;
				}
				// 3th case: Wrapped text is comment, next line is not (otherwise case 2)
				else if (tools.getIndexOfComment(line) >= 0)
				{
					newLineBuf.append(delim);
				}
				// 4rd case: Next line is a comment/command
				else if (isSingleLine(nextTrimLine))
				{
					newLineBuf.append(delim);
				}
				// all other cases
				else
				{
					// merge: Add the whole next line
					newLineBuf.append(' ');
					newLineBuf.append(trimBegin(nextline));
					length += nextline.length();
					isWithNextline = true;
				}
			}


			if (!isLastLine)
				length += delim.length();
			String newLine = newLineBuf.toString();

			int breakpos = getLineBreakPosition(newLine, MAX_LENGTH);
			if (breakpos < 0)
				return;

			c.length = length;
			
			
			String indent = tools.getIndentationWithComment(line);

			c.shiftsCaret = false;
			c.caretOffset = c.offset + c.text.length() + indent.length();
			if (breakpos >= cursorOnLine + c.text.length())
			{
				c.caretOffset -= indent.length();
			}
			if (breakpos < cursorOnLine + c.text.length())
			{
				// Line delimiter - one white space
				c.caretOffset += delim.length() - 1;
			}

			c.offset = commandRegion.getOffset();

			StringBuffer buf = new StringBuffer();
			buf.append(newLine.substring(0, breakpos));
			buf.append(delim);
			buf.append(indent);
			// Are we wrapping a comment onto the next line without its %?
			//This couldn't happen
			if (tools.getIndexOfComment(newLine.substring(0, breakpos)) >= 0 && tools.getIndexOfComment(indent) == -1)
				buf.append("% ");
			buf.append(trimBegin(newLine.substring(breakpos)));

			// Remove unnecessary characters from buf
			int i = 0;
			while (i < line.length() && line.charAt(i) == buf.charAt(i))
			{
				i++;
			}
			buf.delete(0, i);
			c.offset += i;
			c.length -= i;
			if (isWithNextline)
			{
				i = 0;
				while (i < nextline.length() && nextline.charAt(nextline.length() - i - 1) == buf.charAt(buf.length() - i - 1))
				{
					i++;
				}
				buf.delete(buf.length() - i, buf.length());
				c.length -= i;
			}

			c.text = buf.toString();

		} catch (BadLocationException e)
		{
			TexlipsePlugin.log("Problem with hard line wrap", e);
		}
	}*/
	
	/**
	 *  After entering a char in the text, wrap the current paragraph and rewrite 
	 *  <code>c</code> to execute the change by the eclipse later.
	 * 
	 * Done: correct wrap with "\%"
	 * Done: when in-text comment was accidentally wrapped to next line, it will be treated as comment line
	 * @param d
	 * @param c
	 * @param MAX_LENGTH
	 * 
	 * @author lzx
	 */
	public void doWrapD(IDocument d, DocumentCommand c, int MAX_LENGTH)
	{
		try
		{
			IRegion commandRegion = d.getLineInformationOfOffset(c.offset);
			/** the content of current line before inserting <code>c.text</code> */
			String line = d.get(commandRegion.getOffset(), commandRegion.getLength());
			
			final int cursorOnLine = c.offset - commandRegion.getOffset();
			
			//Ignore texts with line endings and needn't to be wrapped 
			if (c.text.indexOf("\n") >= 0 || c.text.indexOf("\r") >= 0 || commandRegion.getLength() + c.text.length() <= MAX_LENGTH)
				return;
			// Ignore the in-text comment to wrap
			if (tools.getIndexOfComment(tools.trimBegin(line)) > 0 && tools.getIndexOfComment(tools.trimBegin(line)) < MAX_LENGTH)
				return;

			/**
			 * @bug
			 * There is a bug that when copy a long paragraph into the document and then input char, the cursor offset is not correct.
			 * <p>But the first thing after copying should be force hard line(Esc, q), so this bug needn't be solved.
			 */
			//set the location of cursor after merge
			c.shiftsCaret = false;
			c.caretOffset = c.offset + c.text.length();
			
			
			StringBuffer newLineBuf = new StringBuffer();
			//insert <code>c.text</code> into the line
			newLineBuf.append(line.substring(0, cursorOnLine));
			newLineBuf.append(c.text);
			newLineBuf.append(trimEnd(line.substring(cursorOnLine)));

			
			int lineNr = d.getLineOfOffset(c.offset);
			
			String delim = d.getLineDelimiter(lineNr);
			boolean isLastLine = false;
			if (delim == null)
			{
				// This is the last line in the document
				isLastLine = true;
				if (lineNr > 0)
					delim = d.getLineDelimiter(lineNr - 1);
				else
				{
					// Last chance
					delim = d.getLegalLineDelimiters()[0];
					System.out.println("Error: you are not supposed to be here.");
				}
			}
			
			c.length = line.length();
			c.offset = commandRegion.getOffset();
			
			int lineDif = 1;
			String nextLine = tools.getStringAt(d, c, false, 1);
			
			boolean isCommentLine = trimBegin(line).startsWith("%");
			if (isCommentLine)
			{
				while(nextLine != "" && trimBegin(nextLine).startsWith("%"))
				{
					lineDif++;
					newLineBuf.append(' ' + trimBeginPlusComment(nextLine).trim());
					c.length += nextLine.length();
					nextLine = tools.getStringAt(d, c, false, lineDif);
				}
			}
			else
			{
				while (nextLine != "" && !isSingleLine(nextLine))
				{
					lineDif++;
					newLineBuf.append(' ' + nextLine.trim());
					c.length += nextLine.length();
					if (getCommentCharPosition(nextLine) > 0)
						break;
					nextLine = tools.getStringAt(d, c, false, lineDif);
				}
			}
			
			boolean isCommentInLine = getCommentCharPosition(newLineBuf.toString()) > 0;
			int[] breakpos = getLineBreakPositions(isCommentInLine? newLineBuf.substring(0, getCommentCharPosition(newLineBuf.toString())) : newLineBuf.toString(), MAX_LENGTH);
			int length = 0;
			for(int i = breakpos.length - 1; i >= 0; i--)
			{
				if (breakpos[i] != 0)
				{
					length = i;
					break;
				}
			}
			for (int i = length; i >= 0; i--)
				newLineBuf.replace(breakpos[i], breakpos[i] + 1, delim + tools.getIndentation(line) + (isCommentLine? "% " : ""));
			
			if (!isLastLine)
			{
				c.length += delim.length() * lineDif;
				c.length -= 2;
				if (nextLine.length() == 0) c.length += 1;
			}
			
			if (cursorOnLine >= breakpos[0])
				c.caretOffset += tools.getIndentation(line).length() + 1 + (isCommentLine ? 2: 0);
			c.text = newLineBuf.toString();
			
		}
		catch(BadLocationException e)
		{
			System.out.println("Error");
			TexlipsePlugin.log("Problem with hard line wrap", e);
		}
	}
	
	/**
	 * Get the line break positions and return as int[]
	 * 
	 * @param originStr (containing indentation at the beginning)
	 * @param MAX_LENGTH How many characters are allowed in one line
	 * @return Positions that need to insert delimiter and indentation
	 * 
	 * @author lzx
	 */
	private static int[] getLineBreakPositions(String originStr, int MAX_LENGTH)
	{
		int length = originStr.length() / MAX_LENGTH + 1;
		int[] breakPositions = new int[length];
		breakPositions[0] = getLineBreakPosition(originStr, MAX_LENGTH);
		for (int i = 1; i < length; i++)
		{
			String nextLine = originStr.substring(breakPositions[i - 1] + 1);
			if (nextLine.length() < MAX_LENGTH) break;
			int pos = getLineBreakPosition(nextLine, MAX_LENGTH);
			breakPositions[i] = breakPositions[i - 1] + pos + 1;
		}
		return breakPositions;
	}
	
	/**
	 * It is supposed that input string only has one in-text comment.
	 * Otherwise it returns the first '%' ("\%" exclusive) index.
	 * 
	 * @param str
	 * @return index of single '%' ("\%" exclusive)
	 * 
	 * @author lzx
	 */
	private static int getCommentCharPosition(String str)
	{
		char[] ar = str.toCharArray();
		int length = ar.length;
		for (int i = 1; i < length; i++)
		{
			if(ar[i] == '%' && ar[i - 1] != '\\')
				return i;
		}
		return -1;
	}
    
 }
