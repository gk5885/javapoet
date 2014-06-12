package com.squareup.javawriter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class JavaIdentifiers {
  static boolean isValidJavaIdentifier(String possibleIdentifier) {
    checkNotNull(possibleIdentifier);
    checkArgument(!possibleIdentifier.isEmpty());
    if (!Character.isJavaIdentifierStart(possibleIdentifier.charAt(0))) {
      return false;
    }
    for (int i = 1; i < possibleIdentifier.length(); i++) {
      if (!Character.isJavaIdentifierPart(possibleIdentifier.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) {
    System.out.println(Character.isJavaIdentifierPart('\u2020'));
  }

  private JavaIdentifiers() {}
}
