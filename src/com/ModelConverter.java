package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public final class ModelConverter
{
	static final String f0 = "0F";
	static final String[] DEF_ROT = { f0, f0, f0 };
	
	static final String DEF_SRC = "F:/works/Java/ModelOptimizer/resources/src.java";
	
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
		
		final HashMap<String, String>
			// Turbo-array-name : array-size
			models = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <x, y, z>
			positions = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <x, y, z>
			offsets = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <, x, y, z>
			lengths = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <, u, v>
			uvOffsets = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <, x0, y0, z0...x7, y7, z7>
			shapes = new HashMap<>();
		
		// Rotations
		final HashMap<String, String[]> rots = new HashMap<>();
		
		try(
			BufferedReader in = new BufferedReader(new FileReader(srcFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(destFile));
		) {
			// Each line in target model file
			for(String s; (s = in.readLine()) != null; )
			{
				if(s.length() == 0) continue;
				
				// Obtain model turbo name and size
				if(s.indexOf("Turbo[") > 0)
					models.put(
						s.substring(0, s.indexOf(" =")),
						s.substring(s.indexOf('[') + 1, s.indexOf(']'))
					);
				// Texture coordinate offset
				else if(s.indexOf("Turbo(") > 0)
					uvOffsets.put(
						s.substring(0, s.indexOf(']')),
						s.substring(s.indexOf(','), s.indexOf(", tex"))
					);
				// Offset, length and shape
				else if(s.indexOf("].add") > 0)
				{
					final String key = s.substring(0, s.indexOf(']'));
					
					int i = s.indexOf(',');
					i = s.indexOf(',', i + 1);
					i = s.indexOf(',', i + 1);
					
					offsets.put(key, s.substring(s.indexOf('(') + 1, i));
					
					int j = s.indexOf(',', i + 1);
					j = s.indexOf(',', j + 1);
					j = s.indexOf(',', j + 1);
					
					lengths.put(key, s.substring(i, j));
					
					i = s.indexOf(',', j + 1);
					
					shapes.put(key, s.substring(i, s.indexOf(')')));
				}
				else if(s.indexOf("].set") > 0)
					positions.put(
						s.substring(0, s.indexOf(']')),
						s.substring(s.indexOf('(') + 1, s.indexOf(')'))
					);
				else if(s.indexOf("].rot") > 0)
				{
					int i = 2;
					switch(s.charAt(s.indexOf(" =") - 1))
					{
					case 'X': --i;
					case 'Y': --i;
					case 'Z': break;
					default: tell("???");
					}
					String key = s.substring(0, s.indexOf(']'));
					String[] buf = rots.get(key);
					if(buf == null)
						buf = new String[] { f0, f0, f0 };
					buf[i] = s.substring(s.indexOf("= ") + 2, s.length() - 1);
					rots.put(key, buf);
				}
			}
			
			for(Entry<String, String> e : models.entrySet())
				for(
					int i = 0, size = Integer.parseInt(e.getValue());
					i < size;
					++i
				) {
					String key = e.getKey() + "[" + i;
					String pos = positions.get(key);
					String offset = offsets.get(key);
					String[] rot = rots.get(key);
					String length = lengths.get(key);
					String uv = uvOffsets.get(key);
					String shape = shapes.get(key);
					
					pos = pos == null ? "0F, 0F, 0F" : pos;
					rot = rot == null ? DEF_ROT : rot;
					
					out.write(
						".addShapeBox(" + pos + ", " + offset + ", " + rot[0] + ", "
						+ rot[1] + ", " + rot[2] + length + uv + shape + ")"
					);
					out.newLine();
				}
		}
		catch(Exception e)
		{
			tell("a error occurred will transfering target file...");
			e.printStackTrace();
		}
		
		tell("complete");
	}
	
	private static void tell(String s) { System.out.print(s + "\n"); }
}
