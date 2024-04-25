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
    PetriNet class - implements Petri Net abstraction - a collection of
    place and transition objects and operations on them.
*/


class PetriNet implements PlaceTransitionIdResolver
{
    /*
        CLASS CONSTANTS
    */


    /*
        return values from some calls
    */


    /* normal status */

    final static int    STATUS_NORMAL            = 1;

    /* file specified for loading was not found */

    final static int    STATUS_FILE_NOT_FOUND    = 2;

    /* file specified for savinging could not be created */

    final static int    STATUS_FILE_CREATE_ERROR = 3;

    /* file read error */

    final static int    STATUS_READ_ERROR        = 4;

    /* file write error */

    final static int    STATUS_WRITE_ERROR       = 5;

    /* input file improperly constructed error */

    final static int    STATUS_BAD_INPUT_FILE    = 6;
    
    /* input file improperly constructed error */

    final static int    STATUS_NO_ACTIVABLE_TRANSITION	 = 7;


    /* maximum number of places */

    final static int    PLACES = 10;

    /* maximum number of transitions */

    final static int    TRANSITIONS = 10;

    /*
        code characters used to mark the file as created by this program
    */

    final static int    CODE1 = (int) 'T';
    final static int    CODE2 = (int) 'P';
    final static int    CODE3 = (int) 'N';
    final static int    CODE4 = (int) 'S';

    /*
        codes characters used to delimit fields in the files
    */

    /* beginning of place data */

    final static int    BOP = 0x7D;

    /* beginning of transition data */

    final static int    BOT = 0x7E;

    /* end of net data (end of file) */

    final static int    EON = 0x7F;


    /*
        INSTANCE VARIABLES
    */


    /* array of place objects */

    private Place       places [];

    /* number of places currently present */

    private int         numPlaces;

    /* array of transition objects */

    private Transition	transitions [];

    /* number of transitions currently present */

    private int         numTransitions;


    /* place we are currently removing */

    private Place       removingPlace;

    /* transition we are currently removing */

    private Transition  removingTransition;

    /* Place of a new arc*/

    private Place       newArcPlace;

    /* Transition of a new arc*/

    private Transition  newArcTransition;

    /* Direction of a new arc and direction set flag */

    public boolean	newArcToPlace;
    public boolean	newArcDirectionSet;

    /* Place being dragged */

    private Place       dragPlace;

    /* Transition being dragged */

    private Transition  dragTransition;

    /* Vector of activable transitions */
    
    private Vector	activableTransitions;

    /* Random number generator used to randomly select a transition */
    
    private Random	wheelOfFortune;


    /*
        PUBLIC METHODS
    */


    /*
        constructor method
    */

    PetriNet ()
    {
        /* initialize instance variables */

        removingPlace = newArcPlace = dragPlace = (Place) null;
        numPlaces = 0;

        places = new Place [PLACES];
        for (int i = 0; i < PLACES; i ++)
            places [i] = (Place) null;

        removingTransition = newArcTransition = dragTransition = (Transition) null;
        numTransitions = 0;

        transitions = new Transition [TRANSITIONS];
        for (int i = 0; i < TRANSITIONS; i ++)
            transitions [i] = (Transition) null;

	activableTransitions = new Vector(TRANSITIONS);
	wheelOfFortune = new Random();

	newArcToPlace = false;
	newArcDirectionSet = false;
    } /* end PetriNet */


    /*
        these routines implements StateIdResolver interface. returns the state
        corresponding to the specified id.
    */

    public Place resolvePlaceId (int id)
    {
        if (id < 0 || id >= PLACES)
            return (Place) null;

        return places [id];

    } /* end resolvePlaceId */

    public Transition resolveTransitionId (int id)
    {
        if (id < 0 || id >= TRANSITIONS)
            return (Transition) null;

        return transitions [id];

    } /* end resolveTransitionId */

    /*
        display state machine on the specified graphics context.
    */

    public synchronized void paint (Graphics g, int offset)
    {
        /* if a place was marked for removal - call its remove method to delete
           its image */

        if (removingPlace != (Place) null)
        {
            removingPlace . remove (g);
            removingPlace = (Place) null;
        }

        /* if a transition was marked for removal - call its remove method to delete
           its image */

        if (removingTransition != (Transition) null)
        {
            removingTransition . remove (g);
            removingTransition = (Transition) null;
        }

        /* paint all existing places on the screen. notice that we traverse
           the array in the reverse order, so that if places overlap on the
           screen, the ones with the lowest ids will end up on top and will
           correspond to the ones that get selected when user clicks on them */

        for (int i = PLACES - 1; i >= 0; i --)
            if (places [i] != (Place) null)
                places [i] . paint (g);

        /* paint all existing transitions on the screen. notice that we traverse
           the array in the reverse order, so that if transitions overlap on the
           screen, the ones with the lowest ids will end up on top and will
           correspond to the ones that get selected when user clicks on them */

        for (int i = TRANSITIONS - 1; i >= 0; i --)
            if (transitions [i] != (Transition) null)
                transitions [i] . paint (g);

    } /* end paint */


    /*
        add a new place at specified coordinates.
    */

    public boolean addPlace (int x, int y)
    {
        /* if maximum number of places already exists - exit */

        if (numPlaces == PLACES)
            return false;

        numPlaces ++;

        int i;

        /* look for the first empty slot in the places array */

        for (i = 0; i < PLACES; i ++)
            if (places [i] == (Place) null)
                break;

        /* create new place */

        places [i] = new Place (i, x, y);

        return true;

    } /* end addPlace */

    /*
        add a new place at specified coordinates.
    */

    public boolean addTransition (int x, int y)
    {
        /* if maximum number of transitions already exists - exit */

        if (numTransitions == TRANSITIONS)
            return false;

        numTransitions ++;

        int i;

        /* look for the first empty slot in the transitions array */

        for (i = 0; i < TRANSITIONS; i ++)
            if (transitions [i] == (Transition) null)
                break;

        /* create new transition */

        transitions [i] = new Transition (i, x, y);

        return true;

    } /* end addTransition */

    /*
        remove place at specified coordinates. if more than one place exist
        at that location, the one with the lowest id will be removed.
    */

    public boolean removePlace (int x, int y)
    {
        /* make sure we have something to remove */

        if (numPlaces == 0)
            return false;

        numPlaces --;

        int i;

        /* find the first place that acknowledges that the point lays inside
           of its space */

        for (i = 0; i < PLACES; i ++)
            if (places [i] != (Place) null && places [i] . inside (x, y))
                break;

        if (i == PLACES)
            return false;

        /* we cannot completely get rid of it yet, as we will have to remove
           the places drawings. this will be done at the next call to paint,
           so just set the removingPlace to places object. */

        removingPlace = places [i];

        /* Invalidate the place. invalidation is needed to remove the arcs
           whose destination this place is. place knows only of arcs
           starting with it, but it cannot invalidate the ones ending at it.
           arcs will check if their traget state is valid during update
           calls and invalidate themselves if necessary. */

        removingPlace . makeInvalid ();

        places [i] = (Place) null;

        return true;

    } /* end removePlace */


    /*
        remove transition at specified coordinates. if more than one transition exist
        at that location, the one with the lowest id will be removed.
    */

    public boolean removeTransition (int x, int y)
    {
        /* make sure we have something to remove */

        if (numTransitions == 0)
            return false;

        numTransitions --;

        int i;

        /* find the first transition that acknowledges that the point lays inside
           of its space */

        for (i = 0; i < TRANSITIONS; i ++)
            if (transitions [i] != (Transition) null && transitions [i] . inside (x, y))
                break;

        if (i == TRANSITIONS)
            return false;

        /* we cannot completely get rid of it yet, as we will have to remove
           the transitions drawings. this will be done at the next call to paint,
           so just set the removingTransition to transitions object. */

        removingTransition = transitions [i];

        /* Invalidate the transition. invalidation is needed to remove the arcs
           whose destination this transition is. transition knows only of arcs
           starting with it, but it cannot invalidate the ones ending at it.
           arcs will check if their traget state is valid during update
           calls and invalidate themselves if necessary. */

        removingTransition . makeInvalid ();

        transitions [i] = (Transition) null;

        return true;

    } /* end removeTransition */

    /*
        add token to place at specified coordinates. if more than one place exist
        at that location, the one with the lowest id will have the token.
    */

    public boolean addTokenToPlaceAt (int x, int y)
    {
        int i;

        /* find the first place that acknowledges that the point lays inside
           of its space */

        for (i = 0; i < PLACES; i ++)
            if (places [i] != (Place) null && places [i] . inside (x, y))
                break;

        if (i == PLACES)
            return false;

        /* add a token to the found place */

        places [i] . addToken();

        return true;

    } /* end addTokenToPlaceAt */

    /*
        add token to place at specified coordinates. if more than one place exist
        at that location, the one with the lowest id will have the token.
    */

    public boolean removeTokenFromPlaceAt (int x, int y)
    {
        int i;

        /* find the first place that acknowledges that the point lays inside
           of its space */

        for (i = 0; i < PLACES; i ++)
            if (places [i] != (Place) null && places [i] . inside (x, y))
                break;

        if (i == PLACES)
            return false;

        /* remove a token from the found place returns false if no token in that place*/

        return places [i] . removeToken();

    } /* end removeTokenFromPlaceAt */

    /*
        select a place for adding an arc. if more than one place
        exist at that location, the one with the lowest id will be marked.
    */

    public boolean selectNewArcPlace (int x, int y)
    {
        newArcPlace = locatePlace (x, y);

        if (newArcPlace == (Place) null)
            return false;

        /* highlight the place and update the hint line */

        newArcPlace . highlightOn ();
	
	if (! newArcDirectionSet) {
	    newArcToPlace = false;
	    newArcDirectionSet = true;
	}

        return true;

    } /* end selectNewArcPlace */


    /*
        select a transition for adding an arc. if more than one transition
        exist at that location, the one with the lowest id will be marked.
    */

    public boolean selectNewArcTransition (int x, int y)
    {
        newArcTransition = locateTransition (x, y);

        if (newArcTransition == (Transition) null)
            return false;

        /* highlight the Transition and update the hint line */

        newArcTransition . highlightOn ();

	if (! newArcDirectionSet) {
	    newArcToPlace = true;
	    newArcDirectionSet = true;
	}

        return true;

    } /* end selectNewArcTransition */


    /*
        add arc between the specified place and transition.
    */

    public void addArc ()
    {
	if (newArcPlace != (Place) null && newArcTransition != (Transition) null)
	{
	    if (newArcToPlace)
			newArcTransition . addArcOut (newArcPlace);
	    else	newArcPlace . addArc (newArcTransition);
	    newArcPlace = (Place) null;
	    newArcTransition = (Transition) null;
	}
	newArcDirectionSet = false;
    } /* end addArc */


    /*
        remove arc between the specified place and transition.
    */

    public void removeArc ()
    {
	if (newArcPlace != (Place) null && newArcTransition != (Transition) null)
	{
	    if (newArcToPlace)
			newArcTransition . removeArcOut (newArcPlace);
	    else	newArcPlace . removeArc (newArcTransition);
	    newArcPlace = (Place) null;
	    newArcTransition = (Transition) null;
	}
	newArcDirectionSet = false;

    } /* end removeArc */


    /*
        select the place or transition for dragging. if more than one state have these
        coordinates, the one with the lowest id will be selected.
    */

    public boolean selectDrag (int x, int y)
    {
        dragPlace = locatePlace (x, y);
        if (dragPlace == (Place) null) {
	    dragTransition = locateTransition (x, y);
	    if (dragTransition == (Transition) null) {
		return false;
	    }
	    else {
		dragTransition . dragStart (x, y);
		return true;
	    }
	}
	else {
	    dragPlace . dragStart (x, y);
	    return true;
	}
    } /* end selectDrag */


    /*
        stop dragging the current place or transition.
    */
    public void deselectDrag (int x, int y)
    {
        if (dragPlace != (Place) null)
	    dragPlace . dragStop (x, y);
	else if (dragTransition != (Transition) null)
	    dragTransition . dragStop (x, y);

	dragPlace = (Place) null;
	dragTransition = (Transition) null;
    } /* end deselectDrag */


    /*
        drag the current drag state.
    */

    public void drag (int x, int y)
    {
        if (dragPlace != (Place) null)
	    dragPlace . drag (x, y);
	else if (dragTransition != (Transition) null)
	    dragTransition . drag (x, y);
    } /* end dragState */


    /*
        make sure simulation can be started (necessary conditions exist), stop
        the previous one if necessary.
    */

    public int startSimulation ()
    {
        /* stop previous simulation */

        stopSimulation ();

        return STATUS_NORMAL;

    } /* end startSimulation */


    /*
        stop simulation in progress.
    */

    public void stopSimulation ()
    {

    } /* end stopSimulation */


    /*
        run one round of simulation.
    */


    public int runSimulation ()
    {
	/* first, empty the set of activable transitions */

	activableTransitions.setSize(0);

	/* compute the set of activable transition */

	for (int i = 0; i < TRANSITIONS && i < numTransitions; i ++) {
	    if (transitions [i] == (Transition) null)
		continue;
	    if (transitions [i] . isActive())
		activableTransitions.addElement((Object) (transitions[i]));
	}

	if ( activableTransitions.size() == 0 )
	    return STATUS_NO_ACTIVABLE_TRANSITION;
	/* choose a random transition */
	int transitionNumber = (int) (wheelOfFortune.nextFloat() * activableTransitions.size());

	/* fire the transition */
	Transition toFire = (Transition) activableTransitions.elementAt(transitionNumber);
	toFire . fire();
	
        return STATUS_NORMAL;

    } /* end runSimulation */


    /*
        load new state machine from the specified file.
    */

    public synchronized int loadFile (String fileName)
    {
        FileInputStream file;

        /* open the file, catching the file not found exception */

        try
        {
            file = new FileInputStream (fileName);
        }
        catch (FileNotFoundException e)
        {
            return STATUS_FILE_NOT_FOUND;
        }

        /* read from the file catching the I/O error exception */

        try
        {
            /* make sure the file was created by this program */

            if (file . read () != CODE1 || file . read () != CODE2 ||
                file . read () != CODE3 || file . read () != CODE4)
            {
                return STATUS_BAD_INPUT_FILE;
            }

            int val = 0;

            /* while delimiter is not End Of Network - keep reading */

            while (val != EON)
            {
                /* read the next delimiter */

                val = file . read ();

                /* if the delimiter marks Beginning Of Place information */

                if (val == BOP)
                {
                    /* read the byte encoding place's id */

                    val = file . read ();

                    if (val < 0 || val >= PLACES)
                        return STATUS_BAD_INPUT_FILE;

                    /* create new state with this id. call the constructor
                       that will initialize the state with the information
                       from the file. */

                    places [val] = new Place (val, file);
                }

                /* if the delimiter marks Beginning Of Input string */

                else if (val == BOT)
                {
                    /* read the byte encoding place's id */

                    val = file . read ();

                    if (val < 0 || val >= TRANSITIONS)
                        return STATUS_BAD_INPUT_FILE;

                    /* create new state with this id. call the constructor
                       that will initialize the state with the information
                       from the file. */

                    transitions [val] = new Transition (val, file);
                }

                /* any delimiter other than EON means garbage */

                else if (val != EON)
                    return STATUS_BAD_INPUT_FILE;
            }

            /* resolve place and transition IDs in arcs. transitions got initialized
               with state IDs, not objects since some of them were read from the
               file before the corresponding states. this step will resovle
              that. */

            numPlaces       = 0;

            for (int i = 0; i < PLACES; i ++)
            {
                if (places [i] != (Place) null)
                {
                    numPlaces ++;

                    places [i] . resolveId ((PlaceTransitionIdResolver) this);
                }
            }

            numTransitions       = 0;

            for (int i = 0; i < TRANSITIONS; i ++)
            {
                if (transitions [i] != (Transition) null)
                {
                    numTransitions ++;

                    transitions [i] . resolveId ((PlaceTransitionIdResolver) this);
                }
            }

        }
        catch (IOException e)
        {
            return STATUS_READ_ERROR;
        }
        return STATUS_NORMAL;

    } /* end loadFile */


    /*
        save current state machine data into specified file.
    */

    public int saveFile (String fileName)
    {
        FileOutputStream file;

        /* open the output file catching the I/O error exception. */

        try
        {
            file = new FileOutputStream (fileName);
        }
        catch (IOException e)
        {
            return STATUS_FILE_CREATE_ERROR;
        }

        /* write to file catching the I/O error exception. */

        try
        {
            /* write out file signature */

            file . write (CODE1);
            file . write (CODE2);
            file . write (CODE3);
            file . write (CODE4);

            /* write out place information */

            for (int i = 0; i < PLACES; i ++)
            {
                if (places [i] == (Place) null)
                    continue;

                /* write Beginning Of Place delimiter and state id */

                file . write (BOP);
                file . write (i);

                /* make state write its own information, throw I/O exception
                   if the operation failed */

                if (! places [i] . saveFile (file))
                    throw new IOException ();
            }

            /* write out transition information */

            for (int i = 0; i < TRANSITIONS; i ++)
            {
                if (transitions [i] == (Transition) null)
                    continue;

                /* write Beginning Of Transition delimiter and state id */

                file . write (BOT);
                file . write (i);

                /* make state write its own information, throw I/O exception
                   if the operation failed */

                if (! transitions [i] . saveFile (file))
                    throw new IOException ();
            }

            /* write End Of Network delimiter */

            file . write (EON);
        }
        catch (IOException e)
        {
            return STATUS_WRITE_ERROR;
        }

        return STATUS_NORMAL;

    } /* end saveFile */


    /*
        PRIVATE METHODS
    */


    /*
        return place object at the specified location. if more that one place
        are located in the same space, return the one with the lowest id.
    */

    private Place locatePlace (int x, int y)
    {
        for (int i = 0; i < PLACES; i ++)
            if (places [i] != (Place) null && places [i] . inside (x, y))

                return places [i];

        return (Place) null;

    } /* end locatePlace */

    /*
        return transition object at the specified location. if more that one trans
        are located in the same space, return the one with the lowest id.
    */

    private Transition locateTransition (int x, int y)
    {
        for (int i = 0; i < TRANSITIONS; i ++)
            if (transitions [i] != (Transition) null && transitions [i] . inside (x, y))

                return transitions [i];

        return (Transition) null;

    } /* end locateTransition */


} /* end PetriNet */


