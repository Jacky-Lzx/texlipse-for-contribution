package org.eclipse.texlipse.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO integrate this class into TexEditorTools.java
 * @author lzx
 *
 */
public class HardLineWrapTools
{
	private static final Pattern simpleCommandPattern = Pattern.compile("\\\\(\\w+|\\\\)\\s*(\\[.*?\\]\\s*)*(\\{.*?\\}\\s*)*");
	
	public HardLineWrapTools()
	{
		
	}
	/**
	 * Removes all whitespaces from the beginning of the String
	 * 
	 * @param str The string to wrap
	 * @return trimmed version of the string
	 */
	public String trimBegin(final String str)
	{
		int i = 0;
		while (i < str.length() && (Character.isWhitespace(str.charAt(i))))
			i++;
		return str.substring(i);
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
	 * Removes all whitespaces from the end of the String
	 * 
	 * @param str The string to wrap
	 * @return trimmed version of the string
	 */
	public String trimEnd(final String str)
	{
		int i = str.length() - 1;
		// while (i >= 0 && (str.charAt(i) == ' ' || str.charAt(i) == '\t'))
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
	public int[] getLineBreakPositions(String originStr, int MAX_LENGTH)
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
