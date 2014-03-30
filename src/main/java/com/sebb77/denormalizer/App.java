package com.sebb77.denormalizer;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;



/**
 * Hello world!
 *
 */
public class App 
{
	public enum FieldType {
		STRING,
		DATE,
		INTEGER,
		DECIMAL,
		BOOLEAN,
		ENUM,
		CLASS,
		LIST,
		MAP,
		GUID,
		AUTOMAPPING,
		UNKNOWN
	}
	private static final String PREFIX_SEPARATOR = "."; 


	public static void main( String[] args ) throws Exception {
		//Logger l = LoggerFactory.getLogger(App.class);

		// Create class structure to convert into table.
		// There is no need that the class is a serializable class. Any class will work, however only lists are being checked for the moment.
		// If other types of lists needs to be checked, fix the getType function and the relevant loops for the lists.
		Test1 c1 = new Test1();
		c1.setA(11);
		c1.setB("12");
		c1.setC(new Test2(21, "22", new ArrayList<String>(Arrays.asList(new String[] {"231", "232"})) ));
		c1.setD(new ArrayList<String>(Arrays.asList(new String[] {"131", "132"})));
		c1.setE(new ArrayList<Test2>(Arrays.asList(new Test2[] 
			{
				new Test2(31, "32", new ArrayList<String>(Arrays.asList(new String[] {"331", "332"})) ),
				new Test2(41, "42")
			})));

		
		
		long t = System.currentTimeMillis();
		// convert class into a guava table
		Table<Integer, String, Object> m = denormalize(c1);
		System.out.println("Total Time: " + (System.currentTimeMillis() - t) + "ms");
		
		// print result of conversion
		System.out.print("\t");
		for (String c : m.columnKeySet())
			System.out.print(c + "\t");
		System.out.println();
		
		for (Integer r : m.rowKeySet()) {
			System.out.print(r+1 + ":\t");
			for (String c : m.columnKeySet()) {
				Object x = m.get(r, c);
				x = x == null ? "~" : x;
				System.out.print(x + "\t");
			}
			System.out.println();
		}
	}

	// Public entry point for recursive function
	public static Table<Integer, String, Object> denormalize(Object obj) throws Exception {
		Table<Integer, String, Object> data = TreeBasedTable.create();
		return denormalize(data, 0, "", obj);
	}
	
	// Recursive function that extracts all values of a class and puts them inside a table.
	//  - By using reflection the function extracts all values from the passed class and generates a new column in the table.
	//  - According to the field of the class being processed, checks if the function has to be called recursively or the 
	//  - list extraction function has to be called.
	@SuppressWarnings("unchecked")
	private static Table<Integer, String, Object> denormalize(Table<Integer, String, Object> data, Integer row, String prefix, Object obj) throws Exception {
		if (obj == null)
			return null;

		if (prefix != "")
			prefix += PREFIX_SEPARATOR;

			
		// get all fields of the class
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field f : fields) {
			// ignore serializable class variable.
			if (f.getName().equals("serialVersionUID"))
				continue;
			
			// enable reading of the value for this field being processed and skip processing if variable is null
			f.setAccessible(true);
			Object val = obj == null ? null : f.get(obj);
			if (val == null)
				continue;

			String fName = prefix + f.getName();
			FieldType type = getType(f.getType());
			if (type == FieldType.CLASS) {
				// Since this field is another class, recursively call this function
				data.putAll(denormalize(data, row, fName, val));
			} else if (type == FieldType.LIST) {
				// call function used to extract values from lists and create a cartesian product of the data
				data.putAll(generateListValues(data, row, fName, (List<Object>)val));
			} else {
				// the field is a normal field, thus just put the value in the table
				data.put(row, fName, val);
			}
		}
		
		return data;
	}
	
	// Extracts all values from a list object and generates a cartesian product of the values.
	private static Table<Integer, String, Object> generateListValues(Table<Integer, String, Object> data, Integer row, String fName, List<Object> vals) throws Exception {
		if (vals == null || vals.size() < 1)
			return TreeBasedTable.create();
		
		Table<Integer, String, Object> newRows = TreeBasedTable.create();
		FieldType type = getType(vals.get(0).getClass());
		Integer rr = row;
		for (Object val : vals) {
			for (Integer r = row ; r < data.rowKeySet().size() ; r++) {
				// copy all previous rows in the new table
				for (String cc : data.columnKeySet()) {
					if (data.get(r, cc) != null)
						newRows.put(rr, cc, data.get(r, cc));
				}
				// add the new value for each row in the new table
				if (type == FieldType.CLASS) {
					// recursively call the denormalize function for this class
					newRows.putAll(denormalize(newRows, rr++, fName, val));
					rr = newRows.rowKeySet().size();
				} else
					newRows.put(rr++, fName, val);
			}
		}
		
		return newRows;
	}
	
	// Function used to understand what type of class is being passed.
	private static FieldType getType(Class<?> klass) {
		if (klass.isAssignableFrom(String.class) ||
			klass.isAssignableFrom(char.class))
			return FieldType.STRING;
		else if (klass.isAssignableFrom(Date.class))
			return FieldType.DATE;
		else if (klass.isAssignableFrom(Boolean.class) ||
				klass.isAssignableFrom(boolean.class))
			return FieldType.BOOLEAN;
		else if (klass.isAssignableFrom(BigDecimal.class) ||
				 klass.isAssignableFrom(Float.class) || 
				 klass.isAssignableFrom(float.class) ||
				 klass.isAssignableFrom(double.class))
			return FieldType.DECIMAL;
		else if (klass.isAssignableFrom(BigInteger.class) ||
				 klass.isAssignableFrom(Long.class) ||
				 klass.isAssignableFrom(Integer.class) || 
				 klass.isAssignableFrom(byte.class) || 
				 klass.isAssignableFrom(short.class) || 
				 klass.isAssignableFrom(int.class) || 
				 klass.isAssignableFrom(long.class))
			return FieldType.INTEGER;
		else if (klass.isAssignableFrom(Map.class))
			return FieldType.MAP;
		else if (klass.isAssignableFrom(List.class))
			return FieldType.LIST;
		else if (klass.isEnum())
			return FieldType.ENUM;
		
		return FieldType.CLASS;
	}		
}
