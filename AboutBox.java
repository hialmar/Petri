/*
    IMPORTS
*/


import java.applet.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

/*
    About class - displays the program and author names.
*/


class AboutBox extends Panel
{
    /*
        PUBLIC METHODS
    */


    /*
        constructor method
    */

    public AboutBox (Panel parent)
    {
        setBackground (new Color (150, 150, 150));

        /* create and add to dialog labels */

        Label l = new Label ("Petri Network Simulator", Label . CENTER);
        l . setForeground (Color . red);
        l . setFont (new Font ("Helvetica", Font . BOLD, 14));
        add ("North", l);

        l = new Label ("Developed by Torguet Patrice, 1996", Label . CENTER);
        l . setForeground (Color . blue);
        l . setFont (new Font ("Helvetica", Font . ITALIC, 10));
        add ("Center", l);

        l = new Label ("Based on Kyril Faenov's Finite State Machine Simulator", Label . CENTER);
        l . setForeground (Color . blue);
        l . setFont (new Font ("Helvetica", Font . ITALIC, 10));
        add ("Center2", l);

        /* create and add to dialog a button */

        add ("South", new Button ("OK"));

        /* make sure dialog does not display until specifically told to */

        hide ();

    } /* end AboutBox */


    /*
        during repaint draw a 3d rectangle around the dialog for cooler look
    */


    public void paint (Graphics g)
    {
        Rectangle bounds = bounds ();

        g . setColor (getBackground ());
        g . draw3DRect (0, 0, bounds . width - 1, bounds . height - 1, true);

    } /* end paint */


    /*
        leave some space around the edges
    */


    public Insets insets()
    {
        return new Insets (5, 5, 5, 5);

    } /* end Insets */


    /*
        handle action events (button presses)
    */

    public boolean action (Event e, Object obj)
    {
        /* just make the dialog go away */

        hide();
        return true;

    } /* action */

} /* end AboutBox */


