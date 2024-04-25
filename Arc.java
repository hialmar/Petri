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
    Arc class - implements arcs. It knows how to draw
    itself on the screen, how to read and write itself to/from a file. It knows
    what place and what transition are at its ends
*/


class Arc
{
    /*
        INSTANCE VARIABLES
    */

    /* direction of the arc : place to transition or transition to place */

    private boolean         _toPlace;

    /* source and destination states of the transition */

    private Place           _place;
    private Transition      _transition;

    /* destination object id, when destination object is not known */

    private int             dstId;

    /*
        coordinates for drawing arc image on the screen
    */

    /* start and end coordinates */

    private int             x1, x2, y1, y2;

    /* label coordinates */

    private int             xs, ys;

    /* arrow top, first and second lines coordinates */

    private int             xat, xa1, xa2, yat, ya1, ya2;


    /*
        PUBLIC METHODS
    */


    /*
        constructor method for adding arc from user click : place -> transition
    */

    public Arc (Place pla, Transition trans)
    {
        /* initialize instance variables */

	_toPlace 	= false;
        _place	 	= pla;
	_transition 	= trans;

        dstId = trans.transitionId();

        x1 = x2 = y1 = y2 = xs = ys = xat = xa1 = xa2 = yat = ya1 = ya2 = 0;

    } /* end Arc */

    /*
        constructor method for adding arc from user click : transition -> place
    */

    public Arc (Transition trans, Place pla)
    {
        /* initialize instance variables */

	_toPlace 	= true;
        _place	 	= pla;
	_transition 	= trans;

        dstId = pla.placeId();

        x1 = x2 = y1 = y2 = xs = ys = xat = xa1 = xa2 = yat = ya1 = ya2 = 0;

    } /* end Arc */

    /*
        constructor method for adding arc (from place to trans) from file
    */

    public Arc (Place pla, FileInputStream file)
    {
        /* read data catching I/O error exception */

        try
        {
            /* initialize instance variables */

	    _toPlace = false;
            _place = pla;
            _transition = (Transition) null;

            x1 = x2 = y1 = y2 = xs = ys = xat = xa1 = xa2 = yat = ya1 = ya2 = 0;

            /* read destination state id */

            dstId = file . read ();

        }
        catch (IOException e)
        {
        }

    } /* end Arc */

    /*
        constructor method for adding arc (from trans to place) from file
    */

    public Arc (Transition trans, FileInputStream file)
    {
        /* read data catching I/O error exception */

        try
        {
            /* initialize instance variables */

	    _toPlace = true;
            _place = (Place) null;
            _transition = trans;

            x1 = x2 = y1 = y2 = xs = ys = xat = xa1 = xa2 = yat = ya1 = ya2 = 0;

            /* read destination state id */

            dstId = file . read ();

        }
        catch (IOException e)
        {
        }

    } /* end Arc */


    /*
        checks if the arc place is valid.
    */

    public boolean placeValid ()
    {
        return (_place != (Place) null && _place . valid ());

    } /* end placeValid */


    /*
        checks if the arc transition is valid.
    */

    public boolean transitionValid ()
    {
        return (_transition != (Transition) null && _transition . valid ());

    } /* end transitionValid */

    /*
        draw this arc image on the specified graphics context.
        actually just calculate the coordinates and call draw() method to
        do the actual drawing.
    */

    public void paint (Graphics g)
    {
        /* get source bounding rectangle */
	Rectangle start;
	Rectangle end;

	if (_toPlace)
        	start = _transition . bounds ();
	else	start = _place . bounds ();

        /* figure out the
           coordinates depending on their relative position */

	/* get destination bounding rectangle */

	if (_toPlace)
		end = _place . bounds ();
	else    end = _transition . bounds ();

	/* figure out the distances between the two objects */

	int dx = Math . abs (end . x - start . x);
	int dy = Math . abs (end . y - start . y);


	/* destination is between -45 and 45 degrees of arc, if source
	   is considered the center of the coordinate system.

	      /
	    S -> D
	      \
	*/

	if (end . x >= start . x + start . width && dx >= dy)
	{
	    x1 = start . x + start . width;
	    y1 = start . y + start . height / 4 * 3;

	    x2 = end . x;
	    y2 = end . y + end . height / 4 * 3;

	    xa1 = x2 - 5;
	    ya1 = y2 - 5;

	    xa2 = x2 - 5;
	    ya2 = y2 + 5;

	    xs = x1 + (x2 - x1) / 4;
	    ys = y1 + (y2 - y1) / 4;
	}

	/* destination is between 45 and 135 degrees of arc, if source
	   is considered the center of the coordinate system.

	      D
	      ^
	     \|/
	      S
	*/

	else if (start . y >= end . y + end . height && dy >= dx)
	{
	    x1 = start . x + start . width / 4 * 3;
	    y1 = start . y;

	    x2 = end . x + end . width / 4 * 3;
	    y2 = end . y + end . height;

	    xa1 = x2 + 5;
	    ya1 = y2 + 5;

	    xa2 = x2 - 5;
	    ya2 = y2 + 5;

	    xs = x1 + (x2 - x1) / 2;
	    ys = y1 + (y2 - y1) / 2;
	}

	/* destination is between 135 and 225 degrees of arc, if source
	   is considered the center of the coordinate system.

	      \
	   D <- S
	      /
	*/

	else if (start . x > end . x + end . width && dx > dy)
	{
	    x1 = start . x;
	    y1 = start . y + start . height / 4;

	    x2 = end . x + end . width;
	    y2 = end . y + end . height / 4;

	    xa1 = x2 + 5;
	    ya1 = y2 + 5;

	    xa2 = x2 + 5;
	    ya2 = y2 - 5;

	    xs = x1 + (x2 - x1) / 4 * 3;
	    ys = y1 + (y2 - y1) / 4 * 3;
	}

	/* destination is between 225 and 315 degrees of arc, if source
	   is considered the center of the coordinate system.

	      S
	     /|\
	      v
	      D
	*/

	else if (end . y > start . y + start . height && dy > dx)
	{
	    x1 = start . x + start . width / 4;
	    y1 = start . y + start . height;

	    x2 = end . x + end . width / 4;
	    y2 = end . y;

	    xa1 = x2 - 5;
	    ya1 = y2 - 5;

	    xa2 = x2 + 5;
	    ya2 = y2 - 5;

	    xs = x1 + (x2 - x1) / 2;
	    ys = y1 + (y2 - y1) / 2;

	}

	/* they must be overlapping - do not draw the transition */

	else
	    x1 = x2 = y1 = y2 = xa1 = xa2 = ya1 = ya2 = 0;

	/* arrow head top is by the destination coordinate */

	xat = x2;
	yat = y2;

        /* now draw the arc */

        draw (g);

    } /* end paint */


    /*
        draw the arc at its old coordinates, without recalculating.
        the color is preset in state's paint method.
    */

    public void remove (Graphics g)
    {
        draw (g);

    } /* end remove */


    /*
        draw the arc image with the current coordinates.
    */

    public void draw (Graphics g)
    {
        /* draw the main transition line */

        g . drawLine (x1, y1, x2, y2);

        /* draw the arrow head */

        g . drawLine (xat, yat, xa1, ya1);
        g . drawLine (xat, yat, xa2, ya2);

    } /* draw */

    /*
        return the direction of this arc.
    */

    public boolean toPlace()
    {
        return _toPlace;

    } /* end toPlace */

    /*
        return the place for this arc.
    */

    public Place place ()
    {
        return _place;

    } /* end place */

    /*
        return the transition for this arc.
    */

    public Transition transition ()
    {
        return _transition;

    } /* end transition */

    /*
        write the transition data to file.
    */

    public boolean saveFile (FileOutputStream file)
    {
        /* write the data catching the I/O error exception */

        try
        {
            /* write destination state id */

            file . write (dstId);

        }
        catch (IOException e)
        {
            return false;
        }

        return true;

    } /* end saveFile */


    /*
        resolve the destination state id into the actual object. this is done
        after reading transitions in from files. call the specified resolver
        object with the id.
    */

    public void resolveId (PlaceTransitionIdResolver res)
    {
	if (_toPlace) {
	    if (_place == (Place) null)
		_place = (Place) res . resolvePlaceId (dstId);
	}
	else {
	    if (_transition == (Transition) null)
		_transition = (Transition) res . resolveTransitionId (dstId);
	}
    } /* end resolveId */

} /* end Arc */

