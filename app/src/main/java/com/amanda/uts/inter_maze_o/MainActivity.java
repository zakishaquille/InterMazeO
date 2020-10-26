package com.amanda.uts.inter_maze_o;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amanda.uts.inter_maze_o.R;

/*
Keywords:
    - Android Game Rotation Vector

Steps taken to create this app:
    1) Create new Java public class called GameView with it's superclass called android.view.View

    View class represents the basic building block for user interface components. A View occupies
    a rectangular area on the screen and is responsible for drawing and event handling.
    View is the base class for widgets, which are used to create interactive UI components
    (buttons, text fields, etc.)

    2) For the GameView class:
        - override constructor (by adding super() method) and onDraw method (by adding drawColor()
        method)

    3) Put the GameView class into the layout (activity_main)

    4) Time to draw cell grid -> back to the GameView class:
        4.1) add 'Cell' class
        4.2) add 2D array
        4.3) add constants
        4.4) add createMaze() method
        4.5) add other fields (e.g. wallPaint)
        4.6) add wallPaint to the constructor
        4.7) in onDraw() method:
            - add width and height getters
            - set cellSize, hMargin, vMargin
            - draw cell's walls by using for loop and drawLine() method - instead of adding vMargin
              and hMargin to position of every cell, we'll use translate() method

    5) Algorithm chosen to generate a maze is the depth-first search algorithm, implemented using
       backtracking:
       5.1) Make the initial cell the current cell and mark it as visited
       5.2) While there are unvisited cells:
            5.2.1) If the current cell has any neighbours which have not been visited:
                5.2.1.1) Choose randomly one of the unvisited neighbours
                5.2.1.2) Push the current cell to the stack
                5.2.1.3) Remove the wall between the current cell and the chosen cell
                5.2.1.4) Make the chosen cell the current cell and mark it as visited
            5.2.2) Else if stack is not empty:
                5.2.2.1) Pop a cell from the stack
                5.2.2.2) Make it the current cell

    6) In createMaze() method: create a stack, 'current' and 'next' Cell objects, initialize
                               'current'

    7) Add 'visited' field to the Cell class and set it for 'current' in the createMaze()

    8) Create getNeighbour() method (create 'random' variable and initialize it in the constructor)
       and assign it to the 'next' object

    9) Create removeWall() method

    10) Add all remaining steps of the algorithm in the createMaze() method - our maze is finished!

    11) Now let's focus on player movement: add 'player', 'exit', 'playerPaint', 'exitPaint' fields

    12) Initialize Paint objects in the constructor

    13) Set player's start position and exit position in the createMaze() method

    14} Create enum field for directions then create movePlayer() method

    15) Add necessary lines of code to take care of sensors

    ...

    Note: User selects size of the maze. I had massive problem on how to implement that. The only
    working solution I came up with was to initialize the maze with random starting size value
    (I've chosen 15). Then, I set maze size by using captured value from previous activity (where
    user selects desired maze size). Maze size's setting is done with setSize() method, which
    at it's end calls initialize() method, which calls onDraw() method in GameView class. Then,
    I explicitly call createMaze() in MainActivity's onCreate() method, so that the maze is
    generated once again, but this time with proper size. I'm not sure of that yet, but calling all
    this methods() (which are responsible for the maze's proper starting look)
    inside onCreate() method, "hides" this whole ugly process of generating the maze from the user,
    so the user sees only the desired result.
 */

public class MainActivity extends AppCompatActivity {

    GameView gameView;
    int [] mazeSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "quick check: content view set");

        Intent intent = getIntent();
        /* Retrieve extended data from the intent, where firs parameter is the name of the desired item
         *  and the second parameter is the value to be returned if no value of the desired type is
         *  stored with the given name. */

        mazeSize = new int[2];
        mazeSize = intent.getIntArrayExtra("mazeSize");
        gameView = findViewById(R.id.gameView);
        Log.d("MainActivity", "quick check: game view found by ID");
        gameView.setSize(mazeSize[0], mazeSize[1]);
        Log.d("MainActivity", "quick check: maze size set");
        gameView.createMaze();
        Log.d("MainActivity", "quick check: createMaze() called");
    }

    /* On devices running Android 9 (API level 28) or higher, apps running in the background have
       the following restrictions:
       - sensors that use the continuous reporting mode, such as accelerometers and gyroscopes,
         don't receive events.
       - sensors that use the on-change or one-shot reporting modes don't receive events.

       As a best practice we should always disable sensors we don't need, especially when our
    *  activity is paused. Failing to do so can drain the battery in just a few hours because some
    *  sensors have substantial power requirements and can use up battery power quickly. The system
    *  will not disable sensors automatically when the screen turns off. Therefore, we use
    *  onResume() and onPause() callback methods to register and unregister the sensor event
    *  listener. */
    @Override
    protected void onResume() {
        super.onResume();
        gameView.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.unregister();
    }


}
