/*
 * $Header: /repository/src3/src/com/emis/http/emisHttpFiles.java,v 1.1.1.1 2005/10/14 12:42:10 andy Exp $
 */
package com.emis.http;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

// Referenced classes of package com.jspsmart.upload:
//            File, SmartUpload

public class emisHttpFiles {
  private emisHttpTransfer m_parent;
  private Hashtable m_files;
  private int m_counter;

  emisHttpFiles() {
    m_files = new Hashtable();
    m_counter = 0;
  }

  protected void addFile(emisHttpFile newFile) {
    if (newFile == null) {
      throw new IllegalArgumentException("newFile cannot be null.");
    } else {
      m_files.put(new Integer(m_counter), newFile);
      m_counter++;
      return;
    }
  }

  public emisHttpFile getFile(int index) {
    if (index < 0)
      throw new IllegalArgumentException("File's index cannot be a negative value (1210).");
    emisHttpFile retval = (emisHttpFile) m_files.get(new Integer(index));
    if (retval == null)
      throw new IllegalArgumentException("Files' name is invalid or does not exist (1205).");
    else
      return retval;
  }

  public int getCount() {
    return m_counter;
  }

  public long getSize()
      throws IOException {
    long tmp = 0L;
    for (int i = 0; i < m_counter; i++)
      tmp += getFile(i).getSize();

    return tmp;
  }

  public Collection getCollection() {
    return m_files.values();
  }

  public Enumeration getEnumeration() {
    return m_files.elements();
  }

}
