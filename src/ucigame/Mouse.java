// Mouse.java

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
import java.awt.event.MouseEvent;

/**
 * The Mouse class exists to provide the programmer with a single instance,
 * the object mouse.
 * The Mouse class does not have a public constructor and no additional
 * objects of this class can be made (or would make any sense).
 * The methods in Mouse are invoked on the mouse object.
 */
public final class Mouse
{
	public final int CROSSHAIR = Cursor.CROSSHAIR_CURSOR;
	public final int DEFAULT   = Cursor.DEFAULT_CURSOR;
	public final int HAND      = Cursor.HAND_CURSOR;
	public final int MOVE      = Cursor.MOVE_CURSOR;
	public final int TEXT      = Cursor.TEXT_CURSOR;
	public final int WAIT      = Cursor.WAIT_CURSOR;
	public final int N_RESIZE  = Cursor.N_RESIZE_CURSOR;
	public final int E_RESIZE  = Cursor.E_RESIZE_CURSOR;
	public final int S_RESIZE  = Cursor.S_RESIZE_CURSOR;
	public final int W_RESIZE  = Cursor.W_RESIZE_CURSOR;
	public final int NE_RESIZE = Cursor.NE_RESIZE_CURSOR;
	public final int NW_RESIZE = Cursor.NW_RESIZE_CURSOR;
	public final int SE_RESIZE = Cursor.SE_RESIZE_CURSOR;
	public final int SW_RESIZE = Cursor.SW_RESIZE_CURSOR;

	public final int NONE   = MouseEvent.NOBUTTON;
	public final int LEFT   = MouseEvent.BUTTON1;
	public final int MIDDLE = MouseEvent.BUTTON2;
	public final int RIGHT  = MouseEvent.BUTTON3;

	private Ucigame ucigame;

	/**
	 * Create new mouse object
	 */
	Mouse(Ucigame _u)
	{
		ucigame = _u;
	}

	/**
	 * Returns the current x position of the mouse, relative to the upper left hand
	 * corner of the game window.
	 */
	public final int x() { return ucigame.mouseX; }

	/**
	 * Returns the current y position of the mouse, relative to the upper left hand
	 * corner of the game window.
	 */
	public final int y() { return ucigame.mouseY; }

	/**
	 * Returns the difference between the current and previous x position of the mouse,
	 * when the mouse is being dragged.
	 */
	public final int Xchange() { return ucigame.mouseChangeX; }

	/**
	 * Returns the difference between the current and previous y position of the mouse,
	 * when the mouse is being dragged.
	 */
	public final int Ychange() { return ucigame.mouseChangeY; }

	/**
	 * Returns the value of the mouse button involved when the mouse is pressed,
	 * released, moved, or dragged.
	 * The return value will be one of: mouse.NONE, mouse.LEFT, mouse.MIDDLE, mouse.RIGHT.
	 */
	public final int button() { return ucigame.mouseButton; }

	/**
	 * Returns <i>true</i> if the keyboard's Alt key is down when the
	 * mouse is pressed, released, moved, or dragged; <i>false</i> otherwise.
	 */
	public final boolean isAltDown() { return ucigame.mouseIsAltDown; }

	/**
	 * Returns <i>true</i> if the keyboard's Ctrl key is down when the
	 * mouse is pressed, released, moved, or dragged; <i>false</i> otherwise.
	 */
	public final boolean isControlDown() { return ucigame.mouseIsControlDown; }

	/**
	 * Returns <i>true</i> if the keyboard's Meta key is down when the
	 * mouse is pressed, released, moved, or dragged; <i>false</i> otherwise.
	 */
	public final boolean isMetaDown() { return ucigame.mouseIsMetaDown; }

	/**
	 * Returns <i>true</i> if the keyboard's Shift key is down when the
	 * mouse is pressed, released, moved, or dragged; <i>false</i> otherwise.
	 */
	public final boolean isShiftDown() { return ucigame.mouseIsShiftDown; }

	/**
	 * Returns the number of clicks as of the last time the wheel
	 * (a special kind of middle mouse button) was rotated.
	 * A negative value means the mouse wheel was rotated up/away from the user,
	 * and a positive value means the mouse wheel was rotated down/towards the user.
	 * The value is usually -1 or 1.
	 */
	public final int wheelClicks() { return ucigame.mouseWheelUnits; }

	/**
	 * Returns the topmost Sprite object under the current mouse position. The return
	 * value will be null if the mouse position is not over a sprite.
	 */
	public final Sprite sprite() { return ucigame.mouseSprite; }

	/**
	 * Changes the cursor to one of the standard system cursors. Legal values for cursorType are:
	 * mouse.CROSSHAIR, mouse.DEFAULT, mouse.HAND, mouse.MOVE, mouse.TEXT, mouse.WAIT, mouse.N_RESIZE,
	 * mouse.NE_RESIZE, mouse.E_RESIZE, mouse.SE_RESIZE, mouse.S_RESIZE, mouse.SW_RESIZE, mouse.W_RESIZE,
	 * mouse.NW_RESIZE.
	 */
	public void setCursor(int cursorType)
	{
		ucigame.setCursor(cursorType);
	}

	/**
	 * Changes the cursor to the specified Image.
	 * The size of the cursor is determined
	 * by the operating system, and the image will be scaled if necessary. In practice 32 by
	 * 32 works well. The xPos and yPos values indicate the position of the cursor's "hot spot,"
	 * relative to the upper left hand corner of the image.
	 */
	public void setCursor(Image image, int xPos, int yPos)
	{
		ucigame.setCursor(image, xPos, yPos);
	}

}
