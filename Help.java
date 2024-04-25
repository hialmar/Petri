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
    Help class - draws hints and messages at the bottom of the applet.
*/


class Help
{
    /*
        CLASS CONSTANTS
    */

    final static int    EMPTY                           = 0;
    final static int    INITIAL                         = 1;
    final static int    NO_PLACE                        = 2;
    final static int    NO_TRANSITION                   = 3;
    final static int    MAX_PLACES                      = 4;
    final static int    MAX_TRANSITIONS                 = 5;
    final static int    SELECT_PLACE                    = 6;
    final static int    SELECT_TRANSITION               = 7;
    final static int    SELECT_OBJECT	                = 8;
    final static int    NO_OBJECT_HERE                  = 9;
    final static int    NO_ACTIVABLE_TRANSITION         = 10;
    final static int    NO_PLACE_OR_NO_TOKEN            = 11;
    final static int    SELECT_LOCATION                 = 12;
    final static int    RUN_INSTRUCTIONS                = 13;
    final static int    FILE_NOT_FOUND                  = 14;
    final static int    FILE_CREATE_ERROR               = 15;
    final static int    FILE_WRITE_ERROR                = 16;
    final static int    FILE_READ_ERROR                 = 17;
    final static int    FILE_BAD_INPUT                  = 18;
    final static int    FILE_LOADED_OK                  = 19;
    final static int    FILE_SAVED_OK                   = 20;


    /*
        INSTANCE VARIABLES
    */


    /* current message string */

    private String      currHelp;


    /*
        PUBLIC METHODS
    */


    /*
        constructor method - set current message to empty
    */

    public Help ()
    {
        currHelp = new String ("");

    } /* end Help */


    /*
        sets current message string depending on the type of message wanted
    */

    public void setHelp (int num)
    {
        switch (num)
        {
            case INITIAL:
                currHelp = new String ("Petri Network Simulator V1.0. Written by Torguet Patrice, Febr 1996.");
                break;

            case NO_PLACE:
                currHelp = new String ("There is no place there");
                break;

            case NO_PLACE_OR_NO_TOKEN:
                currHelp = new String ("There is no place there or else the place is empty");
                break;

            case NO_TRANSITION:
                currHelp = new String ("There is no transition there");
                break;

            case MAX_PLACES:
                currHelp = new String ("Maximum number of places is reached");
                break;

            case MAX_TRANSITIONS:
                currHelp = new String ("Maximum number of transitions is reached");
                break;

            case SELECT_PLACE:
                currHelp = new String ("Click on a place");
                break;

            case SELECT_TRANSITION:
                currHelp = new String ("Click on a transition");
                break;

            case SELECT_OBJECT:
                currHelp = new String ("Click on a place or transition");
                break;

            case NO_OBJECT_HERE:
                currHelp = new String ("There is no place or transition there");
                break;

            case NO_ACTIVABLE_TRANSITION:
                currHelp = new String ("No more activable transitions - halting");
                break;

            case SELECT_LOCATION:
                currHelp = new String ("Click on desired location");
                break;

            case RUN_INSTRUCTIONS:
                currHelp = new String ("Click to allow one firing");
                break;

            case FILE_NOT_FOUND:
                currHelp = new String ("Specified file not found");
                break;

            case FILE_CREATE_ERROR:
                currHelp = new String ("Specified file could not be created");
                break;

            case FILE_WRITE_ERROR:
                currHelp = new String ("Error writing file");
                break;

            case FILE_READ_ERROR:
                currHelp = new String ("Error reading file");
                break;

            case FILE_BAD_INPUT:
                currHelp = new String ("Bad input file");
                break;

            case FILE_LOADED_OK:
                currHelp = new String ("File loaded successfully");
                break;

            case FILE_SAVED_OK:
                currHelp = new String ("File saved successfully");
                break;

            case EMPTY:
            default:
                currHelp = new String ("");
                break;
        }

    } /* end setHelp */


    /*
        draw the current message string at the very bottom of the screen.
    */

    public void paint (Graphics g, int height, int width)
    {
        /* remove previous message */

        remove (g, height, width);

        /* draw new one */

        g . setColor (Color . red);
        g . drawString (currHelp, 0, height - 1);

    } /* end paint */


    /*
        draw a filled rectangle with the width of the screen and the height
        of the font.
    */

    public void remove (Graphics g, int height, int width)
    {
        g . setColor (Color . lightGray);
        g . fillRect (0, height - g . getFontMetrics () . getHeight () - 1,
                      width, g . getFontMetrics () . getHeight () + 1);

    } /* end remove */

} /* end Help */
