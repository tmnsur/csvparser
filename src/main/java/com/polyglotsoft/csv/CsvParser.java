package com.polyglotsoft.csv;

import java.util.List;

public class CsvParser {
    public static final char COMMA = (char) 0x2C;
    public static final char SEMICOLON = (char) 0x3B;
    public static final char CR = (char) 0x0D;
    public static final char SINGLE_QUOTES = (char) 0x27;
    public static final char DOUBLE_QUOTES = (char) 0x22;
    public static final char LF = (char) 0x0A;
    public static final int LINE_LIMIT = 1024;
    public static final int COLUMN_LIMIT = 10;

    private final char delimiter;
    private final char quotation;
    private final int lineLimit;
    private final int columnLimit;

    public CsvParser(char delimiter, char quotation, int lineLimit, int columnLimit) {
        this.delimiter = delimiter;
        this.quotation = quotation;
        this.lineLimit = lineLimit;
        this.columnLimit = columnLimit;
    }

    public CsvParser(char delimiter) {
        this.delimiter = delimiter;
        this.quotation = DOUBLE_QUOTES;
        this.lineLimit = LINE_LIMIT;
        this.columnLimit = COLUMN_LIMIT;
    }

    public CsvParser() {
        this.delimiter = COMMA;
        this.quotation = DOUBLE_QUOTES;
        this.lineLimit = LINE_LIMIT;
        this.columnLimit = COLUMN_LIMIT;
    }

    public static CsvParser detectParser(List<String> first5Lines, int lineLimit, int columnLimit) {
        int minSingleQuoteColumns = Integer.MAX_VALUE;
        int minDoubleQuoteColumns = Integer.MAX_VALUE;

        int maxCommaColumns = 0;

        CsvParser commaDoubleQuotesParser = new CsvParser(COMMA, DOUBLE_QUOTES, lineLimit, columnLimit);
        for (String sampleLine : first5Lines) {
            int columnCount = commaDoubleQuotesParser.parseLine(sampleLine).length;

            maxCommaColumns = Math.max(maxCommaColumns, columnCount);
            minDoubleQuoteColumns = Math.min(minDoubleQuoteColumns, columnCount);
        }

        CsvParser commaSingleQuotesParser = new CsvParser(COMMA, SINGLE_QUOTES, lineLimit, columnLimit);
        for (String sampleLine : first5Lines) {
            int columnCount = commaSingleQuotesParser.parseLine(sampleLine).length;

            maxCommaColumns = Math.max(maxCommaColumns, columnCount);
            minSingleQuoteColumns = Math.min(minSingleQuoteColumns, columnCount);
        }

        int maxSemicolonColumns = 0;

        CsvParser semicolonDoubleQuotesParser = new CsvParser(SEMICOLON, DOUBLE_QUOTES, lineLimit, columnLimit);
        for (String sampleLine : first5Lines) {
            int columnCount = semicolonDoubleQuotesParser.parseLine(sampleLine).length;

            maxSemicolonColumns = Math.max(maxSemicolonColumns, columnCount);
            minDoubleQuoteColumns = Math.min(minDoubleQuoteColumns, columnCount);
        }

        CsvParser semicolonSingleQuotesParser = new CsvParser(SEMICOLON, SINGLE_QUOTES, lineLimit, columnLimit);
        for (String sampleLine : first5Lines) {
            int columnCount = semicolonSingleQuotesParser.parseLine(sampleLine).length;

            maxSemicolonColumns = Math.max(maxSemicolonColumns, columnCount);
            minSingleQuoteColumns = Math.min(minSingleQuoteColumns, columnCount);
        }

        if (maxCommaColumns >= maxSemicolonColumns) {
            if (minDoubleQuoteColumns >= minSingleQuoteColumns) {
                return commaDoubleQuotesParser;
            }

            return commaSingleQuotesParser;
        }

        if (minDoubleQuoteColumns >= minSingleQuoteColumns) {
            return semicolonDoubleQuotesParser;
        }

        return semicolonSingleQuotesParser;
    }

    private String newString(char[] chars, int start, int end) {
        int index = -1;
        int shrankLength = end - start;
        char[] shrankChars = null;

        for (int i = start; i < end; ++i) {
            if (7 == chars[i] || (quotation == chars[i] && i + 1 < end && 7 == chars[i + 1])) {
                if (null == shrankChars) {
                    shrankChars = new char[end - start - 1];

                    int currentLength = i - start;
                    if (0 < currentLength) {
                        System.arraycopy(chars, start, shrankChars, start, i - start);

                        index = currentLength - 1;
                    }
                }

                if (7 == chars[i]) {
                    --shrankLength;

                    continue;
                }
            }

            if (null != shrankChars) {
                shrankChars[++index] = chars[i];
            }
        }

        if (null == shrankChars) {
            return new String(chars, start, end - start);
        }

        return new String(shrankChars, 0, shrankLength);
    }

    public String[] parseLine(String line) {
        if (null == line || line.isEmpty()) {
            return new String[0];
        }

        if (lineLimit < line.length()) {
            line = line.substring(0, lineLimit);
        }

        char[] chars = line.toCharArray();

        int resultLength = 1;
        int lastQuotationStart = -1;
        int lastQuotationEnd = -1;
        int lastDelimiter = -1;
        boolean quoted = false;
        for (int i = 0; i < chars.length; ++i) {
            if (quotation == chars[i]) {
                if (i + 1 < chars.length && quotation == chars[i + 1]) {
                    if (!quoted) {
                        boolean emptyQuotes = true;
                        for (int j = i + 2; j < chars.length; ++j) {
                            if (delimiter == chars[j]) {
                                break;
                            }

                            if (!Character.isWhitespace(chars[j])) {
                                emptyQuotes = false;

                                break;
                            }
                        }

                        if (emptyQuotes) {
                            chars[i] = 7;
                        } else {
                            boolean validQuotation = true;
                            for (int j = i - 1; j != lastDelimiter; --j) {
                                if (!Character.isWhitespace(chars[j])) {
                                    validQuotation = false;
                                    break;
                                }
                            }

                            if (validQuotation) {
                                lastQuotationStart = i - 1;
                                quoted = true;
                            }
                        }
                    }

                    chars[i + 1] = 7;
                } else {
                    if (quoted) {
                        boolean escapedQuotation = true;
                        for (int j = i - 1; j != lastDelimiter; --j) {
                            if (7 != chars[j] && quotation != chars[j]) {
                                escapedQuotation = false;
                                break;
                            }
                        }

                        if (escapedQuotation) {
                            chars[i] = 7;
                        } else {
                            if (2 < i && 7 == chars[i - 1] && quotation == chars[i - 2] && i == chars.length - 1) {
                                chars[i - 2] = 7;
                            }

                            lastQuotationEnd = i;
                            quoted = false;
                        }
                    } else {
                        boolean validQuotation = true;
                        for (int j = i - 1; j != lastDelimiter; --j) {
                            if (!Character.isWhitespace(chars[j])) {
                                validQuotation = false;
                                break;
                            }
                        }

                        if (validQuotation) {
                            lastQuotationStart = i;
                            quoted = true;
                        }
                    }
                }
            } else if (delimiter == chars[i] && !quoted) {
                chars[i] = 0;

                if (-1 != lastQuotationStart && -1 != lastQuotationEnd) {
                    for (int j = lastDelimiter + 1; j <= lastQuotationStart; ++j) {
                        chars[j] = 7;
                    }

                    for (int j = lastQuotationEnd; j < i; ++j) {
                        chars[j] = 7;
                    }
                }

                lastDelimiter = i;
                lastQuotationStart = -1;
                lastQuotationEnd = -1;

                if (resultLength == columnLimit) {
                    break;
                }

                ++resultLength;
            }
        }

        if (-1 != lastQuotationStart && -1 != lastQuotationEnd) {
            for (int j = lastDelimiter + 1; j <= lastQuotationStart; ++j) {
                chars[j] = 7;
            }

            for (int j = lastQuotationEnd; j < chars.length; ++j) {
                chars[j] = 7;
            }
        }

        if (-1 != lastQuotationStart && -1 == lastQuotationEnd) {
            for (int j = lastQuotationStart + 1; j < chars.length; ++j) {
                if (delimiter == chars[j]) {
                    chars[j] = 0;
                    ++resultLength;
                }
            }
        }

        int start = 0;
        int resultIndex = -1;
        String[] result = new String[Math.min(resultLength, columnLimit)];
        for (int i = 0; i < chars.length; ++i) {
            if (7 == chars[i]) {
                continue;
            }

            if (0 == chars[i]) {
                if (resultIndex + 1 == result.length) {
                    break;
                }

                result[++resultIndex] = newString(chars, start, i);

                start = i + 1;
            }
        }

        if (resultIndex + 1 < result.length && 0 < chars.length && start <= chars.length) {
            result[++resultIndex] = newString(chars, start, chars.length);
        }

        return result;
    }
}
