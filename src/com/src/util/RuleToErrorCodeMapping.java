package com.src.util;

public class RuleToErrorCodeMapping {
	
	
	public enum ENUMVALUES {
	
		RULE_50("BRE005000"),
		RULE_51("BRE005001");
		
		String value;
		private ENUMVALUES(String value){
			this.value=value;
		}
		
		public String getValue() {
			return value;
		}
	}

}
