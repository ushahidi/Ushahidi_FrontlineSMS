/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.plugins.forms.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.frontlinesms.Utils;
import net.frontlinesms.plugins.forms.ui.components.PaletteComponent;

import org.apache.log4j.Logger;
/**
 * This class is responsible for handling all the drag and drop actions.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class DragListener implements Transferable, DragSourceListener, DragGestureListener, DropTargetListener, DragSourceMotionListener {
	private static DataFlavor[] flavors;
	private Object data;
	private DrawingPanel drawingPanel;
	
	private static Logger LOG = Utils.getLogger(DragListener.class);
	
	public DragListener(DrawingPanel drawingPanel) {
		flavors = new DataFlavor[1];
		try {
			flavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException e) {
			LOG.debug("", e);
		}
		this.drawingPanel = drawingPanel;
	}
	
	public void dragDropEnd(DragSourceDropEvent dsde) {
		data = null;
		drawingPanel.dragMoved(-1, -1);
	}
	public void dragEnter(DragSourceDragEvent dsde) {}
	public void dragExit(DragSourceEvent dse) {}

	public void dragOver(DragSourceDragEvent dsde) {
		data = dsde.getSource();
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {}

	public void dragGestureRecognized(DragGestureEvent dge) {
		dge.startDrag(null, this, this);
	}

	public void dragEnter(DropTargetDragEvent dtde) {}
	public void dragExit(DropTargetEvent dte) {}
	public void dragOver(DropTargetDragEvent dtde) {}

	public void drop(DropTargetDropEvent dtde) {
		dtde.acceptDrop(dtde.getDropAction());
		try {
			Object target = dtde.getSource();
			Component targ = ((DropTarget) target).getComponent();
			Object source = dtde.getTransferable().getTransferData(flavors[0]);
			if (source != null) {
				Component obj = ((DragSourceContext) source).getComponent();
				/** 
				 * We've got 3 different types of drag and drop in our code:
				 * <li> Drag a component from palette to preview
				 * <li> Drag a component from preview to the bin
				 * <li> Drag a component from preview to itself (moving)
				 */
				if (targ instanceof PreviewPanel) {
					// So if we are dragging something to Preview, either we are moving or creating a new component.
					if (targ.equals(getContainerParent(obj))) {
						// Moving in the preview.
						drawingPanel.moveComponentInPreview(getContainerParentDelete(obj), dtde.getLocation().x, dtde.getLocation().y);
					} else {
						// Adding to preview
						drawingPanel.addToPreview((PaletteComponent) obj, dtde.getLocation().x, dtde.getLocation().y);
					}
				} else if (targ instanceof JLabel) {
					// Here we are dragging something to the bin
					drawingPanel.deleteFromPreview(getContainerParentDelete(obj));
				}
			}
			dtde.dropComplete(true);
		} catch (UnsupportedFlavorException e) {
			LOG.debug("", e);
		} catch (IOException e) {
			LOG.debug("", e);
		}
	}

	/**
	 * Recursively returns the container being deleted.
	 * 
	 * @param obj
	 * @return
	 */
	private Component getContainerParent(Component obj) {
		if (obj == null) return null;
		if (obj instanceof PreviewPanel) 
			return obj;
		return getContainerParent(obj.getParent());
	}
	
	/**
	 * Recursively returns the container being deleted.
	 * 
	 * @param obj
	 * @return
	 */
	private Component getContainerParentDelete(Component obj) {
		if (obj == null) return null;
		if (obj instanceof JPanel || obj instanceof JScrollPane) 
			return obj;
		return getContainerParentDelete(obj.getParent());
	}
	
	public void dropActionChanged(DropTargetDragEvent dtde) {}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType);
	}

	public synchronized Object getTransferData(DataFlavor flavor) {
		return data;
	}

	public void dragMouseMoved(DragSourceDragEvent dsde) {
		drawingPanel.dragMoved(dsde.getX(), dsde.getY());
	}
}
