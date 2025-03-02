package org.sudu.experiments.demo;

import java.nio.charset.StandardCharsets;

public interface StartFile {

  String START_CODE_JAVA = """
      package sudu.editor;
      
      /*
      * This is multiplatform lightweight code editor
      */
      
      public class Main {
      
        private static String helloWorld = "Hello,\\tWorld\\u3000";
        private static char n = '\\n';
        private static int a;
            
        public int field;

        public static void main(String[] args) {
          System.out.println(helloWorld + n);
          sum(a + a);
        }
            
        @Deprecated
        private static void sum() {
        }
            
        @Deprecated
        private static int sum(int a) {
          int b = 10;
          int c = a + b;
          return c;
        }
            
        public int sumField(int field) {
          return field + this.field;
        }

        public interface A {
          int sumField(int field);
          
          default void foo() {
            sumField(10);
          }
        }
      }
      """;

  static char[] getChars() {
    return START_CODE_JAVA.toCharArray();
  }
}