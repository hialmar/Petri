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
    NoTokenInPlaceException class - exception which is raised in Place::removeToken method
    when no more token are left in the place
class NoTokenInPlaceException extends RuntimeException
{

}
*/

/*
    Place class - implements all the logic for the place. It knows how to draw
    itself on the screen, how to read and write itself to/from a file. Also
    maintains all the arcs and all the transitions initiating from it.
*/


class Place
{
    /*
        CLASS CONSTANTS
    */

    /*
        current state of the Place
    */

    final static int    NORMAL      = 0;
    final static int    MOVING      = 1;
    final static int    ACTIVE      = 2;
    final static int    HIGHLIGHTED = 3;

    /* dimensions on the screen in pixels */

    final static int    SIZE = 50;

    /* token dimensions on the screen in pixels */

    final static int    TOKENSIZE = 10;

    /* delimiters used to distinguish records in files */

    final static int    BOA = 0x70;
    final static int    EOP = 0x71;


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

    /* a growing array of arcs */

    private Vector      arcs;

    /* valid flags */

    private boolean     valid;


    /* current number of tokens in place */
    
    private int		tokenNumber;

    /* current and old states */

    private int         state;
    private int         oldState;

    /* arc being removed */

    private Arc  	removing;

    /* place id */

    private int         id;

    /* place id label */

    private String      label;


    /*
        PUBLIC METHODS
    */


    /*
        constructor method for adding state from user click
    */

    public Place (int i, int x, int y)
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
	
	tokenNumber = 0;

        arcs = new Vector (5, 1);

    } /* end Place */


    /*
        constructor method for adding place from file
    */

    public Place (int i, FileInputStream file)
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

	    tokenNumber = 0;

            arcs = new Vector (5, 1);

            /* read in origin coordinates and initial and accepting states */

            oldX = originX = file . read () | (file . read () << 8);
            oldY = originY = file . read () | (file . read () << 8);
 	    tokenNumber	   = file . read () | (file . read () << 8);


            int val = 0;

            /* while delimiter is not End Of Place information */

            while (val != EOP)
            {
                /* read next delimiter */

                val = file . read ();

                /* if delimiter is Beginning Of Transition information */

                if (val == BOA)
                {
                    /* create a new arc, letting its constructor
                       initialize itself from the file */

                    Arc arc = new Arc (this, file);

                    arcs . addElement ((Object) arc);
                }

                /* any delimiter value other than EOP is error */

                else if (val != EOP)
                    return;
            }
        }
        catch (IOException e)
        {
        }

    } /* end Place */





    /*
        called when user starts dragging the place. sample the coordinates
        and set place to MOVING.
    */

    public void dragStart (int x, int y)
    {
        dragX    = x;
        dragY    = y;
        oldState = state;
        state    = MOVING;

    } /* end dragStart */


    /*
        called when user stops dragging the place. update the position by the
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
        called while user stops drags the place. update the position by the
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

        /* do not paint the image if place is not valid */

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

        /* set color and draw the circle */

        g . setColor (color);
        g . drawOval (originX, originY, SIZE, SIZE);

        /* draw tokens that belong to us */

	if (tokenNumber > 0) {
	    g . drawOval (originX + SIZE/2 - TOKENSIZE, originY + SIZE/2 - TOKENSIZE*2, TOKENSIZE, TOKENSIZE);
	    if (tokenNumber > 1) {
		g . drawString ((new Integer (tokenNumber)) . toString (), originX + SIZE / 2, originY + SIZE / 2);
	    }
	}

        /* draw all the arcs that belong to us */

        Arc tmp;

        for (int i = 0; i < arcs . size (); i ++)
        {
            tmp = (Arc) (arcs . elementAt (i));

            /* depending on the state pick the color (could be different
                than the state color) */

            switch (state)
            {
                case MOVING:
                    color = colorSelected;
                    break;

                case ACTIVE:
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

        /* draw place label outside the circle on the right and on the upper*/

        g . drawString (label, originX + SIZE, originY + SIZE - g . getFontMetrics() . getHeight());

    } /* end paint */


    /*
        remove out place from the specified graphics context.
    */

    public void remove (Graphics g)
    {
        /* fill the area occupied by the state with background color */

        g . setColor (Color . lightGray);
        g . fillRect (oldX, oldY, SIZE, SIZE);
        g . drawString (label, originX + SIZE, originY + SIZE - g . getFontMetrics() . getHeight());

        /* remove arc drawings */

        for (int i = 0; i < arcs . size (); i ++)
        {
            Arc tmp = (Arc) (arcs . elementAt (i));

            /* clear the arc */

            tmp . remove (g);

            /* if this arc is being removed or the transition on the other
               side of the arc is removed - delete it */

            if (tmp == removing || ! tmp . transitionValid ())
            {
                arcs . removeElementAt (i);
                i --;
                removing = (Arc) null;
            }
        }

        /* update previous coordinates */

        oldX = originX;
        oldY = originY;

    } /* end remove */


    /*
        move place's origin to specified location.
    */

    public void move (int x, int y)
    {
        originX = x;
        originY = y;

    } /* end move */


    /*
        return the bounding rectangle of the place image.
    */

    public Rectangle bounds ()
    {
        return new Rectangle (originX, originY, SIZE, SIZE);

    } /* end bounds */


    /*
        check if the specified coordinate is withing the place image.
    */

    public boolean inside (int x, int y)
    {
        return (x >= originX && x <= originX + SIZE &&
                y >= originY && y <= originY + SIZE);

    } /* end inside */


    /*
        mark place as invalid
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
        return place id.
    */

    public int placeId ()
    {
        return id;

    } /* end placeId */


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
        add new arc from this place to specified transition.
    */

    public void addArc (Transition trans)
    {
        /* see if we already have an arc between these this place and that trans */

        for (int i = 0; i < arcs . size (); i ++)
        {
           Arc tmp = (Arc) (arcs . elementAt (i));

            /* if one exists - do nothing */
           if (tmp . transition () == trans)
            {
                return;
            }
        }

        /* create a new arc */

        Arc arc = new Arc (this, trans);

        arcs . addElement ((Object) arc);
	
	/* let the trans know a new arc to it has been created */
	trans . addArcIn (arc);

    } /* end addArc */


    /*
        remove arc from this place to specified trans.
    */

    public void removeArc (Transition trans)
    {
        /* find the arc with the specified destination trans and mark
           it as being removed. it will be actually removed in the paint()
           call after its image had been removed from the screen. */

        for (int i = 0; i < arcs . size (); i ++)
        {
            Arc tmp = (Arc) (arcs . elementAt (i));

            if (tmp . transition () == trans)
            {
                removing = tmp;

		/* remove it from its transition set of in arc */
		removing.transition().removeArcIn(removing);
                return;
            }
        }

    } /* removeArc */


    /*
        Add a token
    */

    public void addToken ()
    {
	tokenNumber ++;
    } /* end addToken */

    /*
        Remove a token
    */

    public boolean removeToken ()
    {
	if (tokenNumber > 0) {
	    tokenNumber --;
	    return true;
	}
	else
	    return false;
    } /* end removeToken */


    /*
        get number of tokens
    */

    public int getTokenNumber ()
    {
	    return tokenNumber;
    } /* end removeToken */


    /*
        write placee data into a file.
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

            /* write token number */

            file . write (tokenNumber & 0xff);
            file . write (tokenNumber >>> 8);
 
            /* make all of out arcs write their own data */

            for (int i = 0; i < arcs . size (); i ++)
            {
                /* write Beginning Of Arc delimiter */

                file . write (BOA);

                /* make arc write its data */

                if (! ((Arc) (arcs . elementAt (i))) . saveFile (file))
                    return false;
            }

            /* write End Of Place delimiter */

            file . write (EOP);
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

        for (int i = 0; i < arcs . size (); i ++)
        {
            tmp = (Arc) (arcs . elementAt (i));

            tmp . resolveId (res);

	    /* Now, let the transition know a new arc to it has been created */
	    tmp . transition() . addArcIn (tmp);

        }

    } /* end resolveId */


} /* end Place */


