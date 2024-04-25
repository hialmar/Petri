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
    QuitDialog class - gives user a chance to confirm the exit
*/


class QuitDialog extends Panel
{
    /*
        PUBLIC METHODS
    */


    /*
        constructor method
    */

    public QuitDialog (Panel prnt)
    {
        setBackground (new Color (150, 150, 150));

        /* create and add to dialog labels */

        Label l = new Label ("Really quit?:", Label . CENTER);
        add ("North", l);

        /* create and add to dialog a panel holding two buttons */

        Panel p = new Panel ();
        p . add (new Button ("Yes"));
        p . add (new Button ("No"));
        add ("South", p);

        /* make sure dialog does not display until specifically told to */

        hide ();

    } /* end QuitDialog */


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
        handle action events (button presses)
    */

    public boolean action (Event e, Object arg)
    {
        String label = (String) arg;

        if (label . equals ("Yes"))
        {
            /* perform system exit */

            System . exit (0);
        }
        else if (label . equals ("No"))
        {
            /* make dialog go away */

            hide ();
        }

        return true;

    } /* end action */

} /* end QuitDialog */


