package com.camera.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatetimeUtils {
	
	public static String extractTime(LocalDateTime date){
		 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		 return date.format(formatter);
	}

	public static String extractDate(LocalDateTime date){
		 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		 
		 if(date.toLocalDate().isEqual(LocalDate.now())){
			 return "Today";
		 }else  if(date.toLocalDate().isEqual(LocalDate.now().minusDays(1))){
			 return "Yesterday";
		 }
		 return date.format(formatter);
	}
	
	public static String extractDatetime(LocalDateTime date){
		StringBuilder sb = new StringBuilder();
		sb.append(extractDate(date));
		sb.append(", ");
		sb.append(extractTime(date));
		return sb.toString();
	}
}
