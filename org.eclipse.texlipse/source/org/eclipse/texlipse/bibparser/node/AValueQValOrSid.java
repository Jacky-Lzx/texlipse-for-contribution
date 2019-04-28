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
public final class AValueQValOrSid extends PValOrSid
{
    private TStringLiteral _stringLiteral_;

    public AValueQValOrSid()
    {
        // Constructor
    }

    public AValueQValOrSid(
        @SuppressWarnings("hiding") TStringLiteral _stringLiteral_)
    {
        // Constructor
        setStringLiteral(_stringLiteral_);

    }

    @Override
    public Object clone()
    {
        return new AValueQValOrSid(
            cloneNode(this._stringLiteral_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAValueQValOrSid(this);
    }

    public TStringLiteral getStringLiteral()
    {
        return this._stringLiteral_;
    }

    public void setStringLiteral(TStringLiteral node)
    {
        if(this._stringLiteral_ != null)
        {
            this._stringLiteral_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._stringLiteral_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._stringLiteral_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._stringLiteral_ == child)
        {
            this._stringLiteral_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._stringLiteral_ == oldChild)
        {
            setStringLiteral((TStringLiteral) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
