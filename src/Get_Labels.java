import java.io.*;
import java.util.*;
import java.io.IOException;
import java.net.*;

import org.json.JSONException;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Get_Labels {
	private static final String TARGET_URL =
			"https://vision.googleapis.com/v1/images:annotate?";
	private static final String API_KEY =
			"key=AIzaSyBMnpE7NqfZaV1AIwLQ42BoLTppaw07nEc";
	@SuppressWarnings("unchecked")
	public static void main(String args[]) throws IOException, ParseException, JSONException{
		//Get all photo Id, and store labels inside the j-son file.
		//Write a new file with the new labels, it will have its own tables
		BufferedReader read = new BufferedReader(new FileReader("short_photos.json"));
		BufferedWriter write = new BufferedWriter(new FileWriter("labels.json"));
		while (read.ready()){
			JSONParser parser = new JSONParser();
			JSONObject photo = (JSONObject) parser.parse(read.readLine());
			String photoid = (String) photo.get("photo_id");
			JSONObject labelobj = new JSONObject();
			
			//Now get the labels 
			URL serverUrl = new URL(TARGET_URL + API_KEY);
			URLConnection urlConnection = serverUrl.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection)urlConnection;
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setDoOutput(true);

			//Create a url of the image
			String uri = "http://s3-media2.fl.yelpcdn.com/bphoto/"+photoid+"/o.jpg";

			BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(httpConnection.getOutputStream()));
			httpRequestBodyWriter.write("{\"requests\":  [{ \"features\":  [ {\"type\": \"LABEL_DETECTION\""
					+"}], \"image\": {\"source\": { \"imageUri\":"
					+" \""+uri+"\"}}}]}");
			httpRequestBodyWriter.close();

			String response = httpConnection.getResponseMessage();//Reports OK

			if (httpConnection.getInputStream() == null) {
				System.out.println("No stream");
			}else{//Do nothing if no stream 

				Scanner httpResponseScanner = new Scanner (httpConnection.getInputStream());
				String resp = "";
				while (httpResponseScanner.hasNext()) {
					String line = httpResponseScanner.nextLine();
					resp += line;
					//System.out.println(line);
					//  alternatively, print the line of response
				}

				//Parse the resp string
				Object obj = parser.parse(resp);
				JSONObject wholeresp = (JSONObject) obj;

				JSONArray resparray = (JSONArray) wholeresp.get("responses");
				JSONArray labelarray = null;
				for(Object c: resparray){
					labelarray = (JSONArray) ((JSONObject) c).get("labelAnnotations");
				}
				if(labelarray != null){
					for (Object o: labelarray){
						//Add to JSONObject

						String label = (String) ((JSONObject) o).get("description");
						if(label != null){
							labelobj.put("photo_id", photoid);
							labelobj.put("label", label);
							System.out.println(label);
						}
					}
					write.write(labelobj.toString());
					write.newLine();
				}

				httpResponseScanner.close();

			}}

		read.close();
		write.close();
		System.out.println("Done!");
	}
}
