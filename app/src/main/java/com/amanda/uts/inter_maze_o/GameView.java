package com.amanda.uts.inter_maze_o;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.amanda.uts.inter_maze_o.R;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class GameView extends View implements SensorEventListener {

    Configuration config;

    private Cell[][] cells;
    private Cell player, exit;
    private int COLS = 15, ROWS = 15;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    /* The Paint class holds the style and color information about how to draw geometries, text
        and bitmaps. */
    private Paint wallPaint, playerPaint, exitPaint;
    /* The maze algorithm chooses random not visited neighbour  */
    private Random random;
    private enum Direction { UP, RIGHT, DOWN, LEFT }

    /* A good convention is to declare a TAG constant in our class and use that to log our activity
      (type 'logt' and let the framework autocomplete the variable) */
    private static final String TAG = "GameView";

    private Sensor gameRotationVector;
    private SensorManager sensorManager;

    /*  View(Context context, AttributeSet attrs) - Constructor that is called when inflating
        a view from XML.

        Context - Interface to global information about an application environment. It allows access
        to application-specific resources and classes, as well as up-calls for application-level
        operations such as launching activities, broadcasting and receiving intents, etc.

        '@' is an annotation. It is used to include metadata (data information that provides
        information about other data) within programs.

        '@Nullable' makes it clear that the method accepts null values, and that if we override
        the method, we should also accept null values.

        AttributeSet - A collection of attributes, as found associated with a tag in an XML
        document.
    */
    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "quick check: inside GameView constructor");

        wallPaint = new Paint();
        wallPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(getResources().getColor(R.color.black));

        exitPaint = new Paint();
        exitPaint.setColor(getResources().getColor(R.color.colorAccent));

        random = new Random();

        createMaze();

        /* Used for getting screen orientation */
        config = getResources().getConfiguration();

        /* The Log class allows to create log messages that appear in logcat
           (View- > Tool Windows -> Logcat). Log's public method 'd' sends a DEBUG log message and
           optionally logs the exception. */
        Log.d(TAG, "onCreate: Initializing Sensor Services");

        /* To identify the sensors that are on a device we first need to get a reference to the
           sensor service. To do this, we create an instance of the SensorManager class by calling
           the getSystemService() method and passing in the SENSOR_SERVICE argument */
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        /* Determine whether gameRotationVector exists on a device */
        gameRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if(gameRotationVector != null) Log.d(TAG, "onCreate: Registered gameRotationVector listener");
        else Log.d(TAG, "onCreate: gameRotationVector not supported");
    }

    public void setSize(int rows, int cols) {
        ROWS = rows;
        COLS = cols;
    }

    private class Cell {
        /* Default class content's (methods and fields) access level is 'package' */

        /* Boolean variables will indicate whether certain wall exists or not */
        boolean
                topWall = true,
                leftWall = true,
                bottomWall = true,
                rightWall = true,
                visited = false;

        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }

    private Cell getNeighbour(Cell cell) {
        /* Check if there are unvisited neighbours - use ArrayList to store them
           ArrayList is an array which can change it's size after it's been initialized */
        ArrayList<Cell> neighbours = new ArrayList<>();

        /* Left neighbour */
        /* Include the fact that in case of cell.col == 0, the further index would be '-1' */
        if(cell.col > 0) {
            /* Add the cell to the 'neighbours' ArrayList if it hasn't been visited */
            if(!cells[cell.col - 1][cell.row].visited) neighbours.add(cells[cell.col - 1][cell.row]);
        }

        /* Top neighbour */
        if(cell.row > 0) {
            if(!cells[cell.col][cell.row - 1].visited) neighbours.add(cells[cell.col][cell.row - 1]);
        }

        /* Right neighbour */
        if(cell.col < COLS - 1) {
            if(!cells[cell.col + 1][cell.row].visited) neighbours.add(cells[cell.col + 1][cell.row]);
        }

        /* Bottom neighbour */
        if(cell.row < ROWS - 1) {
            if(!cells[cell.col][cell.row + 1].visited) neighbours.add(cells[cell.col][cell.row + 1]);
        }

        if(neighbours.size() > 0) {
            /* Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the
               specified value (exclusive), drawn from this random number generator's sequence. */
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        /* no need for 'else' because upper 'return', if executed, will stop the method */
        return null;
    }

    private void removeWall(Cell current, Cell next) {
        /* current cell's top wall and next cell's bottom wall */
        if(current.col == next.col && current.row == next.row + 1) {
            current.topWall = false;
            next.bottomWall = false;
        }
        if(current.col == next.col + 1 && current.row == next.row) {
            current.leftWall = false;
            next.rightWall = false;
        }
        if(current.col == next.col && current.row == next.row - 1) {
            current.bottomWall = false;
            next.topWall = false;
        }
        if(current.col == next.col - 1 && current.row == next.row) {
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    public void createMaze() {
        Stack<Cell> stack = new Stack<>();

        Cell current, next;

        Log.d(TAG, "quick check: inside createMaze()");

        cells = new Cell[COLS][ROWS];

        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }

        Log.d(TAG, "quick check: size of created grid is " + COLS + " x " + ROWS);

        player = cells[0][0];
        exit = cells[COLS-1][ROWS-1];


        /* Instead of choosing random cell as the initial one, we'll choose the one in upper left
           corner */
        current = cells[0][0];
        /* and mark it as visited */
        current.visited = true;
        do {
            next = getNeighbour(current);
            if(next != null) {
                /* remove the wall between the current cell and the chosen neighbour */
                removeWall(current, next);
                /* push the current cell to the stack */
                stack.push(current);
                /* Make the neighbour cell the current cell and mark it as visited */
                current = next;
                /* Mark it as visited */
                current.visited = true;
            }
            else {
                /* Pop a cell from the stack (i.e. take the last cell and remove it from the stack) */
                current = stack.pop();
            }
        /* If the stack is empty, we're done */
        } while(!stack.empty());
    }

    /* '@Override' - Indicates that a method declaration is intended to override a method
        declaration in a supertype. If a method is annotated with this annotation type compilers are
        required to generate an error message unless at least one of the following conditions hold:
            - The method does override or implement a method declared in a supertype.
            - The method has a signature that is override-equivalent to that of any public method
              declared in Object.

        The most important step in drawing a custom view is to override the onDraw() method.
        The parameter to onDraw() is a Canvas object that the view can use to draw itself.
        The Canvas class defines methods for drawing text, lines, bitmaps, and many other graphics
        primitives. We can use these methods in onDraw() to create our custom user interface (UI).
        onDraw() method will be automatically called by the system every time we display an object
        of GameView class.
    */
    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "quick check: inside onDraw()");
        /*  drawColor(int color) - fil the entire canvas' bitmap (restricted to the current clip)
            with the specified color, using srcover porterduff mode. */
        canvas.drawColor(getResources().getColor(R.color.white));

        /* getWidth() - return the width of our view. */
        int width = getWidth();
        int height = getHeight();

        config = getResources().getConfiguration();

        /* vertical view */
        if(config.orientation == ORIENTATION_PORTRAIT) {
            cellSize = width / (COLS + 1);
            Log.d(TAG, "quick check: portrait mode detected");
        }
        /* horizontal view */
        else cellSize = height / (ROWS + 1);

        hMargin = (width - COLS * cellSize) / 2;
        vMargin = (height - ROWS * cellSize) / 2;

        /* Take original origin and offset it by hMargin in x-direction and vMargin in y-direction */
        canvas.translate(hMargin, vMargin);

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {

                /*
                *   (x, y)   topWall   (x+1, y)
                *
                *  leftWall            rightWall
                *
                *  (x, y+1) bottomWall (x+1, y+1)
                */

                if(cells[x][y].topWall) canvas.drawLine(
                        x * cellSize,
                        y * cellSize,
                        (x + 1) * cellSize,
                        y * cellSize,
                              wallPaint);
                if(cells[x][y].rightWall) canvas.drawLine(
                        (x + 1) * cellSize,
                        y * cellSize,
                        (x + 1) * cellSize,
                        (y + 1) * cellSize,
                               wallPaint);
                if(cells[x][y].bottomWall) canvas.drawLine(
                        x * cellSize,
                        (y + 1) * cellSize,
                        (x + 1) * cellSize,
                        (y + 1) * cellSize,
                               wallPaint);
                if(cells[x][y].leftWall) canvas.drawLine(
                        x * cellSize,
                        y * cellSize,
                        x * cellSize,
                        (y + 1) * cellSize,
                               wallPaint);
            }
        }

        /* Add margin to the player and the exit so they won't cover parts of the walls */
        float margin = cellSize / 10;

        /* Draw player and the exit */
            /* drawOval() requires at least 21 API level */
        canvas.drawOval(
                player.col * cellSize + margin,
                player.row * cellSize + margin,
              (player.col + 1) * cellSize - margin,
            (player.row + 1) * cellSize - margin,
                    playerPaint);
        canvas.drawOval(
                exit.col * cellSize + margin,
                exit.row * cellSize + margin,
                (exit.col + 1) * cellSize - margin,
                (exit.row + 1) * cellSize - margin,
                exitPaint);
    }

    public void register() {
        sensorManager.registerListener(this, gameRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "Sensor listener registered");
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "Sensor listener unregistered");
    }

    /* We can press 'Ctrl + O' to list methods to override/implement */

    /* To monitor raw sensor data we need to implement two callback methods that are exposed
     *  through the SensorEventListener interface: onAccuracyChanged() and onSensorChanged() */

    /* A sensor's accuracy changes */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    /* A sensor reports a new value */
    /* Sensor data can change at a high rate, which means the system may call the onSensorChanged()
     * method quite often. As a best practise, we should do as little as possible within this method
     * so we don't block it. If our application requites us to do any data filtering or reduction
       of sensor data, we should perform that work outside of this method.*/
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        /* gameRotationVector's units of measure is rad/s */
        Log.d(TAG, "onSensorChanged: MainActivity sent" +
              /*
                                                 y      z
                                                 ^     ^
                  Front of the device:           |    /
                                                 |   /
                                            -----------
                                            |      /  |
                                            |     /   |
                                      ------|    /    |-----> x
                                            |         |
                                            |         |
                                            -----------
                                             /   |
                                            /    |

                    The axis are not swapped when the device's screen orientation changes - that is,
                    the sensor's coordinate system never changes as the device moves.

                    Our application must not assume that a device's natural (default) orientation
                    is portrait. The natural orientation for many tablet devices is landscape. And
                    the sensor coordinate system is always based on the natural orientation of
                    a device.
              */
                " X: " + sensorEvent.values[0] +          /* Rate of rotation around the x axis */
                " Y: " + sensorEvent.values[1] +          /* Rate of rotation around the y axis */
                " Z: " + sensorEvent.values[2]);          /* Rate of rotation around the z axis */

        /* portrait mode */
        if(config.orientation == ORIENTATION_PORTRAIT) {
            if (sensorEvent.values[0] > 0.2) {
                movePlayer(Direction.DOWN);
            } else if (sensorEvent.values[0] < -0.1) {
                movePlayer(Direction.UP);
            }

            if (sensorEvent.values[1] > 0.15) {
                movePlayer(Direction.RIGHT);
            } else if (sensorEvent.values[1] < -0.15) {
                movePlayer(Direction.LEFT);
            }
        /* landscape mode - front camera on the left*/
        } else if(config.orientation == ORIENTATION_LANDSCAPE){
            if (sensorEvent.values[1] > 0.2) {
                movePlayer(Direction.RIGHT);
            } else if (sensorEvent.values[1] < -0.2) {
                movePlayer(Direction.LEFT);
            }

            if (sensorEvent.values[0] > 0.2) {
                movePlayer(Direction.DOWN);
            } else if (sensorEvent.values[0] < -0.2) {
                movePlayer(Direction.UP);
            }
        }
    }

    public void movePlayer(Direction direction) {
        switch(direction) {
            case UP:
                if(!player.topWall) {
                    player = cells[player.col][player.row - 1];
                    Log.d(TAG, "moving UP");
                }
                break;
            case RIGHT:
                if(!player.rightWall) {
                    player = cells[player.col + 1][player.row];
                    Log.d(TAG, "moving RIGHT");
                }
                break;
            case DOWN:
                if(!player.bottomWall) {
                    player = cells[player.col][player.row + 1];
                    Log.d(TAG, "moving DOWN");
                }
                break;
            case LEFT:
                if(!player.leftWall) {
                    player = cells[player.col - 1][player.row];
                    Log.d(TAG, "moving LEFT");
                }
                break;
        }

        checkExit();

        /* Force onDraw() method call */
        invalidate();
    }

    private void checkExit() {
        if(player == exit) createMaze();
    }
}
