package jana.karim.hw3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import jana.karim.hw3.MazeObject;
import jana.karim.hw3.MazeObject.Type;
import jana.karim.hw3.MainActivity;

public class DrawingView extends View {
	private List<MazeObject> mazeObjects = new ArrayList<MazeObject>();
	private Set<RectF> pacmanRobotPath = new HashSet<RectF>();
	private Maze maze;
	private MazeObject pacmanRobot = null;
	private MazeObject movingWall = null;
	private MazeObject floppyDisk = null;
	private MainActivity mainActivity = (MainActivity) getContext();

	Bitmap bitmap;
	float x = 0;
	float y = 0;
	private MazeObject selected;
	private RectF rect = new RectF();
	private int dragLength = 0;

	public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// TODO Auto-generated constructor stub
	}

	public DrawingView(Context context) {
		super(context);

	}

	public void init() {

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.pacmanrobot);
		Bitmap scaledPacmanRobot = scaleBitmapToMazeCell(bitmap);
		pacmanRobot = new MazeObject(Type.PACMAN_ROBOT, scaledPacmanRobot);

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.secondwall);
		Bitmap scaledMovingWall = scaleBitmapToMazeCell(bitmap);
		movingWall = new MazeObject(Type.PURPLE_MOVING_WALL, scaledMovingWall);

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.floppydisk);
		Bitmap scaledFloppyDisk = scaleBitmapToMazeCell(bitmap);
		floppyDisk = new MazeObject(Type.FLOPPY_DISK, scaledFloppyDisk);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);

		float viewX = 0;
		float viewY = 0;

		if (pacmanRobot == null) {
			// Program is starting: Pacman Robot will be set along with maze and
			// objects
			initMaze();
			init();
			maze.setMazeLayoutObjects(canvas, viewX, viewY, mazeObjects);
			startPacmanPosition(canvas);
			setPacmanGoal(canvas);
			rect.set(pacmanRobot.getBounds());

		} else {
			// Program was running: redraw and trackPacman new steps if any
			maze.drawMaze(canvas, viewX, viewY);
			pacmanRobot.drawMazeObject(canvas, pacmanRobot.getBounds());
			floppyDisk.drawMazeObject(canvas, floppyDisk.getBounds());

			trackPacmanRobotPath();
		}

		// Case: User is moving obstacle/purple wall
		if (selected != null && selected.getType() == Type.PURPLE_MOVING_WALL) {
			movingWall.drawMazeObject(canvas, movingWall.getBounds());
		}

		// Case: User tapped on pacman itself, wants to see the traverse path
		if (selected != null && selected.getType() == Type.PACMAN_ROBOT) {
			visualizeTrackPath(canvas);

		}

		// Case: User successfully guided pacman robot to goal!
		if (selected != null && selected.getType() == Type.FLOPPY_DISK) {
			visualizeTrackPath(canvas);
			mainActivity.restartGame();
		}
		// Case: User is trying to pause the game
		if (selected != null && selected.getType() == Type.RED_WALL
				&& dragLength >= 13) {
			mainActivity.pauseGame();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		// user taps on the canvas (finger has been placed on the screen)
		case MotionEvent.ACTION_DOWN:
			x = event.getX();
			y = event.getY();
			selected = findThingAt(x, y);
			if (selected != null) {

				if (selected.getBounds().contains(pacmanRobot.getBounds())) {
					// Case: User wants to see traverse path
					selected = pacmanRobot;
					invalidate();
				}
				if (selected.getType() == Type.FLOOR) {
					// Case: User Wants to move towards this location
					setPacmanNextUserMoveCommand(selected);
					invalidate();
				}
				if (selected.getType() == Type.PURPLE_MOVING_WALL) {
					// case: User wants to move the obstacle
					final int FLOOR = 0;
					int[][] mazeArrayValues = maze.getTileType();
					int tileX = selected.getMazeArrayX();
					int tileY = selected.getMazeArrayY();
					mazeArrayValues[tileY][tileX] = FLOOR;
					maze.setTileType(mazeArrayValues);
					movingWall.getBounds().set(selected.getBounds());
					invalidate();
				}
			}
			break;

		// user is dragging object
		case MotionEvent.ACTION_MOVE:

			Log.d("ACTION_MOVING", selected.getType() + "");
			if (selected != null) {

				if (selected.getBounds().contains(floppyDisk.getBounds())
						&& selected.getBounds().contains(
								pacmanRobot.getBounds())) {
					// Case: User has successfully guided robot pacman to
					// floppyDisk
					selected = floppyDisk;
					invalidate();
				}
				if (selected.getType() == Type.RED_WALL) {
					// Case: User is attempting to drag outside
					dragLength += 1;
					invalidate();
				} else {
					// Case: User drag was not long enough or by accident.
					dragLength = 0;
				}
				if ((selected.getType() == Type.PURPLE_MOVING_WALL)) {
					// Case: User is moving obstacle out of the way
					x = event.getX();
					y = event.getY();
					movingWall.setCurrentLocation(x, y);
					invalidate();
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			x = event.getX();
			y = event.getY();
			MazeObject toDropAt = findThingAt(x, y);
			if ((selected != null)
					&& (selected.getType() == Type.PURPLE_MOVING_WALL)) {
				// Case: User has dropped the purple wall into the desired
				// location in floor
				if (toDropAt.getType() == Type.FLOOR) {
					movingWall.getBounds().set(toDropAt.getBounds());
					final int MOVING_WALL = 2;
					int[][] mazeArrayValues = maze.getTileType();
					int tileX = toDropAt.getMazeArrayX();
					int tileY = toDropAt.getMazeArrayY();

					mazeArrayValues[tileY][tileX] = MOVING_WALL;
					maze.setTileType(mazeArrayValues);
					selected.setType(Type.FLOOR);
					toDropAt.setType(Type.PURPLE_MOVING_WALL);

					invalidate();
				}
				// Case: User tried to drop the purple wall into restricted
				// areas
				else {
					movingWall.getBounds().set(selected.getBounds());
					final int MOVING_WALL = 2;
					int[][] mazeArrayValues = maze.getTileType();
					int tileX = selected.getMazeArrayX();
					int tileY = selected.getMazeArrayY();

					mazeArrayValues[tileY][tileX] = MOVING_WALL;
					maze.setTileType(mazeArrayValues);

					invalidate();
				}
			}
			break;
		}
		return true;
	}

	private void trackPacmanRobotPath() {
		/*
		 * Records all the robot pacman traverse floor cells.
		 */
		RectF rectf = new RectF();
		rectf.set(pacmanRobot.getBounds());
		pacmanRobotPath.add(rectf);
		// Log.d("SIZE","Size of path: " + pacmanRobotPath.size());
	}

	private void printPath() {
		/*
		 * Debugging the traverse path.
		 */
		for (RectF rectf : pacmanRobotPath) {
			// Log.d("SIZE-I",rectf + "");
		}
	}

	private void visualizeTrackPath(Canvas canvas) {
		/*
		 * Draws on the canvas all the floor locations where robot pacman passed
		 * by.
		 */
		for (RectF rectf : pacmanRobotPath) {
			Drawable circle = getResources().getDrawable(R.drawable.circle);
			circle.setAlpha(80);
			Rect r = new Rect();
			rectf.round(r);
			circle.setBounds(r);
			circle.draw(canvas);
		}
	}

	private Bitmap scaleBitmapToMazeCell(Bitmap bitmap) {
		/*
		 * Scales the any bitmap to the measurements of the maze cells in the
		 * screen.
		 */
		int w = Math.round(maze.getCellWidth());
		int h = Math.round(maze.getCellHeight());
		Bitmap scaled = Bitmap.createScaledBitmap(bitmap, w, h, true);
		return scaled;

	}

	private void setPacmanNextUserMoveCommand(MazeObject selected) {

		/*
		 * This is the controller for robot pacman. It interprets the GAMER
		 * input on the screen and sets robot pacman position.
		 */

		int selectedCellFloorRowValue = Math.round(selected.getBounds()
				.centerY());
		int selectedCellFloorColumnValue = Math.round(selected.getBounds()
				.centerX());
		int pacmanRowValue = Math.round(pacmanRobot.getBounds().centerY());
		int pacmanColumnValue = Math.round(pacmanRobot.getBounds().centerX());

		float left = pacmanRobot.getBounds().left;
		float right = pacmanRobot.getBounds().right;
		float top = pacmanRobot.getBounds().top;
		float bottom = pacmanRobot.getBounds().bottom;
		float cellWidth = maze.getCellWidth();
		float cellHeight = maze.getCellHeight();

		if (selectedCellFloorRowValue == pacmanRowValue) {
			Log.d("SELECTED", "SAME ROW");
			if (selectedCellFloorColumnValue < pacmanColumnValue) {
				// LEFT (move pacman left if cell floor selected is in same row
				// (Y axis) )
				rect.set(left - cellWidth, top, right - cellWidth, bottom);
				checkMoveLimits(rect);
				pacmanRobot.getBounds().set(rect);
			}
			if (selectedCellFloorColumnValue > pacmanColumnValue) {
				// RIGHT (move pacman right if cell floor selected is in same
				// row (Y axis) )
				rect.set(left + cellWidth, top, right + cellWidth, bottom);
				checkMoveLimits(rect);
				pacmanRobot.getBounds().set(rect);
			}
		}
		if (selectedCellFloorRowValue < pacmanRowValue) {
			// UP (move pacman up if cell floor selected is anywhere above its
			// row (Y axis) that is a cell floor)
			rect.set(left, top - cellHeight, right, bottom - cellHeight);
			checkMoveLimits(rect);
			pacmanRobot.getBounds().set(rect);
		}
		if (selectedCellFloorRowValue > pacmanRowValue) {
			// DOWN (move pacman down if cell floor selected is anywhere below
			// its row (Y axis) that is a cell floor)
			rect.set(left, top + cellHeight, right, bottom + cellHeight);
			checkMoveLimits(rect);
			pacmanRobot.getBounds().set(rect);
		}
	}

	private void checkMoveLimits(RectF newPosition) {
		/*
		 * Checks the boundaries of where pacman is moving. It prevents moving
		 * onto anything that is not type floor.
		 */
		MazeObject toMove = findThingAt(newPosition.centerX(),
				newPosition.centerY());
		if (toMove.getType() != Type.FLOOR) {
			newPosition.set(pacmanRobot.getBounds().left,
					pacmanRobot.getBounds().top, pacmanRobot.getBounds().right,
					pacmanRobot.getBounds().bottom);

		}
	}

	private void startPacmanPosition(Canvas canvas) {
		/*
		 * Sets the robot pacman in the maze array starting position.
		 */
		for (int i = mazeObjects.size() - 1; i >= 0; i--) {
			MazeObject mazeObject = mazeObjects.get(i);
			if (mazeObject.getProperty() == "STARTING_POSITION") {
				pacmanRobot.drawMazeObject(canvas, mazeObject.getBounds());
			}
		}

	}

	private void setPacmanGoal(Canvas canvas) {
		/*
		 * Sets the floppy disk in the maze array finish position.
		 */
		for (int i = mazeObjects.size() - 1; i >= 0; i--) {
			MazeObject mazeObject = mazeObjects.get(i);
			if (mazeObject.getProperty() == "FINISH_POSITION") {
				floppyDisk.drawMazeObject(canvas, mazeObject.getBounds());
			}
		}

	}

	private MazeObject findThingAt(float x, float y) {
		/*
		 * Credit: Code from SCOTT STANCHFIELD videos Allows you to find Maze
		 * Objects in the Canvas by passing coordinates.
		 */
		for (int i = mazeObjects.size() - 1; i >= 0; i--) {
			MazeObject mazeObject = mazeObjects.get(i);
			if (mazeObject.getBounds().contains(x, y)) {
				return mazeObject;
			}
		}
		return null;
	}

	public void initMaze() {
		/*
		 * Allows to change the Maze Layout.
		 */

		// 0 == floor, 1 == wall, 2 == different looking wall
		// 10 == starting position, 20 == finish
		int[][] mazeArray = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				{ 10, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 1, 1, 1, 1, 1, 1, 1, 2, 1, 1 },
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 1, 1, 1, 1, 2, 0, 1, 1, 1, 1 },
				{ 1, 1, 1, 1, 1, 2, 1, 1, 1, 1 },
				{ 1, 1, 1, 1, 0, 0, 1, 1, 1, 1 },
				{ 1, 1, 1, 1, 0, 0, 0, 1, 1, 1 },
				{ 20, 0, 0, 0, 0, 1, 0, 1, 1, 1 },

		};

		Bitmap[] bitmaps = {
				BitmapFactory.decodeResource(getResources(), R.drawable.floor),
				BitmapFactory.decodeResource(getResources(),
						R.drawable.firstwall),
				BitmapFactory.decodeResource(getResources(),
						R.drawable.secondwall) };

		// Chance the 480 and 320 to match the screen size of your device
		maze = new Maze(bitmaps, mazeArray, 10, 9, getWidth(), getHeight());

	}

}