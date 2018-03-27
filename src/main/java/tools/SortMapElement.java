package tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SortMapElement {
	

	public static List<Map.Entry<String, BigDecimal>> sortBigDecimal(Map<String, BigDecimal> in) {
		Set<Map.Entry<String, BigDecimal>> elements = in.entrySet();
		List<Map.Entry<String, BigDecimal>> entryList = 
				new ArrayList<Map.Entry<String, BigDecimal>>(elements);
		
		Collections.sort(entryList, new Comparator<Map.Entry<String, BigDecimal>>(){
			@Override
			public int compare(Entry<String, BigDecimal> o1,
					Entry<String, BigDecimal> o2) {
				return -1 * o1.getValue().compareTo(o2.getValue());
			}
		});
		
		return entryList;
	}
	
	public static List<Map.Entry<String, Long>> sortLong(Map<String, Long> in) {
		Set<Map.Entry<String, Long>> elements = in.entrySet();
		
		List<Map.Entry<String, Long>> entryList = 
				new ArrayList<Map.Entry<String, Long>>(elements);
		
		Collections.sort(entryList, new Comparator<Map.Entry<String, Long>>(){
			@Override
			public int compare(Entry<String, Long> o1,
					Entry<String, Long> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return entryList;
	}
	
	public static List<Map.Entry<String, Date>> sortDate(Map<String, Date> in) {
		Set<Map.Entry<String, Date>> elements = in.entrySet();
		
		List<Map.Entry<String, Date>> entryList = 
				new ArrayList<Map.Entry<String, Date>>(elements);
		
		Collections.sort(entryList, new Comparator<Map.Entry<String, Date>>(){
			@Override
			public int compare(Entry<String, Date> o1,
					Entry<String, Date> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return entryList;
	}
	
	

	public static List<Map.Entry<String, Double>> sortDouble(Map<String, Double> in) {
		Set<Map.Entry<String, Double>> elements = in.entrySet();
		List<Map.Entry<String, Double>> entryList = 
				new ArrayList<Map.Entry<String, Double>>(elements);
		
		Collections.sort(entryList, new Comparator<Map.Entry<String, Double>>(){
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return -1*Double.compare(o1.getValue(), o2.getValue());
			}
		});
		
		return entryList;
	}
	
	public static List<Map.Entry<Integer, Double>> sortDoubleNH(Map<Integer, Double> in) {
		Set<Map.Entry<Integer, Double>> elements = in.entrySet();
		List<Map.Entry<Integer, Double>> entryList = 
				new ArrayList<Map.Entry<Integer, Double>>(elements);
		
		Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>(){
			@Override
			public int compare(Entry<Integer, Double> o1,
					Entry<Integer, Double> o2) {
				return -1*Double.compare(o1.getValue(), o2.getValue());
			}
		});
		
		return entryList;
	}
	

	public static List<Map.Entry<String, Double>> sortDoubleFromSmallToLarge(Map<String, Double> in) {
		Set<Map.Entry<String, Double>> elements = in.entrySet();
		List<Map.Entry<String, Double>> entryList = 
				new ArrayList<Map.Entry<String, Double>>(elements);
		
		Collections.sort(entryList, new Comparator<Map.Entry<String, Double>>(){
			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return Double.compare(o1.getValue(), o2.getValue());
			}
		});
		
		return entryList;
	}
	

	public static List<Map.Entry<String, Integer>> sortInteger(Map<String, Integer> in) {
		Set<Map.Entry<String, Integer>> entrys = in.entrySet();
		List<Map.Entry<String, Integer>> entryList =
				new ArrayList<Map.Entry<String, Integer>>(entrys);
		Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>(){
			@Override
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return entryList;
	}
	

	public static List<Map.Entry<String, Integer>> sortIntegerDesc(Map<String, Integer> in) {
		Set<Map.Entry<String, Integer>> entrys = in.entrySet();
		List<Map.Entry<String, Integer>> entryList =
				new ArrayList<Map.Entry<String, Integer>>(entrys);
		Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>(){
			@Override
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		return entryList;
	}


	public static List<Entry<Integer, Double>> sortPearson(
			Map<Integer, Double> in) {
		
		Set<Map.Entry<Integer, Double>> elements = in.entrySet();
		List<Map.Entry<Integer, Double>> entryList = 
				new ArrayList<Map.Entry<Integer, Double>>(elements);
		
		Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>(){
			@Override
			public int compare(Entry<Integer, Double> o1,
					Entry<Integer, Double> o2) {
				return -1*Double.compare(Math.abs(o1.getValue()), Math.abs(o2.getValue()));
			}
		});
		
		return entryList;
	}
}
