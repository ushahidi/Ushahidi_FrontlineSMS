/* Thinlet GUI toolkit - www.thinlet.com
 * Copyright (C) 2002-2005 Robert Bajzat (rbajzat@freemail.hu) */
package thinlet;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 * <code>FrameLauncher</code> is a double buffered frame
 * to launch any <i>thinlet</i> component as an application
 */
public class FrameLauncher extends Frame implements WindowListener {

	private static final long serialVersionUID = 151991667097482619L;
	private transient Thinlet content;
	private transient Image doublebuffer;
	
	/**
	 * Construct and show a new frame with the specified title, including the
	 * given <i>thinlet</i> component. The frame is centered on the screen, and its
	 * preferred size is specified (excluding the frame's borders). The icon is
	 * the thinlet logo
	 * 
	 * @param title the title to be displayed in the frame's border
	 * @param content a <i>thinlet</i> instance
	 * @param width the preferred width of the content
	 * @param height the preferred height of the content
	 */
	public FrameLauncher(String title, Thinlet content, int width, int height, Image icon) {
		super(title);
		this.content = content;
		add(content, BorderLayout.CENTER);
		addWindowListener(this);
		pack();
		
		Dimension ss = getToolkit().getScreenSize();
		width = Math.min(width, ss.width);
		height = Math.min(height, ss.height);
		setBounds((ss.width - width) / 2, (ss.height - height) / 2, width, height); 
		setVisible(true);
		//maximize: setBounds(-is.left, -is.top, ss.width + is.left + is.right, ss.height + is.top + is.bottom);
		
		int[] pix = new int[16 * 16];
		for (int x = 0; x < 16; x++) {
			int sx = ((x >= 1) && (x <= 9)) ? 1 : (((x >= 11) && (x <= 14)) ? 2 : 0);
			for (int y = 0; y < 16; y++) {
				int sy = ((y >= 1) && (y <= 9)) ? 1 : (((y >= 11) && (y <= 14)) ? 2 : 0);
				pix[y * 16 + x] = ((sx == 0) || (sy == 0)) ? 0xffffffff :
					((sx == 1) ? ((sy == 1) ? (((y == 2) && (x >= 2) && (x <= 8)) ? 0xffffffff :
						(((y >= 3) && (y <= 8)) ? ((x == 5) ? 0xffffffff : (((x == 4) || (x == 6)) ?
							0xffe8bcbd : 0xffb01416)) : 0xffb01416)) : 0xff377ca4) :
						((sy == 1) ? 0xff3a831d : 0xfff2cc9c)); 
			}
		}
		if(icon == null) icon = createImage(new MemoryImageSource(16, 16, pix, 0, 16));
		setIconImage(icon);
	}
		
	/**
	 * Call the paint method to redraw this component without painting a
	 * background rectangle
	 */
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 * Create a double buffer if needed,
	 * the <i>thinlet</i> component paints the content
	 */
	public void paint(Graphics g) { 
		if (doublebuffer == null) {
			Dimension d = getSize();
			doublebuffer = createImage(d.width, d.height);
		}
		Graphics dg = doublebuffer.getGraphics();
		dg.setClip(g.getClipBounds());
		super.paint(dg);
		dg.dispose();
		g.drawImage(doublebuffer, 0, 0, this);
	}
	
	/**
	 * Clear the double buffer image (because the frame has been resized),
	 * the overriden method lays out its components
	 * (centers the <i>thinlet</i> component)
	 */
	public void doLayout() {
		if (doublebuffer != null) {
			doublebuffer.flush();
			doublebuffer = null;
		}
		super.doLayout();
	}

	/**
	 * Notify the <i>thinlet</i> component and terminates the Java Virtual Machine,
	 * or redisplay the frame depending on the return value of <i>thinlet</i>'s
	 * <code>destroy</code> method (true by default,
	 * thus terminates the VM if not overriden)
	 */
	public void windowClosing(WindowEvent e) {
		if (!content.hasSomethingToDoBeforeExit()) {
			if (content.destroy()) {
				System.exit(0);
			} else {
				setVisible(false);
			}
		} 
	}
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	public void setContent(Thinlet content) {
		this.content = content;
	}
}