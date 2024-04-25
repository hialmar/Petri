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
    FileInput class - allows user to enter the file name for load/save operations.
*/

class FileInput extends Input
{
    /*
        CLASS CONSTANTS
    */


    /* specifies which action (load or save) is to be performed when OK button
       is pressed. */

    final static int    LOAD = 1;
    final static int    SAVE = 2;


    /*
        INSTANCE VARIABLES
    */


    /* current mode (load or save) */

    private int         mode;


    /*
        PUBLIC METHODS
    */


    /*
        constructor method
    */

    public FileInput (Panel par)
    {
        super (par, "Type file name:");

        /* initialize instance variables */

        mode = SAVE;

    } /* end FileInput */


    /*
        set dialog to specified mode (load or save)
    */

    public void setMode (int m)
    {
        mode = m;

    } /* setMode */


    /*
        pass input to the state machine
    */

    public void passInput (String i)
    {
        if (mode == LOAD)
            switch (network . loadFile (i))
            {
                case PetriNet . STATUS_FILE_NOT_FOUND:
                    help . setHelp (help . FILE_NOT_FOUND);
                    break;

                case PetriNet . STATUS_BAD_INPUT_FILE:
                    help . setHelp (help . FILE_BAD_INPUT);
                    break;

                case PetriNet . STATUS_READ_ERROR:
                    help . setHelp (help . FILE_READ_ERROR);
                    break;

                case PetriNet . STATUS_NORMAL:
                default:
                    help . setHelp (help . FILE_LOADED_OK);
                    break;
            }

        else
            switch (network . saveFile (i))
            {
                case PetriNet . STATUS_FILE_CREATE_ERROR:
                    help . setHelp (help . FILE_CREATE_ERROR);
                    break;

                case PetriNet . STATUS_WRITE_ERROR:
                    help . setHelp (help . FILE_WRITE_ERROR);
                    break;

                case PetriNet . STATUS_NORMAL:
                default:
                    help . setHelp (help . FILE_SAVED_OK);
                    break;
            }

    } /* end passInput */

} /* end FileInput */


