package com.emis.report;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public final class emisStringTokenizer extends StringTokenizer {
  String lastToken = "";

  /**
   * Constructs a string tokenizer for the specified string. The
   * characters in the <code>delim</code> argument are the delimiters
   * for separating tokens. Delimiter characters themselves will not
   * be treated as tokens.
   *
   * @param str   a string to be parsed.
   * @param delim the delimiters.
   */
  public emisStringTokenizer(final String str, final String delim) {
    super(str, delim);
  }

  /**
   * Returns the next token from this string tokenizer.
   *
   * @return the next token from this string tokenizer.
   * @throws NoSuchElementException if there are no more tokens in this
   *                                tokenizer's string.
   */
  public final String nextToken() {
    if (this.hasMoreTokens()) {
      lastToken = super.nextToken();
    }
    return lastToken;
  }
}
