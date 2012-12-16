/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2006 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.ac.tuwien.infosys.www.pixy.automaton;

import java.io.IOException;
import java.util.*;

/**
 * Regular Expression extension to <code>Automaton</code>.
 * <p>
 * Regular expressions are built from the following abstract syntax:<p>
 * <table border=0>
 * <tr><td><i>regexp</i></td><td>::=</td><td><i>unionexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>unionexp</i></td><td>::=</td><td><i>interexp</i>&nbsp;<tt><b>|</b></tt>&nbsp;<i>unionexp</i></td><td>(union)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>interexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>interexp</i></td><td>::=</td><td><i>concatexp</i>&nbsp;<tt><b>&amp;</b></tt>&nbsp;<i>interexp</i></td><td>(intersection)</td><td><small>[OPTIONAL]</small></td></tr>
 * <tr><td></td><td>|</td><td><i>concatexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>concatexp</i></td><td>::=</td><td><i>repeatexp</i>&nbsp;<i>concatexp</i></td><td>(concatenation)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>repeatexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>repeatexp</i></td><td>::=</td><td><i>repeatexp</i>&nbsp;<tt><b>?</b></tt></td><td>(zero or one occurrence)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>*</b></tt></td><td>(zero or more occurrences)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>+</b></tt></td><td>(one or more occurrences)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>}</b></tt></td><td>(<tt><i>n</i></tt> occurrences)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>,}</b></tt></td><td>(<tt><i>n</i></tt> or more occurrences)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>,</b><i>m</i><b>}</b></tt></td><td>(<tt><i>n</i></tt> to <tt><i>m</i></tt> occurrences, including both)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>complexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>complexp</i></td><td>::=</td><td><tt><b>~</b></tt>&nbsp;<i>complexp</i></td><td>(complement)</td><td><small>[OPTIONAL]</small></td></tr>
 * <tr><td></td><td>|</td><td><i>charclassexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>charclassexp</i></td><td>::=</td><td><tt><b>[</b></tt>&nbsp;<i>charclasses</i>&nbsp;<tt><b>]</b></tt></td><td>(character class)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>[^</b></tt>&nbsp;<i>charclasses</i>&nbsp;<tt><b>]</b></tt></td><td>(negated character class)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>simpleexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>charclasses</i></td><td>::=</td><td><i>charclass</i>&nbsp;<i>charclasses</i></td><td></td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>charclass</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>charclass</i></td><td>::=</td><td><i>charexp</i>&nbsp;<tt><b>-</b></tt>&nbsp;<i>charexp</i></td><td>(character range, including end-points)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><i>charexp</i></td><td></td><td></td></tr>
 *
 * <tr><td><i>simpleexp</i></td><td>::=</td><td><i>charexp</i></td><td></td><td></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>.</b></tt></td><td>(any single character)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>#</b></tt></td><td>(the empty language)</td><td><small>[OPTIONAL]</small></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>@</b></tt></td><td>(any string)</td><td><small>[OPTIONAL]</small></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>"</b></tt>&nbsp;&lt;Unicode string without double-quotes&gt;&nbsp;<tt><b>"</b></tt></td><td>(a string)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>(</b></tt>&nbsp;<tt><b>)</b></tt></td><td>(the empty string)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>(</b></tt>&nbsp;<i>unionexp</i>&nbsp;<tt><b>)</b></tt></td><td>(precedence override)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>&lt;</b></tt>&nbsp;&lt;identifier&gt;&nbsp;<tt><b>&gt;</b></tt></td><td>(named automaton)</td><td><small>[OPTIONAL]</small></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>&lt;</b><i>n</i>-<i>m</i><b>&gt;</b></tt></td><td>(numerical interval)</td><td><small>[OPTIONAL]</small></td></tr>
 *
 * <tr><td><i>charexp</i></td><td>::=</td><td>&lt;Unicode character&gt;</td><td>(a single non-reserved character)</td><td></td></tr>
 * <tr><td></td><td>|</td><td><tt><b>\</b></tt>&nbsp;&lt;Unicode character&gt;&nbsp;</td><td>(a single character)</td><td></td></tr>
 * </table>
 * <p>
 * The productions marked <small>[OPTIONAL]</small> are only allowed
 * if specified by the syntax flags passed to the <code>RegExp</code>
 * constructor.  The reserved characters used in the (enabled) syntax
 * must be escaped with backslash (<tt><b>\</b></tt>) or double-quotes
 * (<tt><b>"..."</b></tt>). (In contrast to other regexp syntaxes,
 * this is required also in character classes.)  Be aware that
 * dash (<tt><b>-</b></tt>) has a special meaning in <i>charclass</i> expressions.
 * An identifier is a string not containing right angle bracket
 * (<tt><b>&gt;</b></tt>) or dash (<tt><b>-</b></tt>).  Numerical intervals are
 * specified by non-negative decimal integers and include both end
 * points, and if <tt><i>n</i></tt> and <tt><i>m</i></tt> have the
 * same number of digits, then the conforming strings must have that
 * length (i.e. prefixed by 0's).
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt; 
 * */
public class RegExp {
	
	enum Kind {
		REGEXP_UNION,
		REGEXP_CONCATENATION,
		REGEXP_INTERSECTION,
		REGEXP_OPTIONAL,
		REGEXP_REPEAT,
		REGEXP_REPEAT_MIN,
		REGEXP_REPEAT_MINMAX,
		REGEXP_COMPLEMENT,
		REGEXP_CHAR,
		REGEXP_CHAR_RANGE,
		REGEXP_ANYCHAR,
		REGEXP_EMPTY,
		REGEXP_STRING,
		REGEXP_ANYSTRING,
		REGEXP_AUTOMATON,
		REGEXP_INTERVAL
	}
	
	/** 
	 * Syntax flag, enables intersection (<tt>&amp;</tt>). 
	 */
	public static final int INTERSECTION = 0x0001;
	
	/** 
	 * Syntax flag, enables complement (<tt>~</tt>). 
	 */
	public static final int COMPLEMENT = 0x0002;
	
	/** 
	 * Syntax flag, enables empty language (<tt>#</tt>). 
	 */
	public static final int EMPTY = 0x0004;
	
	/** 
	 * Syntax flag, enables anystring (<tt>@</tt>). 
	 */
	public static final int ANYSTRING = 0x0008;
	
	/** 
	 * Syntax flag, enables named automata (<tt>&lt;</tt>identifier<tt>&gt;</tt>). 
	 */
	public static final int AUTOMATON = 0x0010;
	
	/** 
	 * Syntax flag, enables numerical intervals (<tt>&lt;<i>n</i>-<i>m</i>&gt;</tt>). 
	 */
	public static final int INTERVAL = 0x0020;
	
	/** 
	 * Syntax flag, enables all optional regexp syntax. 
	 */
	public static final int ALL = 0xffff;
	
	/** 
	 * Syntax flag, enables no optional regexp syntax. 
	 */
	public static final int NONE = 0x0000;
	
	Kind kind;
	RegExp exp1, exp2;
                           // REGEXP_xyz
	String s;              // _STRING
	char c;                // _CHAR
	int min;               // _MIN, _MAX, _INTERVAL  
	int max;               // _MAX, _INTERVAL
	int digits;            // _INTERVAL
	char from, to;         // _CHAR_RANGE
	
	StringBuilder b;
	int flags;
	int pos;
	
    // performs some simplifications on this regexp
    void simplify() {
        
        // recursively simplify elements
        if (exp1 != null) {
            exp1.simplify();
        }
        if (exp2 != null) {
            exp2.simplify();
        }
        
        switch (kind) {
        case REGEXP_CHAR_RANGE:
            // change a one-element range to one character
            if (from == to) {
                c = from;
                kind = Kind.REGEXP_CHAR;
            }
            break;
        case REGEXP_UNION:
            // in unions, "empty" can be ignored
            if (exp1.kind == Kind.REGEXP_EMPTY) {
                copy(exp2);
            } else if (exp2.kind == Kind.REGEXP_EMPTY) {
                copy(exp1);
            }
            break;
        case REGEXP_REPEAT:
            // empty* = epsilon
            if (exp1.kind == Kind.REGEXP_EMPTY) {
                kind = Kind.REGEXP_STRING;
                s = "";
            }
            break;
        case REGEXP_CONCATENATION:
            // in concats, epsilon can be ignored
            if (exp1.kind == Kind.REGEXP_STRING && exp1.s.equals("")) {
                copy(exp2);
            } else if (exp2.kind == Kind.REGEXP_STRING && exp2.s.equals("")) {
                copy(exp1);
            }
            break;
        }
        

        
    }
    
    void copy(RegExp e) {
        kind = e.kind;
        exp1 = e.exp1;
        exp2 = e.exp2;
        this.s = e.s;
        c = e.c;
        min = e.min;
        max = e.max;
        digits = e.digits;
        from = e.from;
        to = e.to;
        b = null;
    }
    
	RegExp() {}
	
	/** 
	 * Constructs new <code>RegExp</code> from a string. 
	 * Same as <code>RegExp(s, ALL)</code>.
	 * @param s regexp string
	 * @exception IllegalArgumentException if an error occured while parsing the regular expression
	 */
	public RegExp(String s) throws IllegalArgumentException {
		this(s, ALL);
	}
	
	/** 
	 * Constructs new <code>RegExp</code> from a string. 
	 * @param s regexp string
	 * @param syntax_flags boolean 'or' of optional syntax constructs to be enabled
	 * @exception IllegalArgumentException if an error occured while parsing the regular expression
	 */
	public RegExp(String s, int syntax_flags) throws IllegalArgumentException {
		b = new StringBuilder(s);
		flags = syntax_flags;
		RegExp e = parseUnionExp();
		if (pos < b.length())
			throw new IllegalArgumentException("end-of-string expected at position " + pos);
		kind = e.kind;
		exp1 = e.exp1;
		exp2 = e.exp2;
		this.s = e.s;
		c = e.c;
		min = e.min;
		max = e.max;
		digits = e.digits;
		from = e.from;
		to = e.to;
		b = null;
	}
	
	/** 
	 * Constructs new <code>Automaton</code> from this <code>RegExp</code>. 
	 * Same as <code>toAutomaton(null)</code> (empty automaton map).
	 */
	public Automaton toAutomaton() {
		return toAutomaton(null, null);
	}
	
	/** 
	 * Constructs new <code>Automaton</code> from this <code>RegExp</code>. 
	 * The constructed automaton is minimal and deterministic and has no 
	 * transitions to dead states. 
	 * @param automaton_provider provider of automata for named identifiers
	 * @exception IllegalArgumentException if this regular expression uses
	 *   a named identifier that is not available from the automaton provider
	 */
	public Automaton toAutomaton(AutomatonProvider automaton_provider) throws IllegalArgumentException {
		return toAutomaton(null, automaton_provider);
	}
		
	/** 
	 * Constructs new <code>Automaton</code> from this <code>RegExp</code>. 
	 * The constructed automaton is minimal and deterministic and has no 
	 * transitions to dead states. 
	 * @param automata a map from automaton identifiers to automata 
	 *   (of type <code>Automaton</code>).
	 * @exception IllegalArgumentException if this regular expression uses
	 *   a named identifier that does not occur in the automaton map
	 */
	public Automaton toAutomaton(Map<String, Automaton> automata) throws IllegalArgumentException {
		return toAutomaton(automata, null);
	}
		
	private Automaton toAutomaton(Map<String, Automaton> automata, AutomatonProvider automaton_provider) throws IllegalArgumentException {
		List<Automaton> list;
		Automaton a = null;
		switch (kind) {
		case REGEXP_UNION:
			list = new ArrayList<Automaton>();
			findLeaves(exp1, Kind.REGEXP_UNION, list, automata, automaton_provider);
			findLeaves(exp2, Kind.REGEXP_UNION, list, automata, automaton_provider);
			a = Automaton.union(list);
			a.minimize();
			break;
		case REGEXP_CONCATENATION:
			list = new ArrayList<Automaton>();
			findLeaves(exp1, Kind.REGEXP_CONCATENATION, list, automata, automaton_provider);
			findLeaves(exp2, Kind.REGEXP_CONCATENATION, list, automata, automaton_provider);
			a = Automaton.concatenate(list);
			a.minimize();
			break;
		case REGEXP_INTERSECTION:
			a = exp1.toAutomaton(automata, automaton_provider).intersection(exp2.toAutomaton(automata, automaton_provider));
			a.minimize();
			break;
		case REGEXP_OPTIONAL:
			a = exp1.toAutomaton(automata, automaton_provider).optional();
			a.minimize();
			break;
		case REGEXP_REPEAT:
			a = exp1.toAutomaton(automata, automaton_provider).repeat();
			a.minimize();
			break;
		case REGEXP_REPEAT_MIN:
			a = exp1.toAutomaton(automata, automaton_provider).repeat(min);
			a.minimize();
			break;
		case REGEXP_REPEAT_MINMAX:
			a = exp1.toAutomaton(automata, automaton_provider).repeat(min, max);
			a.minimize();
			break;
		case REGEXP_COMPLEMENT:
			a = exp1.toAutomaton(automata, automaton_provider).complement();
			a.minimize();
			break;
		case REGEXP_CHAR:
			a = Automaton.makeChar(c);
			break;
		case REGEXP_CHAR_RANGE:
			a = Automaton.makeCharRange(from, to);
			break;
		case REGEXP_ANYCHAR:
			a = Automaton.makeAnyChar();
			break;
		case REGEXP_EMPTY:
			a = Automaton.makeEmpty();
			break;
		case REGEXP_STRING:
			a = Automaton.makeString(s);
			break;
		case REGEXP_ANYSTRING:
			a = Automaton.makeAnyString();
			break;
		case REGEXP_AUTOMATON:
			Automaton aa = null;
			if (automata != null)
				aa = automata.get(s);
			if (aa == null && automaton_provider != null)
				try {
					aa = automaton_provider.getAutomaton(s);
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			if (aa == null)
				throw new IllegalArgumentException(s + " not found");
			a = aa.clone();
			break;
		case REGEXP_INTERVAL:
			a = Automaton.makeInterval(min, max, digits);
			break;
		}
		return a;
	}

	private void findLeaves(RegExp exp, Kind kind, List<Automaton> list, Map<String, Automaton> automata, AutomatonProvider automaton_provider) {
		if (exp.kind == kind) {
			findLeaves(exp.exp1, kind, list, automata, automaton_provider);
			findLeaves(exp.exp2, kind, list, automata, automaton_provider);
		} else
			list.add(exp.toAutomaton(automata, automaton_provider));
	}

	/** 
	 * Constructs string from parsed regular expression. 
	 */
	@Override
	public String toString() {
		return toStringBuilder(new StringBuilder()).toString();
	}

	StringBuilder toStringBuilder(StringBuilder b) {
		switch (kind) {
		case REGEXP_UNION:
			b.append("(");
			exp1.toStringBuilder(b);
			b.append("|");
			exp2.toStringBuilder(b);
			b.append(")");
			break;
		case REGEXP_CONCATENATION:
			exp1.toStringBuilder(b);
			exp2.toStringBuilder(b);
			break;
		case REGEXP_INTERSECTION:
			b.append("(");
			exp1.toStringBuilder(b);
			b.append("&");
			exp2.toStringBuilder(b);
			b.append(")");
			break;
		case REGEXP_OPTIONAL:
			b.append("(");
			exp1.toStringBuilder(b);
			b.append(")?");
			break;
		case REGEXP_REPEAT:
			b.append("(");
			exp1.toStringBuilder(b);
			b.append(")*");
			break;
		case REGEXP_REPEAT_MIN:
			b.append("(");
			exp1.toStringBuilder(b);
			b.append("){").append(min).append(",}");
			break;
		case REGEXP_REPEAT_MINMAX:
			b.append("(");
			exp1.toStringBuilder(b);
			b.append("){").append(min).append(",").append(max).append("}");
			break;
		case REGEXP_COMPLEMENT:
			b.append("~(");
			exp1.toStringBuilder(b);
			b.append(")");
			break;
		case REGEXP_CHAR:
			//b.append("\\").append(c);
			b.append(c);
			break;
		case REGEXP_CHAR_RANGE:
			b.append("[\\").append(from).append("-\\").append(to).append("]");
			break;
		case REGEXP_ANYCHAR:
			b.append(".");
			break;
		case REGEXP_EMPTY:
			b.append("#");
			break;
		case REGEXP_STRING:
			b.append("\"").append(s).append("\"");
			break;
		case REGEXP_ANYSTRING:
			b.append("@");
			break;
		case REGEXP_AUTOMATON:
			b.append("<").append(s).append(">");
			break;
		case REGEXP_INTERVAL:
			String s1 = (new Integer(min)).toString();
			String s2 = (new Integer(max)).toString();
			b.append("<");
			if (digits > 0)
				for (int i = s1.length(); i < digits; i++)
					b.append('0');
			b.append(s1).append("-");
			if (digits > 0)
				for (int i = s2.length(); i < digits; i++)
					b.append('0');
			b.append(s2).append(">");
			break;
		}
		return b;
	}

	/** 
	 * Returns set of automaton identifiers that occur in this regular expression. 
	 */
	public Set<String> getIdentifiers() {
		HashSet<String> set = new HashSet<String>();
		getIdentifiers(set);
		return set;
	}

	void getIdentifiers(Set<String> set) {
		switch (kind) {
		case REGEXP_UNION:
		case REGEXP_CONCATENATION:
		case REGEXP_INTERSECTION:
			exp1.getIdentifiers(set);
			exp2.getIdentifiers(set);
			break;
		case REGEXP_OPTIONAL:
		case REGEXP_REPEAT:
		case REGEXP_REPEAT_MIN:
		case REGEXP_REPEAT_MINMAX:
		case REGEXP_COMPLEMENT:
			exp1.getIdentifiers(set);
			break;
		case REGEXP_AUTOMATON:
			set.add(s);
			break;
		default:
		}
	}

	static RegExp makeUnion(RegExp exp1, RegExp exp2) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_UNION;
		r.exp1 = exp1;
		r.exp2 = exp2;
		return r;
	}

	static RegExp makeConcatenation(RegExp exp1, RegExp exp2) {
		if ((exp1.kind == Kind.REGEXP_CHAR || exp1.kind == Kind.REGEXP_STRING) && 
			(exp2.kind == Kind.REGEXP_CHAR || exp2.kind == Kind.REGEXP_STRING))
			return makeString(exp1, exp2);
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_CONCATENATION;
		if (exp1.kind == Kind.REGEXP_CONCATENATION && 
			(exp1.exp2.kind == Kind.REGEXP_CHAR || exp1.exp2.kind == Kind.REGEXP_STRING) && 
			(exp2.kind == Kind.REGEXP_CHAR || exp2.kind == Kind.REGEXP_STRING)) {
			r.exp1 = exp1.exp1;
			r.exp2 = makeString(exp1.exp2, exp2);
		} else if ((exp1.kind == Kind.REGEXP_CHAR || exp1.kind == Kind.REGEXP_STRING) && 
				   exp2.kind == Kind.REGEXP_CONCATENATION && 
				   (exp2.exp1.kind == Kind.REGEXP_CHAR || exp2.exp1.kind == Kind.REGEXP_STRING)) {
			r.exp1 = makeString(exp1, exp2.exp1);
			r.exp2 = exp2.exp2;
		} else {
			r.exp1 = exp1;
			r.exp2 = exp2;
		}
		return r;
	}

	static private RegExp makeString(RegExp exp1, RegExp exp2) {
		StringBuilder b = new StringBuilder();
		if (exp1.kind == Kind.REGEXP_STRING)
			b.append(exp1.s);
		else
			b.append(exp1.c);
		if (exp2.kind == Kind.REGEXP_STRING)
			b.append(exp2.s);
		else
			b.append(exp2.c);
		return makeString(b.toString());
	}

	static RegExp makeIntersection(RegExp exp1, RegExp exp2) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_INTERSECTION;
		r.exp1 = exp1;
		r.exp2 = exp2;
		return r;
	}

	static RegExp makeOptional(RegExp exp) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_OPTIONAL;
		r.exp1 = exp;
		return r;
	}

	static RegExp makeRepeat(RegExp exp) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_REPEAT;
		r.exp1 = exp;
		return r;
	}

	static RegExp makeRepeat(RegExp exp, int min) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_REPEAT_MIN;
		r.exp1 = exp;
		r.min = min;
		return r;
	}

	static RegExp makeRepeat(RegExp exp, int min, int max) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_REPEAT_MINMAX;
		r.exp1 = exp;
		r.min = min;
		r.max = max;
		return r;
	}

	static RegExp makeComplement(RegExp exp) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_COMPLEMENT;
		r.exp1 = exp;
		return r;
	}

	static RegExp makeChar(char c) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_CHAR;
		r.c = c;
		return r;
	}

	static RegExp makeCharRange(char from, char to) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_CHAR_RANGE;
		r.from = from;
		r.to = to;
		return r;
	}

	static RegExp makeAnyChar() {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_ANYCHAR;
		return r;
	}

	static RegExp makeEmpty() {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_EMPTY;
		return r;
	}

	static RegExp makeString(String s) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_STRING;
		r.s = s;
		return r;
	}

	static RegExp makeAnyString() {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_ANYSTRING;
		return r;
	}

	static RegExp makeAutomaton(String s) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_AUTOMATON;
		r.s = s;
		return r;
	}

	static RegExp makeInterval(int min, int max, int digits) {
		RegExp r = new RegExp();
		r.kind = Kind.REGEXP_INTERVAL;
		r.min = min;
		r.max = max;
		r.digits = digits;
		return r;
	}

	private boolean peek(String s) {
		return more() && s.indexOf(b.charAt(pos)) != -1;
	}

	private boolean match(char c) {
		if (pos >= b.length())
			return false;
		if (b.charAt(pos) == c) {
			pos++;
			return true;
		}
		return false;
	}

	private boolean more() {
		return pos < b.length();
	}

	private char next() throws IllegalArgumentException {
		if (!more())
			throw new IllegalArgumentException("unexpected end-of-string");
		return b.charAt(pos++);
	}

	private boolean check(int flag) {
		return (flags & flag) != 0;
	}

	RegExp parseUnionExp() throws IllegalArgumentException {
		RegExp e = parseInterExp();
		if (match('|'))
			e = makeUnion(e, parseUnionExp());
		return e;
	}

	RegExp parseInterExp() throws IllegalArgumentException {
		RegExp e = parseConcatExp();
		if (check(INTERSECTION) && match('&'))
			e = makeIntersection(e, parseInterExp());
		return e;
	}

	RegExp parseConcatExp() throws IllegalArgumentException {
		RegExp e = parseRepeatExp();
		if (more() && !peek(")&|"))
			e = makeConcatenation(e, parseConcatExp());
		return e;
	}

	RegExp parseRepeatExp() throws IllegalArgumentException {
		RegExp e = parseComplExp();
		while (peek("?*+{")) {
			if (match('?'))
				e = makeOptional(e);
			else if (match('*'))
				e = makeRepeat(e);
			else if (match('+'))
				e = makeRepeat(e, 1);
			else if (match('{')) {
				int start = pos;
				while (peek("0123456789"))
					next();
				if (start == pos)
					throw new IllegalArgumentException("integer expected at position " + pos);
				int n = Integer.parseInt(b.substring(start, pos));
				int m = -1;
				if (match(',')) {
					start = pos;
					while (peek("0123456789"))
						next();
					if (start != pos)
						m = Integer.parseInt(b.substring(start, pos));
				} else
					m = n;
				if (!match('}'))
					throw new IllegalArgumentException("expected '}' at position " + pos);
				if (m == -1)
					return makeRepeat(e, n);
				else
					return makeRepeat(e, n, m);
			}
		}
		return e;
	}

	RegExp parseComplExp() throws IllegalArgumentException {
		if (check(COMPLEMENT) && match('~'))
			return makeComplement(parseComplExp());
		else
			return parseCharClassExp();
	}

	RegExp parseCharClassExp() throws IllegalArgumentException {
		if (match('[')) {
			boolean negate = false;
			if (match('^'))
				negate = true;
			RegExp e = parseCharClasses();
			if (negate)
				e = makeIntersection(makeAnyChar(), makeComplement(e));
			if (!match(']'))
				throw new IllegalArgumentException("expected ']' at position " + pos);
			return e;
		} else
			return parseSimpleExp();
	}

	RegExp parseCharClasses() throws IllegalArgumentException {
		RegExp e = parseCharClass();
		while (more() && !peek("]"))
			e = makeUnion(e, parseCharClass());
		return e;
	}

	RegExp parseCharClass() throws IllegalArgumentException {
		char c = parseCharExp();
		if (match('-'))
			return makeCharRange(c, parseCharExp());
		else
			return makeChar(c);
	}

	RegExp parseSimpleExp() throws IllegalArgumentException {
		if (match('.'))
			return makeAnyChar();
		else if (check(EMPTY) && match('#'))
			return makeEmpty();
		else if (check(ANYSTRING) && match('@'))
			return makeAnyString();
		else if (match('"')) {
			int start = pos;
			while (more() && !peek("\""))
				next();
			if (!match('"'))
				throw new IllegalArgumentException("expected '\"' at position " + pos);
			return makeString(b.substring(start, pos - 1));
		} else if (match('(')) {
			if (match(')'))
				return makeString("");
			RegExp e = parseUnionExp();
			if (!match(')'))
				throw new IllegalArgumentException("expected ')' at position " + pos);
			return e;
		} else if ((check(AUTOMATON) || check(INTERVAL)) && match('<')) {
			int start = pos;
			while (more() && !peek(">"))
				next();
			if (!match('>'))
				throw new IllegalArgumentException("expected '>' at position " + pos);
			String s = b.substring(start, pos - 1);
			int i = s.indexOf('-');
			if (i == -1) {
				if (!check(AUTOMATON))
					throw new IllegalArgumentException("interval syntax error at position " + (pos - 1));
				return makeAutomaton(s);
			} else {
				if (!check(INTERVAL))
					throw new IllegalArgumentException("illegal identifier at position " + (pos - 1));
				try {
					if (i == 0 || i == s.length() - 1 || i != s.lastIndexOf('-'))
						throw new NumberFormatException();
					String smin = s.substring(0, i);
					String smax = s.substring(i + 1, s.length());
					int imin = Integer.parseInt(smin);
					int imax = Integer.parseInt(smax);
					int digits;
					if (smin.length() == smax.length())
						digits = smin.length();
					else
						digits = 0;
					if (imin > imax) {
						int t = imin;
						imin = imax;
						imax = t;
					}
					return makeInterval(imin, imax, digits);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("interval syntax error at position " + (pos - 1));
				}
			}
		} else
			return makeChar(parseCharExp());
	}

	char parseCharExp() throws IllegalArgumentException {
		match('\\');
		return next();
	}
}
