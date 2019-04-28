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
/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.eclipse.texlipse.bibparser.node;

import org.eclipse.texlipse.bibparser.analysis.*;

@SuppressWarnings("nls")
public final class ABibeBibEntry extends PBibEntry
{
    private PEntry _entry_;

    public ABibeBibEntry()
    {
        // Constructor
    }

    public ABibeBibEntry(
        @SuppressWarnings("hiding") PEntry _entry_)
    {
        // Constructor
        setEntry(_entry_);

    }

    @Override
    public Object clone()
    {
        return new ABibeBibEntry(
            cloneNode(this._entry_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseABibeBibEntry(this);
    }

    public PEntry getEntry()
    {
        return this._entry_;
    }

    public void setEntry(PEntry node)
    {
        if(this._entry_ != null)
        {
            this._entry_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._entry_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._entry_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._entry_ == child)
        {
            this._entry_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._entry_ == oldChild)
        {
            setEntry((PEntry) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
