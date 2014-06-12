package com.squareup.javawriter;


/**
 * Only named types. Doesn't cover anonymous inner classes.
 */
public abstract class TypeWriter /* ha ha */ extends Modifiable
    implements Writable, HasClassReferences {
  final ClassName name;

  TypeWriter(ClassName name) {
    this.name = name;
  }
}
