import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class Get_Short_Data {
	public static ArrayList<String> businesses= new ArrayList<String>();
	public static ArrayList<String> positivewords= new ArrayList<String>();
	public static ArrayList<String> negativewords= new ArrayList<String>();
	public static void main (String args[]) throws IOException, ParseException{
		storeinwordlist();
		writeshortphoto();
		writeshortbusiness();
		writeshortreview();

		System.out.println("Done!");
	}
	@SuppressWarnings("resource")
	public static void storeinwordlist() throws IOException{
		BufferedReader read = new BufferedReader(new FileReader("NegativeWords.txt"));
		while(read.ready()){
			negativewords.add(read.readLine());
		}
		BufferedReader read2 = new BufferedReader(new FileReader("PositiveWords.txt"));
		while(read2.ready()){
			positivewords.add(read2.readLine());
		}
		read.close();
		read2.close();
	}
	@SuppressWarnings("unchecked")
	public static void writeshortbusiness() throws IOException, ParseException{
		BufferedReader read = new BufferedReader(new FileReader("business.json"));
		BufferedWriter write = new BufferedWriter(new FileWriter("short_business.json"));
		JSONParser parser = new JSONParser();
		while(read.ready()){
			Object object = parser.parse(read.readLine());
			JSONObject bus = (JSONObject) object;
			if(businesses.contains(bus.get("business_id"))){
				bus.remove("neighborhood");
				bus.remove("postal_code");
				bus.remove("latitude");
				bus.remove("longitude");
				bus.remove("is_open");
				bus.remove("attributes");
				bus.remove("categories");
				bus.remove("hours");
				
				String name = (String)bus.get("name");
				String newname = name;
				String special = "\\\"'";
				//Parse Name
				if(name !=null){
					//Get new caption
					//Add backslashes
					CharacterIterator charIt = new StringCharacterIterator(name);
					char c = 0; 
					List<Integer> indexes = new ArrayList<Integer>();
					while(c != charIt.DONE){
						c = charIt.next();
						
						if(special.contains(String.valueOf(c))){
//							System.out.println(c);
//							System.out.println(charIt.getIndex());
							indexes.add(charIt.getIndex());	
						}
						
					}
					Collections.sort(indexes);
					for(int x=indexes.size()-1;x>=0;x--){
						int index = indexes.get(x);
						if(newname.charAt(index) == '\'' ){
							newname = newname.substring(0,index) +"'"+newname.substring(index,newname.length());
						}else{
						newname = newname.substring(0,index) +"\\"+newname.substring(index,newname.length());
						}
					}
					System.out.println(newname);
				}
				
				bus.remove("name");
				bus.put("name", newname);
				write.write(bus.toString());
				write.newLine();
			}
		}
		read.close();
		write.close();
		System.out.println("dumped in business");

	}
	@SuppressWarnings("unchecked")
	public static void writeshortreview() throws IOException, ParseException{
		BufferedReader read = new BufferedReader(new FileReader("review.json"));
		BufferedWriter write = new BufferedWriter(new FileWriter("short_review.json"));
		JSONParser parser = new JSONParser();
		while(read.ready()){
			String input = read.readLine();
			Object object = parser.parse(input);
			JSONObject review = (JSONObject) object;
			String check = (String)review.get("business_id");
			if(businesses.contains(check)){
				//Count positive and negative words
				String text = (String)review.get("text");
				String[] words = text.split("[ ,\\.!\\?\"]+");
				int countNeg=0;
				int countPos=0;
				for(String w:words){
					if(w.length() > 1 && w.length() < 20 && Pattern.matches("[-\\w']+", w) && !w.matches(".*\\d.*")){
						if(negativewords.contains(w)){
							countNeg ++;
						}
						if(positivewords.contains(w)){
							countPos++;
						}

					}
				}

				//Delete Text, Add Num
				review.remove("text");
				review.put("NumPositiveWords", countPos);
				review.put("NumNegativeWords", countNeg);
				write.write(review.toString());
				write.newLine();

			}

		}
		read.close();
		write.close();
		System.out.println("dumped in reviews");

	}

	@SuppressWarnings("unchecked")
	public static void writeshortphoto()throws IOException, ParseException{
		BufferedReader read = new BufferedReader(new FileReader("photos.json"));
		BufferedWriter write = new BufferedWriter(new FileWriter("short_photos.json"));
		JSONParser parser = new JSONParser();

		//Get 200 businesses
		HashSet<String> busi = new HashSet<String>();
		while(busi.size()<200){
			String input = read.readLine();
			Object object = parser.parse(input);
			JSONObject photo = (JSONObject) object;
			String id = (String)photo.get("business_id");
			busi.add(id);
		}
		for(String x: busi){
			businesses.add(x);
			System.out.println("Here is " +x);
		}
		read.close();
		read = new BufferedReader(new FileReader("photos.json")); 
		//Escape the Caption
		while(read.ready()){
			String input = read.readLine();
			Object object = parser.parse(input);
			JSONObject photo = (JSONObject) object;
			String check = (String)photo.get("business_id");
			String special = "\\\"'";
			
			if(businesses.contains(check)){
				photo.remove("label");
				String caption = (String)photo.get("caption");
				String newcaption = caption;
				if(caption !=null){
					//Get new caption
					//Add backslashes
					CharacterIterator charIt = new StringCharacterIterator(caption);
					char c = 0; 
					List<Integer> indexes = new ArrayList<Integer>();
					while(c != charIt.DONE){
						c = charIt.next();
						
						if(special.contains(String.valueOf(c))){
//							System.out.println(c);
//							System.out.println(charIt.getIndex());
							indexes.add(charIt.getIndex());	
						}
						
					}
					Collections.sort(indexes);
					for(int x=indexes.size()-1;x>=0;x--){
						int index = indexes.get(x);
						if(newcaption.charAt(index) == '\'' ){
							newcaption = newcaption.substring(0,index) +"'"+newcaption.substring(index,newcaption.length());
						}else{
						newcaption = newcaption.substring(0,index) +"\\"+newcaption.substring(index,newcaption.length());
						}
					}
					System.out.println(newcaption);
					
					
					photo.remove("caption");
					photo.put("caption", newcaption);
					write.write(photo.toString());
					write.newLine();
				
				}
			}
		}
		read.close();
		write.close();
		System.out.println("dumped in photos");
	}

}
