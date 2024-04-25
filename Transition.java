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
    Transition class - implements all the logic for the place. It knows how to draw
    itself on the screen, how to read and write itself to/from a file. Also
    maintains all the arcs and all the transitions initiating from it.
*/


class Transition
{
    /*
        CLASS CONSTANTS
    */

    /*
        current state of the Transition
    */

    final static int    NORMAL      = 0;
    final static int    MOVING      = 1;
    final static int    ACTIVE      = 2;
    final static int    HIGHLIGHTED = 3;

    /* dimensions on the screen in pixels */

    final static int    HSIZE = 50;
    final static int    VSIZE = 10;

    /* delimiters used to distinguish records in files */

    final static int    BOA = 0x70;
    final static int    EOT = 0x72;


    /*
        INSTANCE VARIABLES
    */

    /* place origin coordinates on the screen (top left corner of the bounding
       rectangle). */

    private int         originX;
    private int         originY;

    /* origin positions used to redraw state during dragging and moving */

    private int         oldX;
    private int         oldY;
    private int         dragX;
    private int         dragY;

    /* colors corresponding to different states */

    private Color       colorNormal;
    private Color       colorSelected;
    private Color       colorActive;
    private Color       colorHighlight;

    /* a growing array of incoming arcs */

    private Vector      arcsIn;

    /* a growing array of outcoming arcs */

    private Vector      arcsOut;

    /* currently active In and Out Arcs */

    /* private Vector      currArcIn; */
    /* private Vector      currArcOut; */

    /* valid flags */

    private boolean     valid;

    /* current and old states */

    private int         state;
    private int         oldState;

    /* arc being removed */

    private Arc  removing;

    /* transition id */

    private int         id;

    /* place id label */

    private String      label;


    /*
        PUBLIC METHODS
    */


    /*
        constructor method for adding state from user click
    */

    public Transition (int i, int x, int y)
    {
        /* initialize instance variable */

        id    = i;
        label = new Integer (id) . toString ();

        oldX = originX = x;
        oldY = originY = y;

        colorNormal    = new Color (0, 100, 0);
        colorSelected  = new Color (100, 100, 100);
        colorActive    = new Color (100, 0, 0);
        colorHighlight = new Color (0, 0, 100);

        state = NORMAL;

        removing = (Arc) null;

        valid = true;
	
        arcsIn = new Vector (5, 1);
        arcsOut = new Vector (5, 1);

    } /* end Transition */


    /*
        constructor method for adding transition from file
    */

    public Transition (int i, FileInputStream file)
    {
        /* read data catching I/O error exception */

        try
        {
            /* initialize instance variable */

            id    = i;
            label = new Integer (id) . toString ();

            colorNormal    = new Color (0, 100, 0);
            colorSelected  = new Color (100, 100, 100);
            colorActive    = new Color (100, 0, 0);
            colorHighlight = new Color (0, 0, 100);

            state = NORMAL;

            removing = (Arc) null;

            valid = true;

            arcsIn = new Vector (5, 1);
	    arcsOut = new Vector (5, 1);

            /* read in origin coordinates and initial and accepting states */

            oldX = originX = file . read () | (file . read () << 8);
            oldY = originY = file . read () | (file . read () << 8);

            int val = 0;

            /* while delimiter is not End Of State information */

            while (val != EOT)
            {
                /* read next delimiter */

                val = file . read ();

                /* if delimiter is Beginning Of Arc information */

                if (val == BOA)
                {
                    /* create a new arc, letting its constructor
                       initialize itself from the file */

                    Arc arc = new Arc (this, file);

		    if (arc.toPlace())
                    	arcsOut . addElement ((Object) arc);
		    else
			arcsIn . addElement ((Object) arc);
                }

                /* any delimiter value other than EOT is error */

                else if (val != EOT)
                    return;
            }
        }
        catch (IOException e)
        {
        }

    } /* end Transition */


    /*
        called when user starts dragging the transition. sample the coordinates
        and set transition to MOVING.
    */

    public void dragStart (int x, int y)
    {
        dragX    = x;
        dragY    = y;
        oldState = state;
        state    = MOVING;

    } /* end dragStart */


    /*
        called when user stops dragging the transition. update the position by the
        same amount that user moved the mouse. restore previous state.
    */

    public void dragStop (int x, int y)
    {
        move (originX + (x - dragX), originY + (y - dragY));
        dragX = x;
        dragY = y;
        state = oldState;

    } /* end dragStop */


    /*
        called while user stops drags the transition. update the position by the
        same amount that user moved the mouse.
    */

    public void drag (int x, int y)
    {
        move (originX + (x - dragX), originY + (y - dragY));
        dragX = x;
        dragY = y;

    } /* end drag */


    /*
        draw itself on the specified graphics context.
    */

    public void paint (Graphics g)
    {
        /* remove previous image */

        remove (g);

        /* do not paint the image if transition is not valid */

        if (! valid)
            return;

        /* set color depending on the current state */

        Color   color;

        switch (state)
        {
            case MOVING:
                color = colorSelected;
                break;

            case ACTIVE:
                color = colorActive;
                break;

            case HIGHLIGHTED:
                color = colorHighlight;
                break;

            case NORMAL:
            default:
                color = colorNormal;
                break;
        }

        /* set color and draw the rectangle */

        g . setColor (color);
        g . drawRect (originX, originY, HSIZE, VSIZE);


        /* draw all the arcs that belong to us */

        Arc tmp;

        for (int i = 0; i < arcsOut . size (); i ++)
        {
            tmp = (Arc) (arcsOut . elementAt (i));

            /* depending on the state pick the color (could be different
                than the state color) */

            switch (state)
            {
                case MOVING:
                    color = colorSelected;
                    break;

                case ACTIVE:
                    color = colorActive;
		    state = NORMAL; /* next time transition should be normal */
                case HIGHLIGHTED:
                case NORMAL:
                default:
                    color = colorNormal;
                    break;
            }

            g . setColor (color);

            /* paint arc */

            tmp . paint (g);
        }
        /* draw transition label outside the rectangle on the right */

        g . drawString (label, originX + HSIZE, originY);

    } /* end paint */


    /*
        remove out place from the specified graphics context.
    */

    public void remove (Graphics g)
    {
        /* fill the area occupied by the state with background color */

        g . setColor (Color . lightGray);
        g . fillRect (oldX, oldY, HSIZE, VSIZE);
        g . drawString (label, originX + HSIZE, originY);

        /* remove out arc drawings */

        for (int i = 0; i < arcsOut . size (); i ++)
        {
            Arc tmp = (Arc) (arcsOut . elementAt (i));

            /* clear the arc */

            tmp . remove (g);

            /* if this arc is being removed or the tplace on the other
               side of the arc is removed - delete it */

            if (tmp == removing || ! tmp . placeValid ())
            {
                arcsOut . removeElementAt (i);
                i --;
                removing = (Arc) null;
            }
        }

        /* update previous coordinates */

        oldX = originX;
        oldY = originY;

    } /* end remove */


    /*
        move transition's origin to specified location.
    */

    public void move (int x, int y)
    {
        originX = x;
        originY = y;

    } /* end move */


    /*
        return the bounding rectangle of the transition image.
    */

    public Rectangle bounds ()
    {
        return new Rectangle (originX, originY, HSIZE, VSIZE);

    } /* end bounds */


    /*
        check if the specified coordinate is withing the transition image.
    */

    public boolean inside (int x, int y)
    {
        return (x >= originX && x <= originX + HSIZE &&
                y >= originY && y <= originY + VSIZE);

    } /* end inside */


    /*
        mark transition as invalid
    */

    public void makeInvalid ()
    {
        valid = false;

    } /* end makeInvalid */


    /*
        return the valid flag.
    */

    public boolean valid ()
    {
        return valid;

    } /* end valid */


    /*
        return transition id.
    */

    public int transitionId ()
    {
        return id;

    } /* end transitionId */


    /*
        set the state to highlighted.
    */

    public void highlightOn ()
    {
        state = HIGHLIGHTED;

    } /* end highlighOn */


    /*
        clear the highlighted state (set the state to normal).
    */

    public void highlightOff ()
    {
        state = NORMAL;

    } /* end highlighOn */


    /*
        add new arc from this transition to specified place.
    */

    public void addArcOut (Place place)
    {
        /* see if we already have an arc between these this trans and that place */

        for (int i = 0; i < arcsOut . size (); i ++)
        {
           Arc tmp = (Arc) (arcsOut . elementAt (i));

           /* if one exists - do nothing */
           if (tmp . place () == place)
            {
                return;
            }
        }

        /* create a new arc */

        Arc arc = new Arc (this, place);

        arcsOut . addElement ((Object) arc);
	
    } /* end addArcOut */

    /*
        add specified arc to this transition.
    */

    public void addArcIn (Arc arc)
    {
        arcsIn . addElement ((Object) arc);
	
    } /* end addArcIn */


    /*
        remove arc from this trans to specified place.
    */

    public void removeArcOut (Place place)
    {
        /* find the arc with the specified destination place and mark
           it as being removed. it will be actually removed in the paint()
           call after its image had been removed from the screen. */

        for (int i = 0; i < arcsOut . size (); i ++)
        {
            Arc tmp = (Arc) (arcsOut . elementAt (i));

            if (tmp . place () == place)
            {
                removing = tmp;
                return;
            }
        }

    } /* removeArcOut */

    /*
        remove specified arc to this trans.
    */

    public void removeArcIn (Arc arc)
    {
        /* find this arc and remove it */

        arcsIn.removeElement((Object) arc);

    } /* removeArcIn */


    /*
        is transition active ?
    */

    public boolean isActive()
    {
        /* return true if all places connected to this trans by an in arc has tokens */
	int i;
        for ( i = 0; i < arcsIn . size (); i ++)
        {
            Arc tmp = (Arc) (arcsIn . elementAt (i));

	    /* if at least one place has no tokens then return false */
            if (tmp.place().getTokenNumber() == 0)
            {
                return false;
            }
        }
	return (i != 0); /* return true if there was some arcs connected to this trans */

    } /* isActive */



    /*
        fire transition
	requires transition active (not tested here for efficiency)
    */

    public void fire()
    {
	state = ACTIVE; /* will be reset to NORMAL by paint */

	/* remove one token from all in arc places */
        for (int i = 0; i < arcsIn . size (); i ++)
        {
            Arc tmp = (Arc) (arcsIn . elementAt (i));
	    tmp. place() . removeToken();
        }

	/* add one token to all out arc places */
        for (int i = 0; i < arcsOut . size (); i ++)
        {
            Arc tmp = (Arc) (arcsOut . elementAt (i));
	    tmp. place() . addToken();
        }
    } /* fire */


    /*
        write transition data into a file.
    */

    public boolean saveFile (FileOutputStream file)
    {
        /* write data catching I/O error exception */

        try
        {
            /* write origin coordinates */

            file . write (originX & 0xff);
            file . write (originX >>> 8);
            file . write (originY & 0xff);
            file . write (originY >>> 8);
 
            /* make all of out arcs write their own data
		NOTE : In Arcs will be recreated in Place.resolveId */

            for (int i = 0; i < arcsOut . size (); i ++)
            {
                /* write Beginning Of Arc delimiter */

                file . write (BOA);

                /* make arc write its data */

                if (! ((Arc) (arcsOut . elementAt (i))) . saveFile (file))
                    return false;
            }

            /* write End Of Transition delimiter */

            file . write (EOT);
        }
        catch (IOException e)
        {
            return false;
        }

        return true;

    } /* end saveFile */


    /*
        make all arcs resolve place and transition ids into place and transition objects with the
        specified resolver object.
    */

    public void resolveId (PlaceTransitionIdResolver res)
    {
        Arc tmp;

        for (int i = 0; i < arcsOut . size (); i ++)
        {
            tmp = (Arc) (arcsOut . elementAt (i));

            tmp . resolveId (res);
        }

    } /* end resolveId */


} /* end Transition */


