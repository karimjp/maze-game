/**
 * @author Karim Jana
 * @date March 30, 2015
 * @description
 
Pacman Maze:

Help Robot Pacman find its data!

This is a simple maze where the user has to remove obstacles out of robot pacman path
in order tap in the direction they want robot pacman to move until it reaches its goal
which is a floppy disk.

You can modify the maze Array in DrawingView.initMaze() to change the scenario

Features:

Touch objects:
- touching pacman robot will draw the traverse path of the user up to the current position.

Drag objects: 
- you can drag purple walls out of your way and place them anywhere in the the floor colored Grey. Any
 attempt to place the purple walls in the red walls will replace the walls in their original position.

Touch outside an object
- Tap outside pacman robot anywhere in the grey colored floor to guide it.  It will come to your finger location
 as you tap. Technically speaking this is still touching and object (Type.FLOOR) but from the GAMER perspective
 is outside an object.
 
Drag outside an object
- Dragging in the red walls which have no other purpose than frame the maze and are abundant will "pause"
 the game.  The game has no time notion so the only thing this will do is prompt an Alert Dialog with 
 two options: (Resume - resumes the game, it is glitchy because of a threading problem I did not solve but it works
 						if you press it several times,
 			   Restart - restart the game )
 			   
 @codeReferences
 *Alert Dialog Code:
 *http://www.mkyong.com/android/android-alert-dialog-example/
 *
 *Basic Version of Maze Class:
 *http://stackoverflow.com/questions/20747138/android-drawing-a-maze-to-canvas-with-smooth-character-movement
 * 
 **/

package jana.karim.hw3;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("TEST", "setting layout activity main");
		setContentView(R.layout.activity_main);
		Log.d("TEST", "Layout activity main was set succesfully");

		ViewGroup main = (ViewGroup) findViewById(R.id.main);
		DrawingView drawingView = new DrawingView(this);
		LinearLayout.LayoutParams params = new LayoutParams(10, 10, 0);
		drawingView.setLayoutParams(params);
		main.addView(drawingView);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/* http://www.mkyong.com/android/android-alert-dialog-example/ */
	public void restartGame() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("New Achievement");
		alert.setMessage("Pacman Robot has found the lost data! Try Again?");

		alert.setPositiveButton("Play Again",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						Intent intent = getIntent();
						finish();
						startActivity(intent);
					}
				});

		alert.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alert.create();
		// show it
		alertDialog.show();
	}

	/* http://www.mkyong.com/android/android-alert-dialog-example/ */
	public void pauseGame() {
		Log.d("PAUSE", "INSIDE PAUSE GAME!!!");
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		Log.d("PAUSE", alert + "");
		alert.setTitle("Status");
		alert.setMessage("Game is Paused.");

		alert.setNegativeButton("Restart",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						Intent intent = getIntent();
						finish();
						startActivity(intent);
					}
				});

		alert.setPositiveButton("Resume",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// dialog.dismiss();

						dialog.cancel();

					}
				});
		// create alert dialog
		AlertDialog alertDialog = alert.create();
		// show it
		alertDialog.show();
	}

}
