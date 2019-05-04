This repository is cloned from Jacky-Lzx/texlipse, which is forked from eclipse/texlipse.

New modification will be made here, and maybe one day I will clone it back to Jacky-Lzx/texlipse and pull a request.

# Improvements:
* 2019-04-26 Better new-project-wizard-page. After inputing the name of the project, the output file name and source file name will be set to "<projectName>.pdf" and "<projectName>.tex". (See TexlipseProjectFilesWizardPage.java)
* 2019-04-28 Correct hard line wrap. When the current line is too long, all lines in the paragraph will wrap. (See HardLineWrap.java)
* 2019-04-30 Correct indentation. When a new line begins, indentation will be added. e.g. the next line in \begin and \end command will be indented one tab more(as setted in the properties). (See TexAutoIndentStrategy.java)
* 2019-04-30 Enhanced line wrap. When delete some words in the current line, all lines in the paragraph will wrap. (See HardLineWrap.java)
* 2019-05-04 Smarter command completion. e.g. Previously, /document${cursor}class{} -> /documentclass{}class{}, now, /document${cursor}class{} -> /documentclass{}. (See TexCompletionProposal.java)