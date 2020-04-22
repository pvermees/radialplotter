/*
 * Based on Sun's ColorChooserDemo
 */

package RadialPlotter;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class DensityColourChooser extends JPanel
                              implements ChangeListener {

    @SuppressWarnings("LeakingThisInConstructor")
    public DensityColourChooser(DensityPlot plot) throws Exception {
        super(new BorderLayout());
        this.plot = plot;
        
        this.addStuff();
    }
    
    private void addStuff() throws Exception{
        // Set up the banner at the top of the window
        this.banner = new DensityColourBanner(this.plot);

        //Set up color chooser for setting text color
        this.tcc = new JColorChooser(banner.getBackground());
        tcc.getSelectionModel().addChangeListener(this);
        tcc.setBorder(BorderFactory.createTitledBorder("Choose Colour"));
        tcc.setPreviewPanel(new JPanel());

        buttons = new DensityColourButtons(banner);        
        this.add(banner, BorderLayout.PAGE_START);
        this.add(tcc, BorderLayout.CENTER);
        this.add(buttons, BorderLayout.PAGE_END);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        try {
            Color newColor = tcc.getColor();
            if (banner.doKDEarea()){
                banner.setKDEbackground(newColor);
            } else if (banner.doKDEline()){
                banner.setKDEforeground(newColor);
            } else if (banner.doPDParea()){
                banner.setPDPbackground(newColor);
            } else if (banner.doPDPline()){
                banner.setPDPforeground(newColor);
            } else if (banner.doHistArea()){
                banner.setHistBackground(newColor);
            } else if (banner.doHistLine()){
                banner.setHistForeground(newColor);
            } else if (banner.doPointsArea()){
                banner.setPointsBackground(newColor);
            } else if (banner.doPointsLine()){
                banner.setPointsForeground(newColor);
            }
        } catch (Exception ex){
            if (Data.DEBUGMODE){ex.printStackTrace(System.out);}
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     * @param plot
     * @throws java.lang.Exception
     */
    public static void createAndShowGUI(DensityPlot plot) throws Exception {
        //Create and set up the window.
        JFrame frame = new JFrame("Colour Chooser");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new DensityColourChooser(plot);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    protected DensityPlot plot;
    protected JColorChooser tcc;
    protected DensityColourBanner banner;
    protected DensityColourButtons buttons;

}