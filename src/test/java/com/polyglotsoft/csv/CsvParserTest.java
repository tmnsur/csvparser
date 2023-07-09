package com.polyglotsoft.csv;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvParserTest {
    private CsvParser csvParser = new CsvParser(CsvParser.COMMA, CsvParser.DOUBLE_QUOTES, 100, 10);

    @Test
    public void shouldParseNonSense() {
        assertEquals(0, csvParser.parseLine(null).length);
        assertEquals(0, csvParser.parseLine("").length);
    }

    @Test
    public void shouldParseSingleEmptyField() {
        String[] result = csvParser.parseLine("\"\"");

        assertEquals(1, result.length);
        assertTrue(result[0], result[0].isEmpty());
    }

    @Test
    public void shouldParseSingleField() {
        String[] result = csvParser.parseLine(" ");

        assertEquals(1, result.length);
        assertEquals(" ", result[0]);

        result = csvParser.parseLine("\" \"");

        assertEquals(1, result.length);
        assertEquals(" ", result[0]);

        result = csvParser.parseLine("abc");
        assertEquals(1, result.length);
        assertEquals("abc", result[0]);

        result = csvParser.parseLine(" abc ");
        assertEquals(1, result.length);
        assertEquals(" abc ", result[0]);

        result = csvParser.parseLine("\"abc\"");
        assertEquals(1, result.length);
        assertEquals("abc", result[0]);

        result = csvParser.parseLine(" \" abc\"  ");
        assertEquals(1, result.length);
        assertEquals(" abc", result[0]);

        result = csvParser.parseLine("\"");
        assertEquals(1, result.length);
        assertEquals("\"", result[0]);

        result = csvParser.parseLine("   \" b\"\"bb\"   ");
        assertEquals(1, result.length);
        assertEquals(" b\"bb", result[0]);
    }

    @Test
    public void shouldParseSingleEscapedField() {
        String[] result = csvParser.parseLine("\"aaa\"");
        assertEquals(1, result.length);
        assertEquals("aaa", result[0]);

        result = csvParser.parseLine("\"a,aa\"");
        assertEquals(1, result.length);
        assertEquals("a,aa", result[0]);
    }

    @Test
    public void shouldParseMultipleFields() {
        String[] result = csvParser.parseLine(",");

        assertEquals(2, result.length);
        assertTrue(result[0].isEmpty());
        assertTrue(result[1].isEmpty());

        result = csvParser.parseLine("aaa,bbb");

        assertEquals(2, result.length);
        assertEquals("aaa", result[0]);
        assertEquals("bbb", result[1]);

        result = csvParser.parseLine("aaa , \" bbb\" , ccc ");

        assertEquals(3, result.length);
        assertEquals("aaa ", result[0]);
        assertEquals(" bbb", result[1]);
        assertEquals(" ccc ", result[2]);

        result = csvParser.parseLine("  aaa   ,   \" b\"\"bb\"   ");

        assertEquals(2, result.length);
        assertEquals("  aaa   ", result[0]);
        assertEquals(" b\"bb", result[1]);
    }

    @Test
    public void shouldParseMultipleEscapedFields() {
        String[] result = csvParser.parseLine("\"aaa\",\"bbb\"");
        assertEquals(2, result.length);
        assertEquals("aaa", result[0]);
        assertEquals("bbb", result[1]);

        result = csvParser.parseLine("\"aaa\", \"bbb\"");
        assertEquals(2, result.length);
        assertEquals("aaa", result[0]);
        assertEquals("bbb", result[1]);

        result = csvParser.parseLine(" \"aaa\"  ,   \"bbb\"   ");
        assertEquals(2, result.length);
        assertEquals("aaa", result[0]);
        assertEquals("bbb", result[1]);
    }

    @Test
    public void shouldParseEscapedDoubleQuotes() {
        String[] result = csvParser.parseLine("\"4\"\"four\"");
        assertEquals(1, result.length);
        assertEquals("4\"four", result[0]);
    }

    @Test
    public void shouldParseEscapedQuotation() {
        String[] result = csvParser.parseLine("\"\"a\"\"");
        assertEquals(1, result.length);
        assertEquals("\"a\"", result[0]);

        result = csvParser.parseLine("addr\"\"ess\"\"8@domain.com");
        assertEquals(1, result.length);
        assertEquals("addr\"ess\"8@domain.com", result[0]);
    }

    @Test
    public void shouldParseEscapedDelimiter() {
        String[] result = csvParser.parseLine("\"hello,\"\"\"\",world\"");
        assertEquals(1, result.length);
        assertEquals("hello,\"\",world", result[0]);
    }

    @Test
    public void shouldParseInnerQuotes() {
        String[] result = csvParser.parseLine("\"Quoted test with \"\"inner\"\" quotes\"");
        assertEquals(1, result.length);
        assertEquals("Quoted test with \"inner\" quotes", result[0]);
    }

    @Test
    public void shouldParseScenario01() {
        String[] result = csvParser.parseLine("Unquoted test, \"Quoted test\", 23234, One Two Three, \"343456.45\"");
        assertEquals(5, result.length);
        assertEquals("Unquoted test", result[0]);
        assertEquals("Quoted test", result[1]);
        assertEquals(" 23234", result[2]);
        assertEquals(" One Two Three", result[3]);
        assertEquals("343456.45", result[4]);
    }

    @Test
    public void shouldParseScenario02() {
        String[] result = csvParser.parseLine("Unquoted test 2, \"Quoted test with \"\"inner\"\" quotes\", 23234, One Two Three, \"34312.7\"");
        assertEquals(5, result.length);
        assertEquals("Unquoted test 2", result[0]);
        assertEquals("Quoted test with \"inner\" quotes", result[1]);
        assertEquals(" 23234", result[2]);
        assertEquals(" One Two Three", result[3]);
        assertEquals("34312.7", result[4]);
    }

    @Test
    public void shouldParseScenario03() {
        String[] result = csvParser.parseLine("Unquoted test 3, \"Quoted test 3\", 23234, One Two Three, \"343486.12\"");
        assertEquals(5, result.length);
        assertEquals("Unquoted test 3", result[0]);
        assertEquals("Quoted test 3", result[1]);
        assertEquals(" 23234", result[2]);
        assertEquals(" One Two Three", result[3]);
        assertEquals("343486.12", result[4]);
    }

    @Test
    public void shouldParseScenario04() {
        String[] result = csvParser.parseLine("one  , 2 , three, 4");
        assertEquals(4, result.length);
        assertEquals("one  ", result[0]);
        assertEquals(" 2 ", result[1]);
        assertEquals(" three", result[2]);
        assertEquals(" 4", result[3]);
    }

    @Test
    public void shouldParseScenario05() {
        String[] result = csvParser.parseLine("1,\"2,two\",\"3\nthree\",\"4\"\"four\"");
        assertEquals(4, result.length);
        assertEquals("1", result[0]);
        assertEquals("2,two", result[1]);
        assertEquals("3\nthree", result[2]);
        assertEquals("4\"four", result[3]);
    }

    @Test
    public void shouldParseScenario06() {
        String[] result = csvParser.parseLine("1,\"2,two\",\"3\nthree\",\"4\"\"four\"");
        assertEquals(4, result.length);
        assertEquals("1", result[0]);
        assertEquals("2,two", result[1]);
        assertEquals("3\nthree", result[2]);
        assertEquals("4\"four", result[3]);
    }

    @Test
    public void shouldParseScenario07() {
        String[] result = csvParser.parseLine("1\",2\"");
        assertEquals(2, result.length);
        assertEquals("1\"", result[0]);
        assertEquals("2\"", result[1]);
    }

    @Test
    public void shouldParseScenario08() {
        String[] result = csvParser.parseLine("Year,Make,Model,Description,Price");
        assertEquals(5, result.length);
        assertEquals("Year", result[0]);
        assertEquals("Make", result[1]);
        assertEquals("Model", result[2]);
        assertEquals("Description", result[3]);
        assertEquals("Price", result[4]);
    }

    @Test
    public void shouldParseScenario09() {
        String[] result = csvParser.parseLine("1997,Ford,E350,\"ac, abs, moon\",3000.00");
        assertEquals(5, result.length);
        assertEquals("1997", result[0]);
        assertEquals("Ford", result[1]);
        assertEquals("E350", result[2]);
        assertEquals("ac, abs, moon", result[3]);
        assertEquals("3000.00", result[4]);
    }

    @Test
    public void shouldParseScenario10() {
        String[] result = csvParser.parseLine("1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00");
        assertEquals(5, result.length);
        assertEquals("1999", result[0]);
        assertEquals("Chevy", result[1]);
        assertEquals("Venture \"Extended Edition\"", result[2]);
        assertEquals("", result[3]);
        assertEquals("4900.00", result[4]);
    }

    @Test
    public void shouldParseScenario11() {
        String[] result = csvParser.parseLine("1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00");
        assertEquals(5, result.length);
        assertEquals("1999", result[0]);
        assertEquals("Chevy", result[1]);
        assertEquals("Venture \"Extended Edition, Very Large\"", result[2]);
        assertEquals("", result[3]);
        assertEquals("5000.00", result[4]);
    }

    @Test
    public void shouldParseScenario12() {
        String[] result = csvParser.parseLine("1996,Jeep,Grand Cherokee,\"MUST SELL!\r\nair, moon roof, loaded\",4799.00");
        assertEquals(5, result.length);
        assertEquals("1996", result[0]);
        assertEquals("Jeep", result[1]);
        assertEquals("Grand Cherokee", result[2]);
        assertEquals("MUST SELL!\r\nair, moon roof, loaded", result[3]);
        assertEquals("4799.00", result[4]);
    }

    @Test
    public void shouldParseScenario13() {
        String[] result = csvParser.parseLine("\"weird\"\"\"\"quotes \",true,false,123,45.6");
        assertEquals(5, result.length);
        assertEquals("weird\"\"quotes ", result[0]);
        assertEquals("true", result[1]);
        assertEquals("false", result[2]);
        assertEquals("123", result[3]);
        assertEquals("45.6", result[4]);
    }

    @Test
    public void shouldParseScenario14() {
        String[] result = csvParser.parseLine(".7,8.,9.1.2,null,undefined");
        assertEquals(5, result.length);
        assertEquals(".7", result[0]);
        assertEquals("8.", result[1]);
        assertEquals("9.1.2", result[2]);
        assertEquals("null", result[3]);
        assertEquals("undefined", result[4]);
    }

    @Test
    public void shouldParseScenario15() {
        String[] result = csvParser.parseLine("Null, \"ok whitespace outside quotes\" ,trailing unquoted  ,   both   ,   leading");
        assertEquals(5, result.length);
        assertEquals("Null", result[0]);
        assertEquals("ok whitespace outside quotes", result[1]);
        assertEquals("trailing unquoted  ", result[2]);
        assertEquals("   both   ", result[3]);
        assertEquals("   leading", result[4]);
    }

    @Test
    public void shouldNotParseLongLines() {
        CsvParser newParser = new CsvParser(CsvParser.COMMA, CsvParser.DOUBLE_QUOTES, 9, 1);

        String[] result = newParser.parseLine("123456789");

        assertEquals(1, result.length);
        assertEquals("123456789", result[0]);

        result = newParser.parseLine("1234567890");

        assertEquals(1, result.length);
        assertEquals("123456789", result[0]);

        result = newParser.parseLine("123, 456789");

        assertEquals(1, result.length);
        assertEquals("123", result[0]);

        result = newParser.parseLine("          123456789");

        assertEquals(1, result.length);
        assertEquals("         ", result[0]);

        result = newParser.parseLine(",,");

        assertEquals(1, result.length);
        assertEquals("", result[0]);

        result = newParser.parseLine("\"          \"");

        assertEquals(1, result.length);
        assertEquals("\"        ", result[0]);

        result = newParser.parseLine("        \"  \"");

        assertEquals(1, result.length);
        assertEquals("        \"", result[0]);

        result = newParser.parseLine("       \",");

        assertEquals(1, result.length);
        assertEquals("       \"", result[0]);
    }

    @Test
    public void shouldParseUnclosedQuotation() {
        String[] result = csvParser.parseLine("\",");

        assertEquals(2, result.length);
        assertEquals("\"", result[0]);
        assertEquals("", result[1]);
    }

    @Test
    public void shouldParseDoubleEscapedContent() {
        String[] result = csvParser.parseLine("\"\"\"a\"\"\"");

        assertEquals(1, result.length);
        assertEquals("\"a\"", result[0]);
    }

    @Test
    public void shouldParseDoubleDoubleEscapedContent() {
        String[] result = csvParser.parseLine("\"\"\"\"\"a\"\"\"\"\"");

        assertEquals(1, result.length);
        assertEquals("\"\"a\"\"", result[0]);
    }
}
