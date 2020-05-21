package com.emis.util;



import com.emis.file.emisDirectory;

import com.emis.file.emisFileMgr;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import java.io.*;

import java.util.Enumeration;

import java.util.Hashtable;

import java.util.StringTokenizer;

import java.util.Vector;



/**

 *

<form action='../../servlet/com.emis.servlet.emisUpload' name='foo' method='POST' enctype='multipart/form-data'>

  <input type='hidden' name='DEST_DIR' value="data.images"/>

  <input type='hidden' name='FILENAME_DEST'/>

  <input type='file' name='filename' size='40'/>

  <input type='submit' name='Upload' value='Upload' />

</form>

 *

 *  DEST_DIR 和 FILENAME_DEST 一定要放在 FILENAME 之上

 *  DEST_DIR 值為  A.B ,表示系統根目錄下的子目錄 A 再下面的子目錄 B

 *  FILENAME_DEST 表示系統存檔時是否要換另一個名稱存檔

 *  接收端寫法為

 *  emisMultipartRequest multi = new emisMultipartRequest(application,request)
  
 * Track+[13582] dana.gao 2010/04/06 修改上傳中文名稱文件時會亂碼問題.

 */

public class emisMultipartRequest {

public static final String CUSTOM_MAX_POST_SIZE = "CUSTOM_MAX_POST_SIZE";


private static final int DEFAULT_MAX_POST_SIZE = 10 * 1024 * 1024; // 1 Meg

private int MAX_POST_SIZE = 10 * 1024 * 1024; // 1 Meg

private static final String NO_FILE = "unknown";



private HttpServletRequest req;

private ServletContext application_;

private File dir;



private Hashtable parameters = new Hashtable();

// name -Vector of values

private Hashtable files = new Hashtable();



/**

 *  Constructs a new MultipartRequest to handle the specified request,

 *  saving any uploaded files to the given directory, and limiting the

 *  upload size to the specified length. If the content is too large, an

 *  IOException is thrown. This constructor actually parses the

 *  <tt>multipart/form-data</tt> and throws an IOException if there's any

 *  problem reading or parsing the request.

 *

 *  @param request - the servlet request

 *  @exception IOException - if the uploaded content is larger than

 *      <tt>maxPostSize</tt> or there's a problem reading or parsing the request

 */

public emisMultipartRequest(ServletContext application, HttpServletRequest request)

       throws Exception {

  // Sanity check values

  if (application == null)

    throw new IllegalArgumentException("application cannot be null");



  if (request == null)

    throw new IllegalArgumentException("request cannot be null");



  // Save the request, dir, and max size

  application_ = application;

  req = request;

  try {
    if (req.getAttribute(CUSTOM_MAX_POST_SIZE) != null && !"".equals(req.getAttribute(CUSTOM_MAX_POST_SIZE)))
      MAX_POST_SIZE = Integer.parseInt(req.getAttribute(CUSTOM_MAX_POST_SIZE).toString());
    else
      MAX_POST_SIZE = DEFAULT_MAX_POST_SIZE;
  } catch (Exception e) {
    e.printStackTrace();
    MAX_POST_SIZE = DEFAULT_MAX_POST_SIZE;
  }


  // Now parse the request saving data to "parameters" and "files";

  // write the file contents to the saveDirectory

  readRequest();

}



/**

 *  Returns the names of all the parameters as an Enumeration of

 *  Strings. It returns an empty Enumeration if there are no parameters.

 *

 *  @return the names of all the parameters as an Enumeration of Strings

 */

public Enumeration getParameterNames() {

  return parameters.keys();

}



/**

 *  Returns the names of all the uploaded files as an Enumeration of

 *  Strings. It returns an empty Enumeration if there are no uploaded

 *  files. Each file name is the name specified by the form, not by

 *  the user.

 *

 *  @return the names of all the uploaded files as an Enumeration of Strings

 */

public Enumeration getFileNames() {

  return files.keys();

}



/**

 *  Returns the value of the named parameter as a String, or null if

 *  the parameter was not sent or was sent without a value.The value

 *  is guaranteed to be in its normal, decoded form. If the parameter

 *  has multiple values, only the last one is returned (for backward

 *  compatibility). For parameters with multiple values, it's possible

 *  the last "value" may be null.

 *

 *  @param name - the parameter name

 *  @return the parameter value

 */

public String getParameter(String name) {

  try {

    Vector values = (Vector)parameters.get(name);

    if (values == null || values.size() == 0) {

      return null;

    }

    String value = (String)values.elementAt(values.size() - 1);

    return value;

  } catch (Exception e) {

    return null;

  }

}



/**

 *  Returns the values of the named parameter as a String array, or null if

 *  the parameter was not sent. The array has one entry for each parameter

 *  field sent. If any field was sent without a value that entry is stored

 *  in the array as a null. The values are guaranteed to be in their

 *  normal, decoded form. A single value is returned as a one-element array.

 *

 *  @param name - the parameter name

 *  @return the parameter values

 */

public String[] getParameterValues(String name) {

  try {

    Vector values = (Vector)parameters.get(name);

      if (values == null || values.size() == 0) {

      return null;

    }

    String[] valuesArray = new String[values.size()];

    values.copyInto(valuesArray);

    return valuesArray;

  } catch (Exception e) {

    return null;

  }

}



/**

 *  Returns the filesystem name of the specified file, or null if the

 *  file was not included in the upload. A filesystem name is the name

 *  specified by the user. It is also the name under which the file is

 *  actually saved.

 *

 *  @param name - the filename

 *  @return the filesystem name of the file

 */

public String getFilesystemName(String name) {

  try {

    UploadedFile file = (UploadedFile)files.get(name);

    return file.getFilesystemName(); // may be null

  } catch (Exception e) {

    return null;

  }

}



/**

 *  Returns the content type of the specified file (as supplied by the

 *  client browser), or null if the file was not included in the upload.

 *

 *  @param name - the filename

 *  @return the content type of the file

 */

public String getContentType(String name) {

  try {

    UploadedFile file = (UploadedFile)files.get(name);

    return file.getContentType(); // may be null

  }

  catch (Exception e) {

    return null;

  }

}



/**

 *  Returns a File object for the specified file saved on the server's

 *  filesystem, or null if the file was not included in the upload.

 *

 *  @param name - the filename

 *  @return a File object for the named file

 */

public File getFile(String name) {

  try {

    UploadedFile file = (UploadedFile)files.get(name);

    return file.getFile(); // may be null

  } catch (Exception e) {

    return null;

  }

}



/**

 *  The workhorse method that actually parses the request. A subclass

 *  can override this method for a better optimized or differently

 *  behaved implementation.

 *

 *  @exception IOException if the uploaded content is larger than

 *   <tt>maxSize</tt> or there's a problem parsing the request

 */

protected void readRequest() throws Exception {

  // Check the content length to prevent denial of service attacks

  int length = req.getContentLength();

  if (length > MAX_POST_SIZE) {

    throw new IOException("Posted content length of " + length + " exceeds limit of " + MAX_POST_SIZE);

  }



  // Check the content type to make sure it's "multipart/form-data"

  // Access header two ways to work around WebSphere oddities

  String type = null;

  String type1 = req.getHeader("Content-Type");

  String type2 = req.getContentType();

  // If one value is null, choose the other value

  if (type1 == null && type2 != null) {

    type = type2;

  }

  else if (type2 == null && type1 != null) {

    type = type1;

  }

  // If neither value is null, choose the longer value

  else if (type1 != null && type2 != null) {

    type = (type1.length() > type2.length() ? type1 : type2);

  }



  if (type == null || !type.toLowerCase().startsWith("multipart/form-data")) {

    throw new IOException("Posted content type isn't multipart/form-data");

  }



  // Get the boundary string; it's included in the content type.

  // Should look something like "------------------------12012133613061"

  String boundary = extractBoundary(type);

  if (boundary == null) {

    throw new IOException("Separation boundary was not specified");

  }



  // Construct the special input stream we'll read from

  MultipartInputStreamHandler in = new MultipartInputStreamHandler(req.getInputStream(),length);



  // Read the first line, should be the first boundary

  String line = in.readLine();

  if (line == null) {

    throw new IOException("Corrupt form data: prematureending");

  }



  // Verify that the line is the boundary

  if (!line.startsWith(boundary)) {

    throw new IOException("Corrupt form data: no leading boundary");

  }



  // Now that we're just beyond the first boundary, loop over each part

  boolean done = false;

  //read parameter and upload the file

  while (!done) {

    done = readNextPart(in, boundary, true);

  }

}



/**

 *  A utility method that reads an individual part. Dispatches to

 *  readParameter() and readAndSaveFile() to do the actual work. A

 *  subclass can override this method for a better optimized or

 *  differently behaved implementation.

 *

 *  @param in - the stream from which to read the part

 *  @param boundary - the boundary separating parts

 *  @return a flag indicating whether this is the last part (fasle == not the last)

 *  @exception IOException - if there's a problem reading or parsing the

 *   request

 */

protected boolean readNextPart(MultipartInputStreamHandler in,String boundary, boolean UpLoad)

          throws Exception {

  // Read the first line, should look like this:

  // content-disposition: form-data; name="field1";  filename="file1.txt"

  // UpLoad=true permit uploading;UpLoad=false not to permit uploading

  String line = in.readLine();

  if (line == null) {

  // No parts left, we're done

    return true;

  }

  else if (line.length() == 0) {

  // IE4 on Mac sends an empty line at the end; treat that as the end.

  // Thanks to Daniel Lemire and Henri Tourigny for this fix.

    return true;

  }



  // Parse the content-disposition line

  String[] dispInfo = extractDispositionInfo(line);

  String disposition = dispInfo[0];

  String name = dispInfo[1];

  String filename = dispInfo[2];



  // add by robert

  String sNewName = getParameter(name+"_DEST");

  if( sNewName != null )

  {

    dispInfo[2] = sNewName;

    filename = sNewName;

  }



  // Now onto the next line. This will either be empty

  // or contain a Content-Type and then an empty line.

  line = in.readLine();

  if (line == null) {

    // No parts left, we're done

    return true;

  }



  // Get the content type, or null if none specified

  String contentType = extractContentType(line);

  if (contentType != null) {

    // Eat the empty line

    line = in.readLine();

    if (line == null || line.length() > 0) { // line shouldbe empty   ?? length()>0 非空字串 ??

      throw new IOException("Malformed line after content type: " +line);

    }

  }

  else {

    // Assume a default content type

    contentType = "application/octet-stream";

  }



  // Now, finally, we read the content (end after reading the boundary)

  if (filename == null) {

    // This is a parameter, add it to the vector of values

    String value = readParameter(in, boundary);

    if (value.equals("")) {

      value = null; // treat empty strings like nulls

    }

    Vector existingValues = (Vector)parameters.get(name);

    if (existingValues == null) {

      existingValues = new Vector();

      parameters.put(name, existingValues);

    }



    // modified by robert, 由 form 傳入的儲存目錄

    if("DEST_DIR".equalsIgnoreCase(name))

    {

      emisFileMgr oMgr = emisFileMgr.getInstance(application_);

      emisDirectory _oDir = oMgr.getDirectory("root");

      if("".equals(value)) {

        throw new Exception("there is no destination dir setup");

      }



      StringTokenizer st = new StringTokenizer(value,".");

      while( st.hasMoreTokens() )

      {

        String _subDir = st.nextToken();

        _oDir = _oDir.subDirectory(_subDir);

      }

      // Check saveDirectory is truly a directory

      String saveDirectory = _oDir.getDirectory();

      dir = new File(saveDirectory);

      if (!dir.exists())  {

        if (!dir.mkdirs())    {

          throw new IOException("Error Creating Directory:"+saveDirectory);

        }

      }

      if (!dir.isDirectory())

        throw new IllegalArgumentException("Not a directory: " +saveDirectory);



      // Check saveDirectory is writable

      if (!dir.canWrite())

        throw new IllegalArgumentException("Directory Not writable: " + saveDirectory);

    }

    existingValues.addElement(value);

  }

  else {

    // This is a file

    // String tmpFileName = constructFileName(req,filename);

    filename = new String(filename.getBytes("ISO8859_1"),"UTF-8");

    String sTemp = filename.toUpperCase();  // 不改变原文件名，不然linux下会有问题。

    if ( sTemp.endsWith(".JS") ||sTemp.endsWith(".JSP")  )

    {

      throw new IOException("can't upload JSP & JS files");

    }



    readAndSaveFile(in, boundary, filename, contentType);

    if (filename.equals(NO_FILE)) {

      files.put(name, new UploadedFile(null, null, null));

    } else {

      files.put(name, new UploadedFile(dir.toString(),filename, contentType));

    }

  }

  return false; // there's more to read

}



/**

 *  A utility method that reads a single part of the multipart request

 *  that represents a parameter. A subclass can override this method

 *  for a better optimized or differently behaved implementation.

 *

 *  @param in - the stream from which to read the parameter information

 *  @param boundary - the boundary signifying the end of this part

 *  @return the parameter value

 *  @exception IOException - if there's a problem reading or parsing the request

 */

protected String readParameter(MultipartInputStreamHandler in,String boundary) throws IOException

{

  StringBuffer sbuf = new StringBuffer();

  String line;



  while ((line = in.readLine()) != null) {

    if (line.startsWith(boundary)) break;

    sbuf.append(line + "\r\n"); // add the \r\n in case there are many lines

  }



  if (sbuf.length() == 0) {

    return null; // nothing read

  }



  sbuf.setLength(sbuf.length() - 2); // cut off the last line's \r\n

  return sbuf.toString(); // no URL decoding needed

}



/**

 *  A utility method that reads a single part of the multipart request

 *  that represents a file, and saves the file to the given directory.

 *  A subclass can override this method for a better optimized or

 *  differently behaved implementation.

 *

 *  @param in - the stream from which to read the file

 *  @param boundary - the boundary signifying the end of this part

 *  @param filename - the name under which to save the uploaded file

 *  @exception IOException if there's a problem reading or parsing the request

 */

protected void readAndSaveFile(MultipartInputStreamHandler in,

  String boundary,

  String filename,

  String contentType) throws IOException

{

  if (dir == null ) throw new IOException("you have to put DEST_DIR in form");

  OutputStream out = null;

  // A filename of NO_FILE means no file was sent, so just  read to the

  // next boundary and ignore the empty contents

  if (filename.equals(NO_FILE))

  {

    out = new ByteArrayOutputStream(); // write to nowhere

  }

  // A MacBinary file goes through a decoder

  else if (contentType.equals("application/x-macbinary"))

  {

    File f = new File(dir + File.separator + filename);

    out = new MacBinaryDecoderOutputStream(

              new BufferedOutputStream(new FileOutputStream(f), 8 * 1024) );

  }

  else // A real file's contents are written to disk

  {

    File f = new File(dir + File.separator + filename);

    out = new BufferedOutputStream(new FileOutputStream(f), 8  * 1024);

  }



  byte[] bbuf = new byte[100 * 1024]; // 100K

  int result;

  String line;



  // ServletInputStream.readLine() has the annoying habit of

  // adding a \r\n to the end of the last line.

  // Since we want a byte-for-byte transfer, we have to cut those chars.

  boolean rnflag = false;

  while ((result = in.readLine(bbuf, 0, bbuf.length)) != -1)

  {

    // Check for boundary

    if (result > 2 && bbuf[0] == '-' && bbuf[1] == '-')

    { // quick pre-check

      line = new String(bbuf, 0, result, "ISO-8859-1");

      if (line.startsWith(boundary)) break;

    }

    // Are we supposed to write \r\n for the last iteration?

    if (rnflag)

    {

      out.write('\r'); out.write('\n');

      rnflag = false;

    }

    // Write the buffer, postpone any ending \r\n

    if (result >= 2 &&

      bbuf[result - 2] == '\r' &&

      bbuf[result - 1] == '\n')

    {

      out.write(bbuf, 0, result - 2); // skip the last 2 chars

      rnflag = true; // make a note to write them on the next iteration

    }

    else

    {

      out.write(bbuf, 0, result);

    }

  }

  out.flush();

  out.close();

}



// Extracts and returns the boundary token from a line.

//

private String extractBoundary(String line) {

  // Use lastIndexOf() because IE 4.01 on Win98 has been known to send the

  // "boundary=" string multiple times. Thanks to David Wall for this fix.

  int index = line.lastIndexOf("boundary=");

  if (index == -1) {

    return null;

  }

  String boundary = line.substring(index + 9); // 9  for "boundary="



  // The real boundary is always preceeded by an extra "--"

  boundary = "--" + boundary;



  return boundary;

}



// Extracts and returns disposition info from a line, as a String array

// with elements: disposition, name, filename. Throws an IOException

// if the line is malformatted.

//

private String[] extractDispositionInfo(String line) throws IOException {

  // Return the line's data as an array: disposition, name,filename

  String[] retval = new String[3];



  // Convert the line to a lowercase string without the ending \r\n

  // Keep the original line for error messages and for variable names.

  String origline = line;

  line = origline.toLowerCase();



  // Get the content disposition, should be "form-data"

  int start = line.indexOf("content-disposition: ");

  int end = line.indexOf(";");

  if (start == -1 || end == -1) {

    throw new IOException("Content disposition corrupt: " +  origline);

  }

  String disposition = line.substring(start + 21, end);

  if (!disposition.equals("form-data")) {

    throw new IOException("Invalid content disposition: " +  disposition);

  }



  // Get the field name

  start = line.indexOf("name=\"", end); // start at last semicolon

  end = line.indexOf("\"", start + 7); // skip name=\"

  if (start == -1 || end == -1) {

    throw new IOException("Content disposition corrupt: " +  origline);

  }

  String name = origline.substring(start + 6, end);



  // Get the filename, if given

  String filename = null;

  start = line.indexOf("filename=\"", end + 2); // start  after name

  end = line.indexOf("\"", start + 10); // skip  filename=\"

  if (start != -1 && end != -1) { // note the !=

    filename = origline.substring(start + 10, end);

    // The filename may contain a full path. Cut to just the  filename.

    int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));

    if (slash > -1) {

      filename = filename.substring(slash + 1); // past last  slash

    }

    if (filename.equals("")) filename = NO_FILE; // sanity  check

  }

  // Return a String array: disposition, name, filename

  retval[0] = disposition;

  retval[1] = name;

  retval[2] = filename;

  return retval;

}



// Extracts and returns the content type from a line, or null if the

// line was empty. Throws an IOException if the line is malformatted.

//

private String extractContentType(String line) throws IOException {

  String contentType = null;



  // Convert the line to a lowercase string

  String origline = line;

  line = origline.toLowerCase();



  // Get the content type, if any

  if (line.startsWith("content-type")) {

    int start = line.indexOf(" ");

    if (start == -1) {

      throw new IOException("Content type corrupt: " +  origline);

    }

    contentType = line.substring(start + 1);

  }

  else if (line.length() != 0) { // no content type, so  should be empty

    throw new IOException("Malformed line after disposition: "  + origline);

  }



  return contentType;

}

/*

private String constructFileName(HttpServletRequest request,String filename) {

//create the new filename

  if (!filename.equals(NO_FILE)){

    int idx = filename.lastIndexOf('.');

    String s = null;

    if (idx >= 0 )

      s = filename.substring(0,idx);

    else

      s =filename;

    Calendar now = emisUtil.getLocaleCalendar();

    s = request.getRemoteAddr()+"_"+ emisUtil.getYearS(now)+

    "_"+emisUtil.getMonthS(now)+"_"+emisUtil.getDateS(now)+

    emisUtil.getHourS(now) +"_"+emisUtil.getMinS(now)+s;

    s += filename.substring(filename.lastIndexOf('.'),filename.length());

    //s += ".zip";

    now = null;

    return s;

  }

  else{

    return filename;

  }

}

*/

}





