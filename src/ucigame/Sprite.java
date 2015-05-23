// Sprite.java

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

package ucigame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.*;
import java.awt.geom.AffineTransform;

/**
 * A Sprite object is an image that can be moved around in the game window. Ucigame
 * uses many kinds of sprites: they can be stationary; they can cycle between
 * multiple images, and they can act like buttons and be sensitive to mouse
 * clicks. Sprites can have text written on them, and multiple sprites can be
 * "pinned" together so that when one moves the pinned Sprites follow.
 */
public class Sprite {

	class PinnedSprite // inner class
	{
		Sprite sprite;
		int x;
		int y;

		PinnedSprite(Sprite _s, int _x, int _y) {
			sprite = _s;
			x = _x;
			y = _y;
		}
	}

	private Ucigame ucigame;
	private Vector<Image> frameImage = new Vector<Image>();
	private Image[][] tiledImages = null;
	private Vector<int[][]> transparencyBuffer = null;
	private int currFrame;
	private int numFrames;
	private int tileWidth = 0, tileHeight = 0;
	private int tileCols, tileRows;
	private double deltaX, deltaY;
	private double addOnceDeltaX, addOnceDeltaY;
	private double nextX, nextY;
	private double rotationDegrees;
	private double rotCenterX, rotCenterY;
	private boolean flipH = false, flipV = false;
	private Vector<PinnedSprite> pinnedSprites = new Vector<PinnedSprite>();
	private Sprite whoImPinnedTo = null;
	private int myPinnedX, myPinnedY;
	private boolean isButton = false;
	private String buttonName = null;
	private boolean isShown = true;
	private boolean Xcollision;
	private boolean Ycollision;
	private boolean contactOnThisBottom;
	private boolean contactOnThisRight;
	private Font spriteFont = null;
	private Color spriteFontColor = null;
	private double cumFrames = 0;
	private int spriteGoalFPS = 0;
	private boolean playingAnimationOnce = false;
	private Hashtable<String, int[]> sequences= new Hashtable<String, int[]>();
	private int[] sequenceFrames;
	private String currentSequence = "All";
	private String nextSequence = "";
	private int sequenceRepetitions = 0;
	private int indexWithinSequence = 0;
	private boolean playSequenceOnce = false;

	// package visible
	int width, height;
	double currX, currY;

	private void initialize()
	{
		ucigame = Ucigame.ucigameObject;
		currFrame = 0;
		rotationDegrees = 0;
		addOnceDeltaX = addOnceDeltaY = 0;
		deltaX = deltaY = 0;
	}

	/**
	 * Creates a Sprite with the same width and height as the specified Image.
	 * Usually used for creating Sprites with a single image.
	 */
	public Sprite(Image image)
	{
		if (image == null ||
		    image.width() < 1 ||
		    image.height() < 1)
		{
			Ucigame.logError("in Sprite constructor: image is invalid.");
		}
		initialize();
		frameImage.add(image);
		width = image.width();
		height = image.height();
		numFrames = 1;
	}

	/**
	 * Creates a tiled Sprite with the specified number of rows and columns.
	 * Each tile has the specified width and height.
	 * The Sprite can have any positive number of columns and rows.
	 * The Sprite's tiles are are assigned images with setTiles().
	 */
	public Sprite(int cols, int rows, int tileWidth, int tileHeight) {
		if (cols < 1 || cols > 2000 ||
		    rows < 1 || rows > 2000)
		{
			Ucigame.logError("in Sprite constructor (" + cols + ", " + rows +
			                ", " + tileWidth + ", " + tileHeight +
						") found an illegal number of columns or rows.");
		}
		if (tileWidth < 1 || tileWidth > 2000 ||
		    tileHeight < 1 || tileHeight > 2000)
		{
			Ucigame.logError("in Sprite constructor (" + cols + ", " + rows +
			                ", " + tileWidth + ", " + tileHeight +
						") found an illegal width or height.");
		}
		initialize();
		tileCols = cols;
		tileRows = rows;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		width = cols * tileWidth;
		height = rows * tileHeight;
		tiledImages = new Image[cols][];
		for (int col = 0; col < cols; col++)
			tiledImages[col] = new Image[rows];
		numFrames = 1;
	}

	/**
	 * Creates a Sprite with the specified width and height.
	 */
	// Likely to have multiple animated images.
	public Sprite(int w, int h) {
		if (w < 1 || w > 2000 ||
		    h < 1 || h > 2000)
		{
			Ucigame.logError("in Sprite constructor, the width or height is invalid");
		}
		initialize();
		width = w;
		height = h;
		numFrames = 0;
	}

	/**
	 * Creates a Sprite based on the specified Image object,
	 * with the specified width and height.
	 * If width specified is larger than image's width,
	 * and/or height specified is larger than image's height, then the image will be tiled to
	 * cover the complete Sprite.
	 */
	public Sprite(Image image, int width, int height)
	{
		if (width < 1 || width > 2000 ||
		    height < 1 || height > 2000)
		{
			Ucigame.logError("in Sprite constructor, the width or height is invalid");
		}
		initialize();
		BufferedImage newImage = new BufferedImage(width, height,
		                              BufferedImage.TYPE_INT_ARGB);
		// Copy image to buffered image
		Graphics g = newImage.createGraphics();
		for (int x=0; x < width; x+=image.iwidth)
		{
			for (int y=0;  y < height; y+=image.iheight)
				g.drawImage(image.buffImage, x, y, ucigame);
		}
		g.dispose();

		frameImage.add(new Image(newImage, ucigame));
		this.width = width;
		this.height = height;
		numFrames = 1;
	}

	/**
	 * Adds a frame to the Sprite. Each time the sprite is displayed on the
	 * canvas with draw(), it will automatically cycle to the next frame.
	 * The x and y parameters specify the
	 * upper left hand corner of a rectangle in the image that will be sprite's
	 * next frame. The width and height of that rectangle are same as the
	 * sprite's width and height.
	 */
	public void addFrame(Image image, int x, int y) {
		if (image == null) {
			ucigame.logError("addFrame(Image, " + x + ", " + y + "): "
					+ " first parameter (image) is null.");
			return;
		}
		if (x < 0 || y < 0) {
			ucigame.logError("addFrame(Image, " + x + ", " + y + "): "
					+ " invalid parameter (less than 0).");
			return;
		}
		BufferedImage newimage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[width * height + 1];
		BufferedImage source = image.getBufferedImage();
		if (x >= source.getWidth() || y >= source.getHeight()
				|| x + width > source.getWidth()
				|| y + height > source.getHeight()) {
			ucigame.logError("addFrame(Image, " + x + ", " + y + "): "
					+ " requested frame extends beyond image.");
			return;
		}
		source.getRGB(x, y, width, height, pixels, 0, width);
		for (int x1 = 0; x1 < width; x1++) // maybe should use BufferedImage.
		{ // setData(Raster)
			for (int y1 = 0; y1 < height; y1++) {
				newimage.setRGB(x1, y1, pixels[x1 + (y1 * width)]);
			}
		}
		frameImage.add(new Image(newimage, ucigame));
		numFrames++;
	}

	/**
	 * A variant of addFrame() which allows multiple frames from
	 * one Image object to be added to the sprite with one method call.
	 */
	public void addFrames(Image image, int... locations) {
		if (locations.length % 2 != 0) {
			ucigame.logError("addFrames() does not have an even number of x and y's.");
			return;
		}
		for (int p = 0; p < locations.length; p += 2)
			addFrame(image, locations[p], locations[p + 1]);
	}

	/**
	 * The method takes a frame from image (with an upper left hand corner as
	 * specified by x and y and with width and height as specified in
	 * makeTiledSprite()), and puts that frame into one or more of sprite1's
	 * tiles as specified by the col and row pairs. Note that counting columns
	 * and rows starts from zero, and that position (0, 0) is in the matrix's
	 * upper right hand corner. This method can only be used if sprite1 was
	 * created with makeTiledSprite.
	 */
	public void setTiles(Image _gameImage, int _x, int _y, int... _locations) {
		if (tileWidth == 0 || tileHeight == 0 || tiledImages == null) {
			ucigame.logError("Cannot call setTiles() unless the sprite was\n"
					+ "created with makeTiledSprite().");
			return;
		}
		if (_gameImage == null) {
			ucigame.logError("setTiles(Image, " + _x + ", " + _y + ", ...): "
					+ " first parameter (image) is null.");
			return;
		}
		if (_x < 0 || _y < 0) {
			ucigame.logError("setTiles(Image, " + _x + ", " + _y + ", ...): "
					+ " invalid parameter (less than 0).");
			return;
		}
		if (_locations.length % 2 != 0) {
			ucigame.logError("setTiles(Image, " + _x + ", " + _y + ", ...): "
					+ "not an even number of cols and rows.");
			return;
		}
		if (_locations.length == 0) {
			ucigame.logError("setTiles(Image, " + _x + ", " + _y + ", ...): "
					+ "no cols and rows specified.");
			return;
		}

		BufferedImage newimage = new BufferedImage(tileWidth, tileHeight,
				BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[tileWidth * tileHeight + 1];
		BufferedImage source = _gameImage.getBufferedImage();
		if (_x >= source.getWidth() || _y >= source.getHeight()
				|| _x + tileWidth > source.getWidth()
				|| _y + tileHeight > source.getHeight()) {
			ucigame.logError("setTiles(Image, " + _x + ", " + _y + "): "
					+ " requested frame extends beyond image.");
			return;
		}
		source.getRGB(_x, _y, tileWidth, tileHeight, pixels, 0, tileWidth);
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				newimage.setRGB(x, y, pixels[x + (y * tileWidth)]);
			}
		}
		Image tileImage = new Image(newimage, ucigame);
		for (int p = 0; p < _locations.length; p += 2) {
			int col = _locations[p];
			int row = _locations[p + 1];
			if (col < 0 || col >= tileCols || row < 0 || row >= tileRows) {
				ucigame.logError("setTiles(Image, " + _x + ", " + _y
						+ ", ...): " + " col " + col + " or row " + row
						+ " is invalid");
				return;
			}
			tiledImages[col][row] = tileImage;
		}
	}

	/**
	 * Pins the sprite (_sprite) passed in on top of current sprite (this),
	 * which means that _sprite will be drawn whenever this.draw() is
	 * performed. The upper left hand corner of _sprite is located x pixels to
	 * the right and y pixels below the upper left hand corner of this; thus
	 * _sprite moves when this is moved. Any number of other sprites can be
	 * pinned to a sprite. A sprite cannot be pinned to itself. If a sprite is
	 * hidden, all sprites pinned to it are hidden.
	 */
	public void pin(Sprite _sprite, int _x, int _y)
	{
		if (this == _sprite)
			return;

		for (PinnedSprite ps : pinnedSprites)
		{
			if (ps.sprite == _sprite) // this is a "re-pin"
			{
				ps.x = _x;
				ps.y = _y;
				_sprite.myPinnedX = _x;
				_sprite.myPinnedY = _y;
				return;
			}
		}

		// this is a first time pin of _sprite to this
		pinnedSprites.add(new PinnedSprite(_sprite, _x, _y));
		if (_sprite.whoImPinnedTo == null)
		{
			_sprite.whoImPinnedTo = this;  // keep track of first "parent"
			_sprite.myPinnedX = _x;
			_sprite.myPinnedY = _y;
		}
	}

	/**
	 * Sets a framerate for this sprite; Ucigame will try to change the sprite's
	 * current frame number times per second, although there is no guarantee
	 * that it will be successful. This method has no effect if number is
	 * negative or greater than the framerate specified for the entire game.
	 */
	public void framerate(int _d) {
		if (_d == 0) {
			spriteGoalFPS = 0; // turn it off
			return;
		} else if (0 < _d && _d <= 1000) {
			spriteGoalFPS = _d;
			cumFrames = 0;
		} else
			ucigame.logError("sprite.framerate(" + _d
					+ ") has an invalid parameter.");
	}

	/**
	 * Rotates the sprite counterclockwise by the number of degrees specified.
	 * The center of rotation is the center of the sprite. Any sprites pinned to
	 * this sprite are also rotated around the same center of rotation. Note
	 * that rotation occurs immediately before sprite.draw() is executed, no
	 * matter when the call to rotate() is made (as long as rotate() is called
	 * before draw()). This means that rotation does not affect collision
	 * detection; collision detection is computed on the unrotated sprite.
	 * (Defined as a feature, but could well be a bug.) After draw() is called
	 * on a sprite any rotation is removed; thus if a sprite should be rotated
	 * in every frame rotate() must be called before each draw().
	 */
	public void rotate(double degrees) {
		rotationDegrees = degrees;
		rotCenterX = (double) width / 2.0;
		rotCenterY = (double) height / 2.0;
	}

	/**
	 * Same as the one parameter version of rotate(), except that the center of
	 * rotation is x pixels to the right and y pixels down from the upper left
	 * hand corner of the sprite.
	 */
	public void rotate(double degrees, double _rotCenterX, double _rotCenterY) {
		rotationDegrees = degrees;
		rotCenterX = _rotCenterX;
		rotCenterY = _rotCenterY;
	}

	/**
	 * This methods flip the sprite around a vertical line running through the
	 * center of the sprite.
	 */
	public void flipHorizontal() {
		flipH = true;
	}

	/**
	 * This methods flip the sprite around a horizontal line running through the
	 * center of the sprite.
	 */
	public void flipVertical() {
		flipV = true;
	}

	public void defineSequence(String sequenceName, int... indices)
	{
		if (isButton)
		{
			ucigame.logError("Cannot use defineSequence when Sprite is a button");
			return;
		}
		if (numFrames == 0)
		{
			ucigame.logError("Cannot use defineSequence when Sprite has no frames");
			return;
		}
		if (sequenceName == null || sequenceName.length() == 0)
		{
			ucigame.logError("Invalid sequence name in defineSequence");
			return;
		}
		if (sequenceName.equals("All"))
		{
			ucigame.logError("The sequence name \"All\" cannot be used in defineSequence");
			return;
		}
		if (indices == null || indices.length == 0)
		{
			ucigame.logError("Invalid or missing indices in defineSequence");
			return;
		}

		for (int i : indices)
		{
			if (i < 0 || i >= numFrames)
			{
				ucigame.logError("Invalid index in defineSequence: " + i);
				return;
			}
		}
		sequences.put(sequenceName, indices);
	}

	public String currSequence()
	{
		return currentSequence;
	}


	public void play(String sequenceName)
	{
		play(sequenceName, -3214);	// -3214 is an arbitrary internal flag
	}

	public void play(String sequenceName, int once)
	{
		if (!sequenceName.equals("All") && sequences.get(sequenceName) == null)
		{
			ucigame.logError("Sprite.play() called with invalid sequence name: " + sequenceName);
			return;
		}
		if (once != ucigame.ONCE && once != -3214 )
		{
			ucigame.logError("Sprite.play() called with invalid parameter; use ONCE or omit.");
			return;
		}

		// If we're switching to a different sequence, then start at the beginning.
		if (!sequenceName.equals(currentSequence))
		{
			currentSequence = sequenceName;
			indexWithinSequence = 0;
		}
		sequenceRepetitions = 0;
		nextSequence = "";
		playSequenceOnce = (once == ucigame.ONCE);
		if (currentSequence.equals("All"))
			sequenceFrames = null;
		else
			sequenceFrames = sequences.get(currentSequence);
	}

	public void play(String sequenceNameOnce, String sequenceNameLoop)
	{
		play(sequenceNameOnce, 1, sequenceNameLoop, -3214);
	}

	public void play(String sequenceNameOnce, String sequenceNameLoop, int once)
	{
		play(sequenceNameOnce, 1, sequenceNameLoop, once);
	}

	public void play(String sequenceNameRepeat, int repetitions, String sequenceNameLoop)
	{
		play(sequenceNameRepeat, repetitions, sequenceNameLoop, -3214);
	}

	public void play(String sequenceNameRepeat, int repetitions, String sequenceNameLoop,
						int once)
	{
		if (!sequenceNameRepeat.equals("All") && sequences.get(sequenceNameRepeat) == null)
		{
			ucigame.logError("Sprite.play() called with invalid sequence name: " + sequenceNameRepeat);
			return;
		}
		if (!sequenceNameLoop.equals("All") && sequences.get(sequenceNameLoop) == null)
		{
			ucigame.logError("Sprite.play() called with invalid sequence name: " + sequenceNameLoop);
			return;
		}
		if (repetitions < 1)
		{
			ucigame.logError("Sprite.play() called with invalid repetition number: " + repetitions);
			return;
		}
		if (once != ucigame.ONCE && once != -3214 )
		{
			ucigame.logError("Sprite.play() called with invalid parameter; use ONCE or omit.");
			return;
		}
		// If we're switching to a different sequence, then start at the beginning.
		if (!sequenceNameRepeat.equals(currentSequence))
		{
			currentSequence = sequenceNameRepeat;
			indexWithinSequence = 0;
		}
		sequenceRepetitions = repetitions;
		nextSequence = sequenceNameLoop;
		playSequenceOnce = (once == ucigame.ONCE);
		if (currentSequence.equals("All"))
			sequenceFrames = null;
		else
			sequenceFrames = sequences.get(currentSequence);
	}

	public void restart() {
		setToFrame(0);
	}

	public void setToFrame(int frameSequence) {
		if (isButton)
			return;
		if (frameSequence < 0)
			return;
		if (currentSequence.equals("All"))
		{
			if (frameSequence < numFrames)
				currFrame = frameSequence;
		}
		else
		{
			if (frameSequence < sequenceFrames.length)
				indexWithinSequence = frameSequence;
		}
	}

/*
	public void playAnimationOnce() {
		if (isButton || numFrames == 0)
			return;
		setToFrame(0);
		playingAnimationOnce = true;
	}
*/


	/**
	 * Causes the sprite, and any other sprites pinned to this sprite, to be
	 * drawn on the canvas, unless the sprite is hidden. Can only be called
	 * inside the game class's draw() method.
	 */
	public void draw() {
		draw(new AffineTransform(), 0, 0); // call with identity * no parent from pinning
	}

	/**
	 * The drawn location of a sprite is based on:
	 *   * its currX and currY
	 *   * its rotation
	 *   * if it is pinned, its pinned location relative to the corner of its parent
	 *   * if it is pinned, anything done to its parent (translation or rotation)
	 */
	private void draw(AffineTransform _Tx, double _pinnedX, double _pinnedY) {
		if (!isShown)
			return;
		if (numFrames == 0)
			return;
		currX = nextX;
		currY = nextY;

		AffineTransform at = new AffineTransform(_Tx);
		at.translate(currX + rotCenterX + _pinnedX, currY + rotCenterY + _pinnedY); // third
		at.rotate(rotationDegrees * Math.PI / 180.0); // second
		at.translate(-rotCenterX, -rotCenterY); // first
		if (flipH || flipV) // flips happen, logically, before rotations
		{
			at.translate((double) width / 2.0, (double) height / 2.0); // third
			at.scale(flipH ? -1 : 1, flipV ? -1 : 1); // second
			at.translate(-(double) width / 2.0, -(double) height / 2.0);// first
			flipH = flipV = false;
		}
		if (tiledImages == null)
			frameImage.get(currFrame).draw(at);
		else {
			for (int col = 0; col < tileCols; col++)
				for (int row = 0; row < tileRows; row++) {
					if (tiledImages[col][row] != null) {
						AffineTransform at2 = new AffineTransform(_Tx);
						at2.translate(currX + rotCenterX + (col * tileWidth),
								currY + rotCenterY + (row * tileHeight));
						at2.rotate(rotationDegrees * Math.PI / 180.0);
						at2.translate(-rotCenterX, -rotCenterY);
						tiledImages[col][row].draw(at2);
					}
				}
		}
		ucigame.addSpriteToList(this);
		if (!isButton)
			setCurrFrame();
/*
		{
			if (spriteGoalFPS == 0)
			{
				currFrame = (currFrame + 1) % numFrames;
				if (playingAnimationOnce && currFrame == 0) // don't go back to 0,
					currFrame = numFrames - 1;				// stay on last frame
			}
			else {
				cumFrames += spriteGoalFPS;
				if (cumFrames >= ucigame.goalFPS) {
					cumFrames -= ucigame.goalFPS;
					currFrame = (currFrame + 1) % numFrames;
					if (playingAnimationOnce && currFrame == 0) // don't go back to 0,
						currFrame = numFrames - 1;				// stay on last frame
				}
			}
		}
*/
		for (PinnedSprite ps : pinnedSprites)
		{
			ps.sprite.draw(at, ps.x, ps.y);
			ucigame.addSpriteToList(ps.sprite);
		}
		rotationDegrees = rotCenterX = rotCenterY = 0;
	}

	// Sets currFrame, which is an index into the frameImage Vector,
	// to point to the correct next frame.
	private void setCurrFrame()
	{
		if (spriteGoalFPS > 0)
		{
			cumFrames += spriteGoalFPS;
			if (cumFrames < ucigame.goalFPS)		// don't go to next frame yet
				return;
			cumFrames -= ucigame.goalFPS;
		}

		if (currentSequence.equals("All"))
		{
			currFrame++;
			if (currFrame < numFrames)
				return;
			// we've gone past the end of the current sequence
			if (nextSequence.length() == 0)	// no next sequence, stay on this one
			{
				if (playSequenceOnce)
					currFrame--;				// stay on last frame
				else
					currFrame = 0;				// go back to start of sequence
			}
			else							// there is a next sequence
			{
				sequenceRepetitions--;		// finished one repetition
				if (sequenceRepetitions > 0)
				{
					currFrame = 0;				// start at the beginning
				}
				else					// go to next sequence
				{
					currentSequence = nextSequence;
					nextSequence = "";
					if (currentSequence.equals("All"))
					{
						sequenceFrames = null;
						currFrame = 0;
					}
					else
					{
						sequenceFrames = sequences.get(currentSequence);
						indexWithinSequence = 0;
						currFrame = sequenceFrames[indexWithinSequence];
					}
				}
			}
		}
		else
		{
			indexWithinSequence++;
			if (indexWithinSequence < sequenceFrames.length)
			{
				currFrame = sequenceFrames[indexWithinSequence];
				return;
			}
			// we've gone past the end of the current sequence
			if (nextSequence.length() == 0)	// no next sequence, stay on this one
			{
				if (playSequenceOnce)
					indexWithinSequence--;				// stay on last frame
				else
					indexWithinSequence = 0;			// go back to start of sequence
				currFrame = sequenceFrames[indexWithinSequence];
			}
			else							// there is a next sequence
			{
				sequenceRepetitions--;		// finished one repetition
				if (sequenceRepetitions > 0)
				{
					indexWithinSequence = 0;			// go back to start of sequence
					currFrame = sequenceFrames[indexWithinSequence];
				}
				else					// go to next sequence
				{
					currentSequence = nextSequence;
					nextSequence = "";
					if (currentSequence.equals("All"))
					{
						sequenceFrames = null;
						currFrame = 0;
					}
					else
					{
						sequenceFrames = sequences.get(currentSequence);
						indexWithinSequence = 0;
						currFrame = sequenceFrames[indexWithinSequence];
					}
				}
			}
		}

	}

	/**
	 * This method specifies the font to use in subsequent calls to
	 * sprite1.putText().
	 */
	public void font(String _name, int _style, int _size) {
		font(_name, _style, _size, 0, 0, 0);
	}

	public void font(String _name, int _style, int _size, int _r, int _g, int _b) {
		if (_style == ucigame.BOLD || _style == ucigame.PLAIN ||
			_style == ucigame.ITALIC || _style == ucigame.BOLDITALIC)
			;
		else {
			ucigame.logError("Invalid style parameter in Sprite.font()");
			_style = ucigame.PLAIN;
		}
		spriteFont = new Font(_name, _style, _size);
		// System.out.println("Font: " + spriteFont);
		if (spriteFont.getFamily().equalsIgnoreCase(_name)
				|| spriteFont.getFontName().equalsIgnoreCase(_name))
			;
		else
			ucigame.logWarning("Could not create font with name " + _name
					+ ". Using font " + spriteFont.getFontName() + " instead.");
		if (0 <= _r && _r <= 255 && 0 <= _g && _g <= 255 && 0 <= _b && _b <= 255)
			spriteFontColor = new Color(_r, _g, _b);
		else
			spriteFontColor = Color.BLACK;
	}

	/**
	 * Draws the specified int on the sprite, using the sprite's current font.
	 * The lower left hand corner of text is located x pixels to the right and y
	 * pixels below the upper left hand corner of sprite1. Any part of text that
	 * lies outside the sprite is clipped (not displayed).
	 */
	public void putText(int _n, double _x, double _y) {
		putText("" + _n, _x, _y);
	}

	public void putText(double _d, double _x, double _y) {
		putText("" + _d, _x, _y);
	}

	/**
	 * Draws the specified text on the sprite, using the sprite's current font.
	 * The lower left hand corner of text is located x pixels to the right and y
	 * pixels below the upper left hand corner of sprite1. Any part of text that
	 * lies outside the sprite is clipped (not displayed).
	 */
	public void putText(String _string, double _x, double _y) {
		if (ucigame.offG == null) {
			ucigame.logError("Sprite.putText(" + _string + "," + _x + ", "
					+ _y + ") used outside of draw()");
			return;
		}
		if (isShown) {
			ucigame.offG.setClip(xPixel(), yPixel(), width, height);
			if (spriteFont != null)
				ucigame.offG.setFont(spriteFont);
			Color prevColor = ucigame.offG.getColor();
			if (spriteFontColor != null)
				ucigame.offG.setColor(spriteFontColor);
			ucigame.offG.drawString(_string, (int) (x() + _x), (int) (y() + _y));
			ucigame.offG.setClip(null);
			ucigame.offG.setColor(prevColor);
		}
	}

	/**
	 * This method returns the sprite's width.
	 */
	public int width() {
		return width;
	}

	/**
	 * This method returns the sprite's height.
	 */
	public int height() {
		return height;
	}

	/**
	 * Hide the sprite from view.
	 */
	public void hide() {
		isShown = false;
	}

	/**
	 * Show the sprite.
	 */
	public void show() {
		isShown = true;
	}

	/**
	 * Check to make sure the sprite is set to be visible.
	 */
	public boolean isShown() {
		return isShown;
	}

	/**
	 * Moves the sprite so that its upper left hand corner is at the specified
	 * position, relative to the upper left hand corner of the canvas (or to
	 * the upper left hand corner of its parent, if this sprite is pinned).
	 */
	public void position(double _x, double _y) {
		currX = _x;
		currY = _y;
		nextX = _x;
		nextY = _y;
	}

	/**
	 * Sets the x and y motion amount for this sprite. See move().
	 */
	public void motion(double _x, double _y) {
		deltaX = _x;
		deltaY = _y;
	}

	/**
	 * Change the motion amount base on the command provided.
	 */
	public void motion(double _x, double _y, int _COMMAND) {
		if (_COMMAND == ucigame.SET) {
			deltaX = _x;
			deltaY = _y;
		} else if (_COMMAND == ucigame.ADD) {
			deltaX += _x;
			deltaY += _y;
		} else if (_COMMAND == ucigame.ADDONCE) {
			addOnceDeltaX += _x;
			addOnceDeltaY += _y;
		} else if (_COMMAND == ucigame.MULTIPLY) {
			deltaX *= _x;
			deltaY *= _y;
		} else
			ucigame.logError("motion(" + _x + ", " + _y
					+ ", ???) -- last parameter not valid.");
	}

	/**
	 * Moves the sprite by its x and y motion amounts. Typically this method is
	 * called once for every moving sprite, before collision detection.
	 */
	public void move() {
		nextX = currX + deltaX + addOnceDeltaX;
		nextY = currY + deltaY + addOnceDeltaY;
		addOnceDeltaX = addOnceDeltaY = 0;
	}

	public double x() {
		if (whoImPinnedTo == null)
			return currX;
		else
			return currX + whoImPinnedTo.x() + myPinnedX;
	}

	public double y() {
		if (whoImPinnedTo == null)
			return currY;
		else
			return currY + whoImPinnedTo.y() + myPinnedY;
	}

	private double nextx() {
		if (whoImPinnedTo == null)
			return nextX;
		else
			return nextX + whoImPinnedTo.nextx() + myPinnedX;
	}

	private double nexty() {
		if (whoImPinnedTo == null)
			return nextY;
		else
			return nextY + whoImPinnedTo.nexty() + myPinnedY;
	}

	/**
	 * This method returns the x value of the sprite's upper left hand corner
	 * (relative to the upper left hand corner of the canvas).
	 *
	 * @return the x value of the sprite's upper left hand corner
	 */
	public int xPixel() {
		return (int) (Math.round(x()));  // rounding seems to match what draw() is actually doing
	}

	/**
	 * This method returns the y values of the sprite's upper left hand corner
	 * (relative to the upper left hand corner of the canvas).
	 *
	 * @return the y value of the sprite's upper left hand corner
	 */
	public int yPixel() {
		return (int) (Math.round(y()));
	}

	/**
	 * Sets the sprite's next x positions (relative to the upper left hand
	 * corner of the canvas). This method is usually used instead of move(), but
	 * like move() is called before collision detection and before draw().
	 * Calling this method sets the sprite's x motion amounts to 0.
	 *
	 * @param _nextX
	 *            the next x value for the sprite
	 */
	public void nextX(double _nextX) {
		nextX = _nextX;
		deltaX = 0;
	}

	/**
	 * Sets the sprite's next y positions (relative to the upper left hand
	 * corner of the canvas). This method is usually used instead of move(), but
	 * like move() is called before collision detection and before draw().
	 * Calling this method sets the sprite's y motion amounts to 0.
	 *
	 * @param _nextY
	 *            the next y value for the sprite
	 */
	public void nextY(double _nextY) {
		nextY = _nextY;
		deltaY = 0;
	}

	/**
	 * This methods return the sprite's current x change amounts.
	 *
	 * @return the sprite's x change amount
	 */
	public double xspeed() {
		return deltaX;
	}

	/**
	 * This methods return the sprite's current y change amounts.
	 */
	public double yspeed() {
		return deltaY;
	}

	/**
	 * Returns a boolean value of true or false, depending on
	 * whether an collision was detected in the immediately preceeding
	 * xxxIfCollidesWith() call.
	 */
	public boolean collided(int... sides) {
		if (sides.length == 0) // any side
			return Xcollision || Ycollision;
		for (int side : sides) {
			if (side == ucigame.LEFT && Xcollision && !contactOnThisRight)
				return true;
			else if (side == ucigame.RIGHT && Xcollision && contactOnThisRight)
				return true;
			else if (side == ucigame.TOP && Ycollision && !contactOnThisBottom)
				return true;
			else if (side == ucigame.BOTTOM && Ycollision
					&& contactOnThisBottom)
				return true;
			if (side != ucigame.LEFT && side != ucigame.RIGHT
					&& side != ucigame.TOP && side != ucigame.BOTTOM)
				ucigame.logError("collided() called with illegal value");
		}
		return false;
	}

	/**
	 * Makes a button with the specified name.
	 */
	public void makeButton(String _name) {
		isButton = true;
		buttonName = _name;
	}

	// d drag, m move, p press r release, x, y);
	public final void buttonAction(char _action, int _x, int _y) {
		if (!(isShown && isButton))
			return;
		boolean over = _x >= x() && _x < x() + width &&
		               _y >= y() && _y < y() + height;
		// System.out.println("action: " + _action + " over: " + over);
		if (_action == 'M' && over) // mouseMove event
		{
			if (currFrame == 0)
				currFrame = 1;
		} else if (_action == 'M' && !over) // mouseMove event
		{
			if (currFrame == 1 || currFrame == 2)
				currFrame = 0;
		} else if (_action == 'D' && over) // mouseDrag event
		{
			;
		} else if (_action == 'D' && !over) // mouseDrag event
		{
			;
		} else if (_action == 'P' && over) // mousePressed event
		{
			if (currFrame == 0 || currFrame == 1)
				currFrame = 2;
		} else if (_action == 'P' && !over) // mousePressed event
		{
			;
		} else if (_action == 'R' && over) // mouseReleased event
		{
			if (currFrame == 2) {
				currFrame = 1;
				Method m = ucigame.name2method.get(buttonName);
				try {
					m.invoke(ucigame.isApplet ? ucigame : ucigame.gameObject);
				} catch (Exception e) {
					e.printStackTrace(System.err);
					ucigame.logError("Exception4 while invoking " + m.getName()
							+ "\n" + e + "\n" + e.getCause());
				}
			}
		} else if (_action == 'R' && !over) // mouseReleased event
		{
			if (currFrame == 2)
				currFrame = 0;
		}
	}

	private final int BOUNCE = 1234321;
	private final int STOP   = 1234322;
	private final int CHECK  = 1234567;
	private final int PAUSE  = 1234568;

	/**
	 * This method tests whether this sprite overlaps with the sprite passed in
	 * and bounce if collision occurs.
	 */
	public void bounceIfCollidesWith(Sprite... _sprite) {
		somethingIfCollidesWith("bounce", BOUNCE, _sprite);
	}

	/**
	 * This method tests whether this sprite overlaps with the sprite passed in
	 * and stop movement if collision occurs.
	 */
	public void stopIfCollidesWith(Sprite... _sprite) {
		somethingIfCollidesWith("stop", STOP, _sprite);
	}

	/**
	 * Like the other xxxIfCollidesWith() methods, this methods test whether the
	 * two sprites will overlap in the next x and y positions. However, this
	 * methods don't change the position or motion of either sprite. The
	 * programmer can then test whether an overlap occurred with collided() and
	 * code the desired behavior.
	 */
	public void checkIfCollidesWith(Sprite... _sprite) {
		somethingIfCollidesWith("check", CHECK, _sprite);
	}

	/**
	 * This method tests whether this sprite overlaps with the sprite passed in
	 * and pauses. Pausing means that sprite1 moves as far as possible without
	 * overlapping sprite2, and its x and y motion amounts are unchanged.
	 */
	public void pauseIfCollidesWith(Sprite... _sprite) {
		somethingIfCollidesWith("pause", PAUSE, _sprite);
	}

	/**
	 * Check for collision and perform appropriate actions.
	 */
	private void somethingIfCollidesWith(
			String _name, int _action, Sprite... _sprite) {
		if (_sprite.length == 0 ||
		    (_sprite.length == 1 && _sprite[0] == Ucigame.PIXELPERFECT))
			ucigame.logError(_name + "IfCollidesWith called with no Sprite specified.");

		boolean pixelPerfect = (_sprite[_sprite.length-1] == ucigame.PIXELPERFECT);

		// check each sprite listed, after initializing flags to false
		Xcollision = false;
		Ycollision = false;
		contactOnThisBottom = false;
		contactOnThisRight = false;
		for (Sprite s : _sprite)
		{
			if (s == ucigame.PIXELPERFECT)	// skip this flag
				continue;
			if (pixelPerfect)
				ifCollidesWithPixelPerfect(s, _action);
			else
				ifCollidesWith(s, _action);
		}
	}

	// for right now, just based on final (next) positions, not on entire trajectory
	private void ifCollidesWith(Sprite _sprite, int _action)
	{
		double XcollisionTime = 0;
		double YcollisionTime = 0;
		boolean XcollisionThisSprite = false;		// based on _sprite
		boolean YcollisionThisSprite = false;

		if (_sprite == null)
			return;
		if (!overlapsWith(_sprite)) {
			return;
		}
		if (_action == CHECK) {
			Xcollision = Ycollision = true;
			return;
		}

		if (_sprite == ucigame.BOTTOMEDGE)
		{
			if (nexty() + height >= ucigame.window.clientHeight())
			{
				if(y() + height > ucigame.window.clientHeight())	// already out of screen
					YcollisionTime = 0;
				else
					YcollisionTime = (ucigame.window.clientHeight() - (y() + height))
								   / (nexty() - y());
				Ycollision = true;
				YcollisionThisSprite = true;
				contactOnThisBottom = true;
			}
		}
		else if (_sprite == ucigame.TOPEDGE)
		{
			if (nexty() < 0)
			{
				if (y() < 0)	// already out of screen
					YcollisionTime = 0;
				else
					YcollisionTime = -y()	/ (nexty() - y());
				Ycollision = true;
				YcollisionThisSprite = true;
				contactOnThisBottom = false;
			}
		}

		// this's bottom hit other's top
		else if (y() + height <= _sprite.y() && nexty() + height > _sprite.nexty())
		{
			if ((_sprite.nexty() - (y() + height)) < 0)
				YcollisionTime = 0;
			else
				YcollisionTime = (_sprite.nexty() - (y() + height))
						       / (nexty() - y());
			Ycollision = true;
			YcollisionThisSprite = true;
			contactOnThisBottom = true;
		}
		// this's top hit other's bottom
		else if (y() >= _sprite.y() + _sprite.height
				&& nexty() < _sprite.nexty() + _sprite.height)
		{
			if((y() - (_sprite.nexty() + _sprite.height)) < 0)
				YcollisionTime = 0;
			else
				YcollisionTime = (y() - (_sprite.nexty() + _sprite.height))
						       / (y() - nexty());
			Ycollision = true;
			YcollisionThisSprite = true;
			contactOnThisBottom = false;
		}

		if (_sprite == ucigame.RIGHTEDGE)
		{
			if (nextx() + width >= ucigame.window.clientWidth())
			{
				if(x() + width > ucigame.window.clientWidth())	// already out of screen
					XcollisionTime = 0;
				else
					XcollisionTime = (ucigame.window.clientWidth() - (x() + width))
									/ (nextx() - x());
				Xcollision = true;
				XcollisionThisSprite = true;
				contactOnThisRight = true;
			}
		}
		else if (_sprite == ucigame.LEFTEDGE)
		{
			if (nextx() < 0)
			{
				if(x() < 0)		// already out of screen
					XcollisionTime = 0;
				else
					XcollisionTime = -x() / (nextx() - x());
				Xcollision = true;
				XcollisionThisSprite = true;
				contactOnThisRight = false;
			}
		}

		// this's right hit other's left
		else if (x() + width <= _sprite.x() && nextx() + width > _sprite.nextx())
		{
			if (_sprite.nextx() - (x() + width) < 0)
				XcollisionTime = 0;
			else
				XcollisionTime = (_sprite.nextx() - (x() + width))
						/ (nextx() - x());
			contactOnThisRight = true;
			Xcollision = true;
			XcollisionThisSprite = true;
		}
		// this's left hit other's right
		else if (x() >= _sprite.x() + _sprite.width
				&& nextx() < _sprite.nextx() + _sprite.width)
		{
			if (x() - (_sprite.nextx() + _sprite.width) < 0)
				XcollisionTime = 0;
			else
				XcollisionTime = (x() - (_sprite.nextx() + _sprite.width))
						       / (x() - nextx());
			contactOnThisRight = false;
			Xcollision = true;
			XcollisionThisSprite = true;
		}

		// XcollisionTime and YcollisionTime are both between 0 and 1 now

		if (!XcollisionThisSprite && !YcollisionThisSprite)
		{
			// If we get here, it means the two sprites do overlap,
			// but they didn't just collide.  That means they were
			// overlapping based on the curr positions.  And that
			// usually happens when there are multiple objects bouncing
			// into each other, so that the if _action == BOUNCE logic
			// below pushes two sprites on top of each other.
			// This should be fixed.
			return;
		}

		if ((!XcollisionThisSprite && YcollisionThisSprite) || // collision in Y only or Y first
			(XcollisionThisSprite && YcollisionThisSprite && YcollisionTime < XcollisionTime))
		{
			if (contactOnThisBottom) // assume _sprite is not moving
			{
				if (_action == BOUNCE) {
					double overlap = (nexty() + height) - _sprite.nexty();
					nextY = nextY - overlap; 	// bounce
					deltaY = -deltaY;
				} else if (_action == STOP) {				// It's not clear what STOP or
					nextY = _sprite.nexty() - height - 1;	// PAUSE should mean if this
					deltaY = 0;								// is a pinned sprite, so the
				} else if (_action == PAUSE)				// logic here might not be right.
					nextY = _sprite.nexty() - height - 1;
			} else {
				if (_action == BOUNCE) {
					double overlap = (_sprite.nexty() + _sprite.height) - nexty();
					nextY = nextY + overlap; 	// bounce
					deltaY = -deltaY;
				} else if (_action == STOP) {
					nextY = _sprite.nexty() + _sprite.height + 1;
					deltaY = 0;
				} else if (_action == PAUSE)
					nextY = _sprite.nexty() + _sprite.height + 1;
			}
		}
		else if ((XcollisionThisSprite && !YcollisionThisSprite) || // collision in X only or X first
				(XcollisionThisSprite && YcollisionThisSprite && XcollisionTime <= YcollisionTime))
		{
			if (contactOnThisRight)
			{
				if (_action == BOUNCE) {
					double overlap = (nextx() + width) - _sprite.nextx();
					nextX = nextX - overlap; // bounce
					deltaX = -deltaX;
				} else if (_action == STOP) {
					nextX = _sprite.nextx() - width - 1;
					deltaX = 0;
				} else if (_action == PAUSE)
					nextX = _sprite.nextx() - width - 1;
			} else {
				if (_action == BOUNCE) {
					double overlap = (_sprite.nextx() + _sprite.width) - nextx();
					nextX = nextX + overlap; // bounce
					deltaX = -deltaX;
				} else if (_action == STOP) {
					nextX = _sprite.nextx() + _sprite.width + 1;
					deltaX = 0;
				} else if (_action == PAUSE)
					nextX = _sprite.nextx() + _sprite.width + 1;
			}
		}
		else  // this shouldn't happen
			; // System.out.println("say what?");
	}

	/**
	 * Pixel Perfect check for collision and perform appropriate action.
	 */
	private void ifCollidesWithPixelPerfect(Sprite _sprite, int _action)
	{
		if (_sprite == null)
			return;

		// Find how many pixels overlap.
		int ppCollisionCount = pixelPerfectOverlap(_sprite, 0, 0);
		if (ppCollisionCount <= 0)
			return;

		Xcollision = Ycollision = true; // make collided() work

		// Pause isn't implemented for pixel perfect.
		// Stop is implemented in a very simple way: the sprite just
		// stops in its tracks -- it doesn't get as close to the other
		// sprite as possible (as in the regular collision detection).
		if (_action == PAUSE)
			return;
		if (_action == STOP)
		{
			nextX = currX;
			nextY = currY;
			deltaX = 0;
			deltaY = 0;
			return;
		}

		// The remaining logic in this method is a work in progress.
		// The goal is to have bouncing work in a reasonable way, but
		// that's tricky because it's hard to determine the angle of
		// reflection when two non-rectangular sprites collide.  The
		// approach here is to only consider three possibilities:
		// horizontal, vertical, or something resembling 45 degrees.

		// Normal vector
		int dx = pixelPerfectOverlap(_sprite, 1, 0) -
			     pixelPerfectOverlap(_sprite, -1, 0);
		int dy = pixelPerfectOverlap(_sprite, 0, 1) -
			     pixelPerfectOverlap(_sprite, 0, -1);

		// Calculate the angle between the normal vector and the horizontal line
		double angleVal;
		if (dx == 0)
			angleVal = Math.PI/2;
		else
			angleVal = Math.atan2(Math.abs(dy), Math.abs(dx));

		double minThreshold = (Math.PI)/32;
		double maxThreshold = (15*Math.PI)/32;

		// Assume to be a 45 degree angle collision
		if(angleVal >= minThreshold && angleVal <= maxThreshold )
		{
			if ((dx > 0 && dy < 0)
				|| dx < 0 && dy > 0){
				double temp = this.deltaX;
				this.deltaX = this.deltaY;
				this.deltaY = temp;
			}
			// the normal vectors have the same signs
			else
			{
				double temp = this.deltaX;
				this.deltaX = -this.deltaY;
				this.deltaY = -temp;
			}
		}
		// Normal vector leaning toward vertical
		else if (angleVal > maxThreshold && angleVal <= Math.PI/2){
			// Assume vertical, and reset dx to 0
			dx = 0;
		}
		else if (angleVal >= 0 && angleVal < minThreshold){
			// Assume horizontal, reset dy to 0
			dy = 0;
		}

		// Assume vertical or horizontal collision
		{
			// If the x component for the normal vector is positive
			if (dx > 0) {
				// if the current deltaX is positive, make it negative
				if(this.deltaX > 0)
					this.deltaX = -this.deltaX;
			}
			else if (dx < 0) {	// x component for the normal vector is negative
				// if the current delta is negative as well
				if(this.deltaX < 0)
					this.deltaX = -this.deltaX;
			}

			// If the x component for the normal vector is positive
			if (dy > 0) {
				// if the current deltaX is positive, make it negative
				if(this.deltaY > 0)
					this.deltaY = -this.deltaY;
			}
			else if (dy < 0) {	// x component for the normal vector is negative
				// if the current delta is negative as well
				if(this.deltaY < 0)
					this.deltaY = -this.deltaY;
			}
		}

		// Since the old deltaX and deltaY caused an overlap,
		// don't use them for nextX and nextY
		nextX = currX;
		nextY = currY;
	}

	/**
	 * Pixel perfect check for overlap.
	 */
	// extraX and extraY let us test how much overlap there would be
	// if this sprite were moved a little more or less in x or y.
	private int pixelPerfectOverlap(Sprite _sprite, int extraX, int extraY)
	{
		if (this.transparencyBuffer == null)
			this.setUpTransparencyBuffer();
		if (_sprite.transparencyBuffer == null)
			_sprite.setUpTransparencyBuffer();

		int thisLeft   = (int)(Math.round(nextx())) + extraX;
		int thisRight  = thisLeft + width;
		int thisTop    = (int)(Math.round(nexty())) + extraY;
		int thisBottom = thisTop + height;
		int thatLeft   = (int)(Math.round(_sprite.nextx()));
		int thatRight  = thatLeft + _sprite.width;
		int thatTop    = (int)(Math.round(_sprite.nexty()));
		int thatBottom = thatTop + _sprite.height;

		// Check for the easy cases
		if (thisLeft > thatRight ||
		    thisRight < thatLeft ||
		    thisTop > thatBottom ||
		    thisBottom < thatTop)
		    return 0;

		int pixelCollisionCount = 0;
		int[][] thisSprite = this.transparencyBuffer.get(this.currFrame);

		if (_sprite == ucigame.TOPEDGE || _sprite == ucigame.BOTTOMEDGE ||
		    _sprite == ucigame.LEFTEDGE || _sprite == ucigame.RIGHTEDGE)
		{
			for (int x = thisLeft; x < thisRight; ++x)
			{
				for (int y = thisTop; y < thisBottom; ++y)
				{
					if (thisSprite[x-thisLeft][y-thisTop] != 0 &&
						( (_sprite == ucigame.TOPEDGE && y < 0) ||
						  (_sprite == ucigame.BOTTOMEDGE && y >= ucigame.window.clientHeight()) ||
						  (_sprite == ucigame.LEFTEDGE && x < 0) ||
						  (_sprite == ucigame.RIGHTEDGE && x >= ucigame.window.clientWidth())
						))
					{
						++pixelCollisionCount;
					}
				}
			}
			return pixelCollisionCount;
		}

		// compute boundaries of the rectangle of intersection
		int left   = Math.max(thisLeft, thatLeft);
		int right  = Math.min(thisRight, thatRight);
		int top    = Math.max(thisTop, thatTop);
		int bottom = Math.min(thisBottom, thatBottom);

		int[][] otherSprite = _sprite.transparencyBuffer.get(_sprite.currFrame);

		// loop through the overlapping area to see if there is any
		// non-transparent pixel overlap
		for (int x = left; x < right; ++x)
		{
			for (int y = top; y < bottom; ++y)
			{
				if (thisSprite[x-thisLeft][y-thisTop] != 0 &&
				    otherSprite[x-thatLeft][y-thatTop] != 0)
				{
					++pixelCollisionCount;
				}
			}
		}

		return pixelCollisionCount;
	}

	private void setUpTransparencyBuffer() {
		transparencyBuffer = new Vector<int[][]>();
		for (int frame = 0; frame < numFrames; frame++) {
			transparencyBuffer.add(frame,
				frameImage.get(frame).getTransparencyBuffer());
		}
	}


	// regular (non-Pixel Perfect) version
	private boolean overlapsWith(Sprite _sprite) {
		// System.out.println(
		// "**this x: " + nextx() + " to " + (nextx() + width) +
		// " y: " + nexty() + " to " + (nexty() + height) +
		// " other x: " + _sprite.nextx() + " to " + (_sprite.nextx() +
		// _sprite.width) +
		// " y: " + _sprite.nexty() + " to " + (_sprite.nexty() + _sprite.height)
		// );
		if (this.nextx() > _sprite.nextx() + _sprite.width ||
			this.nextx() + this.width < _sprite.nextx()||
			this.nexty() > _sprite.nexty() + _sprite.height ||
			this.nexty() + this.height < _sprite.nexty())
			return false;
		else
			return true;
	}

	/**
	 * non-API
	 */
	public final String toString()
	{
		if (this == ucigame.TOPEDGE) return "<TOPEDGE>";
		if (this == ucigame.BOTTOMEDGE) return "<BOTTOMEDGE>";
		if (this == ucigame.LEFTEDGE) return "<LEFTEDGE>";
		if (this == ucigame.RIGHTEDGE) return "<RIGHTEDGE>";
		if (this == ucigame.PIXELPERFECT) return "<PIXELPERFECT>";
		return "<Sprite [" + width + "," + height + "]#" + (hashCode() % 10000) + ">";
	}
}
