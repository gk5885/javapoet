package com.squareup.javawriter;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map.Entry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@RunWith(JUnit4.class)
public class JavaWriterTest {
  @Test public void test() {
    ImmutableMap<String, Class<?>> values = ImmutableMap.<String, Class<?>>of(
        "firstName", String.class,
        "lastName", String.class,
        "locale", Locale.class);

    JavaWriter writer = JavaWriter.inPackage("test");
    ClassWriter personWriter = writer.addClass("Person");
    personWriter.addModifiers(PUBLIC, FINAL);

    ClassWriter builderWriter = personWriter.addNestedClass("Builder");
    builderWriter.addModifiers(PUBLIC, STATIC, FINAL);

    ConstructorWriter personConstructor = personWriter.addConstructor();
    personConstructor.addParameter(builderWriter, "builder");
    personConstructor.addModifiers(PRIVATE);

    MethodWriter buildMethod = builderWriter.addMethod(personWriter, "build");
    BlockWriter builderMethodBody = buildMethod.body();

    for (Entry<String, Class<?>> entry : values.entrySet()) {
      String name = entry.getKey();
      Class<?> type = entry.getValue();
      // value type field
      personWriter.addField(type, name).withModifiers(PRIVATE, FINAL);
      // value type method
      personWriter.addMethod(type, name).body().addSnippet("return %s;", name);
      // builder field
      builderWriter.addField(type, name).withModifiers(PRIVATE);
      // person constructor init
      personConstructor.blockWriter().addSnippet("this.%1$s = builder.%1$s;", name);
      // builder method
      MethodWriter builderMethod = builderWriter.addMethod(builderWriter, name);
      builderMethod.withModifiers(PUBLIC);
      builderMethod.addParameter(type, name);
      builderMethod.body()
          .addSnippet("this.%1$s = %1$s;", name)
          .addSnippet("return this;");
      // build method
      builderMethodBody.addSnippet(Joiner.on('\n').join(
          "if (%1$s == null) {",
          "  throw new NullPointerException(%1$s);",
          "}"), name);
    }

    builderMethodBody
        .addSnippet(String.format("return new %s(this);", personWriter.name.simpleName()));

    System.out.println(writer);
  }
}
