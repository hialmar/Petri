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
    Input class - an abstract class for text input dialog. Allows user to
    enter some textual data and pass it to the state machine. Subclasses
    must implement passInput method.
*/

abstract class Input extends Panel
{
    /*
        INSTANCE VARIABLES
    */


    /* text field where user will be entering parameters */

    private TextField   input;

    /* parent object */

    private Panel       parent;

    /* state machine object that will be given entered parameters */

    protected PetriNet  network;


    /* hint line object */

    protected Help      help;


    /*
        PUBLIC METHODS
    */


    /*
        constructor method
    */

    public Input (Panel par, String label)
    {
        /* initialize instance variables */

        parent  = par;
        network = (PetriNet) null;
        help    = (Help) null;

        setBackground (new Color (150, 150, 150));

        /* create and add to dialog command label */

        Label l = new Label (label);
        l . setAlignment (Label . CENTER);
        add ("North", l);

        /* create and add to dialog text field */

        input = new TextField (50);
        add ("Center", input);

        /* create and add to dialog a panel holding two buttons */

        Panel p = new Panel ();
        p . add (new Button ("OK"));
        p . add (new Button ("Cancel"));
        add ("South", p);

        /* make sure dialog does not display until specifically told to */

        hide ();

    } /* end Input */


    /*
        set PetriNet object that will receive the entered entered parameters
    */

    public void setPetriNet (PetriNet pn, Help h)
    {
        network = pn;
        help = h;

    } /* end setMachine */


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
        pass data to the state machine - must be implemented by subclass
    */

    abstract public void passInput (String i);


    /*
        handle action events (button presses)
    */

    public boolean action (Event e, Object obj)
    {
        String label = (String) obj;

        /* only handle button presses */

        if (e . target instanceof Button)
        {
            if (label . equals ("OK"))
            {
                /* pass entered text to the machine */

                passInput (input . getText ());

                /* hide our dialog and force repaint of the parent */

                hide();
                parent . repaint ();

                return true;
            }
            else if (label . equals ("Cancel"))
            {
                /* clear the text field */

                input . setText ("");

                /* hide our dialog and force repaint of the parent */

                hide();
                parent . repaint ();

                return true;
            }
        }

        return true;

    } /* action */

} /* end Input */


