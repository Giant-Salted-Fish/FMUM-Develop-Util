package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import com.util.CoordSystem;
import com.util.Vec3f;

public class HitboxConverter
{
	static final String DEF_SRC = "D:/Work/Java/FMUM-Develop-Util/run/src.java";
	
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
			models = new HashMap<>();
		
		final HashMap<String, float[]>
			// Turbo-array-name + '[' + index : <x, y, z>
			positions = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : rotation
			rots = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <x, y, z>
			offsets = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <, x, y, z>
			lengths = new HashMap<>(),
			
			// Turbo-array-name + '[' + index : <, x0, y0, z0...x7, y7, z7>
			shapes = new HashMap<>();
		
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
				// Offset, length and shape
				else if(s.indexOf("].add") > 0)
				{
					final String key = s.substring(0, s.indexOf(']'));
					
					int i = s.indexOf(',');
					offsets.put(
						key,
						new float[] {
							Float.parseFloat(s.substring(s.indexOf('(') + 1, i)),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1)))
						}
					);
					
					lengths.put(
						key,
						new float[] {
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
						}
					);
					
					i = s.indexOf(',', i + 1);
					
					shapes.put(
						key,
						new float[] {
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),

							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),

							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),

							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),

							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),

							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(')')))
						}
					);
				}
				else if(s.indexOf("].set") > 0)
				{
					int i = s.indexOf('(');
					positions.put(
						s.substring(0, s.indexOf(']')),
						new float[] {
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(',', i + 1))),
							Float.parseFloat(s.substring(i + 1, i = s.indexOf(')')))
						}
					);
				}
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
					float[] buf = rots.get(key);
					if(buf == null)
						buf = new float[] { 0F, 0F, 0F };
					buf[i] = Float.parseFloat(s.substring(s.indexOf("= ") + 2, s.length() - 1));
					rots.put(key, buf);
				}
			}
			
			final float[] DEF_POS_ROT = { 0F, 0F, 0F };
			final CoordSystem sys = CoordSystem.get();
			for(Entry<String, String> e : models.entrySet())
				for(
					int i = 0, size = Integer.parseInt(e.getValue());
					i < size;
					++i
				) {
					String key = e.getKey() + "[" + i;
					float[] pos = positions.get(key);
					float[] offset = offsets.get(key);
					float[] rot = rots.get(key);
					float[] len = lengths.get(key);
					float[] shape = shapes.get(key);
					
					pos = pos == null ? DEF_POS_ROT : pos;
					rot = rot == null ? DEF_POS_ROT : rot;
					
					float lenX = len[0];
					float lenY = len[1];
					float lenZ = len[2];
					
					Vec3f ver0 = Vec3f.get(0F - shape[0 * 3 + 0], 0F + shape[0 * 3 + 1], 0F + shape[0 * 3 + 2]);
					Vec3f ver1 = Vec3f.get(lenX + shape[1 * 3 + 0], 0F + shape[1 * 3 + 1], 0F + shape[1 * 3 + 2]);
					Vec3f ver2 = Vec3f.get(lenX + shape[2 * 3 + 0], 0F + shape[2 * 3 + 1], -lenZ - shape[2 * 3 + 2]);
					Vec3f ver3 = Vec3f.get(0F - shape[3 * 3 + 0], 0F + shape[3 * 3 + 1], -lenZ - shape[3 * 3 + 2]);
					Vec3f ver4 = Vec3f.get(0F - shape[4 * 3 + 0], -lenY - shape[4 * 3 + 1], 0F + shape[4 * 3 + 2]);
					Vec3f ver5 = Vec3f.get(lenX + shape[5 * 3 + 0], -lenY - shape[5 * 3 + 1], 0F + shape[5 * 3 + 2]);
					Vec3f ver6 = Vec3f.get(lenX + shape[6 * 3 + 0], -lenY - shape[6 * 3 + 1], -lenZ - shape[6 * 3 + 2]);
					Vec3f ver7 = Vec3f.get(0F - shape[7 * 3 + 0], -lenY - shape[7 * 3 + 1], -lenZ - shape[7 * 3 + 2]);
					
					final float TO_DEGREES = 180F / (float)Math.PI;
					sys.setDefault();
					sys.globalTrans(pos[0], -pos[1], -pos[2]);
					sys.globalRot(rot[0] * TO_DEGREES, rot[1], rot[2]);
					sys.trans(offset[0], -offset[1], -offset[2]);
					
					sys.apply(ver0, ver0);
					sys.apply(ver1, ver1);
					sys.apply(ver2, ver2);
					sys.apply(ver3, ver3);
					sys.apply(ver4, ver4);
					sys.apply(ver5, ver5);
					sys.apply(ver6, ver6);
					sys.apply(ver7, ver7);
					
					out.write(
						"[ " + vToS(ver0) + " " + vToS(ver1) + " " + vToS(ver2) + " " + vToS(ver3)
						+ " " + vToS(ver4) + " " + vToS(ver5) + " " + vToS(ver6) + " " + vToS(ver7)
						+ " ]"
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
	
	public static String vToS(Vec3f v) { return "(" + v.x + "," + v.y + "," + v.z + ")"; }
	
	public static void tell(String s) { System.out.print(s + "\n"); }
}
