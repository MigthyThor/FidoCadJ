package net.sourceforge.fidocadj.toolbars;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import net.sourceforge.fidocadj.dialogs.*;
import net.sourceforge.fidocadj.geom.*;
import net.sourceforge.fidocadj.globals.*;
import net.sourceforge.fidocadj.layers.*;


/** ToolbarZoom class

    <pre>
    This file is part of FidoCadJ.

    FidoCadJ is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FidoCadJ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FidoCadJ.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2007-2014 by Davide Bucci
    </pre>

    @author Davide Bucci
*/
public class ToolbarZoom extends JToolBar implements ActionListener,
                                                     ChangeZoomListener,
                                                     ChangeCoordinatesListener
{
    private final JComboBox<String> zoom;
    private final JToggleButton showGrid;
    private final JToggleButton snapGrid;
    private final JLabel coords;
    private final JLabel infos;
    private ChangeGridState changeListener;
    private boolean flagModify;
    private double oldzoom;
    private ChangeZoomListener notifyZoomChangeListener;
    private ZoomToFitListener actualZoomToFitListener;

    private final JComboBox<LayerDesc> layerSel;
    private ChangeSelectedLayer changeLayerListener;

    /** Standard constructor
        @param l the layer description
    */
    public ToolbarZoom (Vector<LayerDesc> l)
    {
        putClientProperty("Quaqua.ToolBar.style", "title");
        zoom = new JComboBox<String>();
        zoom.addItem("25%");
        zoom.addItem("50%");
        zoom.addItem("75%");
        zoom.addItem("100%");
        zoom.addItem("150%");
        zoom.addItem("200%");
        zoom.addItem("300%");
        zoom.addItem("400%");
        zoom.addItem("600%");
        zoom.addItem("800%");
        zoom.addItem("1000%");
        zoom.addItem("1500%");
        zoom.addItem("2000%");
        zoom.setPreferredSize(new Dimension (80,29));
        zoom.setMaximumSize(new Dimension (80,38));
        zoom.setMinimumSize(new Dimension (80,18));

        /* Commented the following line due to this remark:
            http://www.electroyou.it/phpBB2/viewtopic.php?f=4&
                t=18347&start=450#p301931
        */
        //zoom.setFocusable(false);
        JButton zoomFit;
        zoomFit=new JButton(Globals.messages.getString("Zoom_fit"));
        showGrid=new JToggleButton(Globals.messages.getString("ShowGrid"));

        snapGrid=new JToggleButton(Globals.messages.getString("SnapToGrid"));

        coords = new JLabel("");
        infos = new JLabel("");

        setBorderPainted(false);
        layerSel = new JComboBox<LayerDesc>(new Vector<LayerDesc>(l));
        layerSel.setToolTipText(
            Globals.messages.getString("tooltip_layerSel"));
        layerSel.setRenderer( new LayerCellRenderer());
        changeListener=null;


        // MacOSX Quaqua informations
        zoomFit.putClientProperty("Quaqua.Button.style","toggleWest");
        showGrid.putClientProperty("Quaqua.Button.style","toggleCenter");
        snapGrid.putClientProperty("Quaqua.Button.style","toggleEast");

        zoom.addActionListener(this);
        zoomFit.addActionListener(this);
        showGrid.addActionListener(this);
        snapGrid.addActionListener(this);
        layerSel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                if (layerSel.getSelectedIndex()>=0 && changeListener!=null) {
                    changeLayerListener.changeSelectedLayer(
                        layerSel.getSelectedIndex());
                }
            }
        });

        changeListener=null;
        add(zoom);
        add(zoomFit);
        add(showGrid);
        add(snapGrid);
        add(layerSel);
        add(Box.createGlue());
        add(infos);
        add(coords);
        infos.setPreferredSize(new Dimension (250,28));
        infos.setMinimumSize(new Dimension (50,18));
        infos.setMaximumSize(new Dimension (250,38));

        coords.setPreferredSize(new Dimension (300,28));
        coords.setMinimumSize(new Dimension (300,18));
        coords.setMaximumSize(new Dimension (300,38));
        setFloatable(true);
        setRollover(false);
        zoom.setEditable(true);
        showGrid.setSelected(true);
        snapGrid.setSelected(true);
        changeCoordinates(0, 0);
    }
    /** Add a layer listener (object implementing the ChangeSelectionListener
        interface) whose change method will be called when the current
        layer should be changed.
        @param c the change selection layer listener.
    */
    public void addLayerListener(ChangeSelectedLayer c)
    {
        changeLayerListener=c;
    }


    /** Add a grid state listener whose methods will be called when the current
        grid state should be changed.
        @param c the wanted grid state
    */
    public void addGridStateListener(ChangeGridState c)
    {
        changeListener=c;
    }

    /** Add a zoom change listener to be called when the zoom is changed by the
        user with the combo box slide.
        @param c the new listener.
    */
    public void addChangeZoomListener(ChangeZoomListener c)
    {
        notifyZoomChangeListener=c;
    }

    /** Add a zoom to fit listener to be called when the user wants to fit
        @param c the new listener.
    */
    public void addZoomToFitListener(ZoomToFitListener c)
    {
        actualZoomToFitListener=c;
    }

    /** The event listener to be called when the buttons are pressed, or the
        zoom setup is changed.
        @param evt the event to be processed.
    */
    public void actionPerformed(ActionEvent evt)
    {

        String s = evt.getActionCommand();

        // Buttons
        if(s.equals(Globals.messages.getString("ShowGrid"))) {
            //showGrid.setSelected(!showGrid.isSelected());
            if(changeListener!=null)
                changeListener.setGridVisibility(showGrid.isSelected());
        } else if(s.equals(Globals.messages.getString("SnapToGrid"))) {
            //snapGrid.setSelected(!snapGrid.isSelected());
            if(changeListener!=null)
                changeListener.setSnapState(snapGrid.isSelected());
        } else if(s.equals(Globals.messages.getString("Zoom_fit"))) {
            if(actualZoomToFitListener!=null) {
                actualZoomToFitListener.zoomToFit();
            }
        } else if(evt.getSource() instanceof JComboBox) {
            // ComboBox: the only one is about the zoom settings.
            JComboBox<String> source=(JComboBox<String>)evt.getSource();
            if (notifyZoomChangeListener!=null) {
                try {
                    s=(String)source.getSelectedItem();

                    // The percent symbol should be eliminated.
                    s=s.replace('%',' ').trim();
                    //System.out.println ("New zoom: "+s);
                    double z=Double.parseDouble(s);
                    // Very important: if I remove that, CPU goes to 100%
                    // since this is called continuously!
                    if(z==oldzoom)
                        return;
                    oldzoom=z;
                    if(10<=z && z<=2000) {
                        notifyZoomChangeListener.changeZoom(z/100);
                    }
                } catch (NumberFormatException E) {
                    // Just ignore
                }
            }
        }
    }

    /** Change the zoom level and show it in the combo box
        @param z the new zoom level to be considered
    */
    public void changeZoom(double z)
    {
        zoom.setSelectedItem(""+((int)(z*100))+"%");
    }

    /** Change the cursor coordinates to be shown
        @param x the x value of the cursor coordinates (logical units)
        @param y the y value of the cursor coordinates (logical units)
    */
    public void changeCoordinates(int x, int y)
    {
        int xum=x*127;
        int yum=y*127;

        float xmm=(float)xum/1000;
        float ymm=(float)yum/1000;

        coords.setText(""+x+"; "+y+ " ("+xmm+" mm; "+ymm+" mm)");
    }

    /** Change the strict compatibility mode with the old FidoCAD.
        @param strict true if a strict compatibility mode should be active.
    */
    public void changeStrict(boolean strict)
    {
        // Does nothing.
    }

    /** Change the infos.
        @param s the string to be shown.
    */
    public void changeInfos(String s)
    {
        infos.setText(s);
    }
}