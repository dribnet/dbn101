/*
	Trivial applet that displays a string - 4/96 PNL
*/

import java.awt.*;
import java.applet.Applet;
import java.net.URL;
import java.io.InputStream;
import java.io.DataInputStream;

public class dbn extends Applet
{
	TextArea ta;
	DrawSpace ds;
	
	String getProgram(String filename) {
		URL theUrl;
		String openMe, program = new String(), line;
		InputStream inStream;
		DataInputStream dStream;
				
     	try {
			theUrl = new URL(filename);
			//System.out.println("A Trying " + filename);
			inStream = theUrl.openStream();
		}
		catch(Exception e) { try {
			theUrl = getDocumentBase();
			openMe = theUrl.toString();
			//System.out.println("B URL is " + openMe);			
			if(!openMe.endsWith("/")) openMe = openMe.concat("/");
			openMe = openMe.concat(filename);
			//System.out.println("B Trying " + openMe);
			theUrl = new URL(openMe);
			inStream = theUrl.openStream();
		}
		catch(Exception ee) { try {
			theUrl = getCodeBase();
			openMe = theUrl.toString();
			//System.out.println("C URL is " + openMe);			
			if(!openMe.endsWith("/")) openMe = openMe.concat("/");
			openMe = openMe.concat(filename);
			//System.out.println("C Trying " + openMe);
			theUrl = new URL(openMe);
			inStream = theUrl.openStream();
		}
		catch(Exception eee) {
			System.out.println("Errors: " + e + "\n" + ee + "\n" + eee);
			return new String();
		} } }
		try {
			dStream = new DataInputStream(inStream);
			while(true) {
				line = dStream.readLine();
				if(line == null) return program;
				//System.out.println("Read line: " + line);
				program = program.concat(line + "\n");
			}
		}
		catch (Exception eeee) {
			return program;
		}
	}

	public void init() {
		String file, prog;
		
		Panel p;
		this.setLayout(new BorderLayout());
		this.add("North", new Label(" Design by numbers. If you don't, who will?"));
		p = new Panel();
		p.setLayout(new GridLayout(1,2));
		this.add("Center", p);		
		
		file = getParameter("program");
		if(file != null && file.length() > 0) prog = getProgram(file);
		else prog = new String("// type program here\n");
		
		ta = new TextArea(prog);

		p.add(ta);

		ds = new DrawSpace(ta);
		p.add(ds);
	}
	
	public void start() {
		ds.resume();
	}
	
	public void stop() {
		//ds.pause();
	}
	
	public void destroy() {
		ds.destroy();
	}	
}
