package com.ushahidi.plugins.mapping.ui;

import java.awt.event.*;
import java.awt.*;
import java.util.Vector;

/**
 * Simple test class for Thinlet CustomComponent extension.
 *
 * @autor: stephan@stean.ch
 * @version: 1.0
 */
public class DragBean extends thinlet.CustomComponent {
  private Vector coordinates = new Vector();
  private Point currentPoint;
  private int ovalDiam = 8;

	public DragBean() {
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me) {
        int r = ovalDiam/2;
        for(int i=0;i<coordinates.size();i++) {
          Point p = (Point)coordinates.elementAt(i);
          if(p.x-r<me.getX() && p.x+r>me.getX() && p.y-r<me.getY() && p.y+r>me.getY()) {
            currentPoint=p;
            break;
          }
        }
      }
      public void mouseReleased(MouseEvent me) {
        if(currentPoint!=null) {
          currentPoint.x=me.getX();
          currentPoint.y=me.getY();
          currentPoint=null;
        }
        repaint();
      }
      public void mouseEntered(MouseEvent me) {
    	  
      }
      public void mouseExited(MouseEvent me) {
    	  
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent me) {
        if(currentPoint!=null) {
          Graphics g = getGraphics();
          g.setColor(Color.red);
          drawPoint(g,me.getX(),me.getY());
        }
      }
    });

    this.coordinates.addElement(new Point(20,20));
    this.coordinates.addElement(new Point(30,80));
    this.coordinates.addElement(new Point(70,90));
    this.coordinates.addElement(new Point(90,40));
	}

  private void drawPoint(Graphics g, int x, int y) {
    g.fillOval(x-this.ovalDiam/2,y-this.ovalDiam/2,this.ovalDiam,this.ovalDiam);
  }

	public void paint(Graphics g) {
    Dimension d = getSize();
		g.setColor(Color.white);
		g.fillRect(1, 1, d.width - 2, d.height - 2);
    for(int i=0;i<this.coordinates.size();i++) {
      Point p1 = (Point) this.coordinates.elementAt(i);
      Point p2=(Point)(i!=this.coordinates.size()-1?this.coordinates.elementAt(i+1):this.coordinates.elementAt(0));
      g.setColor(Color.green);
      g.drawLine(p1.x,p1.y,p2.x,p2.y);
      g.setColor(Color.blue);
      drawPoint(g,p1.x,p1.y);
    }
	}
}