package com;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KeyPointsParser
{
	static final String DEF_SRC = "D:/Work/Java/FMUM-Develop-Util/run/src.java";
	
	static Pattern KEY_PATTERN = Pattern.compile("\".*\"");
	
	public static void main(String[] args)
	{
		String srcFile = args.length < 1 ? DEF_SRC : args[0];
		
		String destFile;
		if(args.length < 2)
		{
			int i = srcFile.lastIndexOf('/');
			if(i < 0) i = srcFile.lastIndexOf('\\');
			destFile = srcFile.substring(0, i + 1) + "after.java";
		}
		else destFile = args[1];
		
		tell("target file is <" + srcFile + ">, dest file is <" + destFile + ">");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		JsonNode base;
		try { base = mapper.readValue(new File(srcFile), JsonNode.class); }
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}
		
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s))) { out.newLine(); }
		catch(IOException e) { e.printStackTrace(); }
		String separator = new String(s.toByteArray());
		
		StringBuilder builder = new StringBuilder(1024);
		for(Iterator<Entry<String, JsonNode>> i = base.get("animations").fields(); i.hasNext(); )
		{
			Entry<String, JsonNode> entry = i.next();
			JsonNode ani = entry.getValue();
			double length = ani.get("animation_length").asDouble();
			builder.append("/// ").append(entry.getKey()).append(": ")
				.append(length).append("s ///").append(separator);
			
			for(Iterator<Entry<String, JsonNode>> j = ani.get("bones").fields(); j.hasNext(); )
			{
				entry = j.next();
				builder.append("// ").append(entry.getKey()).append(separator);
				
				// Position
				ArrayList<Entry<String, JsonNode>> nodes = new ArrayList<>();
				readAllPointsFrom(entry.getValue().get("position"), nodes);
				
				if(nodes.size() == 0)
					builder.append(".appendPos(0D, 0D, 0D, 0D)").append(separator);
				else for(Entry<String, JsonNode> n : nodes)
					builder.append(".appendPos(").append(toVecStr(n))
						.append(n.getKey()).append("D)").append(separator);
				
				// Rotation
				nodes.clear();
				readAllPointsFrom(entry.getValue().get("rotation"), nodes);
				
				if(nodes.size() == 0)
					builder.append(".appendRot(0D, 0D, 0D, 0D)").append(separator);
				else for(Entry<String, JsonNode> n : nodes)
					builder.append(".appendRot(").append(toVecStr(n))
						.append(n.getKey()).append("D)").append(separator);
				
				builder.append(separator);
			}
		}
		
		try(BufferedWriter out = new BufferedWriter(new FileWriter(new File(destFile)))) {
			out.write(builder.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		tell("complete");
	}
	
	private static void readAllPointsFrom(JsonNode node, ArrayList<Entry<String, JsonNode>> dest)
	{
		if(node == null) return;
		
		for(Iterator<Entry<String, JsonNode>> itr = node.fields(); itr.hasNext(); )
			dest.add(itr.next());
		
		dest.sort((a, b) -> a.getKey().compareTo(b.getKey()));
	}
	
	private static String toVecStr(Entry<String, JsonNode> e)
	{
		Iterator<JsonNode> itr = e.getValue().get("post").elements();
		return itr.next().toString() + "D, " + itr.next().toString()
			+ "D, " + itr.next().toString() + "D, ";
	}
	
	private static void tell(String s) { System.out.print(s + "\n"); }
}
