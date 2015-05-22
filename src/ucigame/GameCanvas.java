package ucigame;

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

// This class is used to create a single object called canvas, in the Ucigame class.
// The object class is used by the game programmer to write the game.
// This class doesn't actually derive from Component or Canvas.

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;

/**
 * The GameCanvas class exists to provide the programmer with a
 * single instance, the object <i>canvas</i>.
 * The GameCanvas class does not have a public constructor and no
 * additional objects of this class can be made (or would make any sense).
 * The methods in GameCanvas are invoked on the <i>canvas</i> object.
 */
public class GameCanvas
{
	private Ucigame ucigame;
	private Color canvasFontColor = null;

	GameCanvas(Ucigame u)
	{
		ucigame = u;
	}

	/**
	 * Returns the width of the canvas in pixels.
	 */
	public int width() { return ucigame.window.clientWidth(); }

	/**
	 * Returns the height of the canvas in pixels.
	 */
	public int height() { return ucigame.window.clientHeight(); }

	/**
	 * Sets the canvas background to the specified Image.
	 * The background image is drawn when canvas.clear() is called
	 * inside the game's draw() method.
	 */
	public void background(Image image)
	{
		ucigame.bgImage = image;
		ucigame.bgColor = null;
	}

	/**
	 * Sets the canvas background to the specified shade.
	 * The color determined by the shade parameter is drawn when canvas.clear() is called
	 * inside the game's draw() method.
	 * The color is a shade of grey, which can range
	 * from 0 (black) to 128 (gray) to 255 (white).
	 * If shade is less than 0 or greater than 255, the method call has no effect.
	 * The default background color is white.
	 */
	public void background(int shade)
	{
		if (0 <= shade && shade <= 255)
		{
			ucigame.bgColor = new Color(shade, shade, shade);
			ucigame.bgImage = null;
		}
	}

	/**
	 * Sets the canvas background to the specified color.
	 * The color determined by (r, g, b) parameters is drawn when canvas.clear() is called
	 * inside the game's draw() method.
	 * See the About Color page for more information about colors in Ucigame and computer graphics.
	 */
	public void background(int r, int g, int b)
	{
		if (0 <= r && r <= 255 &&
			0 <= g && g <= 255 &&
			0 <= b && b <= 255)
		{
			ucigame.bgColor = new Color(r, g, b);
			ucigame.bgImage = null;
		}
	}

	/**
	 * Draws the specified background image or background color on the canvas.
	 */
	public void clear()
	{
		if (ucigame.offG != null)
		{
			if (ucigame.bgColor != null)
			{
				Color c = ucigame.offG.getColor();
				ucigame.offG.setColor(ucigame.bgColor);
				ucigame.offG.fillRect(0, 0, ucigame.canvas.width(), ucigame.canvas.height());
				ucigame.offG.setColor(c);
			}
			else if (ucigame.bgImage != null)
			{
				ucigame.bgImage.draw(0, 0);
			}
		}
		else
			ucigame.logError("canvas.clear() used outside of draw()");
	}


	/**
	 * Sets the canvas's font to the specified fontname, style and size.
	 * An array of Strings containing the font names available on the computer
	 * is returned by Ucigame's <i>arrayOfAvailableFonts()</i> method.
	 * The style must be one of the following: PLAIN, BOLD, ITALIC, BOLDITALIC.
	 * The font's color will be black.
	 */
	public void font(String fontname, int style, int size) {
		font(fontname, style, size, 0, 0, 0);
	}

	/**
	 * Sets the canvas's font to the specified fontname, style, size, and color.
	 * An array of Strings containing the font names available on the computer
	 * is returned by Ucigame's <i>arrayOfAvailableFonts()</i> method.
	 * The style must be one of the following: PLAIN, BOLD, ITALIC, BOLDITALIC.
	 * The font's color is determined by the r, g, and b parameters.
	 */
	public void font(String fontname, int style, int size, int r, int g, int b)
	{
		if (style == ucigame.BOLD || style == ucigame.PLAIN ||
			style == ucigame.ITALIC ||	style == ucigame.BOLDITALIC)
			;
		else
		{
			ucigame.logError("Invalid style parameter in canvas.font()");
			style = ucigame.PLAIN;
		}
		ucigame.windowFont = new Font(fontname, style, size);
		if (ucigame.windowFont.getFamily().equalsIgnoreCase(fontname) ||
			ucigame.windowFont.getFontName().equalsIgnoreCase(fontname))
			;
		else
			ucigame.logWarning("Could not create font with name " + fontname +
					". Using font " + ucigame.windowFont.getFontName() + " instead.");
		if (0 <= r && r <= 255 && 0 <= g && g <= 255 && 0 <= b && b <= 255)
			canvasFontColor = new Color(r, g, b);
		else
			canvasFontColor = Color.BLACK;
	}

	/**
	 * Writes the specified number n on the canvas at location (x, y).
	 * n is converted to a String which is positioned so that its upper
	 * left hand corner is <i>x</i> pixels to the right and <i>y</i>
	 * pixels down relative to the upper left hand corner of the canvas.
	 */
	public void putText(int n, double x, double y)
	{
		putText(""+n, x, y);
	}

	/**
	 * Writes the specified number n on the canvas at location (x, y).
	 * n is converted to a String which is positioned so that its upper
	 * left hand corner is <i>x</i> pixels to the right and <i>y</i>
	 * pixels down relative to the upper left hand corner of the canvas.
	 */
	public void putText(double n, double x, double y)
	{
		putText(""+n, x, y);
	}

	/**
	 * Writes the specified String string on the canvas at location (x, y).
	 * string is positioned so that its upper
	 * left hand corner is <i>x</i> pixels to the right and <i>y</i>
	 * pixels down relative to the upper left hand corner of the canvas.
	 */
	public void putText(String string, double x, double y)
	{
		if (ucigame.offG == null)
		{
			ucigame.logError("canvas.putText(" + string + "," + x + ", " + y +
					 ") used outside of draw()");
			return;
		}
		if (ucigame.windowFont != null)
			ucigame.offG.setFont(ucigame.windowFont);
		Color prevColor = ucigame.offG.getColor();
		if (canvasFontColor != null)
			ucigame.offG.setColor(canvasFontColor);
		ucigame.offG.drawString(string, (int)x, (int)y);
		ucigame.offG.setColor(prevColor);
	}
}

