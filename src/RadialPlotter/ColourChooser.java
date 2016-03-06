/*
 * Based on Sun's ColorChooserDemo
 */

package RadialPlotter;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.colorchooser.*;

/* ColorChooserDemo.java requires no other files. */
public class ColourChooser extends JPanel
                              implements ChangeListener {

    @SuppressWarnings("LeakingThisInConstructor")
    public ColourChooser(RadialPlot plot) throws Exception {
        super(new BorderLayout());
        this.plot = plot;

        // Set up the banner at the top of the window
        banner = new ColourBanner(plot);
        banner.setPreferredSize(new Dimension(100, 65));

        // Add the radio buttons     
        minRadioButton = new javax.swing.JRadioButton();
        maxRadioButton = new javax.swing.JRadioButton();        
        minRadioButton.setSelected(true);
        minRadioButton.setText("min");
        maxRadioButton.setText("max");
        ButtonGroup minmax = new ButtonGroup();
        minmax.add(this.minRadioButton);
        minmax.add(this.maxRadioButton);   
        
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.add(minRadioButton, BorderLayout.BEFORE_LINE_BEGINS);
        bannerPanel.add(banner, BorderLayout.CENTER);
        bannerPanel.add(maxRadioButton, BorderLayout.AFTER_LINE_ENDS);
        bannerPanel.setBorder(BorderFactory.createTitledBorder("Preview"));

        buttons = new ColourButtons(banner);

        //Set up color chooser
        tcc = new JColorChooser(plot.data.preferences.minbarcolour());
        tcc.getSelectionModel().addChangeListener(this);
        tcc.setBorder(BorderFactory.createTitledBorder("Pick a Colour"));
        tcc.setPreviewPanel(new JPanel()); // remove preview pane
        this.removePanes();
        
        add(bannerPanel, BorderLayout.PAGE_START);
        add(tcc, BorderLayout.CENTER);
        add(buttons, BorderLayout.PAGE_END);

    }
    
    private void removePanes() {
        // Retrieve the current set of panels
        AbstractColorChooserPanel[] oldPanels = tcc.getChooserPanels();
        for (AbstractColorChooserPanel oldPanel : oldPanels) {
            String clsName = oldPanel.getClass().getName();
            if (clsName.equals("javax.swing.colorchooser.DefaultSwatchChooserPanel")) {
            } else if (clsName.equals("javax.swing.colorchooser.DefaultRGBChooserPanel")) {
            } else if (clsName.equals("javax.swing.colorchooser.DefaultHSBChooserPanel")) {
                // Remove hsb chooser if desired
                tcc.removeChooserPanel(oldPanel);
            }
        } 
    }

    @Override
    public void stateChanged(ChangeEvent e){
        try {
            this.refresh(tcc.getColor());
        } catch (Exception ex){
            if (Data.debugmode){ex.printStackTrace(System.out);}
        }
    }
    
    private void refresh(Color c) throws Exception {
        if (this.minRadioButton.isSelected()){
            plot.data.preferences.minbarcolour(c);
        } else if (this.maxRadioButton.isSelected()){
            plot.data.preferences.maxbarcolour(c);
        }
        plot.getColourScale().initColours();
        banner.repaint();
    }     

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     * @param plot
     * @throws java.lang.Exception
     */
    public static void createAndShowGUI(RadialPlot plot) throws Exception {
        //Create and set up the window.
        final JFrame frame = new JFrame("Colour Chooser");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ColourChooser(plot);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.addWindowFocusListener(new WindowAdapter() { 
            @Override
            public void windowLostFocus(WindowEvent evt) { 
                frame.requestFocus(); 
            } 
        });         
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    RadialPlot plot;
    protected JColorChooser tcc;
    protected ColourBanner banner;
    protected javax.swing.JRadioButton maxRadioButton;
    protected javax.swing.JRadioButton minRadioButton;
    protected ColourButtons buttons;
    
}