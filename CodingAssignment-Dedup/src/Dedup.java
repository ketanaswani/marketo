import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;


public class Dedup {
	public static FileWriter log;
	public static BufferedWriter logs;
	
	public static String parseDate(String date) {
		if(date != "" || date != null) {
		date = date.replace('T', ' ');
		return date;
		}
		return "";
	}
	// Comparison of three possible items
	public static int compareRecords(JSONObject record1, JSONObject record2, JSONObject record3)throws IOException
	{
		int winnerOfTwo = compareRecords(record2, record3);
		if(winnerOfTwo == 1){
			int newIsWinner = compareRecords(record1, record2);
			if(newIsWinner == 1)
			{
				return 1;
			}
			else
			{
				return 2;
			}
		}
		else if(winnerOfTwo == 0) {
			int newIsWinner = compareRecords(record1, record3);
			if(newIsWinner == 1)
			{
				return 1;
			}
			else
			{
				return 3;
			}
		}
		else 
			return -1;
	}
	
	public static int compareRecords(JSONObject record1, JSONObject record2) throws IOException
	{
		String date1 = parseDate((String) record1.get("entryDate"));
		String date2 = parseDate((String) record2.get("entryDate"));
		try {
			Date d1 = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").parse(date1);
			Date d2 = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").parse(date2);
			logs.write("Date of first entry: "+ d1+ "...\nDate of Second Entry: "+ d2+ "...\n");
			if(d1.compareTo(d2)>0) {
				return 1;
			}
			else if(d1.compareTo(d2)<0) {
				return 0;
			}
			else {
				logs.write("Dates are the same... Selecting latest entry... "+ "... \n");
				return 1;
			}
		} catch (java.text.ParseException e) {
			
			e.printStackTrace();
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, JSONObject> parseFile(String inpFileName) throws IOException{		
		try {
			if(inpFileName.isEmpty()) {
				logs.write("Invalid file ... \n");
				return null;
			}
			JSONParser parser = new JSONParser();
			FileReader fr = new FileReader(inpFileName);
			Object records = parser.parse(fr);
			JSONObject jsonRecords = (JSONObject) records;
			JSONArray recs = (JSONArray) jsonRecords.get("leads");
			HashMap<String, JSONObject> idToRecordMap = new HashMap<String, JSONObject>();
			HashMap<String, String> emailToIdMap = new HashMap<String, String>();
			logs.write("Two maps \"idToRecord\" and \"emailToId\" have been initialized...\n");
			logs.write("Iterating through the objects...\n");
			Iterator<Object> iterator = recs.iterator();		
			while(iterator.hasNext()) {
				JSONObject record = (JSONObject) iterator.next();
				String recordId = (String) record.get("_id");
				String recordEmail = (String) record.get("email");
				logs.write("New Entry with id = "+recordId+ " email = "+recordEmail+ "... \n");
				
				if (!idToRecordMap.containsKey(recordId) && !emailToIdMap.containsKey(recordEmail)) {
					logs.write("Entry not present in both maps...\n");
					logs.write("Adding"+record+" to both maps...\n");
					idToRecordMap.put(recordId, record);
					emailToIdMap.put(recordEmail, recordId);
				}	
				else {	
					if(idToRecordMap.containsKey(recordId) && emailToIdMap.containsKey(recordEmail)) {
						logs.write("Entry has correspondance in both maps...\n");
						JSONObject rec1 = record;
						JSONObject rec2 = idToRecordMap.get(recordId);
						JSONObject rec3 = idToRecordMap.get(emailToIdMap.get(recordEmail));
						String recordId2 = emailToIdMap.get(recordEmail);
						logs.write("Current record in idToRecord Map: "+ rec2.toString()+ "... \n");
						logs.write("Current record corresponding in emailToId Map: "+rec3.toString()+ "... \n");
						// check if records found in both maps are the same. 
						if(recordId.equals(recordId2))
						{
							logs.write("Entry in idToRecord corresponding to entry in emailToId...\n");
							logs.write("Checking new entry with current for the newest...\n");
							int flag = compareRecords(rec1, rec2);
							// if the new record is the newest replace in both maps
							if(flag == 1) {
								logs.write("New Entry wins... Replacing in both maps"+ "... \n");
								logs.write("Adding entry "+record+" to idToRecord map"+ "... \n");
								logs.write("Adding entry "+recordEmail+" to emailtoId map"+ "... \n");
								idToRecordMap.put(recordId, rec1);
								emailToIdMap.put(recordEmail, recordId);
							}
							else {
								logs.write("Current Entry wins... New Entry ignored... \n");
							}
						}
						// if records don't point to the same id in both maps
						// the worst case scenario
						else{
							logs.write("Entry in idToRecord corresponding to entry in emailToId...\n");
							logs.write("Checking new entry with current for the newest...\n");
							int winner= compareRecords(rec1, rec2, rec3);
							// if the new entry wins
							if(winner == 1)
							{
								logs.write("New Entry wins... Replacing in both maps"+ "... \n");
								// remove email from emailToId Map and the corresponding id from idToRecordMap
								emailToIdMap.remove(recordEmail);
								logs.write("Removing entry "+recordEmail+" from emailToId map"+ "... \n");
								idToRecordMap.remove(recordId2);
								logs.write("Removing entry "+recordId2+" from idToRecord map"+ "... \n");
								// update/replace the entries in both Maps
								logs.write("Adding entry "+record+" to idToRecord map");
								logs.write("Adding entry "+recordEmail+" to emailtoId map"+ "... \n");
								idToRecordMap.put(recordId, record);
								emailToIdMap.put(recordEmail, recordId);
							}
							else if(winner == 2)
							// if record in idToRecord Map is the newest we check who is newer among the new entry and the entry corresponding to the id in the emailToId map
							{
								logs.write("Current Entry in idToRecords wins ... Removing all other entries corresponding to "+record + "... \n");
								logs.write("Removing entry "+recordEmail+" from emailToId map" + "... \n");
								logs.write("Removing entry "+recordId2+" from idToRecord map" + "... \n");
								emailToIdMap.remove(recordEmail);
								idToRecordMap.remove(recordId2);
							}
							else if (winner == 3)
							{
								logs.write("Current Entry in emailToId wins ... Removing all other entries corresponding to "+record + "... \n");
								logs.write("Removing entry "+record+" from idToRecord map" + "... \n");
								logs.write("Removing entry "+rec2.get("email")+" from emailToId map" + "... \n");
								idToRecordMap.remove(recordId);
								emailToIdMap.remove(rec2.get("email"));
							}
						}
					}
					else
					{
						if(idToRecordMap.containsKey(recordId))
						{
							logs.write("id of New Entry is already present in the idToRecord Map... Comparing ...\n");
							JSONObject rec1 = idToRecordMap.get(recordId);
							int flag = compareRecords(record, rec1);
							if (flag == 1)
							{
								logs.write("The New Entry wins ... Updating in idToRecord map and emailToRecord map ...\n");
								logs.write("Adding "+ record + " to idToRecord Map"+ "... \n");
								idToRecordMap.put(recordId, record);
								logs.write("Adding "+ recordEmail + " to emailToId Map"+ "... \n");
								emailToIdMap.put(recordEmail,recordId);
							}
							else {
								logs.write("Current Entry wins... New Entry ignored... \n");
							}
						}
						else if(emailToIdMap.containsKey(recordEmail))
						{
							logs.write("id of New Entry is already present in the emailToId Map... Retrieving and Comparing ...\n");
							JSONObject rec1 = idToRecordMap.get(emailToIdMap.get(recordEmail));
							int flag = compareRecords(record, rec1);
							if(flag == 1)
							{
								logs.write("The New Entry wins ... Updating in idToRecord map and emailToRecord map ...\n");
								idToRecordMap.put(recordId, record);
								logs.write("Adding "+ record + " to idToRecord Map"+ "... \n");
								logs.write("Removing "+ rec1 + " from idToRecord Map"+ "... \n");
								logs.write("Adding "+ recordEmail + " to emailToId Map"+ "... \n");
								idToRecordMap.remove(rec1.get("_id"));
								emailToIdMap.put(recordEmail, recordId);
							}
						}
					}
				}
			}
			logs.write("Result is ready ...\n");
			return idToRecordMap;
		}
		catch(FileNotFoundException e)
		{
			logs.write("The file was not found ... \n");
			
		} catch (IOException e) {
			logs.write("Something went wrong while reading the file...\n");
			e.printStackTrace();
		} catch (ParseException e) {
			logs.write("Something went wrong while parsing JSON objects in the file...\n");
			e.printStackTrace();
		}
		return null;
	
	}
	public static void printResult(HashMap<String, JSONObject> resultMap) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("output.json"));
			for (JSONObject o : resultMap.values()) {
				System.out.println(o);
				writer.write(o.toString() + "\n");
				logs.write(o.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static void main(String[] args) throws IOException {
		String inpFile;
		try {
			log = new FileWriter("log.log");
			logs= new BufferedWriter(log);
			inpFile = args[0];
			logs.write("Parsing the JSON file...\n");
			HashMap<String, JSONObject> resultMap  = parseFile(inpFile);
			if(resultMap != null) {
				logs.write("Final De-Dupe set of records: \n");
				printResult(resultMap);
			}
			else
			{
				logs.write("The result is a null value"+ "... \n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			logs.write("Something went wrong while reading the input file...\n");
		} 
		logs.close();
	}
	

}
