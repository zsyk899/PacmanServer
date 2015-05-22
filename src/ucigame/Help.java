// Help.java

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

// Managing classpaths and jars can be confusing.
// This class is meant to be invoked by typing
//
//           java -jar ucigame.jar
//
// and it will give some interesting information about ucigame.jar files
// on the computer, and other information as well.
// Still under development.

public class Help
{
    public static void main(String[] args) {

		System.out.println("Ucigame version " + Ucigame.version());
		System.out.println("Running version of Java is " +
							System.getProperty("java.runtime.version"));
		System.out.println("Current directory is " +
							System.getProperty("user.dir"));

		String sep = System.getProperty("path.separator");    // ; or :
		String slash = System.getProperty("file.separator");  //   / or \

		String[] ext = System.getProperty("java.ext.dirs").split(sep);
		String[] classpath = System.getProperty("java.class.path").split(sep);
		Vector<String> existingUcigames = new Vector<String>();

		for (String e : ext)
		{
			if (e.endsWith(".jar"))
				e = "";   // we'll add ucigame.jar later
			else if (!e.endsWith(slash))
				e += slash;
			try {
				System.out.println("**Checking " + e + "ucigame.jar");
				FileInputStream f = new FileInputStream(e + "ucigame.jar");
				existingUcigames.add(e + "ucigame.jar");  // only get here if file exists
				f.close();
			}
			catch (FileNotFoundException fnf) {}
			catch (IOException fnf) {}
		}

		//System.out.println("Current classpath includes:");
		for (String c : classpath)
		{
			if (c.endsWith(".jar"))
				c = "";   // we'll add ucigame.jar later
			else if (!c.endsWith(slash))
				c += slash;
			try {
				System.out.println("*Checking " + c + "ucigame.jar");
				FileInputStream f = new FileInputStream(c + "ucigame.jar");
				existingUcigames.add(c + "ucigame.jar");
				f.close();
			}
			catch (FileNotFoundException fnf) {}
			catch (IOException fnf) {}
		}

		int c = existingUcigames.size();
		if (c == 0)
			System.out.println("No existing ucigame.jar files found on this computer.");
		else {
			System.out.println("" + c + " existing ucigame.jar " +
							(c == 1 ? "file " : "files ") + "found on this computer:");
			for (String f : existingUcigames)
				System.out.println("  " + f);
		}

		System.out.println("");
		java.util.Properties p = System.getProperties();
		p.list(System.out);

		/*
        String [] properties = {
            "java.ext.dirs",
            "java.home",
            "path.separator",
            "file.separator",
            "java.library.path",
            "os.arch",
            "sun.boot.class.path"
        };
        for (int i = 0; i < properties.length; i++) {
            String key = properties[i];
            System.out.println(key + ": " + System.getProperty(key));
        }
        */
    }
}
