package org.sudu.experiments.parser.common;

import org.antlr.v4.runtime.*;
import org.sudu.experiments.parser.*;

import java.util.*;

public abstract class BaseParser {

  protected String fileSource;
  protected List<Token> allTokens;
  protected int[] tokenTypes;
  protected int[] tokenStyles;
  protected ArrayWriter writer;

  protected CommonTokenStream tokenStream;

  protected TokenRecognitionListener tokenRecognitionListener;

  protected abstract boolean isMultilineToken(int tokenType);
  protected abstract boolean isComment(int tokenType);
  protected abstract Lexer initLexer(CharStream stream);
  protected abstract boolean tokenFilter(Token token);

  protected void initLexer(String source) {
    this.fileSource = source;
    this.tokenRecognitionListener = new TokenRecognitionListener();
    CharStream stream = CharStreams.fromString(fileSource);
    Lexer lexer = initLexer(stream);
    lexer.addErrorListener(tokenRecognitionListener);

    tokenStream = new CommonTokenStream(lexer);
    tokenStream.fill();
/*    if (tokenErrorOccurred()) {
      makeErrorToken();
      return;
    }*/

    allTokens = tokenStream.getTokens();
    tokenTypes = new int[allTokens.size()];
    tokenStyles = new int[allTokens.size()];
  }

  protected boolean tokenErrorOccurred() {
    return tokenRecognitionListener.syntaxErrorOccurred;
  }

  protected void makeErrorToken() {
    Token errorToken = new ErrorToken(fileSource);
    allTokens = List.of(errorToken);
    tokenTypes = new int[1];
    tokenStyles = new int[1];
  }

  protected static Token makeSplitToken(Token token, String text, int line, int start, int stop) {
    return new Token() {
      @Override
      public String getText() {
        return text;
      }

      @Override
      public int getType() {
        return token.getType();
      }

      @Override
      public int getLine() {
        return line;
      }

      @Override
      public int getCharPositionInLine() {
        return token.getCharPositionInLine();
      }

      @Override
      public int getChannel() {
        return token.getChannel();
      }

      @Override
      public int getTokenIndex() {
        return token.getTokenIndex();
      }

      @Override
      public int getStartIndex() {
        return start;
      }

      @Override
      public int getStopIndex() {
        return stop;
      }

      @Override
      public TokenSource getTokenSource() {
        return token.getTokenSource();
      }

      @Override
      public CharStream getInputStream() {
        return token.getInputStream();
      }
    };
  }

  protected void writeTokens(int N, Map<Integer, List<Token>> tokensByLine) {
    for (int i = 0; i < N; i++) {
      var tokensOnLine = tokensByLine.getOrDefault(i + 1, Collections.emptyList());
      writer.write(tokensOnLine.size());
      for (var token: tokensOnLine) {
        int start = token.getStartIndex();
        int stop = token.getStopIndex() + 1;
        int ind = token.getTokenIndex();
        int type = token instanceof SplitToken splitToken ? splitToken.getSplitType() : tokenTypes[ind];
        int style = token instanceof SplitToken ? ParserConstants.TokenTypes.DEFAULT : tokenStyles[ind];
        writer.write(start, stop, type, style);
      }
    }
  }

  protected void writeIntervals(List<Interval> intervalList, int intervalStart) {
    for (Interval interval : intervalList) {
      writer.write(interval.start + intervalStart, interval.stop + intervalStart, interval.intervalType);
    }
  }

  protected void writeUsageToDefinitions(Map<Pos, Pos> usageMap) {
    for (var entry: usageMap.entrySet()) {
      var usage = entry.getKey();
      var def = entry.getValue();
      writer.write(usage.line - 1, usage.pos, def.line - 1, def.pos);
    }
  }

  protected Map<Integer, List<Token>> groupTokensByLine(List<Token> allTokens) {
    Map<Integer, List<Token>> lineToTokens = new HashMap<>();
    for (var token : allTokens) {
      for (var splitted : splitToken(token)) {
        int line = splitted.getLine();
        if (!lineToTokens.containsKey(line)) lineToTokens.put(line, new ArrayList<>());
        lineToTokens.get(line).add(splitted);
      }
    }
    return lineToTokens;
  }

  protected List<Token> splitToken(Token token) {
     if (isMultilineToken(token.getType())) return splitTokenByLine(token);
     return Collections.singletonList(token);
  }

  protected List<Token> splitTokenByLine(Token token) {
    List<Token> result = new ArrayList<>();
    String text = token.getText();

    StringTokenizer lineTokenizer = new StringTokenizer(text, "\n\r", true);
    int lineNum = token.getLine();
    int start = token.getStartIndex();
    while (lineTokenizer.hasMoreTokens()) {
      var line = lineTokenizer.nextToken();
      if (line.equals("\n"))
        lineNum++;
      else if (!line.equals("\r"))
        result.add(makeSplitToken(token, line, lineNum, start, start + line.length() - 1));

      start += line.length();
    }
    return result;
  }

  protected void highlightTokens() {
    for (var token: allTokens) {
      int ind = token.getTokenIndex();
      if (isComment(token.getType())) tokenTypes[ind] = ParserConstants.TokenTypes.COMMENT;
    }
  }

}
