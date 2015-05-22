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

import java.awt.Dimension;

public class GameWindow
{
	private Ucigame ucigame;

	GameWindow(Ucigame _u)
	{
		ucigame = _u;
	}

	/*
		Sets the width and height of the internal area of the window.
		The actual window will be slightly wider and higher to account
		for the frame and title bar.
	*/
	int clientWidth, clientHeight;
	String title;
	boolean showfps = false;

	/**
	 * Sets the width and height of the internal area of the window. The actual
	 * window will be slightly wider and higher to account for the frame and title bar.
	 *
	 *  @param _width the width of the internal area of the window
	 *  @param _height the height of the internal area of the window
	 *
	 */
	public void size(int _width, int _height)
	{
		if (ucigame.isApplet)
		{
			clientWidth = ucigame.getWidth();
			clientHeight = ucigame.getHeight();
		}
		else if (50 <= _width && _width <= 2000 &&
				 50 <= _height && _height <= 2000)
		{
			ucigame.gameComponent.setPreferredSize(new Dimension(_width, _height));
			ucigame.gameComponent.setMinimumSize(new Dimension(_width, _height));
			clientWidth = _width;
			clientHeight = _height;
		}
		else
			size(100, 100);  // bad parm values, use reasonable defaults
	}

	/**
	 * Set the title of the window.
	 *
	 * @param _title the title of the window
	 */
	public void title(String _title)
	{
		title = _title;
		if (ucigame.frame != null)
			ucigame.frame.setTitle(_title);
	}

	/**
	 * Method will show the FPS of the window.
	 */
	public void showFPS() { showfps = true; }

	/**
	 * Method will hide the FPS of the window.
	 */
	public void hideFPS()
	{
		showfps = false;
		if (ucigame.frame != null)
			ucigame.frame.setTitle(title);
	}

	/**
	 * Method will set the FPS of the window.
	 *
	 * @param _f the FPS value
	 */
	public void setfps(int _f)
	{
		if (showfps && ucigame.frame != null)
			ucigame.frame.setTitle(title + " (" + _f + ")");
	}

	/**
	 * Get the width of the window.
	 *
	 * @return the width of the internal area of the window
	 */
	int clientWidth() { return clientWidth; }

	/**
	 * Get the height of the window.
	 *
	 * @return the height of the internal area of the window
	 */
	int clientHeight() { return clientHeight; }
}
