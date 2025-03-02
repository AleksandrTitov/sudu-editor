package org.sudu.experiments.parser.java.walker;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sudu.experiments.parser.Interval;
import org.sudu.experiments.parser.Pos;
import org.sudu.experiments.parser.java.gen.JavaParser;
import org.sudu.experiments.parser.java.gen.JavaParserBaseListener;
import org.sudu.experiments.parser.java.model.Field;
import org.sudu.experiments.parser.java.model.JavaClass;
import org.sudu.experiments.parser.java.model.Method;

import static org.sudu.experiments.parser.ParserConstants.IntervalTypes.Java.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Walker for getting JavaClass
public class ClassWalker extends JavaParserBaseListener {
  public JavaClass dummy;
  public JavaClass current;

  public int curDepth = 0;
  public int maxDepth = 0;
  public double depthSum = 0;
  public int amount = 0;

  public List<Interval> intervals;

  private int lastIntervalEnd = 0;

  public ClassWalker() {
    dummy = new JavaClass(null, null, null);
    current = dummy;
    intervals = new ArrayList<>();
  }

  @Override
  public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
    super.enterPackageDeclaration(ctx);
    addInterval(ctx, PACKAGE);
  }

  @Override
  public void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
    super.enterImportDeclaration(ctx);
    addInterval(ctx, IMPORT);
  }

  @Override
  public void enterTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
    super.enterTypeDeclaration(ctx);
    addInterval(ctx, TYPE_DECL);
  }

  @Override
  public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
    super.enterClassBodyDeclaration(ctx);
    if (ctx.parent instanceof JavaParser.EnumBodyDeclarationsContext) return;
    addInterval(ctx, CLASS_BODY);
  }

  @Override
  public void enterInterfaceBodyDeclaration(JavaParser.InterfaceBodyDeclarationContext ctx) {
    super.enterInterfaceBodyDeclaration(ctx);
    addInterval(ctx, CLASS_BODY);
  }

  @Override
  public void enterAnnotationTypeElementDeclaration(JavaParser.AnnotationTypeElementDeclarationContext ctx) {
    super.enterAnnotationTypeElementDeclaration(ctx);
    addInterval(ctx, UNKNOWN);
  }

//  @Override
//  public void enterAnySeq(JavaParser.AnySeqContext ctx) {
//    super.enterAnySeq(ctx);
//    addInterval(ctx, UNKNOWN);
//  }

  @Override
  public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    super.enterClassDeclaration(ctx);
    var id = getIdentifier(ctx.identifier());
    JavaClass clazz = new JavaClass(id.getText(), Pos.fromNode(id), current);
    current.nestedClasses.add(clazz);
    current = clazz;

    lastIntervalEnd = ctx.classBody().LBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
    super.enterInterfaceDeclaration(ctx);
    var id = getIdentifier(ctx.identifier());
    JavaClass clazz = new JavaClass(id.getText(), Pos.fromNode(id), current);
    current.nestedClasses.add(clazz);
    current = clazz;

    lastIntervalEnd = ctx.interfaceBody().LBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
    super.enterEnumDeclaration(ctx);
    var id = getIdentifier(ctx.identifier());
    JavaClass clazz = new JavaClass(id.getText(), Pos.fromNode(id), current);
    current.nestedClasses.add(clazz);
    current = clazz;

    lastIntervalEnd = ctx.LBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void enterRecordDeclaration(JavaParser.RecordDeclarationContext ctx) {
    super.enterRecordDeclaration(ctx);
    var id = getIdentifier(ctx.identifier());
    JavaClass clazz = new JavaClass(id.getText(), Pos.fromNode(id), current);
    current.nestedClasses.add(clazz);
    current = clazz;

    lastIntervalEnd = ctx.recordBody().LBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void enterAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
    super.enterAnnotationTypeDeclaration(ctx);
    var id = getIdentifier(ctx.identifier());
    JavaClass clazz = new JavaClass(id.getText(), Pos.fromNode(id), current);
    current.nestedClasses.add(clazz);
    current = clazz;

    lastIntervalEnd = ctx.annotationTypeBody().LBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    super.exitClassDeclaration(ctx);
    current = current.innerClass;

    lastIntervalEnd = ctx.classBody().RBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
    super.exitInterfaceDeclaration(ctx);
    current = current.innerClass;

    lastIntervalEnd = ctx.interfaceBody().RBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
    super.exitEnumDeclaration(ctx);
    current = current.innerClass;

    lastIntervalEnd = ctx.RBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void exitRecordDeclaration(JavaParser.RecordDeclarationContext ctx) {
    super.exitRecordDeclaration(ctx);
    current = current.innerClass;

    lastIntervalEnd = ctx.recordBody().RBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
    super.exitAnnotationTypeDeclaration(ctx);
    current = current.innerClass;

    lastIntervalEnd = ctx.annotationTypeBody().RBRACE().getSymbol().getStartIndex() + 1;
  }

  @Override
  public void exitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
    super.exitFieldDeclaration(ctx);
    var variableDeclarators = ctx.variableDeclarators().variableDeclarator();
    for (var variableDeclarator : variableDeclarators) {
      var variableDeclaratorId = variableDeclarator.variableDeclaratorId();
      var node = getIdentifier(variableDeclaratorId.identifier());
      boolean isStatic = isStatic(getModifiers(ctx));
      addField(node, isStatic);
    }
  }

  @Override
  public void exitConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
    super.exitConstDeclaration(ctx);
    var constantDeclarators = ctx.constantDeclarator();
    for (var constantDeclarator : constantDeclarators) {
      var node = getIdentifier(constantDeclarator.identifier());
      addField(node, true);
    }
  }

  @Override
  public void exitAnnotationConstantRest(JavaParser.AnnotationConstantRestContext ctx) {
    super.exitAnnotationConstantRest(ctx);
    var variableDeclarators = ctx.variableDeclarators().variableDeclarator();
    for (var variableDeclarator : variableDeclarators) {
      var variableDeclaratorId = variableDeclarator.variableDeclaratorId();
      var node = getIdentifier(variableDeclaratorId.identifier());
      boolean isStatic = isStatic(getAnnotationMethodOrConstantRestModifiers((JavaParser.AnnotationMethodOrConstantRestContext) ctx.parent));
      addField(node, isStatic);
    }
  }

  @Override
  public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
    super.exitMethodDeclaration(ctx);
    var node = getIdentifier(ctx.identifier());
    var isStatic = isStatic(getModifiers(ctx));
    addMethod(node, isStatic);
  }

  @Override
  public void exitAnnotationMethodRest(JavaParser.AnnotationMethodRestContext ctx) {
    super.exitAnnotationMethodRest(ctx);
    var node = getIdentifier(ctx.identifier());
    var isStatic = isStatic(getAnnotationMethodOrConstantRestModifiers((JavaParser.AnnotationMethodOrConstantRestContext) ctx.parent));
    addMethod(node, isStatic);
  }

  @Override
  public void exitInterfaceCommonBodyDeclaration(JavaParser.InterfaceCommonBodyDeclarationContext ctx) {
    super.exitInterfaceCommonBodyDeclaration(ctx);
    var node = getIdentifier(ctx.identifier());
    addMethod(node, false);
  }

  private void addField(TerminalNode node, boolean isStatic) {
    var token = node.getSymbol();
    var pos = new Pos(token.getLine(), token.getCharPositionInLine());
    var text = token.getText();
    current.fields.add(new Field(text, pos, isStatic));
  }

  private void addMethod(TerminalNode node, boolean isStatic) {
    var token = node.getSymbol();
    var pos = new Pos(token.getLine(), token.getCharPositionInLine());
    var text = token.getText();
    current.methods.add(new Method(text, pos, isStatic, List.of()));
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    super.enterEveryRule(ctx);
    curDepth++;
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    super.exitEveryRule(ctx);
    curDepth--;
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    super.visitTerminal(node);
    maxDepth = Math.max(maxDepth, curDepth);
    depthSum += curDepth;
    amount++;
  }

  private void addInterval(ParserRuleContext ctx, int type) {
    int end = ctx.stop.getStopIndex() + 1;
    intervals.add(new Interval(lastIntervalEnd, end, type));
    lastIntervalEnd = end;
  }

  public static boolean isStatic(List<String> list) {
    return list.contains("static");
  }

  public static TerminalNode getIdentifier(JavaParser.IdentifierContext ctx) {
    return (TerminalNode) ctx.children.get(0);
  }

  public static List<String> getAnnotationMethodOrConstantRestModifiers(JavaParser.AnnotationMethodOrConstantRestContext ctx) {
    ArrayList<String> modifiers = new ArrayList<>();
    if (ctx.parent.parent instanceof JavaParser.AnnotationTypeElementDeclarationContext declarationContext) {
      for (var mod : declarationContext.modifier())
        modifiers.add(mod.children.get(0).getText());
    }
    return modifiers;
  }

  public static List<String> getModifiers(RuleContext ctx) {
    var parent = ctx.parent.parent;
    JavaParser.ClassBodyDeclarationContext classBodyDeclaration = null;

    if (parent instanceof JavaParser.ClassBodyDeclarationContext) {
      classBodyDeclaration = (JavaParser.ClassBodyDeclarationContext) parent;
    } else if (parent instanceof JavaParser.MemberDeclarationContext memberDeclarationContext) {
      if (memberDeclarationContext.parent instanceof JavaParser.ClassBodyDeclarationContext) {
        classBodyDeclaration = (JavaParser.ClassBodyDeclarationContext) memberDeclarationContext.parent;
      }
    }
    if (classBodyDeclaration == null) return Collections.emptyList();
    ArrayList<String> modifiers = new ArrayList<>();
    for (var mod : classBodyDeclaration.modifier())
      modifiers.add(mod.children.get(0).getText());
    return modifiers;
  }
}