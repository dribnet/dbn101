import java.awt.*;

class DrawCanvas extends Canvas {
	String str;
	Color bgColor = Color.white;
	Color fgColor = Color.black;
	
	public DrawCanvas(String s) {
		str = s;
	}

	public void paint(Graphics g) {
		Dimension d = size();
		int w = d.width, h = d.height;
		
		g.setColor(bgColor);
		g.fillRect(0,0,w,h);
		g.setColor(fgColor);
		g.drawString(str, 12, 12);
	}
			
	public void loadString(String s) {
		str = s;
	}         
	
	public void setPaper(int paperColor) {
		int c;
		
		c = paperColor * 255 / 100;
		bgColor = new Color(c, c, c);
	}

	public void setPen(int penColor) {
		int c;
		
		c = penColor * 255 / 100;
		fgColor = new Color(c, c, c);
	}

}
