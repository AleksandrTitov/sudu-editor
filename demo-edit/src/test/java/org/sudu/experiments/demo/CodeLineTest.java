package org.sudu.experiments.demo;

import org.junit.jupiter.api.Test;
import org.sudu.experiments.math.ArrayOp;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CodeLineTest {

  @Test
  public void makeStringTest() {
    CodeLine testLine = line1();

    String r1 = testLine.makeString();
    String r2 = testLine.makeString(0);
    String r3 = testLine.makeString(16);
    String r4 = testLine.makeString(16, 24);
    assertEquals(r1, LINE_1);
    assertEquals(r2, LINE_1);
    assertEquals(r3, LINE_1.substring(16));
    assertEquals(r4, LINE_1.substring(16, 24));
  }

  @Test
  public void deleteFullLine() {
    CodeLine testLine = line1();

    testLine.delete(0, LINE_1.length());

    assertArrayEquals(testLine.elements, new CodeLine[]{});
    assertEquals(testLine.totalStrLength, 0);
  }

  @Test
  public void deleteTest1() {
    CodeLine testLine = line1();
    CodeElement[] elements = Arrays.copyOf(elements1, elements1.length);

    elements = ArrayOp.remove(elements, 0, 1, new CodeElement[elements.length - 1]);
    testLine.delete(0, testLine.elements[0].s.length());

    assertArrayEquals(testLine.elements, elements);
    assertEquals(testLine.totalStrLength, countLen(testLine));

    elements = ArrayOp.remove(elements, 1, testLine.elements.length, new CodeElement[elements.length - (testLine.elements.length - 1)]);
    testLine.delete(testLine.elements[0].s.length());

    assertArrayEquals(testLine.elements, elements);
    assertEquals(testLine.totalStrLength, countLen(testLine));

    elements = ArrayOp.remove(elements, 0, 1, new CodeElement[elements.length - 1]);
    testLine.delete(0, testLine.elements[0].s.length());

    assertArrayEquals(testLine.elements, elements);
    assertArrayEquals(testLine.elements, new CodeElement[]{});
    assertEquals(testLine.totalStrLength, countLen(testLine));
    assertEquals(testLine.totalStrLength, 0);
  }

  @Test
  public void deleteTest2() {
    CodeLine testLine = line1();
    String lineString = LINE_1;

    assertEquals(testLine.makeString(), lineString);

    testLine.delete(0, 4);
    lineString = " is an experimental project to write a portable (Web + Desktop) editor in java and kotlin.";
    assertEquals(testLine.makeString(), lineString);

    testLine.delete(1, 3);
    lineString =  "  an experimental project to write a portable (Web + Desktop) editor in java and kotlin.";
    assertEquals(testLine.makeString(), lineString);

    testLine.delete(14, 26);
    lineString =  "  an experimento write a portable (Web + Desktop) editor in java and kotlin.";
    assertEquals(testLine.makeString(), lineString);

    testLine.delete(34, 48);
    lineString =  "  an experimento write a portable ) editor in java and kotlin.";
    assertEquals(testLine.makeString(), lineString);

    testLine.delete(19, lineString.length());
    lineString = "  an experimento wr";
    assertEquals(testLine.makeString(), lineString);
  }

  private int countLen(CodeLine line) {
    return Arrays.stream(line.elements).reduce(0, (integer, codeElement) -> integer + codeElement.s.length(), Integer::sum);
  }

  private static final String LINE_1 =
      "This is an experimental project to write a portable (Web + Desktop) editor in java and kotlin.";

  private static final CodeElement[] elements1 = new CodeElement[]{
      new CodeElement("This is an experimental project"),
      new CodeElement(" "),
      new CodeElement("to write a portable"),
      new CodeElement(" "),
      new CodeElement("(Web + Desktop)"),
      new CodeElement(" "),
      new CodeElement("editor in java and kotlin"),
      new CodeElement(".")
  };

  private static CodeLine line1() {
    return new CodeLine(Arrays.copyOf(elements1, elements1.length));
  }
}
