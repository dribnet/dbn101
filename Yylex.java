import java.lang.System;
import java.util.Vector;
import java.util.Hashtable;
import java_cup.runtime.*;


class Yylex {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final char YYEOF = '\uFFFF';

	Vector callStack = new Vector(10);
	static Hashtable commands = new Hashtable();
	static Hashtable functions = new Hashtable();
	static Hashtable variables = new Hashtable();
	public boolean doEscape = false;
	public void cleanTables() {
		//System.out.println("Cleaning the tables");
		commands = new Hashtable();
		functions = new Hashtable();
		variables = new Hashtable();
	}		
	public Symbol tomScan() throws java.io.IOException {
		codeBlock block;
		Symbol s;
		if(callStack.isEmpty())
			return yylex();
		else {
			if(doEscape) {
				doEscape = false;
				block = (codeBlock)callStack.lastElement();
				callStack.removeElementAt(callStack.size()-1);
				while (block.isConditional && !callStack.isEmpty()) {							
					block = (codeBlock)callStack.lastElement();
					callStack.removeElementAt(callStack.size()-1);
				}
				for(;;) {
					s = tomScan();
					// don't return RETURNs
					if(s.sym != sym.RETURN) return(s);
				}
			}
			//System.out.println("Reading symbol from storage");
			block = (codeBlock)callStack.lastElement();
			if(block.curSymbol >= block.symbols.size()) {
				// we have reached the end of the block
				if(block.isForever) 
					// just move to after the first brace
					// in the future check for escape
					block.curSymbol = 0;
				else if(block.runOnceOnly) {
					// just end it here
					callStack.removeElementAt(callStack.size()-1);
					for(;;) {
						s = tomScan();
						// don't return RETURNs
						if(s.sym != sym.RETURN) return(s);
					}
				}					
				else {
					block.curval += block.stepval;
					setVariable(block.variable, block.curval);
					if((block.testLess && block.curval < block.endval) ||
						(!block.testLess && block.curval > block.endval)) {
						//System.out.println("this block is all done");
						// we are all done with this repeat loop
						callStack.removeElementAt(callStack.size()-1);
						//Thread thread = Thread.currentThread( );
						//try {thread.sleep(100);} catch (Exception e) {}
						for(;;) {
							s = tomScan();
							// don't return RETURNs
							if(s.sym != sym.RETURN) return(s);
						}
					}
					else {
						//System.out.println("Reached end of block, returning to top:("+block.curval+","+block.endval+","+block.testLess+")");
						// just move to after the first brace
						block.curSymbol = 0;
					}
				}
			}
			// now we have insured block.curSymbol is valid
			s = (Symbol)(block.symbols.elementAt(block.curSymbol));
			//System.out.println("Returning the "+block.curSymbol+"th element = "+s);
			++block.curSymbol;
			return s;
		}
	}	
	Hashtable resolveVariable(String name) {
		codeBlock block;
		Hashtable vars;
		boolean quit=false;
		if(!callStack.isEmpty()) {
			block = (codeBlock)callStack.lastElement();
			while(!quit) {
				if(block.variables != null) {
					vars = block.variables;
					if(vars.containsKey(name))
						return (vars);
				}
				if(block.parent == null) quit = true;
				else block = block.parent;
			}
		}
		if(variables.containsKey(name))
			return(variables);
		else
			return null;
	}
	Hashtable createNewVariable(String name) {
		codeBlock block;
		Hashtable vars;
		Integer newGuy = new Integer(0);
		boolean quit=false;
		if(!callStack.isEmpty()) {
			block = (codeBlock)callStack.lastElement();
			while(!quit) {
				if(block.variables != null) {
					vars = block.variables;
					vars.put(name, newGuy);
					return(vars);
				}
				if(block.parent == null) quit = true;
				else block = block.parent;
			}
		}
		variables.put(name, newGuy);
		return(variables);
	}
	public void doBreak() {
		doEscape = true;
	}
	/*
	Hashtable nevergetCurrentVariables() {
		codeBlock block;
		Hashtable vars;
		if(callStack.isEmpty())
			vars = variables;
		else {
			//System.out.println("Reading symbol from storage");
			block = (codeBlock)callStack.lastElement();
			if(block.variables != null) vars = block.variables;
			else vars = variables;
		}
		return vars;
	}
	public int nevergetVariable(String varName) {
		Hashtable v = getCurrentVariables();
		//System.out.println("getVariable("+varName+") called");
		if(!v.containsKey(varName)) {
			setVariable(varName, 0);
			//System.out.println("No key for " + varName + ", initing to 0");
			//Integer newEntry = new Integer(0);
			//variables.put(varName, newEntry);
		}
		//System.out.println("Returning " + varName + "=" + variables.get(varName));
		return ((Integer) v.get(varName)).intValue();
	}
	public void neversetVariable(String varName, int varValue) {
		Hashtable v = getCurrentVariables();
		//System.out.println("setVariable("+varName+","+varValue+") called");
		Integer newEntry = new Integer(varValue);
		v.put(varName, newEntry);
	}
	*/
	public int getVariable(String varName) {
		Integer varInt;
		Hashtable vars;
		vars = resolveVariable(varName);
		if(vars == null) vars = createNewVariable(varName);
		return ((Integer) vars.get(varName)).intValue();
	}
	public void setVariable(String varName, int varValue) {
		Integer varInt;
		Hashtable vars;
		vars = resolveVariable(varName);
		if(vars == null) vars = createNewVariable(varName);
		varInt = new Integer(varValue);
		vars.put(varName, varInt);
	}	
	public void doRepeat(boolean forever, String variable, int from, int to, DrawSpace ds) {
		boolean haveStarted = false;
		int braceCount = 0;
		Symbol s;
		codeBlock cb=null;
		//System.out.println("Setting up repeat loop for "+variable+" from "+from+" to "+to+".");
		while (!haveStarted || braceCount!=0) {
			try {s = tomScan();}
			catch (Exception e) {
				System.err.println("Exceptionn getting next token in repeat: " + e);
				return;
			}
			if(!haveStarted && s.sym == sym.RETURN) continue;
			else if(!haveStarted && s.sym == sym.LBLOCK) {
				//System.out.println("setting up codeblock");
				// begin a new block of symbols
				// first allocate and compute a codeBlock
				haveStarted = true;
				braceCount = 1;
				cb = new codeBlock();
				cb.symbols = new Vector(30);
				cb.curSymbol = 0;
				cb.ds = ds;
				cb.runOnceOnly = false;
				if(forever) {
					cb.isForever = true;
					cb.testLess = false;
					cb.variable = "";
					cb.curval = 0;
					cb.endval = 0;
					cb.stepval = 0;
					cb.variables = null;
				}
				else {
					cb.isForever = false;
					cb.testLess = (from > to);
					cb.variable = new String(variable);
					cb.curval = from;
					cb.endval = to;
					if(cb.testLess) cb.stepval = -1;
					else cb.stepval = 1;
					cb.variables = new Hashtable(1);
					cb.variables.put(cb.variable, new Integer(cb.curval));
				}
			}
			else if(!haveStarted) {
				System.err.println("I was expecting { to start my repeat loop, but I got " + s);
				return;
			}	
			else {
				// record next symbol
				if(s.sym == sym.LBLOCK) {
					++braceCount;
					cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RBLOCK) {
					--braceCount;
					if(braceCount != 0) cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RETURN && cb.symbols.isEmpty()) {
					/* do not record initial RETURN */
				}
				else {
					//System.out.println("Recording normal element "+s);
					cb.symbols.addElement(s);
				}
			}
		}
		if(!callStack.isEmpty()) cb.parent = (codeBlock)callStack.lastElement();
		else cb.parent = null;
		// codeBlock is all set up, push it on the call stack
		callStack.addElement(cb);
		//System.out.println("Repeat loop set up done, "+cb.symbols.size()+" elements added, callstack size is " + callStack.size());
	}
	public void doConditional(String question, int val1, int val2, DrawSpace ds) {
		boolean haveStarted = false, conditionalState = false;
		int braceCount = 0;
		Symbol s;
		codeBlock cb=null;
		if(question.equalsIgnoreCase("smaller?") && val1 < val2) 
			conditionalState = true;
		else if(question.equalsIgnoreCase("notsmaller?") && !(val1 < val2)) 
			conditionalState = true;
		else if(question.equalsIgnoreCase("bigger?") && val1 > val2) 
			conditionalState = true;
		else if(question.equalsIgnoreCase("notbigger?") && !(val1 > val2)) 
			conditionalState = true;
		else if(question.equalsIgnoreCase("notsame?") && val1 != val2)
			conditionalState = true;
		else if(question.equalsIgnoreCase("same?") && val1 == val2)
			conditionalState = true;
		//System.out.println("Question: " + question + "("+val1+","+val2+") is " + conditionalState);
		//System.out.println("Setting up repeat loop for "+variable+" from "+from+" to "+to+".");
		if(!conditionalState) {
			// gobbler everything up
			while(!haveStarted || braceCount != 0) {
				try {s = tomScan();}
				catch (Exception e) {
					System.err.println("Exception gobbling next token in conditional: " + e);
					return;
				}
				if(!haveStarted && s.sym == sym.LBLOCK) {
					haveStarted = true;
					braceCount = 1;
				}
				else if(s.sym == sym.LBLOCK) ++braceCount;
				else if(s.sym == sym.RBLOCK) --braceCount;
				//System.out.println("eating " + s);
			}
			for(s=new Symbol(sym.PEN);s.sym!=sym.EOF && s.sym!=sym.error;) {
				try {s=tomScan();} catch(Exception e) {
					System.err.println("Problem gobbling the last return in conditional");
					return;
				}
				if(s.sym != sym.RETURN)
					System.err.println("I swallowed symbol " + s + " because I expect a return after my close brace");
				else				
					return;
			}
		}
		//System.out.println("Must be true");
		while (!haveStarted || braceCount!=0) {
			try {s = tomScan();}
			catch (Exception e) {
				System.err.println("Exception getting next token in conditional: " + e);
				return;
			}
			if(!haveStarted && s.sym == sym.RETURN) continue;
			else if(!haveStarted && s.sym == sym.LBLOCK) {
				//System.out.println("setting up codeblock");
				// begin a new block of symbols
				// first allocate and compute a codeBlock
				haveStarted = true;
				braceCount = 1;
				cb = new codeBlock();
				cb.symbols = new Vector(30);
				cb.curSymbol = 0;
				cb.ds = ds;
				cb.variables = null;
				cb.runOnceOnly = true;
				cb.isForever = false;
				cb.testLess = false;
				cb.isConditional = true;
				cb.variable = question;
				cb.curval = 0;
				cb.endval = 0;
				cb.stepval = 0;
			}
			else if(!haveStarted) {
				System.err.println("I was expecting { to start my repeat loop, but I got " + s);
				return;
			}	
			else {
				// record next symbol
				if(s.sym == sym.LBLOCK) {
					++braceCount;
					cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RBLOCK) {
					--braceCount;
					if(braceCount != 0) cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RETURN && cb.symbols.isEmpty()) {
					/* do not record initial RETURN */
				}
				else {
					//System.out.println("Recording normal element "+s);
					cb.symbols.addElement(s);
				}
			}
		}
		// codeBlock is all set up, push it on the call stack
		if(!callStack.isEmpty()) cb.parent = (codeBlock)callStack.lastElement();
		else cb.parent = null;
		callStack.addElement(cb);
		//System.out.println("Repeat loop set up done, "+cb.symbols.size()+" elements added, callstack size is " + callStack.size());
	}
	public void saveCommand(Vector args, DrawSpace ds) {
		boolean haveStarted = false;
		int braceCount = 0, i;
		Symbol s;
		codeBlock cb=null;
		String commandName;
		if(args.size() < 1) {
			System.err.println("I don't know the name of the command to save.");
			return;
		}
		commandName = (String)args.elementAt(0);
		//System.out.println("Must be true");
		while (!haveStarted || braceCount!=0) {
			try {s = tomScan();}
			catch (Exception e) {
				System.err.println("Exception getting next token in conditional: " + e);
				return;
			}
			if(!haveStarted && s.sym == sym.RETURN) continue;
			else if(!haveStarted && s.sym == sym.LBLOCK) {
				//System.out.println("setting up codeblock");
				// begin a new block of symbols
				// first allocate and compute a codeBlock
				haveStarted = true;
				braceCount = 1;
				cb = new codeBlock();
				cb.symbols = new Vector(30);
				cb.curSymbol = 0;
				cb.ds = ds;
				cb.runOnceOnly = true;
				cb.isForever = false;
				cb.testLess = false;
				cb.variable = "";
				cb.curval = 0;
				cb.endval = 0;
				cb.stepval = 0;
				if(args != null) {
					cb.variableNames = new Vector();
					for(i=1;i<args.size();i++) {
						cb.variableNames.addElement(new String((String)args.elementAt(i)));
					}
				}
			}
			else if(!haveStarted) {
				System.err.println("I was expecting { to start my repeat loop, but I got " + s);
				return;
			}	
			else {
				// record next symbol
				if(s.sym == sym.LBLOCK) {
					++braceCount;
					cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RBLOCK) {
					--braceCount;
					if(braceCount != 0) cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RETURN && cb.symbols.isEmpty()) {
					/* do not record initial RETURN */
				}
				else {
					//System.out.println("Recording normal element "+s);
					cb.symbols.addElement(s);
				}
			}
		}
		// add this command to list of commands
		commands.put(commandName, cb);
		for(s=new Symbol(sym.PEN);s.sym!=sym.EOF && s.sym!=sym.error;) {
			try {s=tomScan();} catch(Exception e) {
				System.err.println("Problem gobbling the last return in conditional");
				return;
			}
			if(s.sym != sym.RETURN)
				System.err.println("I swallowed symbol " + s + " because I expect a return after my close brace");
			else				
				return;
		}
		//System.out.println("Repeat loop set up done, "+cb.symbols.size()+" elements added, callstack size is " + callStack.size());
	}
	public void saveFunction(Vector args, DrawSpace ds) {
		boolean haveStarted = false;
		int braceCount = 0, i;
		Symbol s;
		codeBlock cb=null;
		String commandName;
		if(args.size() < 1) {
			System.err.println("I don't know the name of the function to save.");
			return;
		}
		commandName = (String)args.elementAt(0);
		//System.out.println("Must be true");
		while (!haveStarted || braceCount!=0) {
			try {s = tomScan();}
			catch (Exception e) {
				System.err.println("Exception getting next token in conditional: " + e);
				return;
			}
			if(!haveStarted && s.sym == sym.RETURN) continue;
			else if(!haveStarted && s.sym == sym.LBLOCK) {
				//System.out.println("setting up codeblock");
				// begin a new block of symbols
				// first allocate and compute a codeBlock
				haveStarted = true;
				braceCount = 1;
				cb = new codeBlock();
				cb.symbols = new Vector(30);
				cb.curSymbol = 0;
				cb.ds = ds;
				cb.runOnceOnly = true;
				cb.isForever = false;
				cb.testLess = false;
				cb.variable = "";
				cb.curval = 0;
				cb.endval = 0;
				cb.stepval = 0;
				if(args != null) {
					cb.variableNames = new Vector();
					for(i=1;i<args.size();i++) {
						cb.variableNames.addElement(new String((String)args.elementAt(i)));
					}
				}
			}
			else if(!haveStarted) {
				System.err.println("I was expecting { to start my repeat loop, but I got " + s);
				return;
			}	
			else {
				// record next symbol
				if(s.sym == sym.LBLOCK) {
					++braceCount;
					cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RBLOCK) {
					--braceCount;
					if(braceCount != 0) cb.symbols.addElement(s);
				}
				else if(s.sym == sym.RETURN && cb.symbols.isEmpty()) {
					/* do not record initial RETURN */
				}
				else {
					//System.out.println("Recording normal element "+s);
					cb.symbols.addElement(s);
				}
			}
		}
		// add this command to list of commands
		functions.put(commandName, cb);
		//System.out.println("I just saved function " + commandName);
		for(s=new Symbol(sym.PEN);s.sym!=sym.EOF && s.sym!=sym.error;) {
			try {s=tomScan();} catch(Exception e) {
				System.err.println("Problem gobbling the last return in conditional");
				return;
			}
			if(s.sym != sym.RETURN)
				System.err.println("I swallowed symbol " + s + " because I expect a return after my close brace");
			else				
				return;
		}
		//System.out.println("Repeat loop set up done, "+cb.symbols.size()+" elements added, callstack size is " + callStack.size());
	}
	public int runFunction(String command, Vector args) {
		parser parser_obj;
		Symbol parse_tree;
		codeBlock cb;
		//System.out.println("Trying to run " + command + " + with args " + args);
		if(!functions.containsKey(command)) {
			System.err.println("no function named " + command);
			return 0;
		}
		cb = (codeBlock)functions.get(command);
		cb.variables = new Hashtable(5);
		for(int i=0;i<cb.variableNames.size();i++) {
			if(args.size() < i+1) {
				System.err.println("only " + args.size() + " arguments given for command " + 
					command + " but I need " + cb.variableNames.size() + " arguments.");
				return 0;
			}
			cb.variables.put(cb.variableNames.elementAt(i), (Integer)args.elementAt(i));
			//System.out.println("Saving "+cb.variableNames.elementAt(i)+" as "+(Integer)args.elementAt(i));
		}
		cb.curSymbol = 0;
		if(!callStack.isEmpty()) cb.parent = (codeBlock)callStack.lastElement();
		else cb.parent = null;
 		parser_obj = new parser();
		parse_tree = null;
		parser_obj.setCodeBlock(cb);
		parser_obj.setDrawSpace(cb.ds);
		parser_obj.setParseString("\n");
		try {parse_tree = parser_obj.parse();}
		catch (Exception e) {
			System.out.println("" + command + " parsin' err: " +e);
		}
		return parser_obj.getValue();
	}		
	public void runCommand(String command, Vector args) {
		codeBlock cb;
		if(!commands.containsKey(command)) {
			System.err.println("no command named " + command);
			return;
		}
		cb = (codeBlock)commands.get(command);
		cb.variables = new Hashtable(5);
		for(int i=0;i<cb.variableNames.size();i++) {
			if(args.size() < i+1) {
				System.err.println("only " + args.size() + " arguments given for command " + 
					command + " but I need " + cb.variableNames.size() + " arguments.");
				return;
			}
			cb.variables.put(cb.variableNames.elementAt(i), (Integer)args.elementAt(i));
			//System.out.println("Saving "+cb.variableNames.elementAt(i)+" as "+(Integer)args.elementAt(i));
		}
		cb.curSymbol = 0;
		if(!callStack.isEmpty()) cb.parent = (codeBlock)callStack.lastElement();
		else cb.parent = null;
		callStack.addElement(cb);
	}		
	public void pushCodeBlock(codeBlock cb) {
		//System.out.println("Pushing code block");
		callStack.addElement(cb);
	}
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
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
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int YYINITIAL = 0;
	private final int yy_state_dtrans[] = {
		0
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
		++yy_buffer_start;
	}
	private void yy_pushback () {
		--yy_buffer_end;
	}
	private void yy_mark_start () {
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
		YY_END,
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
		YY_NO_ANCHOR
	};
	private int yy_cmap[] = {
		0, 0, 0, 0, 0, 0, 0, 0,
		1, 1, 2, 0, 0, 2, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		1, 0, 0, 0, 0, 0, 0, 0,
		3, 4, 5, 6, 0, 7, 0, 8,
		9, 9, 9, 9, 9, 9, 9, 9,
		9, 9, 0, 0, 10, 0, 11, 12,
		0, 13, 13, 13, 13, 13, 13, 13,
		13, 13, 13, 13, 13, 13, 13, 13,
		13, 13, 13, 13, 13, 13, 13, 13,
		13, 13, 13, 14, 0, 15, 0, 0,
		0, 16, 17, 18, 19, 20, 21, 13,
		13, 22, 13, 13, 23, 24, 25, 26,
		27, 13, 28, 29, 30, 31, 32, 13,
		13, 13, 13, 33, 0, 34, 0, 0
		
	};
	private int yy_rmap[] = {
		0, 1, 2, 3, 1, 1, 1, 1,
		1, 4, 5, 1, 1, 6, 1, 1,
		1, 1, 1, 1, 6, 6, 6, 6,
		6, 6, 6, 6, 6, 6, 6, 6,
		6, 7, 8, 9, 10, 11, 12, 13,
		14, 15, 16, 17, 18, 19, 20, 21,
		22, 23, 24, 25, 26, 27, 28, 29,
		30, 31, 32, 33, 34, 35, 36, 37,
		38, 39, 40, 41, 42, 43, 44, 45,
		46, 47, 48, 49, 50, 51, 52, 53,
		54, 55, 56, 57, 58, 59, 60, 61
		
	};
	private int yy_nxt[][] = {
		{ 1, 2, 3, 4, 5, 6, 7, 8,
			9, 10, 11, 12, 1, 13, 14, 15,
			70, 13, 84, 13, 78, 85, 13, 60,
			13, 79, 13, 47, 80, 48, 13, 13,
			71, 16, 17 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, 2, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, 3, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			33, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 10, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ 33, 33, 19, 33, 33, 33, 33, 33,
			33, 33, 33, 33, 33, 33, 33, 33,
			33, 33, 33, 33, 33, 33, 33, 33,
			33, 33, 33, 33, 33, 33, 33, 33,
			33, 33, 33 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 20, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 21, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 22, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 23, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 24, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 25, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 26, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 27, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 28, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 29, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 30, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 31, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 32, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			62, 13, 13, 13, 34, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 35, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 36, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			37, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			38, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 39, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 40,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 41, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 42, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			43, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 44, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 45, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			46, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 49, 13,
			13, 13, 50, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 51, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 52, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 53,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			54, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 55, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 56, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			57, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			58, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 59, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 61,
			13, 87, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			63, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 64, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			65, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 66, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			67, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 68, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 69,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 72, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 73,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 74, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			75, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 76, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			77, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 81, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 82, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 83, 13,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, -1, -1 
		},
		{ -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 13, -1, -1, 18, 13, -1, -1,
			13, 13, 13, 13, 13, 13, 13, 13,
			13, 13, 13, 13, 13, 13, 86, 13,
			13, -1, -1 
		}
	};
	public Symbol yylex ()
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
 
return (new Symbol(sym.EOF)); 
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
	System.err.println("Illegal character: <" + yytext() + ">");
	}
					case -2:
						break;
					case 2:
						{ }
					case -3:
						break;
					case 3:
						{ return new Symbol(sym.RETURN); }
					case -4:
						break;
					case 4:
						{ return (new Symbol(sym.LPAREN)); }
					case -5:
						break;
					case 5:
						{ return (new Symbol(sym.RPAREN)); }
					case -6:
						break;
					case 6:
						{ return (new Symbol(sym.TIMES)); }
					case -7:
						break;
					case 7:
						{ return (new Symbol(sym.PLUS)); }
					case -8:
						break;
					case 8:
						{ return (new Symbol(sym.MINUS)); }
					case -9:
						break;
					case 9:
						{ return (new Symbol(sym.DIVIDE)); }
					case -10:
						break;
					case 10:
						{
	try {return (new Symbol(sym.INTEGER, Integer.valueOf(yytext())));}
	catch (NumberFormatException e) {return new Symbol(sym.INTEGER, 0);} 
	}
					case -11:
						break;
					case 11:
						{ return (new Symbol(sym.LBRACE)); }
					case -12:
						break;
					case 12:
						{ return (new Symbol(sym.RBRACE)); }
					case -13:
						break;
					case 13:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -14:
						break;
					case 14:
						{ return (new Symbol(sym.LBRACKET)); }
					case -15:
						break;
					case 15:
						{ return (new Symbol(sym.RBRACKET)); }
					case -16:
						break;
					case 16:
						{ return (new Symbol(sym.LBLOCK)); }
					case -17:
						break;
					case 17:
						{ return (new Symbol(sym.RBLOCK)); }
					case -18:
						break;
					case 18:
						{ 
	return new Symbol(sym.QUESTION, new String(yytext()));
	}
					case -19:
						break;
					case 19:
						{ }
					case -20:
						break;
					case 20:
						{return (new Symbol(sym.PEN)); }
					case -21:
						break;
					case 21:
						{return (new Symbol(sym.SET)); }
					case -22:
						break;
					case 22:
						{return (new Symbol(sym.LINE)); }
					case -23:
						break;
					case 23:
						{return (new Symbol(sym.LOAD)); }
					case -24:
						break;
					case 24:
						{return (new Symbol(sym.ALIAS)); }
					case -25:
						break;
					case 25:
						{return (new Symbol(sym.PAPER)); }
					case -26:
						break;
					case 26:
						{return (new Symbol(sym.VALUE)); }
					case -27:
						break;
					case 27:
						{return (new Symbol(sym.ESCAPE)); }
					case -28:
						break;
					case 28:
						{return (new Symbol(sym.NUMBER)); }
					case -29:
						break;
					case 29:
						{return (new Symbol(sym.REPEAT)); }
					case -30:
						break;
					case 30:
						{return (new Symbol(sym.COMMAND)); }
					case -31:
						break;
					case 31:
						{return (new Symbol(sym.FOREVER)); }
					case -32:
						break;
					case 32:
						{return (new Symbol(sym.ANTIALIAS)); }
					case -33:
						break;
					case 34:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -34:
						break;
					case 35:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -35:
						break;
					case 36:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -36:
						break;
					case 37:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -37:
						break;
					case 38:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -38:
						break;
					case 39:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -39:
						break;
					case 40:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -40:
						break;
					case 41:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -41:
						break;
					case 42:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -42:
						break;
					case 43:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -43:
						break;
					case 44:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -44:
						break;
					case 45:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -45:
						break;
					case 46:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -46:
						break;
					case 47:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -47:
						break;
					case 48:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -48:
						break;
					case 49:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -49:
						break;
					case 50:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -50:
						break;
					case 51:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -51:
						break;
					case 52:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -52:
						break;
					case 53:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -53:
						break;
					case 54:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -54:
						break;
					case 55:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -55:
						break;
					case 56:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -56:
						break;
					case 57:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -57:
						break;
					case 58:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -58:
						break;
					case 59:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -59:
						break;
					case 60:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -60:
						break;
					case 61:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -61:
						break;
					case 62:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -62:
						break;
					case 63:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -63:
						break;
					case 64:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -64:
						break;
					case 65:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -65:
						break;
					case 66:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -66:
						break;
					case 67:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -67:
						break;
					case 68:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -68:
						break;
					case 69:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -69:
						break;
					case 70:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -70:
						break;
					case 71:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -71:
						break;
					case 72:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -72:
						break;
					case 73:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -73:
						break;
					case 74:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -74:
						break;
					case 75:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -75:
						break;
					case 76:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -76:
						break;
					case 77:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -77:
						break;
					case 78:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -78:
						break;
					case 79:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -79:
						break;
					case 80:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -80:
						break;
					case 81:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -81:
						break;
					case 82:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -82:
						break;
					case 83:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -83:
						break;
					case 84:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -84:
						break;
					case 85:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -85:
						break;
					case 86:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -86:
						break;
					case 87:
						{
	String s = new String(yytext());
	if(commands.containsKey(s)) return new Symbol(sym.COMMANDNAME, s);
	else return new Symbol(sym.NAME, s);
	}
					case -87:
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
