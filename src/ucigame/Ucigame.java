// Ucigame.java

// Copyright (c) 2008, Daniel Frost
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of Ucigame nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY DANIEL FROST ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL DANIEL FROST BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// The following callback methods, if coded, all called asychronously:
//   onMouseDragged(), onMouseMoved(), onMousePressed(), onMouseReleased(),
//   onMouseWheelMoved(), onClickButtonName()

// The following callback methods, if coded, are called once per frame:
//   timernameTimer(), startSceneName() [if appropriate], onKeyPress(),
//   lastly draw() or drawSceneName()

package ucigame;

// Doesn't use the 1.6 SwingWorker, for 1.5 compatibility.
// This SwingWorker is a 1.6 backport from https://swingworker.dev.java.net/
import org.jdesktop.swingworker.SwingWorker;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;
import java.applet.*;
import java.lang.reflect.Method;
import java.net.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * The Ucigame class is the superclass of all Ucigame game classes.
 * Every Ucigame game has a class that "extends Ucigame".
 * Since Ucigame itself extends Java's JApplet class, all
 * Ucigame game classes can be run as applets, as well as applications.
 */
public abstract class Ucigame
	extends JApplet
	implements MouseMotionListener,
			   MouseListener,
			   MouseWheelListener,
	           KeyListener,
	           FocusListener
{
	private static String VERSION = "2008.05.30a";
	static Ucigame gameObject = null;  			 // only used when not an applet
	private static Object lock1 = new Object();  // for synchronization
	private static Object lock2 = new Object();  // for synchronization
	private static Object lock3 = new Object();  // for synchronization  of keysThatAreDown

	private static UcigameWorker worker = null;
	private static StaticUcigameWorker staticworker = null;
	static Ucigame ucigameObject = null;  		// seems to overlap gameObject; need both?
	public static final long serialVersionID = 12345L;

	static Ucigame ucigameAppletObject = null;	// for use only when running as an applet

	/**
	 * non-API
	 */
	public static void main(String[] commandLineArgs)
	{
		System.out.println("Ucigame version " + VERSION);
		if (commandLineArgs.length < 1)
		{
			logError("Please repeat the program name.  For example, java MyGame MyGame");
			System.exit(0);
		}

		String className = commandLineArgs[0];
		Object object = null;
		try {
			// see www.javageeks.com/Papers/ClassForName/ClassForName.pdf
			// for info on the next line
			Class classDefinition = Class.forName(className, true,
									ClassLoader.getSystemClassLoader());
			object = classDefinition.newInstance();
			if (object instanceof Ucigame)
				gameObject = (Ucigame)object;
			else
			{
				logError("Class " + className + " does not extend Ucigame.");
				System.exit(0);
			}
		}
		catch (InstantiationException e) {
        	System.out.println(e);
        	System.exit(0);

		}
		catch (IllegalAccessException e) {
        	System.out.println(e);
        	System.exit(0);
		}
		catch (ClassNotFoundException e) {
        	logError("No class found with name " + className);
        	System.exit(0);
		}
		catch (NoClassDefFoundError e) {
        	logError("No class definition found with name " + className);
        	System.exit(0);
		}

		gameObject.isApplet = false;

		// Execute a job on the event-dispatching thread:
		// creating this applet's GUI.
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					gameObject.setupGUI();
				}
			});
		} catch (Exception e) {
			System.err.println("setupGUI didn't successfully complete\n" + e);
			e.printStackTrace();
		}
		staticworker = new StaticUcigameWorker();
		staticworker.execute();   // returns immediately, background thread continues
	}

	/////////////////////// Applet methods ////////////////////////////////

	private String appletID = "";
	static private int appCount = 0;

	// Initialize the game if an applet.
	/**
	 * non-API
	 */
	final public void init()
	{
		appletID = "Ucigame " + appCount + " " + this.hashCode();
		appCount++;
		System.out.println(appletID + " Ucigame version " + VERSION);
		System.out.println(appletID + " applet.init()");
		isApplet = true;
		ucigameAppletObject = this;

		// Execute a job on the event-dispatching thread to create the GUI.
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					setupGUI();
				}
			});
		} catch (Exception e) {
			System.err.println(appletID + " setupGUI didn't successfully complete\n" + e);
			e.printStackTrace();
		}
	}

	/**
	 * non-API
	 */
	final public void start()
	{
		System.err.println(appletID + " applet.start()");
		worker = new UcigameWorker();
		worker.execute();           // runs in a different thread
		//System.err.println("applet.start() is done");
	}

	private boolean workerIsDone;

	/**
	 * non-API
	 */
	final public void stop()
	{
		System.err.println(appletID + " applet.stop()");
		ucigameAppletObject = null;
		for (Sound s : soundsPossiblyPlaying)
			s.stop();
		soundsPossiblyPlaying.clear();
		workerIsDone = false;
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					worker.cancel(true);  // send worker thread an interrupt
				}
			});
		} catch (Exception e) {
			System.err.println(appletID + " worker.cancel(true) didn't successfully complete\n" + e);
			e.printStackTrace();
		}

		// The following code is a work-around to address the problem
		// that stop() can complete, and destroy() can be run and complete,
		// before the UcigameWorker thread is done.  This generates the
		// following message in the Java Console:

		// Exception in thread "SwingWorker-pool-2-thread-1" java.lang.IllegalMonitorStateException


		//while (!workerIsDone)
		//{
		//	try {
		//		Thread.sleep(50);
		//	}
		//	catch (InterruptedException ie) {;}
		//}

		//System.err.println("applet.stop() is done");
	}

	/**
	 * non-API
	 */
	final public void destroy()
	{
		System.err.println(appletID + " applet.destroy()");
	}

	////// Constants for use by game and intrapackage///////////
	public static final int FILL = 982;
	public static final int SET = 20001;
	public static final int ADD = 20002;
	public static final int ADDONCE = 20004;
	public static final int MULTIPLY = 20003;
	public static final int TOP = 21001;
	public static final int BOTTOM = 21002;
	public static final int LEFT = 21003;
	public static final int RIGHT = 21004;
	public static final int ONCE = -100010010;
	public static Sprite TOPEDGE;
	public static Sprite BOTTOMEDGE;
	public static Sprite LEFTEDGE;
	public static Sprite RIGHTEDGE;
	public static final Sprite PIXELPERFECT = new Sprite(1, 1);
	public static final int BOLD   = Font.BOLD;
	public static final int PLAIN  = Font.PLAIN;
	public static final int ITALIC = Font.ITALIC;
	public static final int BOLDITALIC = Font.BOLD | Font.ITALIC;


	////// accessible within the package
	boolean isApplet;
	int mouseX, mouseY;
	int mouseChangeX, mouseChangeY, mousePrevX, mousePrevY;
	int mouseButton;
	boolean mouseIsAltDown, mouseIsControlDown, mouseIsMetaDown, mouseIsShiftDown;
	int mouseWheelUnits;
	Sprite mouseSprite;
	Graphics2D offG;
	int goalFPS = 0;
	Vector<Sprite> spritesFromBottomToTopList = new Vector<Sprite>();
	Hashtable<String, Method> name2method = new Hashtable<String, Method>();
	Sprite edgeLeft, edgeRight, edgeTop, edgeBottom;
	HashMap<Integer,String> keysThatAreDown = new HashMap<Integer, String>();
	HashMap<Integer,String> keysThatHaveJustBeenReleased = new HashMap<Integer, String>();
	HashMap<Integer,String> keysWithTypematicDifferent = new HashMap<Integer, String>();
	int lastKeyPressed;
	boolean shiftPressed, ctrlPressed, altPressed;
	boolean typematicIsOn = true;
	char lastKeyChar = 0;
	Vector<Sound> soundsPossiblyPlaying = new Vector<Sound>();

	JFrame frame;
	GameComponent gameComponent;
	java.awt.Image offscreen;
	Color bgColor;
	Image bgImage;
	Font windowFont = null;

	private Random rand = null;
	private Vector<Sprite> buttonList = new Vector<Sprite>();
	private Vector<Sprite> buttonList2 = new Vector<Sprite>(); // these get appended to buttonList
	private Method[] methods = null;
	private String currScene = null;
	private Method startSceneMethod = null;
	private Method sceneKeyPressMethod = null;
	private Method sceneKeyReleaseMethod = null;

	private int delayTime = 0;		// means no refreshing
	private int fps = 0;
	private Font fontFPS;

	private javax.swing.Timer fpsTimer = null;
	private int frames = 0;
	private boolean playing;
	private boolean suspended = false;
	private boolean oneStep = false;
	private Vector<Timer> timers = new Vector<Timer>();
	private Vector<Timer> timers2 = new Vector<Timer>();  // to add later
	private Vector<Timer> timers3 = new Vector<Timer>();  // to remove later

	// Informational variables accessible to the game.
	public Mouse mouse = new Mouse(this);
	public GameWindow window = new GameWindow(this);
	public GameCanvas canvas = new GameCanvas(this);
	public Keyboard keyboard = new Keyboard(this);

	private void setupGUI()
	{
		ucigameObject = this;
		// Create the window.
		if (!isApplet)
		{
			frame = new JFrame("No Title");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
        gameComponent = new GameComponent(this);
        gameComponent.setOpaque(true);
        if (isApplet)
        	this.getContentPane().add(gameComponent, BorderLayout.CENTER);
        else
        	frame.setContentPane(gameComponent);

        if (isApplet)
        {
        	this.addFocusListener(this);
		}
        else
        	frame.addFocusListener(this);

        gameComponent.addMouseMotionListener(this);
        gameComponent.addMouseListener(this);
        gameComponent.addMouseWheelListener(this);
        gameComponent.addKeyListener(this);
        gameComponent.addFocusListener(gameComponent);
        gameComponent.requestFocusInWindow();

        // Let the game set up the window, especially its size.
        window.size(100, 100);		// in case setup() has no size()
  		canvas.background(255);		// in case background is not called, default is white
        setup();

        // set default framerate if none in setup
        if (goalFPS == 0)
        	framerate(10);

        // Display the window.
        if (isApplet)
			this.setVisible(true);
		else
        {
			frame.pack();
			frame.setVisible(true);
		}

		// Set up the offscreen buffer
		if (isApplet)
			offscreen = this.createImage(canvas.width(), canvas.height());
		else
			offscreen = frame.createImage(canvas.width(), canvas.height());
		System.out.flush();
		offG = (Graphics2D)offscreen.getGraphics();
		offG.setColor(bgColor);
		offG.fillRect(0, 0, canvas.width(), canvas.height());
		offG.dispose();
		offG = null;

		createEdgeSprites();

		playing = true;
	}

	int r(double _x) { return (int)(Math.round(_x)); }  // should get rid of this


	// The following methods are meant to be called by the game code.

	/**
	 * Sets the desired framerate to the specified value.
	 * Ucigame will try to refresh the window <i>framerate</i>
	 * times per second (by calling the draw() method),
	 * but there is no guarantee that it will be successful.
	 * This method has no effect if <i>framerate</i> is negative or greater than 1000.
	 * In practice, most monitors can be refreshed at most 70 to 100 times per second.
	 */
	public final void framerate(double framerate)
	{
		int fr = (int)framerate;
		if (0 < fr && fr <= 1000)
		{
			goalFPS = fr;
			delayTime = 1000 / fr;
		}
		if (fpsTimer != null)
			fpsTimer.stop();
		fpsTimer = new javax.swing.Timer(1000, fpsChecker);
		fpsTimer.start();
	}

	/**
	 * Returns the number of times the game window has been refreshed in the last second.
	 */
	public final int actualFPS()
	{
		return fps;
	}

	/**
	 * Specifies a seed for the random number generator.
	 * The numbers returned by Ucigame's random() and randomInt() methods appear to be random,
	 * but are actually created by a specific formula, which uses a starting number called a seed.
	 * The same pseudorandom numbers will be generated by calls to random()
	 * and randomInt() if the same seed is used in randomSeed() (before any calls
	 * to random() or randomInt()).
	 * If this method is not used, then calls to random() and randomInt()
	 * will return different values each time the game is run.
	 */
	public final void randomSeed(int seed)
	{
		rand = new Random(seed);
	}

	/**
	 * Returns a random double greater than or equal to 0
	 * and less than limit.
	 * If limit is not positive, this method returns 0.0.
	 */
	public final double random(double limit)
	{
		if (rand == null)
			rand = new Random();
		if (limit > 0.0)
			return rand.nextDouble() * limit;
		else
			return 0.0;
	}

	/**
	 * Returns a random double greater than or equal to lowerlimit
	 * and less than or equal to upperlimit.
	 * If upperlimit is less than lowerlimit, this method
	 * returns lowerlimit.
	 */
	public final double random(double lowerlimit, double upperlimit)
	{
		if (rand == null)
			rand = new Random();
		if (upperlimit > lowerlimit)
			return (rand.nextDouble() * (upperlimit - lowerlimit)) + lowerlimit;
		else
			return lowerlimit;
	}

	/**
	 * Returns a random int greater than or equal to 0 and less than limit.
	 * If limit is not positive, this method returns 0.
	 */
	public final int randomInt(int limit)
	{
		if (rand == null)
			rand = new Random();
		if (limit <= 0)
			return 0;
		return rand.nextInt(limit);
	}

	/**
	 * Returns a random int greater than or equal to lowerlimit
	 * and less than upperlimit. If upperlimit is less than lowerlimit,
	 * this method returns lowerlimit.
	 */
	public final int randomInt(int lowerlimit, int upperlimit)
	{
		if (rand == null)
			rand = new Random();
		if (upperlimit > lowerlimit)
			return rand.nextInt(upperlimit-lowerlimit) + lowerlimit;
		return lowerlimit;
	}

	/**
	 * Returns an array of Strings; each element of the array is the
	 * name of a font installed on the computer.
	 */
	public final String[] arrayOfAvailableFonts()
	{
		return GraphicsEnvironment.
					getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}

	/**
	 * Returns true or false depending on whether the specified fontName is
	 * installed on the computer.
	 */
	public final boolean isAvailableFont(String fontName)
	{
		String fonts[] = GraphicsEnvironment.
					getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (String f : fonts)
		{
			if (f.equals(fontName))
				return true;
		}
		return false;
	}

	/**
	 * Returns an Image object created from the specified image file from disk.
	 * The file can be in
	 * any image format supported by Java (e.g. GIF, JPEG, PNG).
	 */
	public final Image getImage(String filename)  // dangerous having same name as Applet.getImage()?
	{
		java.awt.Image i;
		if (isApplet)
			i = getImage(getCodeBase(), filename);
		else
			i = Toolkit.getDefaultToolkit().getImage(filename);
		if (i == null)
			logError("getImage(" + filename + ") failed [1].");
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(i, 0);
		try { mt.waitForAll(); }
		catch (InterruptedException ie) {}

		//System.err.println("Done with image in " + filename +
		//	" width: " + i.getWidth(this) +
		//	" height: " + i.getHeight(this) +
		//	" is BufferedImage: " + (i instanceof BufferedImage) );
		//System.err.println(i);

		if (i.getWidth(this) == -1 || i.getHeight(this) == -1)
		{
			logError("getImage(" + filename + ") failed.");
			return null;
		}
		return new Image(i, this);
	}

	/**
	 * Returns an Image object created from the specified image file from disk;
	 * pixels with the specified shade of gray will be transparent.
	 * The file can be in
	 * any image format supported by Java (e.g. GIF, JPEG, PNG).
	 */
	public final Image getImage(String filename, int shade)
	{
		Image image = getImage(filename);
		image.transparent(shade);
		return image;
	}

	/**
	 * Returns an Image object created from the specified image file from disk;
	 * pixels of the specified color will be transparent.
	 * The file can be in
	 * any image format supported by Java (e.g. GIF, JPEG, PNG).
	 */
	public final Image getImage(String filename, int r, int g, int b)
	{
		Image image = getImage(filename);
		image.transparent(r, g, b);
		return image;
	}

	/**
	 * This method reads in the specified sound file from disk. If the file is in a format
	 * that cannot be played, subsequent calls to play() or loop() will have no effect.
	 */
	public final Sound getSound(String _filename)
	{
		if (_filename.toLowerCase().endsWith(".mp3"))
		{
			// make sure the file can be opened, for an immediate fail if not.
			if (!isApplet)
			{
				try {
					FileInputStream fis = new FileInputStream(_filename);
				}
				catch (FileNotFoundException fnf)
				{
					logError("getSound(" + _filename + ") failed [3].");
					return null;
				}
			}
			return new SoundMP3(_filename, this);
		}

		// not an mp3, use Applet's AudioClip functionality
		AudioClip clip;
		try {
			if (isApplet)
				clip = getAudioClip(getCodeBase(), _filename);
			else
				clip = Applet.newAudioClip(
							getClass().getResource(_filename));
		}
		catch (NullPointerException npe)
		{
			logError("getSound(" + _filename + ") failed [1].");
			return null;
		}
		if (clip == null)
		{
			logError("getSound(" + _filename + ") failed [2].");
			return null;
		}
		return new SoundAudioClip(clip, this);
	}

	/**
	 * Returns a Sprite object based on the specified Image object.
	 * The Sprite has the same width and height as the Image.
	 */
	public final Sprite makeSprite(Image image)
	{
		if (image == null ||
		    image.width() < 1 ||
		    image.height() < 1)
		{
			logError("makeSprite(Image): image is invalid.");
			return null;
		}
		return new Sprite(image);
	}

	/**
	 * Returns a Sprite with the specified width and height, but with no image(s).
	 * Usually used when multiple images will be added later
	 * to the Sprite with addFrame().
	 */
	public final Sprite makeSprite(int width, int height)
	{
		if (width < 1 || width > 1000 ||
		    height < 1 || height > 1000)
		{
			logError("makeSprite(image, " + width + ", " + height +
						") has an illegal parameter.");
			return null;
		}
		return new Sprite(width, height);
	}

	/**
	 * Returns a Sprite object based on the specified Image object,
	 * with the specified width and height.
	 * If width specified is larger than image's width,
	 * and/or height specified is larger than image's height, then the image will be tiled to
	 * cover the complete Sprite.
	 */
	public final Sprite makeSprite(Image image, int width, int height)
	{
		if (width < 1 || width > 2000 ||
		    height < 1 || height > 2000)
		{
			logError("makeSprite(image, " + width + ", " + height +
						") has an illegal parameter.");
			return new Sprite(image);
		}

		return new Sprite(image, width, height);
	}

	/**
	 * Returns a button Sprite with the specified name, based on the specified Image.
	 * A button Sprite can be composed of one or three images,
	 * all of which are in the Image object. If three images are supplied, then the first is
	 * the "at rest" appearance of the button, the second is the "mouse over" image, and the
	 * third is the "mouse down" image.
	 * The specified width and height are the dimensions of the button Sprite.
	 */
	public final Sprite makeButton(String name, Image image, int width, int height)
	{
		if (width < 1 || width > 2000 ||
		    height < 1 || height > 2000)
		{
			logError("makeButton(" + name + ", image, " + width + ", " + height +
						") has an illegal size parameter.");
			return new Sprite(5, 5);
		}
		if (name == null || name.length() == 0)
		{
			logError("makeButton(" + name + ", image, " + width + ", " + height +
						") has an illegal name.");
			return new Sprite(width, height);
		}
		if (image == null ||
		    image.width() < 1 ||
		    image.height() < 1)
		{
			logError("in makeButton(" + name + ") the image (second parameter) " +
					"is not valid.");
			return new Sprite(width, height);
		}
		if ( (image.width() == width && image.height() == height) ||
		     (image.width() == width && image.height() == 3*height) ||
		     (image.width() == 3*width && image.height() == height))
		     ; // good
		else
		{
			logError("in makeButton(" + name + ") the width and height of the " +
					"image must be (" + width + "," + height + ") or (" +
					(width*3) + "," + height + ") or (" +
					width + "," + (height*3) + ")\nFound width =" +
					image.width() + " height =" + image.height());
			return new Sprite(width, height);
		}
		Sprite s = new Sprite(width, height);
		s.addFrame(image, 0, 0);
		if (image.width() == width && image.height() == 3*height)
		{
			s.addFrame(image, 0, height);
			s.addFrame(image, 0, height*2);
		}
		else if (image.width() == 3*width) // ordered horizontally
		{
			s.addFrame(image, width, 0);
			s.addFrame(image, width*2, 0);
		}
		else // just a single image, use for all three frames
		{
			s.addFrame(image, 0, 0);
			s.addFrame(image, 0, 0);
		}

		if (methods == null)
			methods = this.getClass().getDeclaredMethods();
		boolean ok = false;
		for (Method m : methods)
		{
			if (m.getName().equals("onClick" + name) &&
				m.getReturnType().toString().equals("void") &&
				m.getParameterTypes().length == 0)
				{
					name2method.put(name, m);
					ok = true;
					break;
				}
		}
		if (!ok)
		{
			logError("Required method void onClick" + name + "() not found.");
			return s;
		}
		s.makeButton(name);
		synchronized(lock2)
		{
			buttonList2.add(s);
		}

		return s;
	}

	/**
	 * Returns a Sprite which is a matrix of tiles.
	 * Each tile has the specified width and height. The matrix can have any positive
	 * number of columns and rows.
	 * The Sprite's matrix entries are filled in with setTiles().
	 */
	public final Sprite makeTiledSprite(int cols, int rows, int tileWidth, int tileHeight)
	{
		if (cols < 1 || cols > 2000 ||
		    rows < 1 || rows > 2000)
		{
			logError("makeTiledSprite(" + cols + ", " + rows +
			                ", " + tileWidth + ", " + tileHeight +
						") has an illegal number of columns or rows.");
			return new Sprite(5, 5);
		}
		if (tileWidth < 1 || tileWidth > 2000 ||
		    tileHeight < 1 || tileHeight > 2000)
		{
			logError("makeTiledSprite(" + cols + ", " + rows +
			                ", " + tileWidth + ", " + tileHeight +
						") has an illegal tileWidth or tileHeight.");
			return new Sprite(5, 5);
		}
		return new Sprite(cols, rows, tileWidth, tileHeight);
	}


	private void createEdgeSprites()
	{
		int edgeWidth = 200;
		int overlap = 200;
		int halfOverlap = overlap / 2;
		TOPEDGE = new Sprite(canvas.width()+overlap, edgeWidth);
		TOPEDGE.position(-halfOverlap, -edgeWidth);
		TOPEDGE.hide();
		BOTTOMEDGE = new Sprite(canvas.width()+overlap, edgeWidth);
		BOTTOMEDGE.position(-halfOverlap, canvas.height());
		BOTTOMEDGE.hide();
		LEFTEDGE = new Sprite(edgeWidth, canvas.height()+overlap);
		LEFTEDGE.position(-edgeWidth, -halfOverlap);
		LEFTEDGE.hide();
		RIGHTEDGE = new Sprite(edgeWidth, canvas.height()+overlap);
		RIGHTEDGE.position(canvas.width(), -halfOverlap);
		RIGHTEDGE.hide();
	}

	/**
	 * Defines a new scene for the game.
	 * A scene is frequently part or all of a game level.
	 * Calling startScene(<i>SceneName</i>) has the following effects:
	 * <ol>
	 * <li> The current method completes normally.
	 * <li> If the program has a public method called start<i>SceneName</i>() with no parameters
	 * and returning void, then that method is performed.
	 * <li> Every time the window needs to be repainted, the method
	 * draw<i>SceneName</i>() is called (instead of draw()).
	 * This method must take no parameters, be public, and return void.
	 * <li> If the user presses a keyboard key and there is a public method
	 * called onKeyPress<i>SceneName</i> with no
	 * parameters and returning void, then that method is invoked.
	 * </ol>
	 */
	public final void startScene(String sceneName)
	{
		if (name2method.get(sceneName) == null)
		{
			if (methods == null)
				methods = this.getClass().getDeclaredMethods();
			boolean ok = false;
			for (Method m : methods)
			{
				if (m.getName().equals("draw" + sceneName) &&
					m.getReturnType().toString().equals("void") &&
					m.getParameterTypes().length == 0)
					{
						name2method.put(sceneName, m);
						ok = true;
						break;
					}
			}
			if (!ok)
			{
				logError("Required method void draw" + sceneName + "() not found.");
				return;
			}
		}
		currScene = sceneName;
		startSceneMethod = null;
		for (Method m : methods)
		{
			if (m.getName().equals("start" + sceneName) &&
				m.getReturnType().toString().equals("void") &&
				m.getParameterTypes().length == 0)
				{
					startSceneMethod = m;
				}
			if (m.getName().equals("onKeyPress" + sceneName) &&
				m.getReturnType().toString().equals("void") &&
				m.getParameterTypes().length == 0)
				{
					sceneKeyPressMethod = m;
				}
			if (m.getName().equals("onKeyRelease" + sceneName) &&
				m.getReturnType().toString().equals("void") &&
				m.getParameterTypes().length == 0)
				{
					sceneKeyReleaseMethod = m;
				}
		}
	}

	/**
	 * Causes the method <i>timerName</i>Timer() to be run milliBetween milliseconds later,
	 * and every millisBetween milliseconds afterwards.
	 * The <i>timerName</i>Timer() method must be defined as public, must return void,
	 * and must not take any parameters.
	 */
	public final void startTimer(String timerName, double millisBetween)
	{
		if (millisBetween < 1)
		{
			logError("Invalid second parameter in startTimer(" + timerName +
					 ", " + millisBetween + ")");
			return;
		}
		if (methods == null)
			methods = this.getClass().getDeclaredMethods();
		for (Method m : methods)
		{
			if (m.getName().equals(timerName + "Timer") &&
				m.getReturnType().toString().equals("void") &&
				m.getParameterTypes().length == 0)
				//*******NEED TO CHECK FOR PUBLIC********
				{
					timers2.add(new Timer( (long)millisBetween, m));
					return;
				}
		}
		logError("Required method public void " + timerName + "Timer() not found.");
		return;
	}

	/**
	 * Cancels subsequent executions of the <i>timerName</i>Timer() method.
	 */
	public final void stopTimer(String timerName)
	{
		for (Timer t : timers)
		{
			Method m = t.timerMethod;
			if (m.getName().equals(timerName + "Timer"))
			{
				timers3.add(t);		// a delayed timers.remove(t);
				return;
			}
		}
		for (Timer t : timers2)   // just in case a startTimer and stopTimer call are
		{                         // made closely together
			Method m = t.timerMethod;
			if (m.getName().equals(timerName + "Timer"))
			{
				timers2.remove(t);
				return;
			}
		}
		logError("No timer found with name " + timerName);
		return;
	}

	/**
	 * Prints information on the console.
	 */
	public final void print(String x)  { System.out.print(x); }
	public final void print(int x)     { System.out.print("" + x); }
	public final void print(short x)   { System.out.print("" + x); }
	public final void print(char x)    { System.out.print("" + x); }
	public final void print(double x)  { System.out.print("" + x); }
	public final void print(float x)   { System.out.print("" + x); }
	public final void print(long x)    { System.out.print("" + x); }
	public final void print(boolean x) { System.out.print("" + x); }
	public final void print(Object x)  { System.out.print(x); }

	/**
	 * Prints information on the console, followed by a new line.
	 */
	public final void println(String x)  { System.out.println(x); }
	public final void println(int x)     { System.out.println("" + x); }
	public final void println(short x)   { System.out.println("" + x); }
	public final void println(char x)    { System.out.println("" + x); }
	public final void println(double x)  { System.out.println("" + x); }
	public final void println(float x)   { System.out.println("" + x); }
	public final void println(long x)    { System.out.println("" + x); }
	public final void println(boolean x) { System.out.println("" + x); }
	public final void println(Object x)  { System.out.println(x); }

	/**
	 * Called by Ucigame once, at the start of the game.
	 * A game should override this method to make it
	 * set up the window, load images, create sprites,
	 * and perform other initialization events.
	 */
	public void setup() {
		window.size(100, 100);
		canvas.background(220);
	}

	/**
	 * Called by Ucigame every time the game window needs to be repainted.
	 * The window can be repainted because of the requested framerate of because part
	 * or all of the window has been exposed.
	 * A game should override this method.
	 */
	public void draw() {}

	/**
	 * Called by Ucigame when the player presses a key on the keyboard.
	 * If a key is held down and not released, the method
	 * may be invoked multiple times, depending on operating system parameters
	 * and whether keyboard.typematicOn() or keyboard.typematicOff() have been called.
	 * A game should override this method if it needs to react to key press events.
	 */
	public void onKeyPress() {}

	/**
	 * Called by Ucigame when the player releases a key on the keyboard.
	 * A game should override this method if it needs to react to key release events.
	 */
	public void onKeyRelease() {}

	/**
	 * Called by Ucigame when the player presses down a mouse button.
	 * A game should override this method if it needs to react to mouse button events.
	 */
	public void onMousePressed() {}

	/**
	 * Called by Ucigame when the player moves the mouse.
	 * A game should override this method if it needs to react to mouse motion events.
	 */
	public void onMouseMoved() {}

	/**
	 * Called by Ucigame when the player moves the mouse while holding down a mouse button.
	 * A game should override this method if it needs to react to mouse drag events.
	 */
	public void onMouseDragged() {}

	/**
	 * Called by Ucigame when the player releases a mouse button.
	 * A game should override this method if it needs to react to mouse button release events.
	 */
	public void onMouseReleased() {}

	/**
	 * Called by Ucigame when the mouse wheel is rotated.
	 * A game should override this method if it needs to react to mouse wheel events.
	 */
	public void onMouseWheelMoved() {}

	////////////////////// Listener implementations //////////////////////////

	// Called when the mouse is moved and a button is being held down.
	/**
	 * non-API
	 */
	public final void mouseDragged(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		mouseChangeX = mouseX - mousePrevX;
		mouseChangeY = mouseY - mousePrevY;
		mousePrevX = mouseX;
		mousePrevY = mouseY;
		if (e.getButton() != e.NOBUTTON)	// I'm getting NOBUTTON with drag,
			mouseButton = e.getButton();	// so leave mouseButton from Pressed.
		mouseIsAltDown = e.isAltDown();
		mouseIsControlDown = e.isControlDown();
		mouseIsMetaDown = e.isMetaDown();
		mouseIsShiftDown = e.isShiftDown();
		synchronized(lock2)
		{
			buttonList.addAll(buttonList2);
			buttonList2.clear();
			for (Sprite s : buttonList)
				s.buttonAction('D', mouseX, mouseY);
		}
		checkSpritesAndMouse();
		onMouseDragged();
	}

	// Called when the mouse is moved and no button is being held down.
	/**
	 * non-API
	 */
	public final void mouseMoved(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		mouseChangeX = 0;
		mouseChangeY = 0;
		mouseButton = e.getButton();
		mouseIsAltDown = e.isAltDown();
		mouseIsControlDown = e.isControlDown();
		mouseIsMetaDown = e.isMetaDown();
		mouseIsShiftDown = e.isShiftDown();
		//System.out.println("mouse x: " + mouseX + " mouse y: " + mouseY);
		synchronized(lock2)
		{
			buttonList.addAll(buttonList2);
			buttonList2.clear();
			for (Sprite s : buttonList)
				s.buttonAction('M', mouseX, mouseY);
		}
		checkSpritesAndMouse();
		onMouseMoved();
	}

	// Called when a mouse button is pressed down.
	/**
	 * non-API
	 */
	public final void mousePressed(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		mouseChangeX = 0;
		mouseChangeY = 0;
		mousePrevX = mouseX;
		mousePrevY = mouseY;
		mouseButton = e.getButton();
		mouseIsAltDown = e.isAltDown();
		mouseIsControlDown = e.isControlDown();
		mouseIsMetaDown = e.isMetaDown();
		mouseIsShiftDown = e.isShiftDown();
		synchronized(lock2)
		{
			buttonList.addAll(buttonList2);
			buttonList2.clear();
			for (Sprite s : buttonList)
				s.buttonAction('P', mouseX, mouseY);
		}
		checkSpritesAndMouse();
		onMousePressed();
	}

	// Called when a mouse button is released.
	/**
	 * non-API
	 */
	public final void mouseReleased(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		mouseChangeX = 0;
		mouseChangeY = 0;
		mouseButton = e.getButton();
		mouseIsAltDown = e.isAltDown();
		mouseIsControlDown = e.isControlDown();
		mouseIsMetaDown = e.isMetaDown();
		mouseIsShiftDown = e.isShiftDown();
		synchronized(lock2)
		{
			buttonList.addAll(buttonList2);
			buttonList2.clear();
			for (Sprite s : buttonList)
				s.buttonAction('R', mouseX, mouseY);
		}
		checkSpritesAndMouse();
		onMouseReleased();
	}

	// Called when the mouse wheel is rotated
	/**
	 * non-API
	 */
	public final void mouseWheelMoved(MouseWheelEvent e)
	{
		mouseWheelUnits = e.getWheelRotation();
		onMouseWheelMoved();
	}


	// Find topmost sprite (last in list) which is under the mouse position
	private void checkSpritesAndMouse()
	{
		mouseSprite = null;
		synchronized(lock1)  // prevent it from getting nulled out
		{
			for (int i=spritesFromBottomToTopList.size()-1; i >= 0; i--)
			{
				Sprite s = spritesFromBottomToTopList.get(i);
				if ( mouseX >= s.currX &&
					 mouseX <  (s.currX + s.width) &&
					 mouseY >= s.currY &&
					 mouseY <  (s.currY + s.height)
				   )
				{
					mouseSprite = s;
					break;
				}
			}
		}
	}

	final void addSpriteToList(Sprite _sprite)
	{
		synchronized(lock1)
		{
			spritesFromBottomToTopList.add(_sprite);
		}
	}

	/**
	 * non-API
	 */
	public final void mouseClicked(MouseEvent e) {}

	/**
	 * non-API
	 */
	public final void mouseEntered(MouseEvent e) {}

	/**
	 * non-API
	 */
	public final void mouseExited(MouseEvent e) {}

	/**
	 * non-API
	 */
	public final void keyPressed(KeyEvent e)
	{
		lastKeyPressed = e.getKeyCode();		// returned by keyboard.key()
		//System.out.println("lastKeyPressed: " + lastKeyPressed);

       //System.out.print(" ID=" + e.getID());
       //System.out.print(" KeyCode=" + e.getKeyCode());
       //System.out.print("KeyChar=\"" + e.getKeyChar() + "\" " + (int)e.getKeyChar());
       //System.out.println();

		shiftPressed = e.isShiftDown();
		ctrlPressed  = e.isControlDown();
		altPressed   = e.isAltDown();
		if (lastKeyPressed == e.VK_ESCAPE)
		{
			if (shiftPressed)
				suspended = !suspended;
			else
				playing = false;
		}
		else if (suspended && lastKeyPressed == e.VK_F1)
		{
			suspended = false;
			oneStep = true;
		}
		else
		{
			synchronized(lock3)
			{
				//System.out.println("=> " + e.getKeyCode() + " " + typematicOnForKey(e.getKeyCode());
				if (typematicOnForKey(e.getKeyCode()))
				    keysThatAreDown.put(e.getKeyCode(), "X");		// note that key is down
				else
				{
					String code = keysThatAreDown.get(e.getKeyCode());
					if (code == null)
						keysThatAreDown.put(e.getKeyCode(), "X");	// note that key is down
					// if (code is "X") then we've already noted it's down
					// if (code is "Y") then it's been handled by onKeyPress and further presses should be ignored
				}
			}
		}
	}

	/**
	 * non-API
	 */
	public final void keyReleased(KeyEvent e)
	{
		synchronized(lock3)
		{
			keysThatAreDown.remove(e.getKeyCode());
			keysThatHaveJustBeenReleased.put(e.getKeyCode(), "*");
		}
	}

	/**
	 * non-API
	 */
	public final void keyTyped(KeyEvent e)
	{
		lastKeyChar = e.getKeyChar();
	}


	/**
	 * non-API
	 */
	public final void focusGained(FocusEvent e) {
		gameComponent.requestFocus();
	}

	/**
	 * non-API
	 */
	public final void focusLost(FocusEvent e)
	{
		// all keys go "up" when focus is lost
		synchronized(lock3)
		{
			if (!keysThatAreDown.isEmpty())
				keysThatHaveJustBeenReleased.put(-1, "Doesn't matter"); // make non-empty
			keysThatAreDown.clear();
		}
	}

	// Updates the keysThatAreDown hashmap after onKeyPress has been called.
	// Keys that are flagged with an X -- key is down -- are reflagged to Y
	// if typematic is not on for that key. The Y allows Ucigame to suppress
	// further keyDown events for the key (generated by the OS or the user).
	private void processKeysThatAreDown()
	{
		for (int k : keysThatAreDown.keySet())
		{
			if (keysThatAreDown.get(k).equals("X") &&
			    !typematicOnForKey(k))
			{
				keysThatAreDown.put(k, "Y");
			}
		}
	}

	// Check if any keys are mapped to X, which means they are down
	// and should be handled as being down.
	private boolean keysThatAreDownHasDownKeys()
	{
		return keysThatAreDown.containsValue("X");
	}

	private boolean typematicOnForKey(int key)
	{
		if (keysWithTypematicDifferent.containsKey(key))  // key was in keysWithTypematicDifferent
			return !typematicIsOn;		// so return reverse of default
		else
			return typematicIsOn;		// else return default
	}


	/////// default scope methods called from within the package
	final void setCursor(int _c)  // called by Mouse.setCursor()
	{
		Cursor cursor = null;
		try {
			cursor = new Cursor(_c);
		}
		catch (IllegalArgumentException iae)
		{
			logError("mouse.setCursor() called with invalid argument.");
			return;
		}
		if (isApplet)
			Ucigame.this.setCursor(cursor);
		else
			gameComponent.setCursor(cursor);
	}

	final void setCursor(Image _image, int _x, int _y)
	{								 // called by Mouse.setCursor()
		Cursor cursor = null;
		Toolkit tk = Toolkit.getDefaultToolkit();
		cursor = tk.createCustomCursor(_image.getBufferedImage(),
								new Point(_x,_y),"customCursor");
		if (isApplet)
			Ucigame.this.setCursor(cursor);
		else
			gameComponent.setCursor(cursor);

	}


	static String version() { return VERSION; }


	////////////////////// Utility methods //////////////////////////
	////////////////////// with package visibility

	static int countOfErrors = 0;

	static void logError(String _s)
	{
		++countOfErrors;
		if (gameObject == null && countOfErrors > 1)  // we're an applet, and we've displayed the message already
		{
			System.err.println("#" + countOfErrors + ": " + _s);
			return;
		}
		JOptionPane.showMessageDialog(null, _s, "Ucigame error", JOptionPane.ERROR_MESSAGE);
		if (gameObject != null)
			System.exit(0);
	}

	static void logWarning(String _s)
	{
		JOptionPane.showMessageDialog(null, _s, "Ucigame error", JOptionPane.ERROR_MESSAGE);
	}

	private Graphics2D getOffG() { return offG; }

	ActionListener fpsChecker = new ActionListener() {
		int prevFrames = 0;
		public void actionPerformed(ActionEvent event) {
			fps = frames - prevFrames;
			window.setfps(fps);
			if (isApplet && window.showfps)
				showStatus("Ucigame fps: " + fps);
			if (fps > 0 & !suspended)
			{
				//System.out.print("goalFPS: " + goalFPS + " fps: " +
				//		fps + " old delayTime: " + delayTime);
				if (fps < goalFPS - 6)
					delayTime = delayTime - 2;
				else if (fps < goalFPS)
					delayTime = delayTime - 1;
				else if (fps > goalFPS + 6)
					delayTime = delayTime + 2;
				else if (fps > goalFPS)
					delayTime = delayTime + 1;
				// else fps == delayFPS
				if (delayTime < 1)
					delayTime = 1;
				//System.out.println(" new delayTime: " + delayTime);
			}
			prevFrames = frames;
		}
	};

	//inner class
	class Timer
	{
		private long blastOffTime;   // when the timer method should next be called
		private long pauseLength;    // interval
		private Method timerMethod;

		Timer(long _pause, Method _m)
		{
			blastOffTime = System.currentTimeMillis() + _pause;
			pauseLength = _pause;
			timerMethod = _m;
		}
	}

	// this non-static version is used by Applets
	class UcigameWorker extends SwingWorker<String, Void>
	{
		@Override
		protected void done()
		{
			workerIsDone = true;		// communicate with Applet.stop()
			//System.err.println("worker is done");
		}

		@Override
		public String doInBackground()
		{
        // Redraw the window periodically.
        while (playing && !isCancelled())
        {
			if (!suspended)
			{
				long now = System.currentTimeMillis();
				for (Timer t : timers)
				{
					if (t.blastOffTime < now)
					{
						t.blastOffTime += t.pauseLength;
						try {
							t.timerMethod.invoke(Ucigame.this);
						}
						catch (Exception ex) {
							ex.printStackTrace(System.err);
							logError("Exception3ta while invoking " + t.timerMethod.getName()
									+ "\n" + ex + "\n" + ex.getCause());
						}
					}
				}
				timers.addAll(timers2);		// append timers2 to the end of timers
				timers.removeAll(timers3);	// zap those in timers3
				timers2.clear();			// and clear timers2
				timers3.clear();			// likewise

				offG = (Graphics2D)offscreen.getGraphics();
				if (offG == null)
					logError("Internal error: null offG in doInBackground()");
				else
				{
					synchronized(lock1)
					{
						spritesFromBottomToTopList.removeAllElements();
					}
					if (currScene == null)
					{
						synchronized(lock3)
						{
							if (!keysThatHaveJustBeenReleased.isEmpty())
							{
								onKeyRelease();
								keysThatHaveJustBeenReleased.clear();
							}
							if (keysThatAreDown.isEmpty() ||
								!keysThatAreDownHasDownKeys())
								; // nothing to do
							else
							{
								onKeyPress();
								processKeysThatAreDown();
							}
							processKeysThatAreDown();
						}
						draw();
					}
					else
					{
						if (startSceneMethod != null)
						{
							try { startSceneMethod.invoke(ucigameAppletObject); }
							catch (Exception ex) {
								ex.printStackTrace(System.err);
								logError("Exception1 while invoking " + startSceneMethod.getName()
										+ "\n" + ex + "\n" + ex.getCause());
							}
							startSceneMethod = null;
						}
						synchronized(lock3)
						{
							if (!keysThatHaveJustBeenReleased.isEmpty())
							{
								if (sceneKeyReleaseMethod == null)
									onKeyRelease();
								else
								{
									try {
										sceneKeyReleaseMethod.invoke(isApplet? this : gameObject);
									}
									catch (Exception ex) {
										ex.printStackTrace(System.err);
										logError("Exception3azr while invoking " +
												sceneKeyReleaseMethod.getName() +
												"\n" + ex + "\n" + ex.getCause());
									}
								}
								keysThatHaveJustBeenReleased.clear();
							}
							if (!keysThatAreDown.isEmpty())
							{
								if (sceneKeyPressMethod == null)
									onKeyPress();
								else
								{
									try {
										sceneKeyPressMethod.invoke(ucigameAppletObject); }
									catch (Exception ex) {
										ex.printStackTrace(System.err);
										logError("Exception3az while invoking " +
												sceneKeyPressMethod.getName() +
												"\n" + ex + "\n" + ex.getCause());
									}
								}
								processKeysThatAreDown();
							}
						}
						Method m = name2method.get(currScene);  // get drawForScene
						try { m.invoke(ucigameAppletObject); }
						catch (Exception ex) {
							ex.printStackTrace(System.err);
							logError("Exception2 while invoking " + m.getName()
									+ "\n" + ex + "\n" + ex.getCause());
						}
					}
					frames++;
					offG.dispose();
					offG = null;
				}
				if (isApplet)
					repaint();
				else
					gameComponent.repaint();
				if (oneStep)
				{
					oneStep = false;
					suspended = true;
				}
			}
			if (goalFPS == 0)
				break;
			else
			{
				try { Thread.sleep(delayTime); }
				catch (InterruptedException ie) { break; }  // we're done
			}
		}
		//System.err.println("playing: " + playing + " isCancelled(): " + isCancelled());
		return "";
		}

	}

	// This class is used when running as an application, not applet.
	static class StaticUcigameWorker extends SwingWorker<String, Void>
	{
		@Override
		protected void done()
		{
			//workerIsDone = true;		// communicate with Applet.stop()
			//System.err.println("worker is done");
		}

		@Override
		public String doInBackground()
		{
        // Redraw the window periodically.
        while (gameObject.playing && !isCancelled())
        {
			if (!gameObject.suspended)
			{
				long now = System.currentTimeMillis();
				for (Timer t : gameObject.timers)
				{
					if (t.blastOffTime < now)
					{
						t.blastOffTime += t.pauseLength;
						try {
							t.timerMethod.invoke(gameObject.isApplet? this : gameObject);
						}
						catch (Exception ex) {
							ex.printStackTrace(System.err);
							logError("Exception3t while invoking " + t.timerMethod.getName()
									+ "\n" + ex + "\n" + ex.getCause());
						}
					}
				}
				gameObject.timers.addAll(gameObject.timers2);		// append timers2 to the end of timers
				gameObject.timers.removeAll(gameObject.timers3);	// zap those in timers3
				gameObject.timers2.clear();			// and clear timers2
				gameObject.timers3.clear();			// likewise

				gameObject.offG = (Graphics2D)gameObject.offscreen.getGraphics();
				if (gameObject.offG == null)
					logError("Internal error: null offG in doInBackground()");
				else
				{
					synchronized(lock1)
					{
						gameObject.spritesFromBottomToTopList.removeAllElements();
					}
					if (gameObject.currScene == null)
					{
						synchronized(lock3)
						{
							if (!gameObject.keysThatHaveJustBeenReleased.isEmpty())
							{
								gameObject.onKeyRelease();
								gameObject.keysThatHaveJustBeenReleased.clear();
							}
							if (gameObject.keysThatAreDown.isEmpty() ||
								!gameObject.keysThatAreDownHasDownKeys())
								; // nothing to do
							else
							{
								gameObject.onKeyPress();
								gameObject.processKeysThatAreDown();
							}
						}
						gameObject.draw();
					}
					else
					{
						if (gameObject.startSceneMethod != null)
						{
							try { gameObject.startSceneMethod.invoke(
										gameObject.isApplet? this : gameObject); }
							catch (Exception ex) {
								ex.printStackTrace(System.err);
								logError("Exception1x while invoking " + gameObject.startSceneMethod.getName()
										+ "\n" + ex + "\n" + ex.getCause());
							}
							gameObject.startSceneMethod = null;
						}
						synchronized(lock3)
						{
							if (!gameObject.keysThatHaveJustBeenReleased.isEmpty())
							{
								if (gameObject.sceneKeyReleaseMethod == null)
									gameObject.onKeyRelease();
								else
								{
									try {
										gameObject.sceneKeyReleaseMethod.invoke(gameObject.isApplet? this : gameObject);
									}
									catch (Exception ex) {
										ex.printStackTrace(System.err);
										logError("Exception3ar while invoking " +
												gameObject.sceneKeyReleaseMethod.getName() +
												"\n" + ex + "\n" + ex.getCause());
									}
								}
								gameObject.keysThatHaveJustBeenReleased.clear();
							}
							if (!gameObject.keysThatAreDown.isEmpty())
							{
								if (gameObject.sceneKeyPressMethod == null)
									gameObject.onKeyRelease();
								else
								{
									try {
										gameObject.sceneKeyPressMethod.invoke(gameObject.isApplet? this : gameObject);
									}
									catch (Exception ex) {
										ex.printStackTrace(System.err);
										logError("Exception3a while invoking " +
												gameObject.sceneKeyPressMethod.getName() +
												"\n" + ex + "\n" + ex.getCause());
									}
								}
								gameObject.processKeysThatAreDown();
							}
						}
						Method m = gameObject.name2method.get(gameObject.currScene);  // get drawForScene
						try {
							m.invoke(gameObject.isApplet? this : gameObject);
						}
						catch (Exception ex) {
							ex.printStackTrace(System.err);
							logError("Exception2x while invoking " + m.getName()
									+ "\n" + ex + "\n" + ex.getCause());
						}
					}
					gameObject.frames++;
					gameObject.offG.dispose();
					gameObject.offG = null;
				}
				if (gameObject.isApplet)
					gameObject.repaint();
				else
					gameObject.gameComponent.repaint();
				if (gameObject.oneStep)
				{
					gameObject.oneStep = false;
					gameObject.suspended = true;
				}
			}
			if (gameObject.goalFPS == 0)
				break;
			else
			{
				try { Thread.sleep(gameObject.delayTime); }
				catch (InterruptedException ie) { break; }  // we're done
			}
		}
		System.exit(0);
		return "";
		}

	}

}
