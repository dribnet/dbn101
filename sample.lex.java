import java.lang.System;
class Utility {
  public static void assert
    (
     boolean expr
     )
      { 
	if (false == expr) {
	  throw (new Error("Error: Assertion failed."));
	}
      }
  private static final String errorMsg[] = {
    "Error: Unmatched end-of-comment punctuation.",
    "Error: Unmatched start-of-comment punctuation.",
    "Error: Unclosed string.",
    "Error: Illegal character."
    };
  public static final int E_ENDCOMMENT = 0; 
  public static final int E_STARTCOMMENT = 1; 
  public static final int E_UNCLOSEDSTR = 2; 
  public static final int E_UNMATCHED = 3; 
  public static void error
    (
     int code
     )
      {
	System.out.println(errorMsg[code]);
      }
}
class Yytoken {
  Yytoken 
    (
     int index,
     String text,
     int line,
     int charBegin,
     int charEnd
     )
      {
	m_index = index;
	m_text = new String(text);
	m_line = line;
	m_charBegin = charBegin;
	m_charEnd = charEnd;
      }
  public int m_index;
  public String m_text;
  public int m_line;
  public int m_charBegin;
  public int m_charEnd;
}


class Yylex {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final char YYEOF = '\uFFFF';

  private int comment_count = 0;
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yychar;
	private int yyline;
	private int yy_lexical_state;

	Yylex (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	Yylex (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Yylex () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yychar = 0;
		yyline = 0;
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int COMMENT = 1;
	private final int YYINITIAL = 0;
	private final int yy_state_dtrans[] = {
		0,
		31
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private char yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YYEOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YYEOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_start () {
		if ((byte) '\n' == yy_buffer[yy_buffer_start]) {
			++yyline;
		}
		++yychar;
		++yy_buffer_start;
	}
	private void yy_pushback () {
		--yy_buffer_end;
	}
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ((byte) '\n' == yy_buffer[i]) {
				++yyline;
			}
		}
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int yy_acpt[] = {
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR
	};
	private int yy_cmap[] = {
		0, 0, 0, 0, 0, 0, 0, 0,
		1, 1, 2, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		1, 0, 3, 0, 0, 0, 4, 0,
		5, 6, 7, 8, 9, 10, 11, 12,
		13, 13, 13, 13, 13, 13, 13, 13,
		13, 13, 14, 15, 16, 17, 18, 0,
		0, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 20, 21, 22, 0, 23,
		0, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 24, 25, 26, 0, 0
		
	};
	private int yy_rmap[] = {
		0, 1, 2, 3, 1, 1, 1, 1,
		1, 1, 1, 1, 4, 5, 6, 1,
		7, 1, 8, 9, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 10,
		1, 1, 1, 11, 12, 13, 3, 14,
		15, 16, 17, 18, 19, 20 
	};
	private int yy_nxt[][] = {
		{ 1, 2, 2, 3, 4, 5, 6, 7,
			8, 9, 10, 11, 12, 13, 14, 15,
			16, 17, 18, 19, 20, 1, 21, 1,
			22, 23, 24 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, 2, 2, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ 3, 3, -1, 25, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 37, 3, 3,
			3, 3, 3 
		},
		{ -1, -1, -1, -1, -1, -1, -1, 26,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, 13, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, 27, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, 28, 29, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, 30, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, 19, -1, -1,
			-1, -1, -1, 19, -1, -1, -1, 19,
			-1, -1, -1 
		},
		{ 39, 39, 32, 39, 39, 39, 39, 36,
			39, 39, 39, 39, 41, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		},
		{ -1, 35, 35, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, 3, -1, -1,
			-1, -1, -1 
		},
		{ 39, 39, -1, 39, 39, 39, 39, 43,
			39, 39, 39, 39, 33, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		},
		{ 3, 42, 35, 38, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 37, 3, 3,
			3, 3, 3 
		},
		{ 39, 39, -1, 39, 39, 39, 39, 40,
			39, 39, 39, 39, 44, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		},
		{ 39, 39, -1, 39, 39, 39, 39, 43,
			39, 39, 39, 39, -1, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		},
		{ 39, 39, -1, 39, 39, 39, 39, 34,
			39, 39, 39, 39, 45, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		},
		{ 3, 42, 35, 25, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 37, 3, 3,
			3, 3, 3 
		},
		{ 39, 39, -1, 39, 39, 39, 39, 43,
			39, 39, 39, 39, 44, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		},
		{ 39, 39, -1, 39, 39, 39, 39, -1,
			39, 39, 39, 39, 45, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		},
		{ 39, 39, -1, 39, 39, 39, 39, 40,
			39, 39, 39, 39, 45, 39, 39, 39,
			39, 39, 39, 39, 39, 39, 39, 39,
			39, 39, 39 
		}
	};
	public Yytoken yylex ()
		throws java.io.IOException {
		char yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			if (YYEOF != yy_lookahead) {
				yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YYEOF == yy_lookahead && true == yy_initial) {
					return null;
				}
				else if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_to_mark();
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_pushback();
					}
					if (0 != (YY_START & yy_anchor)) {
						yy_move_start();
					}
					switch (yy_last_accept_state) {
					case 1:
						{
        System.out.println("Illegal character: <" + yytext() + ">");
	Utility.error(Utility.E_UNMATCHED);
}
					case -2:
						break;
					case 2:
						{ }
					case -3:
						break;
					case 3:
						{
	String str =  yytext().substring(1,yytext().length());
	Utility.error(Utility.E_UNCLOSEDSTR);
	Utility.assert(str.length() == yytext().length() - 1);
	return (new Yytoken(41,str,yyline,yychar,yychar + str.length()));
}
					case -4:
						break;
					case 4:
						{ return (new Yytoken(20,yytext(),yyline,yychar,yychar+1)); }
					case -5:
						break;
					case 5:
						{ return (new Yytoken(3,yytext(),yyline,yychar,yychar+1)); }
					case -6:
						break;
					case 6:
						{ return (new Yytoken(4,yytext(),yyline,yychar,yychar+1)); }
					case -7:
						break;
					case 7:
						{ return (new Yytoken(12,yytext(),yyline,yychar,yychar+1)); }
					case -8:
						break;
					case 8:
						{ return (new Yytoken(10,yytext(),yyline,yychar,yychar+1)); }
					case -9:
						break;
					case 9:
						{ return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
					case -10:
						break;
					case 10:
						{ return (new Yytoken(11,yytext(),yyline,yychar,yychar+1)); }
					case -11:
						break;
					case 11:
						{ return (new Yytoken(9,yytext(),yyline,yychar,yychar+1)); }
					case -12:
						break;
					case 12:
						{ return (new Yytoken(13,yytext(),yyline,yychar,yychar+1)); }
					case -13:
						break;
					case 13:
						{ 
	return (new Yytoken(42,yytext(),yyline,yychar,yychar + yytext().length()));
}
					case -14:
						break;
					case 14:
						{ return (new Yytoken(1,yytext(),yyline,yychar,yychar+1)); }
					case -15:
						break;
					case 15:
						{ return (new Yytoken(2,yytext(),yyline,yychar,yychar+1)); }
					case -16:
						break;
					case 16:
						{ return (new Yytoken(16,yytext(),yyline,yychar,yychar+1)); }
					case -17:
						break;
					case 17:
						{ return (new Yytoken(14,yytext(),yyline,yychar,yychar+1)); }
					case -18:
						break;
					case 18:
						{ return (new Yytoken(18,yytext(),yyline,yychar,yychar+1)); }
					case -19:
						break;
					case 19:
						{
	return (new Yytoken(43,yytext(),yyline,yychar,yychar + yytext().length()));
}
					case -20:
						break;
					case 20:
						{ return (new Yytoken(5,yytext(),yyline,yychar,yychar+1)); }
					case -21:
						break;
					case 21:
						{ return (new Yytoken(6,yytext(),yyline,yychar,yychar+1)); }
					case -22:
						break;
					case 22:
						{ return (new Yytoken(7,yytext(),yyline,yychar,yychar+1)); }
					case -23:
						break;
					case 23:
						{ return (new Yytoken(21,yytext(),yyline,yychar,yychar+1)); }
					case -24:
						break;
					case 24:
						{ return (new Yytoken(8,yytext(),yyline,yychar,yychar+1)); }
					case -25:
						break;
					case 25:
						{
	String str =  yytext().substring(1,yytext().length() - 1);
	Utility.assert(str.length() == yytext().length() - 2);
	return (new Yytoken(40,str,yyline,yychar,yychar + str.length()));
}
					case -26:
						break;
					case 26:
						{ yybegin(COMMENT); comment_count = comment_count + 1; }
					case -27:
						break;
					case 27:
						{ return (new Yytoken(22,yytext(),yyline,yychar,yychar+2)); }
					case -28:
						break;
					case 28:
						{ return (new Yytoken(17,yytext(),yyline,yychar,yychar+2)); }
					case -29:
						break;
					case 29:
						{ return (new Yytoken(15,yytext(),yyline,yychar,yychar+2)); }
					case -30:
						break;
					case 30:
						{ return (new Yytoken(19,yytext(),yyline,yychar,yychar+2)); }
					case -31:
						break;
					case 31:
						{ }
					case -32:
						break;
					case 32:
						{ }
					case -33:
						break;
					case 33:
						{ 
	comment_count = comment_count - 1; 
	Utility.assert(comment_count >= 0);
	if (comment_count == 0) {
    		yybegin(YYINITIAL);
	}
}
					case -34:
						break;
					case 34:
						{ comment_count = comment_count + 1; }
					case -35:
						break;
					case 36:
						{
        System.out.println("Illegal character: <" + yytext() + ">");
	Utility.error(Utility.E_UNMATCHED);
}
					case -36:
						break;
					case 37:
						{
	String str =  yytext().substring(1,yytext().length());
	Utility.error(Utility.E_UNCLOSEDSTR);
	Utility.assert(str.length() == yytext().length() - 1);
	return (new Yytoken(41,str,yyline,yychar,yychar + str.length()));
}
					case -37:
						break;
					case 38:
						{
	String str =  yytext().substring(1,yytext().length() - 1);
	Utility.assert(str.length() == yytext().length() - 2);
	return (new Yytoken(40,str,yyline,yychar,yychar + str.length()));
}
					case -38:
						break;
					case 39:
						{ }
					case -39:
						break;
					case 41:
						{
        System.out.println("Illegal character: <" + yytext() + ">");
	Utility.error(Utility.E_UNMATCHED);
}
					case -40:
						break;
					case 42:
						{
	String str =  yytext().substring(1,yytext().length());
	Utility.error(Utility.E_UNCLOSEDSTR);
	Utility.assert(str.length() == yytext().length() - 1);
	return (new Yytoken(41,str,yyline,yychar,yychar + str.length()));
}
					case -41:
						break;
					case 43:
						{ }
					case -42:
						break;
					case 45:
						{ }
					case -43:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
					}
				}
			}
		}
	}
}
