import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DedupeTest {


	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Dedup.logs = new BufferedWriter(new FileWriter("testlogs.log"));
	}

	@Test
	void testParseDate() {
		String observed = Dedup.parseDate("");
		assertTrue(observed.equals(""));
		observed = Dedup.parseDate("2014-05-07T17:30:20+00:00");
		assertTrue(observed.equals("2014-05-07 17:30:20+00:00"));
	}
	
	
	@Test
	void testCompareRecordsJSONObjectJSONObjectJSONObject() throws ParseException, IOException {
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		JSONObject obj3 = new JSONObject();
		JSONParser parser = new JSONParser();
		obj1 = (JSONObject) parser.parse("{\n" + 
				"\"_id\": \"jkj238238jdsnfsj23\",\n" + 
				"\"email\": \"foo@bar.com\",\n" + 
				"\"firstName\":  \"John\",\n" + 
				"\"lastName\": \"Smith\",\n" + 
				"\"address\": \"123 Street St\",\n" + 
				"\"entryDate\": \"2014-05-07T17:30:20+00:00\"\n" + 
				"}");
		obj2 = (JSONObject)parser.parse("{\n" + 
				"\"_id\": \"edu45238jdsnfsj23\",\n" + 
				"\"email\": \"mae@bar.com\",\n" + 
				"\"firstName\":  \"Ted\",\n" + 
				"\"lastName\": \"Masters\",\n" + 
				"\"address\": \"44 North Hampton St\",\n" + 
				"\"entryDate\": \"2014-05-07T17:31:20+00:00\"\n" + 
				"}");
		obj3 = (JSONObject) parser.parse("{\n"+
				 "\"_id\": \"wabaj238238jdsnfsj23\",\n" + 
				"\"email\": \"bog@bar.com\",\n" + 
				"\"firstName\":  \"Fran\",\n" + 
				"\"lastName\": \"Jones\",\n" + 
				"\"address\": \"8803 Dark St\",\n" + 
				"\"entryDate\": \"2014-05-07T17:31:20+00:00\"\n"+
				"}");
		int observedResult = Dedup.compareRecords(obj3, obj2, obj1);
		assertTrue(observedResult == 1);
	}

	@Test
	void testCompareRecordsJSONObjectJSONObject() throws ParseException, IOException {
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		JSONParser parser = new JSONParser();
		obj1 = (JSONObject) parser.parse("{\n" + 
				"\"_id\": \"jkj238238jdsnfsj23\",\n" + 
				"\"email\": \"foo@bar.com\",\n" + 
				"\"firstName\":  \"John\",\n" + 
				"\"lastName\": \"Smith\",\n" + 
				"\"address\": \"123 Street St\",\n" + 
				"\"entryDate\": \"2014-05-07T17:30:20+00:00\"\n" + 
				"}");
		obj2 = (JSONObject)parser.parse("{\n" + 
				"\"_id\": \"edu45238jdsnfsj23\",\n" + 
				"\"email\": \"mae@bar.com\",\n" + 
				"\"firstName\":  \"Ted\",\n" + 
				"\"lastName\": \"Masters\",\n" + 
				"\"address\": \"44 North Hampton St\",\n" + 
				"\"entryDate\": \"2014-05-07T17:31:20+00:00\"\n" + 
				"}");

		int observedResult = Dedup.compareRecords(obj1, obj2);
		assertTrue(observedResult == 0);
		//Check for same time-stamps
		obj1 = (JSONObject)parser.parse("{\n" + 
				"\"_id\": \"edu45238jdsnfsj23\",\n" + 
				"\"email\": \"mae@bar.com\",\n" + 
				"\"firstName\":  \"Ted\",\n" + 
				"\"lastName\": \"Masters\",\n" + 
				"\"address\": \"44 North Hampton St\",\n" + 
				"\"entryDate\": \"2014-05-07T17:31:20+00:00\"\n" + 
				"}");
		obj2 = (JSONObject) parser.parse("{\n"+
				 "\"_id\": \"wabaj238238jdsnfsj23\",\n" + 
				"\"email\": \"bog@bar.com\",\n" + 
				"\"firstName\":  \"Fran\",\n" + 
				"\"lastName\": \"Jones\",\n" + 
				"\"address\": \"8803 Dark St\",\n" + 
				"\"entryDate\": \"2014-05-07T17:31:20+00:00\"\n"+
				"}");
		observedResult = Dedup.compareRecords(obj2, obj1);
		assertTrue(observedResult == 1);
	}
	
	@Test
	void testParseFile() {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		Dedup.logs.close();
	}
}
