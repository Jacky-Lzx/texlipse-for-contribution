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
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.texlipse.TexlipsePlugin;


/**
 * Offers general tools for different TexEditor features.
 * Tools are used mainly to implement the word wrap and the indentation
 * methods.
 * 
 * @author Laura Takkinen 
 * @author Antti Pirinen
 * @author Oskar Ojala
 */
/**
 * 	TODO getCommentCharPosition() is equal to getIndexOfComment()?
 *  TODO WrapWordString() could be merged with getLineBreakPositions().
 * @author lzx
 *
 */
public class TexEditorTools {

	/**
	 * Matches some simple LaTeX -commands
	 */
	private static final Pattern simpleCommandPattern = Pattern.compile("\\\\([a-zA-Z]+)\\s*\\{(.*?)\\}\\s*");

	public TexEditorTools() {}
	
	/**
	 * Calculates the number of tabs ('\t') in given text.
	 * 
	 * @param text Text where tabs are calculated.
	 * @return number of found tabs.
	 */
	public int numberOfTabs(String text)
	{
		int count = 0;
		char[] array = text.toCharArray();
		if (array.length == 0)
			return count;

		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == '\t')
				count++;
		}
		return count;
	}

	/**
	 * Calculates the number of spaces (' ') in given text.
	 * 
	 * @param text Text where spaces are calculated.
	 * @return number of found spaces.
	 */
	public int numberOfSpaces(String text)
	{
		int count = 0;
		char[] array = text.toCharArray();
		if (array.length == 0)
			return count;
		
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == ' ')
				count++;
			else
				break;
		}
		return count;
	}

	/**
	 * Returns the indentation of the given string. If the indentation contains
	 * tabular characters they are converted to the spaces.
	 * 
	 * @param text     source where to find the indentation
	 * @param tabWidth how many spaces one tabular character is
	 * @return the indentation of the line
	 * @edited lzx
	 */
	public String getIndentation(String text, int tabWidth) 
	{
		StringBuffer indentation = new StringBuffer();
		char[] array = text.toCharArray();
		
		if (array.length == 0)
			return "";
			
		int length = array.length;
		for (int i = 0; i < length; i++)
		{
			if (array[i] == ' ')
				indentation.append(' ');
			else if (array[i] == '\t')
			{
				for (int j = 1; j <= tabWidth; j++)
					indentation.append(' ');
			}
			else
				break;
		}
		return indentation.toString();
	}
	
	/**
	 * Returns indentation string without tabs. Method calculates number of tabs of
	 * text and converts them to spaces.
	 * 
	 * @param document Document that contains the text.
	 * @param line     Line for which the indentation is calculated.
	 * @param text     Text that marks the beginning of the text in given line.
	 * @param tabWidth Number of spaces in tab.
	 * @return Indentation String.
	 */
	public String getIndentation(IDocument document, int line, String text, int tabWidth)
	{
		if (line == -1 || line >= document.getNumberOfLines())
			return "";
		
		try
		{
			String lineText = document.get(document.getLineOffset(line), document.getLineLength(line));
			
			return getIndentation(lineText, tabWidth);
		}
		catch (Exception e)
		{
			TexlipsePlugin.log("TexEditorTools:getIndentation", e);
		}
		
		//It shouldn't be here
		return "";
	}
		
	/**
	 * Gets substring of the given text by removing the given prefix from the
	 * string. Removes also white spaces from the substring. For example: if text is
	 * "\begin {itemize}" and prefix is "\begin" return value is "{itemize}".
	 * 
	 * @param text   Text from where the substring is created.
	 * @param prefix Prefix that is removed from the text.
	 * @return Substring of the text.
	 * @throws IndexOutOfBoundsException
	 */
	public String getEndLine(String text, String prefix) throws IndexOutOfBoundsException
	{
		String endOfLine = text.substring(prefix.length());
		return endOfLine.trim();
	}
	
	/**
	 * Gets environment string from the given text.
	 * <p> '{' has to be the first char of <code>text</code>.
	 * 
	 * @param text Text where the string is searched.
	 * @return Environment string (itemize, table...). If nothing is found, empty
	 *         string is returned.
	 * @throws IndexOutOfBoundsException
	 */
	public String getEnvironment(String text) throws IndexOutOfBoundsException
	{
		int begin = text.indexOf('{');
		int end = text.indexOf('}');

		// "{" has to be to the first character of the text
		if (begin == 0 && end > begin)
			return text.substring(begin + 1, end);
		return "";
	}

	/**
	 * Finds matching \begin{environment} expression to the given \end{environment}
	 * line.
	 * 
	 * @param document    Document that contains line.
	 * @param line        End line for which the matching begin line is searched.
	 * @param environment String that defines begin-end environment type (itemize,
	 *                    enumerate...)
	 * @return Returns line number of matching begin equation, if match does not
	 *         found -1 is returned.
	 * @throws BadLocationException
	 */
	public int findMatchingBeginEquation(IDocument document, int line, String environment) throws BadLocationException
	{
		int startLine = line - 1;
		int startOffset = document.getLineOffset(startLine);
		int lineLength = document.getLineLength(startLine);
		String lineText = document.get(startOffset, lineLength).trim();
		boolean noMatch = true;
		int beginCounter = 0;
		int endCounter = 1; // one end has been detected earlier

		while (noMatch)
		{
			if (lineText.startsWith("\\begin"))
			{
				String end = getEndLine(lineText, "\\begin");
				if (getEnvironment(end).equals(environment))
				{
					beginCounter++;

					if (beginCounter == endCounter)
						return startLine;
				}
			} 
			else if (lineText.startsWith("\\end"))
			{
				String end = getEndLine(lineText, "\\end");
				if (getEnvironment(end).equals(environment))
					endCounter++;
			}
			if (startLine > 0)
			{
				startLine--;
				startOffset = document.getLineOffset(startLine);
				lineLength = document.getLineLength(startLine);
				lineText = document.get(startOffset, lineLength).trim();
			} 
			else
				noMatch = false;
		}
		return -1;
	}
	
   /** 
	 * Returns the longest legal line delimiter. 
	 * @param document 	IDocument
	 * @param command 	DocumentCommand
	 * @return 			the longest legal line delimiter
	 */
	public String getLineDelimiter(IDocument document, DocumentCommand command)
	{
		String delimiter = "\n";
		try
		{
			delimiter = document.getLineDelimiter(0);
		} catch (BadLocationException e)
		{
			TexlipsePlugin.log("TexEditorTools.getLineDelimiter: ", e);
		}
		return delimiter == null ? "\n" : delimiter;
	}
    
    
	public String getLineDelimiter(IDocument document)
	{
		return getLineDelimiter(document, null);
	}
    	
	/**
	 * Returns a length of a line.
	 * 
	 * @param document IDocument that contains the line.
	 * @param command  DocumentCommand that determines the line.
	 * @param delim    are line delimiters counted to the line length
	 * @return the line length
	 */	
	public int getLineLength(IDocument document, DocumentCommand command, boolean delim)
	{
		return getLineLength(document, command, delim, 0);
	}
	
	/**
	 * Returns a length of a line.
	 * 
	 * @param document IDocument that contains the line.
	 * @param command  DocumentCommand that determines the line.
	 * @param delim    are line delimiters counted to the line length
	 * @param target   -1 = previous line, 0 = current line, 1 = next line etc...
	 * @return the line length
	 */
	public int getLineLength(IDocument document, DocumentCommand command, boolean delim, int target)
	{
		int line;

		int length = 0;
		try
		{
			line = document.getLineOfOffset(command.offset) + target;
			if (line < 0 || line >= document.getNumberOfLines())
			{
				// line = document.getLineOfOffset(command.offset);
				return 0;
			}

			length = document.getLineLength(line);
			if (length == 0)
			{
				return 0;
			}
			if (!delim)
			{
				String txt = document.get(document.getLineOffset(line), document.getLineLength(line));
				String[] del = document.getLegalLineDelimiters();
				int cnt = TextUtilities.endsWith(del, txt);
				if (!delim && cnt > -1)
				{
					length = length - del[cnt].length();
				}
			}
		} catch (BadLocationException e)
		{
			TexlipsePlugin.log("TexEditorTools.getLineLength:", e);
		}
		return length;
	}

	/**
	 * Returns a text String of the (line + <code>lineDif</code>).
	 * 
	 * @param document IDocument that contains the line.
	 * @param command  DocumentCommand that determines the line.
	 * @param delim    are delimiters included
	 * @param lineDif  0 = current line, 1 = next line, -1 previous line etc...
	 * @return the text of the line.
	 */
	public String getStringAt(IDocument document, DocumentCommand command, boolean delim, int lineDif)
	{
		String line = "";
		int lineBegin, lineLength;
		try
		{
			lineLength = getLineLength(document, command, delim, lineDif);
			if (lineLength > 0)
			{
				lineBegin = document.getLineOffset(document.getLineOfOffset(command.offset) + lineDif);
				line = document.get(lineBegin, lineLength);
			}
		} catch (BadLocationException e)
		{
			TexlipsePlugin.log("TexEditorTools.getStringAt", e);
		}
		return line;
	}

    /**
	 * Returns a text String of the line.
	 * 
	 * @param d   IDocument that contains the line.
	 * @param c   DocumentCommand that determines the line.
	 * @param del Are delimiters included?
	 * @return The text of the current line (lineDif = 0).
	 */
	public String getStringAt(IDocument d, DocumentCommand c, boolean del)
	{
		return getStringAt(d, c, del, 0);
	}
	
	/**
	 * Detects the position of the first white space character smaller than the
	 * limit. The first character at a row is 0 and last is lineText.length-1
	 * 
	 * @param text  to search
	 * @param limit the detected white space must be before this
	 * @return index of last white space character, returns -1 if not found.
	 */
	public int getLastWSPosition(String text, int limit)
	{
		int index = -1;
		if (text.length() >= limit && limit > -1)
		{
			String temp = text.substring(0, limit); // TODO limit+1?
			int lastSpace = temp.lastIndexOf(' ');
			int lastTab = temp.lastIndexOf('\t');
			index = (lastSpace > lastTab ? lastSpace : lastTab);
		}
		return index;
	}

	/**
	 * Detects the position of the first white space character larger than the
	 * limit. The first character at a row is 0 and last is lineText.length-1
	 * 
	 * @param text  to search
	 * @param limit the detected white space is the first white space after this
	 * @return index of first white space character, returns -1 if not found.
	 */
	public int getFirstWSPosition(String text, int limit)
	{
		int index = -1;
		if (text.length() > limit && limit > -1)
		{
			String temp = text.substring(limit + 1);
			int firstSpace = temp.indexOf(' ');
			int firstTab = temp.indexOf('\t');

			if (firstSpace == -1 && firstTab != -1)
				index = firstTab + limit + 1;
			else if (firstSpace != -1 && firstTab == -1)
				index = firstSpace + limit + 1;
			else if (firstSpace > -1 && firstTab > -1)
				index = (firstSpace < firstTab ? firstSpace : firstTab) + limit + 1;
		}
		return index;
	}
	
	
	/**
	 * Trims the beginning of the given text.
	 * 
	 * @param text String that will be trimmed.
	 * @return trimmed String.
	 */
	public String trimBegin(String text)
	{
		char[] array = text.toCharArray();
		int i = 0;
		for (; i < array.length; i++)
		{
			if (array[i] != ' ' && array[i] != '\t')
				break;
		}
		return text.substring(i);
	}

	/**
	 * Trims the end of the given text.
	 * 
	 * @param text String that will be trimmed.
	 * @return trimmed String.
	 */
	public String trimEnd(String text)
	{
		int i = text.length() - 1;
		// while (i >= 0 && (str.charAt(i) == ' ' || str.charAt(i) == '\t'))
		while (i >= 0 && (Character.isWhitespace(text.charAt(i))))
			i--;
		return text.substring(0, i + 1);
	}
	

    /**
	 * Checks if the target text begins with a LaTeX command.
	 * 
	 * @param text source string
	 * @return <code>true</code> if the line contains the latex command word,
	 *         <code>false</code> otherwise
	 */
	public boolean isLineCommandLine(String text)
	{
		String txt = text.trim();
		Matcher m = simpleCommandPattern.matcher(txt);
		if (m.matches())
			return true;
		return false;
	}
	
	
	/**
	 * Checks if the target text is a comment line
	 * 
	 * @param text source text
	 * @return <code>true</code> if line starts with %-character, <code>false</code>
	 *         otherwise
	 */
	public boolean isLineCommentLine(String text)
	{
		return text.trim().startsWith("%");
	}

	/**
	 * Checks is the line begins with \item keyword
	 * 
	 * @param text string to test
	 * @return <code>true</code> if the line contains the item key word,
	 *         <code>false</code> otherwise
	 */
	public boolean isLineItemLine(String text)
	{
		return text.trim().startsWith("\\item");
	}
	
    /**
	 * This method will return the starting index of first comment on the given line
	 * or -1 if non is found.
	 * 
	 * This method looks for the first occurrence of an unescaped %
	 * 
	 * No special treatment of newlines is done.
	 * 
	 * @param line The line on which to look for a comment.
	 * 
	 * @return the index of the first % which marks the beginning of a comment or -1
	 *         if there is no comment on the given line.
	 */
	public int getIndexOfComment(String line)
	{
		int p = 0;
		int n = line.length();
		while (p < n)
		{
			char c = line.charAt(p);
			if (c == '%')
                return p;
            else if (c == '\\')
                p++; // Ignore next character
            p++;
        }
        return -1;
    }
    
    
    // Oskar's additions
    
    /**
     * Returns the indentation of the given string taking
     * into account if the string starts with a comment.
     * The comment character is included in the output.
     * 
     * @param text      source where to find the indentation
     * @return          The indentation of the line including the comment
     */
    public String getIndentationWithComment(String text) {
        StringBuffer indentation = new StringBuffer();
        char[] array = text.toCharArray();
        
        if (array.length == 0) {
            return indentation.toString();
        }
        
        int i = 0;
        while (i < array.length
                && (array[i] == ' ' || array[i] == '\t')) {
            indentation.append(array[i]);
            i++;
        }
        if (i < array.length && array[i] == '%') {
            indentation.append("% ");
        }
        
        return indentation.toString();
    }
    
    /**
     * Returns the indentation of the given string but keeping tabs.
     * 
     * @param text      source where to find the indentation
     * @return          The indentation of the line
     */
    public String getIndentation(String text) {
        StringBuffer indentation = new StringBuffer();
        char[] array = text.toCharArray();
        
        int i = 0;
        while (i < array.length
                && (array[i] == ' ' || array[i] == '\t')) {
            indentation.append(array[i]);
            i++;
        }
        return indentation.toString();
    }
    
    
    public String wrapWordString(String input, String indent, int width, String delim) {
        String[] words = input.split("\\s");
        if (input.length() == 0 || words.length == 0) {
            return "";
        }
        StringBuffer sbout = new StringBuffer(indent);
        sbout.append(words[0]);
        int currLength = indent.length() + words[0].length();
        for (int j = 1; j < words.length; j++) {
            // Check whether the next word still fits on the current line
            if (currLength + 1 + words[j].length() <= width) {
                sbout.append(" ");
                sbout.append(words[j]);
                currLength += 1 + words[j].length(); 
            } else {
                sbout.append(delim);
                sbout.append(indent);
                sbout.append(words[j]);
                currLength = indent.length() + words[j].length();
            }
        }
        sbout.append(delim);
        return sbout.toString();
    }

    public String[] getEnvCommandArg(String text) {
        String txt = text.trim();
        Matcher m = simpleCommandPattern.matcher(txt);
        while (m.find()) {
            if ("begin".equals(m.group(1)) || "end".equals(m.group(1))) {
                return new String[] {m.group(1), m.group(2)};
            }
        }
        return new String[] {"", ""};
    }

    /**
     * e.g. if <code>text = "This is an example\r\n\r\n\r\n"</code> and <code>delimiter = "\r\n"</code>,
     * it will return <code>"\r\n\r\n\r\n"</code>
     * 
     * @param text
     * @param delim
     * @return new lines at the end of the <code> text </code> (including current line's delimiter)
     * 
     * @AddExplanation lzx
     */
    public String getEmptylinesAtEnd(String text, String delim) {
        StringBuffer sb = new StringBuffer();
        for (int i = text.length() - delim.length(); i >= 0; i -= delim.length()) {
            if (text.regionMatches(i, delim, 0, delim.length())) {
                sb.append(delim);
            } else {
                break;
            }
        }
        return sb.toString();
    }
    
    /**
	 * Removes all whitespaces and the first "% " from the beginning of the String.
	 * 
	 * Examples: " hello world" will return "hello world" " % hello" will return
	 * "hello" " %hello" will return "hello" " % % hello" will return "% hello" " %%
	 * hello" will return "% hello"
	 * 
	 * @param str The string to trim
	 * @return trimmed version of the string
	 */
	public String trimBeginPlusComment(final String str)
	{
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
	public boolean isSingleLine(String line)
	{
		if (trimBegin(line).startsWith("%"))
			return true;
		if ((trimBegin(line).startsWith("\\")))
			return true; // e.g. \\ or \[
		if (trimBegin(line).startsWith("\\item"))
			return true;
		if (trimBegin(line).length() == 0)
			return true;
		Matcher m = simpleCommandPattern.matcher(line);
		if (m.matches())
			return true;
		return false;
	}
	
	/**
	 * Finds the best position in the given String to make a line break
	 * 
	 * @param line
	 * @param MAX_LENGTH
	 * @return -1 if not found
	 */
	public int getLineBreakPosition(String line, int MAX_LENGTH)
	{
		int offset = 0;
		// Ignore indentation
		while (offset < line.length() && (line.charAt(offset) == ' ' || line.charAt(offset) == '\t'))
		{
			offset++;
		}

		int breakOffset = -1;
		while (offset < line.length())
		{
			if (offset > MAX_LENGTH && breakOffset != -1)
				break;
			if (line.charAt(offset) == ' ' || line.charAt(offset) == '\t')
			{
				breakOffset = offset;
			}
			offset++;
		}
		return breakOffset;
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
	public int[] getLineBreakPositions(String originStr, int MAX_LENGTH, int indentationLength)
	{
		int length = originStr.length() / MAX_LENGTH + 1;
		int[] breakPositions = new int[length];
		breakPositions[0] = getLineBreakPosition(originStr, MAX_LENGTH);
		for (int i = 1; i < length; i++)
		{
			String nextLine = originStr.substring(breakPositions[i - 1] + 1);
			if (nextLine.length() < MAX_LENGTH) break;
			int pos = getLineBreakPosition(nextLine, MAX_LENGTH - indentationLength);
			breakPositions[i] = breakPositions[i - 1] + pos + 1;
		}
		return breakPositions;
	}
	
	public boolean isCommentLine(String str)
	{
		return trimBegin(str).startsWith("%");
	}
	
	
	public boolean isCommentInLine(String str)
	{
		return this.getCommentCharPosition(str) > 0;
	}
	
	/**
	 * TODO: merge it with the one in HardLineWrap.java
	 * It is supposed that input string only has one in-text comment.
	 * Otherwise it returns the first '%' ("\%" exclusive) index.
	 * 
	 * @param str
	 * @return index of single '%' ("\%" exclusive)
	 * 
	 * @author lzx
	 */
	public int getCommentCharPosition(String str)
	{
		char[] ar = str.toCharArray();
		int length = ar.length;
		for (int i = 1; i < length; i++)
		{
			if(ar[i] == '%' && ar[i - 1] != '\\')
			{
				return i;
			}
		}
		return -1;
	}
}
