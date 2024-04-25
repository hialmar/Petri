/*
    Petri Net Simulator

    Author:     Torguet Patrice 
    Version:    1.0
    Date:       3/25/95

    Notes: based on Kyril Faenov's Finite State Machine Simulator

    Todo:

*/


/*
    IMPORTS
*/


import java.awt.*;
import java.lang.*;




/*
    INTERFACES
*/


/*
    PlaceTransitionIdResolver interface - classes complying to this interface must
    implement resolveId routine, which finds state object based on specified
    id.
*/


interface PlaceTransitionIdResolver
{
    /*
        return place object with specified id.
    */

    Place resolvePlaceId (int id);

    /*
        return transition object with specified id.
    */

    Transition resolveTransitionId (int id);

} /* end PlaceTransitionIdResolver */


/*
    PUBLIC CLASSES
*/


/*
    Simulator class - this is the top-level applet class. It does all of
    the initialization, event processing, re-paint initiation and dialog
    window management.
*/


public class Petri extends Panel
{
    /*
        CLASS CONSTANTS
    */


    /*
        constants secifying what to do on the next mouse click
    */

    /* regular state */

    final static int    CLICK_NORMAL                  = 0;

    /* adding new place */

    final static int    CLICK_PLACE_ADD               = 1;

    /* deleting place */

    final static int    CLICK_PLACE_REMOVE            = 2;

    /* adding new trnasition */

    final static int    CLICK_TRANSITION_ADD          = 3;

    /* removing transition */

    final static int    CLICK_TRANSITION_REMOVE       = 4;

    /* selecting a place/trans for new arc */

    final static int    CLICK_START_NEW_ARC           = 5;

    /* selecting a place for new arc end */

    final static int    CLICK_PLACE_END_NEW_ARC       = 6;

    /* selecting a trans for new arc end */

    final static int    CLICK_TRANS_END_NEW_ARC       = 7;

    /* running simulation */

    final static int    CLICK_RUN_SIMULATION          = 8;

    /* selecting place/trans of the arc to be removed */

    final static int    CLICK_START_REMOVE_ARC        = 9;

    /* selecting place at the end of arc to be removed */

    final static int    CLICK_PLACE_END_REMOVE_ARC    = 10;

    /* selecting trans at the end of arc to be removed */

    final static int    CLICK_TRANS_END_REMOVE_ARC    = 11;

    /* adding token to a place */

    final static int    CLICK_ADD_TOKEN               = 12;

    /* removing token from a place */

    final static int    CLICK_REMOVE_TOKEN            = 13;

    /* applet's dimensions */

    final static int    DIMENSION   = 500;

    /* height of the button control area */

    final static int    CONTROL_HEIGHT = 75;


    /*
        INSTANCE VARIABLES
    */


    /* petri network */

    private PetriNet    network;

    /* hint line support module */

    private Help        help;

    /* specifies what to do on the next mouse click (see the constants above) */

    private int         clickState;

    /* if set - clear the application panel on the next re-draw */

    private boolean     clearAll;


    /* if set - draggin of a place/trans is in progress */

    private boolean     dragging;

    /*
        sub-dialog classes
    */

    private QuitDialog  quitDialog;
    private AboutBox    aboutBox;
    private FileInput   fileInput;
    private Panel       buttons;


    /*
        PUBLIC METHODS
    */


    /*
        called when applet is being initialized
    */

    public void init ()
    {
        /* initialize instance variables */

        clickState = 0;
        clearAll   = true;
        dragging   = false;

        /* create help and network class instances */

        help    = new Help ();
        network = new PetriNet ();

        /* set applet size */

        resize (DIMENSION, DIMENSION);

        /* create button bar panel */

        buttons = new Panel ();

        /* set layout within the panel to 5 elements per row */

        buttons . setLayout (new GridLayout (0, 5));

        /* add all buttons */

        buttons . add (new Button ("New"));
        buttons . add (new Button ("Load"));
        buttons . add (new Button ("Save"));
        buttons . add (new Button ("About"));
        buttons . add (new Button ("Quit"));

        buttons . add (new Button ("Add Place"));
        buttons . add (new Button ("Add Token"));
        buttons . add (new Button ("Add Transition"));
        buttons . add (new Button ("Add Arc"));
        buttons . add (new Button ("Run"));

        buttons . add (new Button ("Del Place"));
        buttons . add (new Button ("Del Token"));
        buttons . add (new Button ("Del Transition"));
        buttons . add (new Button ("Del Arc"));
        buttons . add (new Button ("Stop"));

        /* add button panel to applet panel */

        add (buttons);

        /* resize bar panel and position it at the top of the applet */

        buttons . resize (DIMENSION, CONTROL_HEIGHT);
        buttons . move (0, 0);
        buttons . show ();

        /* create dialog panels, add them to applet panel and resize to
           desired dimensions. for some reason resizing within the dialog
           itself seems to have no effect. i guess i am missing something
           here. note that dialogs are initialized hidden and will not show
           up until show() method is invoked on them. */

        aboutBox       = new AboutBox (this);
        quitDialog     = new QuitDialog (this);
        fileInput      = new FileInput (this);

        add (aboutBox);
        add (quitDialog);
        add (fileInput);

        aboutBox       . resize (350, 120);
        quitDialog     . resize (100, 75);
        fileInput      . resize (400, 120);

        Rectangle bounds = bounds();
        Rectangle abounds;

        abounds = quitDialog . bounds ();
        quitDialog     . move (bounds . x + (bounds . width  - abounds . width) / 2,
                               bounds . y + (bounds . height - abounds . height) / 2);

        abounds = aboutBox . bounds();
        aboutBox       . move (bounds . x + (bounds . width  - abounds . width) / 2,
                               bounds . y + (bounds . height - abounds . height) / 2);

        abounds = fileInput . bounds();
        fileInput      . move (bounds . x + (bounds . width  - abounds . width) / 2,
                               bounds . y + (bounds . height - abounds . height) / 2);


        /* let the dialogs know about the network - they will have
           to communicate user commands and input to it */

        fileInput      . setPetriNet (network, help);

        /* display initial message on the hint line */

        help . setHelp (help . INITIAL);

        /* make applet show now */

        show ();

    } /* end init */


    /*
        override the layout method with the one that does nothing. we do not
        need layout for the applet panel since all the positioning being done
        manually.
    */

    public synchronized void layout ()
    {

    } /* end layout */


    /*
        this method is called when repaint of the panle is requested
    */

    public void paint (Graphics g)
    {
        Rectangle size = bounds ();

        /* if cleanup was requested - clear the entire panel */

        if (clearAll)
        {
            clearAll = false;

            g . setColor (Color . lightGray);
            g . fillRect (0, 0, size . width, size . height);
        }

        /* repaint the components */

        buttons . paint (g);
        network . paint (g, CONTROL_HEIGHT + 5);
        help    . paint (g, size . width, size . height);

    } /* end paint */


    /*
        this method handles all of the the events for the applet
    */

    public boolean handleEvent (Event evt)
    {
        switch (evt . id)
        {

            case Event . MOUSE_DOWN:

                switch (clickState)
                {
                    case CLICK_PLACE_ADD:

                        /* tell network to create a new place at the location
                           of the mouse click */

                        if (! network . addPlace (evt . x, evt . y))
                            help . setHelp (help . MAX_PLACES);
                        else
                            help . setHelp (help . EMPTY);

                        break;

                    case CLICK_PLACE_REMOVE:

                        /* tell network to remove a place at the location
                           of the mouse click */

                        if (! network . removePlace (evt . x, evt . y))
                            help . setHelp (help . NO_PLACE);

                        break;

                    case CLICK_TRANSITION_ADD:

                        /* tell network to create a new place at the location
                           of the mouse click */

                        if (! network . addTransition (evt . x, evt . y))
                            help . setHelp (help . MAX_TRANSITIONS);
                        else
                            help . setHelp (help . EMPTY);

                        break;

                    case CLICK_TRANSITION_REMOVE:

                        /* tell network to remove a place at the location
                           of the mouse click */

                        if (! network . removeTransition (evt . x, evt . y))
                            help . setHelp (help . NO_TRANSITION);

                        break;

                    case CLICK_START_NEW_ARC:

                        /* tell network to mark a place/trans at the location
                           of the mouse click as place for the upcoming
                           arc */

                        if ( network . selectNewArcPlace (evt . x, evt . y)) {
			    help . setHelp (help . SELECT_TRANSITION);
			    
			    clickState = CLICK_TRANS_END_NEW_ARC;
			}
			else
                        {
			    if ( network . selectNewArcTransition (evt . x, evt . y)) {
				help . setHelp (help . SELECT_PLACE);
				
				clickState = CLICK_PLACE_END_NEW_ARC;
			    } else
			    {
				help . setHelp (help . NO_OBJECT_HERE);
				break;
			    }
                        }

                        repaint ();

                        return true;

                    case CLICK_TRANS_END_NEW_ARC:

                        /* tell network to mark a trans at the location
                           of the mouse click as end for the upcoming
                           arc */

                        if (! network . selectNewArcTransition (evt . x, evt . y))
                        {
                            help . setHelp (help . NO_TRANSITION);
                            break;
                        }

                        help . setHelp (help . EMPTY);

                        /* add the arc */

                        network.addArc();

                        break;

                    case CLICK_PLACE_END_NEW_ARC:

                        /* tell network to mark a place at the location
                           of the mouse click as end for the upcoming
                           arc */

                        if (! network . selectNewArcPlace (evt . x, evt . y))
                        {
                            help . setHelp (help . NO_PLACE);
                            break;
                        }

                        help . setHelp (help . EMPTY);

                        /* add the arc */

                        network.addArc();

                        break;

                    case CLICK_START_REMOVE_ARC:

                        /* tell network to mark a place/trans at the location
                           of the mouse click as place for the upcoming
                           arc */

                        if ( network . selectNewArcPlace (evt . x, evt . y)) {
			    help . setHelp (help . SELECT_TRANSITION);
			    
			    clickState = CLICK_TRANS_END_REMOVE_ARC;
			}
			else
                        {
			    if ( network . selectNewArcTransition (evt . x, evt . y)) {
				help . setHelp (help . SELECT_PLACE);
				
				clickState = CLICK_PLACE_END_REMOVE_ARC;
			    } else
			    {
				help . setHelp (help . NO_OBJECT_HERE);
				break;
			    }
                        }

                        repaint ();

                        return true;

                    case CLICK_TRANS_END_REMOVE_ARC:

                        /* tell network to mark a trans at the location
                           of the mouse click as end for the upcoming
                           arc */

                        if (! network . selectNewArcTransition (evt . x, evt . y))
                        {
                            help . setHelp (help . NO_TRANSITION);
                            break;
                        }

                        help . setHelp (help . EMPTY);

                        /* remove the arc */

                        network.removeArc();

                        break;

                    case CLICK_PLACE_END_REMOVE_ARC:

                        /* tell network to mark a place at the location
                           of the mouse click as end for the upcoming
                           arc */

                        if (! network . selectNewArcPlace (evt . x, evt . y))
                        {
                            help . setHelp (help . NO_PLACE);
                            break;
                        }

                        help . setHelp (help . EMPTY);

                        /* remove the arc */

                        network.removeArc();

                        break;

                    case CLICK_RUN_SIMULATION:

                        /* invoke runSimulation method on network until it returns
                           false */

                        switch (network . runSimulation ())
                        {
                            case PetriNet.STATUS_NO_ACTIVABLE_TRANSITION:
                                help . setHelp (help . NO_ACTIVABLE_TRANSITION);
                                break;

                            case PetriNet.STATUS_NORMAL:
                            default:
                                repaint ();
                                return true;
                        }

                        break;

                    case CLICK_ADD_TOKEN:

                        /* tell network to add a token to the place at the location
                           of the mouse click */

                        if (! network . addTokenToPlaceAt (evt . x, evt . y))
                        {
                            help . setHelp (help . NO_PLACE);
                            break;
                        }

                        help . setHelp (help . EMPTY);

                        break;

                    case CLICK_REMOVE_TOKEN:

                        /* tell network to remove a token from the place at the location
                           of the mouse click */

                        if (! network . removeTokenFromPlaceAt (evt . x, evt . y))
                        {
                            help . setHelp (help . NO_PLACE_OR_NO_TOKEN);
                            break;
                        }

                        help . setHelp (help . EMPTY);

                        break;

                    case CLICK_NORMAL:
                    default:

                        /* see if there is a state to drag */

                        dragging = network . selectDrag (evt . x, evt . y);

                        break;
                }

                clickState = CLICK_NORMAL;

                break;

            case Event . MOUSE_UP:

                /* if we are dragging something - stop it */

                if (dragging)
                {
                    network . deselectDrag (evt . x, evt . y);
                    dragging = false;
                }

                break;

            case Event . MOUSE_DRAG:

                /* if we are dragging something - keep dragging */

                if (dragging)
                    network . drag (evt . x, evt . y);

                break;

            /* handle action events (button presses) */

            case Event . ACTION_EVENT:

                /* only respond to button presses */

                if (evt . target instanceof Button)
                {
                    /* find out the name of the button pressed */

                    String label = ((Button)(evt . target)) . getLabel ();

                    /* pressing a button destroys previous click state */

                    clickState = CLICK_NORMAL;

                    if (label . equals ("Quit"))
                    {
                        /* popup quit confirmation dialog */

                        quitDialog . show ();
                    }
                    else if (label . equals ("About"))
                    {
                        /* popup about window and play a sound */

                        aboutBox . show();

                        // play (getCodeBase (), "welcome.au");
                    }
                    else if (label . equals ("New"))
                    {
                        clearAll = true;

                        /* create new network instance - the old one will get
                           garbage collected */

                        network = new PetriNet ();

                        /* link dialogs with the new network */

                        fileInput      . setPetriNet (network, help);

                        help . setHelp (help . INITIAL);
                    }
                    else if (label . equals ("Load"))
                    {
                        /* 'load' does implicit 'new' */

                        clearAll = true;

                        /* create new network instance - the old one will get
                           garbage collected */

                        network = new PetriNet ();

                        /* link dialogs with the new network */

                        fileInput      . setPetriNet (network, help);

                        help . setHelp (help . INITIAL);

                        /* tell file dialog to perform load and pop it up */

                        fileInput . setMode (FileInput . LOAD);
                        fileInput . show();
                    }
                    else if (label . equals ("Save"))
                    {
                        /* tell file dialog to perform save and pop it up */

                        fileInput . setMode (FileInput . SAVE);
                        fileInput . show();
                    }
                    else if (label . equals ("Run"))
                    {
                        /* tell network to start simulation */

                        switch (network . startSimulation ())
                        {
                            case PetriNet . STATUS_NORMAL:
                                help . setHelp (help . RUN_INSTRUCTIONS);
                                clickState = CLICK_RUN_SIMULATION;
                                break;

                            default:
                                break;
                        }

                    }
                    else if (label . equals ("Stop"))
                    {
                        /* tell network to stop simulation */

                        network . stopSimulation ();
                        help . setHelp (help . EMPTY);
                        clickState = CLICK_NORMAL;
                    }

                    /* for the following buttons just set the appropriate
                       click state and hint line text. all the work will be
                       done after the mouse is clicked */

                    else if (label . equals ("Add Place"))
                    {
                        clickState = CLICK_PLACE_ADD;
                        help . setHelp (help . SELECT_LOCATION);
                    }
                    else if (label . equals ("Del Place"))
                    {
                        clickState = CLICK_PLACE_REMOVE;
                        help . setHelp (help . SELECT_PLACE);
                    }
                   else if (label . equals ("Add Transition"))
                    {
                        clickState = CLICK_TRANSITION_ADD;
                        help . setHelp (help . SELECT_LOCATION);
                    }
                    else if (label . equals ("Del Transition"))
                    {
                        clickState = CLICK_TRANSITION_REMOVE;
                        help . setHelp (help . SELECT_TRANSITION);
                    }
                    else if (label . equals ("Add Arc"))
                    {
                        clickState = CLICK_START_NEW_ARC;
                        help . setHelp (help . SELECT_OBJECT);
                    }
                    else if (label . equals ("Del Arc"))
                    {
                        clickState = CLICK_START_REMOVE_ARC;
                        help . setHelp (help . SELECT_OBJECT);
                    }
                    else if (label . equals ("Add Token"))
                    {
                        clickState = CLICK_ADD_TOKEN;
                        help . setHelp (help . SELECT_PLACE);
                    }
                    else if (label . equals ("Del Token"))
                    {
                        clickState = CLICK_REMOVE_TOKEN;
                        help . setHelp (help . SELECT_PLACE);
                    }
                }

                break;

            /* for all other events run the default Applet class handler */

            default:


                return super . handleEvent (evt);
        }

        /* force applet repaint as something could have changed */

        repaint ();

        /* return true - means we have handled the event and there is no
           no need to pass it along to other components */

        return true;

    } /* end handleEvent */


    public static void main(String [] args) {
        Frame frame = new Frame();

        Petri petri = new Petri();
        petri.init();
        frame.add(petri);
        frame.setSize(600,400);
        frame.setVisible(true);
    }


} /* end Petri */


