package io.github.valfadeev.rundeck.plugin.nomad.util;

import java.util.Map;
import java.util.HashMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.*;

public class ParseInputTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldSplitOnDelimiter() throws Exception {
        assertThat("failure - string is not split correctly",
                ParseInput.checkedSplit("ls,-al", ","),
                is(new String[]{"ls", "-al"}));
    }

    @Test
    public void throwsIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        ParseInput.checkedSplit("foobae", "=");

    }

    @Test
    public void shouldParseEnvLikeString() throws Exception {

        final Map<String, String> expected = new HashMap<>();
        expected.put("USER", "alice");
        expected.put("PASSWORD", "bob");
        expected.put("DONTDOTHIS","true");

        final Map<String, String> result = ParseInput
                .kvToMap("USER=alice\nPASSWORD=bob\nDONTDOTHIS=true");

        assertThat("failure - string is not parsed into map",
                result, is(expected));

    }

}