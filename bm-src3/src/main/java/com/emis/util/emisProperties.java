package com.emis.util;



import java.io.*;

import java.util.Date;

import java.util.Enumeration;

import java.util.Hashtable;



/**

 *  這個 Class 只是把 java.util.Properties 改成可以讀中文的 Property 檔

 */

public class emisProperties extends Hashtable

{

    /**

     *  A property list that contains default values for any keys not

     *  found in this property list.

     *

     *  @serial

     */

    protected emisProperties defaults;



    /**

     *  Creates an empty property list with no default values.

     */

    public emisProperties() {

	this(null);

    }



    /**

     *  Creates an empty property list with the specified defaults.

     *

     *  @param defaults - the defaults.

     */

    public emisProperties(emisProperties defaults) {

	this.defaults = defaults;

    }



    /**

     *  Calls the hashtable method <code>put</code>. Provided for

     *  parallelism with the <tt>getProperty</tt> method. Enforces use of

     *  strings for property keys and values.

     *

     *  @param key the key to be placed into this property list.

     *  @param value the value corresponding to <tt>key</tt>.

     *  @see #getProperty

     *  @since    1.2

     */

    public synchronized Object setProperty(String key, String value) {

        return put(key, value);

    }



    private static final String keyValueSeparators = "=: \t\r\n\f";



    private static final String strictKeyValueSeparators = "=:";



    private static final String specialSaveChars = "=: \t\r\n\f#!";



    private static final String whiteSpaceChars = " \t\r\n\f";



    /**

     *  將檔案資料讀入至Properties Object

     */

    public synchronized void load(InputStream inStream) throws IOException

    {

        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, emisUtil.FILENCODING));

	while (true)

        {

            // Get next line

            String line = in.readLine();

            if (line == null)

                return;



            if (line.length() > 0)

            {

                // Continue lines that end in slashes if they are not comments

                char firstChar = line.charAt(0);

                if ((firstChar != '#') && (firstChar != '!'))

                {

                    while (continueLine(line)) {

                        String nextLine = in.readLine();

                        if(nextLine == null)

                            nextLine = new String("");

                        String loppedLine = line.substring(0, line.length()-1);

                        // Advance beyond whitespace on new line

                        int startIndex=0;

                        for(startIndex=0; startIndex<nextLine.length(); startIndex++)

                            if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)

                                break;

                        nextLine = nextLine.substring(startIndex,nextLine.length());

                        line = new String(loppedLine+nextLine);

                    }



                    // Find start of key

                    int len = line.length();

                    int keyStart;

                    for(keyStart=0; keyStart<len; keyStart++) {

                        if(whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)

                            break;

                    }



                    // Blank lines are ignored

                    if (keyStart == len)

                        continue;



                    // Find separation between key and value

                    int separatorIndex;

                    for(separatorIndex=keyStart; separatorIndex<len; separatorIndex++) {

                        char currentChar = line.charAt(separatorIndex);

                        if (currentChar == '\\')

                            separatorIndex++;

                        else if(keyValueSeparators.indexOf(currentChar) != -1)

                            break;

                    }



                    // Skip over whitespace after key if any

                    int valueIndex;

                    for (valueIndex=separatorIndex; valueIndex<len; valueIndex++)

                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)

                            break;



                    // Skip over one non whitespace key value separators if any

                    if (valueIndex < len)

                        if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)

                            valueIndex++;



                    // Skip over white space after other separators if any

                    while (valueIndex < len) {

                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)

                            break;

                        valueIndex++;

                    }

                    String key = line.substring(keyStart, separatorIndex);

                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";



                    // Convert then store key and value

                    key = loadConvert(key);

                    value = loadConvert(value);

                    put(key, value);

                }

            }

	}

    }

  /**
   * 將檔案資料讀入至Properties Object
   * @param inStream
   * @param encoding
   * @throws IOException
   */
    public synchronized void load(InputStream inStream, String encoding) throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(inStream, encoding));
      while (true) {
        // Get next line
        String line = in.readLine();
        if (line == null)
          return;
        if (line.length() > 0) {
          // Continue lines that end in slashes if they are not comments
          char firstChar = line.charAt(0);
          if ((firstChar != '#') && (firstChar != '!')) {
            while (continueLine(line)) {
              String nextLine = in.readLine();
              if (nextLine == null)
                nextLine = new String("");
              String loppedLine = line.substring(0, line.length() - 1);
              // Advance beyond whitespace on new line
              int startIndex = 0;
              for (startIndex = 0; startIndex < nextLine.length(); startIndex++)
                if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)
                  break;
              nextLine = nextLine.substring(startIndex, nextLine.length());
              line = new String(loppedLine + nextLine);
            }
            // Find start of key
            int len = line.length();
            int keyStart;
            for (keyStart = 0; keyStart < len; keyStart++) {
              if (whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                break;
            }

            // Blank lines are ignored
            if (keyStart == len)
              continue;

            // Find separation between key and value
            int separatorIndex;
            for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
              char currentChar = line.charAt(separatorIndex);
              if (currentChar == '\\')
                separatorIndex++;
              else if (keyValueSeparators.indexOf(currentChar) != -1)
                break;
            }

            // Skip over whitespace after key if any
            int valueIndex;
            for (valueIndex = separatorIndex; valueIndex < len; valueIndex++)
              if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                break;

            // Skip over one non whitespace key value separators if any
            if (valueIndex < len)
              if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                valueIndex++;

            // Skip over white space after other separators if any
            while (valueIndex < len) {
              if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                break;
              valueIndex++;
            }

            String key = line.substring(keyStart, separatorIndex);
            String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

            // Convert then store key and value
            key = loadConvert(key);
            value = loadConvert(value);
            put(key, value);
          }
        }
      }
    }

    /*

     *  Returns true if the given line is a line that must

     *  be appended to the next line

     */

    private boolean continueLine (String line) {

        int slashCount = 0;

        int index = line.length() - 1;

        while((index >= 0) && (line.charAt(index--) == '\\'))

            slashCount++;

        return (slashCount % 2 == 1);

    }



    /*

     *  Converts encoded &#92;uxxxx to unicode chars

     *  and changes special saved chars to their original forms

     */

    private String loadConvert (String theString)

    {

        char aChar;

        int len = theString.length();

        StringBuffer outBuffer = new StringBuffer(len);



        for(int x=0; x<len; ) {

            aChar = theString.charAt(x++);

            if (aChar == '\\') {

                aChar = theString.charAt(x++);

                if(aChar == 'u') {

                    // Read the xxxx

                    int value=0;

		    for (int i=0; i<4; i++) {

		        aChar = theString.charAt(x++);

		        switch (aChar) {

		          case '0': case '1': case '2': case '3': case '4':

		          case '5': case '6': case '7': case '8': case '9':

		             value = (value << 4) + aChar - '0';

			     break;

			  case 'a': case 'b': case 'c':

                          case 'd': case 'e': case 'f':

			     value = (value << 4) + 10 + aChar - 'a';

			     break;

			  case 'A': case 'B': case 'C':

                          case 'D': case 'E': case 'F':

			     value = (value << 4) + 10 + aChar - 'A';

			     break;

			  default:

                              throw new IllegalArgumentException(

                                           "Malformed \\uxxxx encoding.");

                        }

                    }

                    outBuffer.append((char)value);

                } else {

                    if (aChar == 't') aChar = '\t';

                    else if (aChar == 'r') aChar = '\r';

                    else if (aChar == 'n') aChar = '\n';

                    else if (aChar == 'f') aChar = '\f';

                    outBuffer.append(aChar);

                }

            } else

                outBuffer.append(aChar);

        }

        return outBuffer.toString();

    }



    /**

     *  Converts unicodes to encoded &#92;uxxxx

     *  and writes out any of the characters in specialSaveChars

     *  with a preceding slash

     */

    private String saveConvert(String theString, boolean escapeSpace)

    {

        int len = theString.length();

        StringBuffer outBuffer = new StringBuffer(len*2);



        for(int x=0; x<len; x++) {

            char aChar = theString.charAt(x);

            switch(aChar) {

		case ' ':

		    if (x == 0 || escapeSpace)

			outBuffer.append('\\');



		    outBuffer.append(' ');

		    break;

                case '\\':outBuffer.append('\\'); outBuffer.append('\\');

                          break;

                case '\t':outBuffer.append('\\'); outBuffer.append('t');

                          break;

                case '\n':outBuffer.append('\\'); outBuffer.append('n');

                          break;

                case '\r':outBuffer.append('\\'); outBuffer.append('r');

                          break;

                case '\f':outBuffer.append('\\'); outBuffer.append('f');

                          break;

                default:

                    if ((aChar < 0x0020) || (aChar > 0x007e)) {

                        outBuffer.append('\\');

                        outBuffer.append('u');

                        outBuffer.append(toHex((aChar >> 12) & 0xF));

                        outBuffer.append(toHex((aChar >>  8) & 0xF));

                        outBuffer.append(toHex((aChar >>  4) & 0xF));

                        outBuffer.append(toHex( aChar        & 0xF));

                    } else {

                        if (specialSaveChars.indexOf(aChar) != -1)

                            outBuffer.append('\\');

                        outBuffer.append(aChar);

                    }

            }

        }

        return outBuffer.toString();

    }



    /**

     *  Calls the <code>store(OutputStream out, String header)</code> method

     *  and suppresses IOExceptions that were thrown.

     *

     *  @deprecated This method does not throw an IOException if an I/O error

     *  occurs while saving the property list.  As of the Java 2 platform v1.2, the preferred

     *  way to save a properties list is via the <code>store(OutputStream out,

     *  String header)</code> method.

     *

     *  @param   out    -  an output stream.

     *  @param   header -  a description of the property list.

     *  @exception  ClassCastException  if this <code>Properties</code> object

     *             contains any keys or values that are not <code>Strings</code>.

     */

    public synchronized void save(OutputStream out, String header)

    {

        try {

            store(out, header);

        } catch (IOException e) {}

    }



    public synchronized void store(OutputStream out, String header)

           throws IOException

    {

        BufferedWriter awriter;

        awriter = new BufferedWriter(new OutputStreamWriter(out, emisUtil.FILENCODING ));

        if (header != null)

            writeln(awriter, "#" + header);

        writeln(awriter, "#" + new Date().toString());

        for (Enumeration e = keys(); e.hasMoreElements();) {

            String key = (String)e.nextElement();

            String val = (String)get(key);

            key = saveConvert(key, true);



	    /* No need to escape embedded and trailing spaces for value, hence

	     * pass false to flag.

	     */

            val = saveConvert(val, false);

            writeln(awriter, key + "=" + val);

        }

        awriter.flush();

    }



    private static void writeln(BufferedWriter bw, String s) throws IOException {

        bw.write(s);

        bw.newLine();

    }



    /**

     *  Searches for the property with the specified key in this property list.

     *  If the key is not found in this property list, the default property list,

     *  and its defaults, recursively, are then checked. The method returns

     *  <code>null</code> if the property is not found.

     *

     *  @param   key - the property key.

     *  @return  the value in this property list with the specified key value.

     *  @see     #setProperty

     *  @see     #defaults

     */

    public String getProperty(String key) {

	Object oval = super.get(key);

	String sval = (oval instanceof String) ? (String)oval : null;

	return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;

    }



    /**

     *  Searches for the property with the specified key in this property list.

     *  If the key is not found in this property list, the default property list,

     *  and its defaults, recursively, are then checked. The method returns the

     *  default value argument if the property is not found.

     *

     *  @param   key          -  the hashtable key.

     *  @param   defaultValue -  a default value.

     *  @return  the value in this property list with the specified key value.

     *  @see     #setProperty

     *  @see     #defaults

     */

    public String getProperty(String key, String defaultValue) {

	String val = getProperty(key);

	return (val == null) ? defaultValue : val;

    }



    /**

     *  Returns an enumeration of all the keys in this property list, including

     *  the keys in the default property list.

     *

     *  @return  an enumeration of all the keys in this property list, including

     *           the keys in the default property list.

     *  @see     java.util.Enumeration

     *  @see     java.util.Properties#defaults

     */

    public Enumeration propertyNames() {

	Hashtable h = new Hashtable();

	enumerate(h);

	return h.keys();

    }



    /**

     *  Prints this property list out to the specified output stream.

     *  This method is useful for debugging.

     *

     *  @param   out   an output stream.

     */

    public void list(PrintStream out) {

	out.println("-- listing properties --");

	Hashtable h = new Hashtable();

	enumerate(h);

	for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {

	    String key = (String)e.nextElement();

	    String val = (String)h.get(key);

	    if (val.length() > 40) {

                val = val.substring(0, 37) + "...";

	    }

	    out.println(key + "=" + val);

	}

    }



    /**

     *  Prints this property list out to the specified output stream.

     *  This method is useful for debugging.

     *

     *  @param   out   an output stream.

     *  @since   JDK1.1

     */

    /*

     *  Rather than use an anonymous inner class to share common code, this

     *  method is duplicated in order to ensure that a non-1.1 compiler can

     *  compile this file.

     */

    public void list(PrintWriter out) {

	out.println("-- listing properties --");

	Hashtable h = new Hashtable();

	enumerate(h);

	for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {

	    String key = (String)e.nextElement();

	    String val = (String)h.get(key);

	    if (val.length() > 40) {

		val = val.substring(0, 37) + "...";

	    }

	    out.println(key + "=" + val);

	}

    }



    /**

     *  Enumerates all key/value pairs in the specified hastable.

     *  @param h the hashtable

     */

    private synchronized void enumerate(Hashtable h) {

	if (defaults != null) {

	    defaults.enumerate(h);

	}

	for (Enumeration e = keys() ; e.hasMoreElements() ;) {

	    String key = (String)e.nextElement();

	    h.put(key, get(key));

	}

    }



    /**

     *  Convert a nibble to a hex character

     *  @param	nibble	the nibble to convert.

     */

    private static char toHex(int nibble) {

	return hexDigit[(nibble & 0xF)];

    }



    /** A table of hex digits */

    private static final char[] hexDigit = {

	'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'

    };

}

