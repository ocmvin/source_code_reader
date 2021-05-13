package com.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ErrorCodeMainBak {

	private static String sourceFolder = ".\\src\\com\\src\\bl";
	private static String sourceFolderUtil = ".\\src\\com\\src\\util\\";
	private static Map<String, String> rule_ErrorMessage_Map = new HashMap();
	private static Map<String, String> rule_ErrorCode_Map = new HashMap();
	private static String error_Message_Declaration = "(.*?)public static final (.*?);";
	private static String error_Code_Declaration = "(.*?)RULE_(.*?)\"";
	private static String RuleToErrorCodeMapping_CONST= "RuleToErrorCodeMapping.";
	private static String ErrorCodeConstants_CONST= "ErrorCodeConstants.";
	private static String ErrorLevel="ErrorLevel.";
	
	private static Map<String, String> keyValMap = new ConcurrentHashMap<String, String>();

	public static void main(String[] args) throws IOException {

		ErrorCodeMainBak errorCodeMain = new ErrorCodeMainBak();
		errorCodeMain.startupTask();
		
	}

	private void startupTask() {
		
		readAllFilesAndCreateMap(); 
		System.out.println("COLLECTIVE MAP====>");
		keyValMap.entrySet().forEach(entry ->
		{
		System.out.println("Key: " + entry.getKey() + " Value : " + entry.getValue());
		if(entry.getKey().strip().equalsIgnoreCase("RULE_50_ERROR_MESSAGE")) {
			System.out.println("@@@@@@@@@@@@@@@@@@Matching "+entry.getKey());
		}
		});
		System.out.println("===================Read File & =====================");
	
		
		assessFilesForErrorCodeNErrorMsg();
		/*
		loadruleErrorCodeMap("ErrorCodeConstants.java");
		rule_ErrorMessage_Map.entrySet().forEach(entry -> System.out.println("Key: " + entry.getKey() + " Value : " + entry.getValue()));
		ruleToErrorCodeMap("RuleToErrorCodeMapping.java");
		System.out.println("========================================");
		rule_ErrorCode_Map.entrySet().forEach(entry -> System.out.println("Key: " + entry.getKey() + " Value : " + entry.getValue()));
		*/
	}

	

	private void ruleToErrorCodeMap(String file) {

		//RULE_50("BRE005000")
		
		Path path =Path.of(sourceFolderUtil+file);
		Map<String, String> rule_code = null;
		try {
			rule_code = Files.lines(path).filter(line -> isRule_ErrorCode_PatternMatching(line))
					//.peek(line->System.out.println("DEBUG "+line))
					.map(line ->
					{
						
						return line.strip().split("\\(\"");
					})
					.collect(Collectors.toMap(keyVal -> keyVal[0].trim(), keyVal -> getErrorCodeValue(keyVal[1])));

			rule_ErrorCode_Map=rule_code;
		} catch (IOException e) {
			System.out.println("Excetion in load RuleError Code map");
			e.printStackTrace();
		}

		
	}

	private void loadruleErrorCodeMap(String file) {

		
		Path path =Path.of(sourceFolderUtil+file);
		
		Map<String, String> code_msg = null;
		try {
			code_msg = Files.lines(path).filter(line -> isError_Message_PatternMatching(line))
					.map(line -> line.split("="))
					.collect(Collectors.toMap(keyVal -> getKey(keyVal[0].trim()), keyVal -> keyVal[1]));

			
		} catch (IOException e) {

			e.printStackTrace();
		}

		rule_ErrorMessage_Map=code_msg;

	}

//public static final String RULE_50_ERROR_MESSAGE	
	private String getKey(String key) {
		if(key.strip().contains(" ")) {
			return key.trim().substring(key.lastIndexOf(" "), key.length());
		}
		return key;
	}

	private String getErrorCodeValue(String keyVal) {
		return keyVal.trim().substring(0, keyVal.lastIndexOf("\""));
	}
	private boolean isError_Message_PatternMatching(String line) {

		Pattern pattern = Pattern.compile(error_Message_Declaration);
		return pattern.matcher(line).find();

	}
	private boolean isRule_ErrorCode_PatternMatching(String line) {

		Pattern pattern = Pattern.compile(error_Code_Declaration);
		return pattern.matcher(line).find();

	}
	
	
	private void readAllFilesAndCreateMap() {

		try {
			Files.walk(Paths.get(sourceFolderUtil)).filter(path -> !path.toFile().isDirectory()).parallel()
					.forEach(filePath -> readFile(filePath));

		} catch (IOException e) {
			System.out.println("Excetion in load RuleError Code map");
			e.printStackTrace();
		}

	}
	
	
	private boolean isConstantDeclarationPattern(String line) {
	
		String error_Message_Declaration = "(.*?)public static final (.*?);";
		Pattern pattern = Pattern.compile(error_Message_Declaration);
		return pattern.matcher(line).find();

	}
	
	private boolean isErrorCode_EnumPattern(String line) {

		Pattern pattern = Pattern.compile(error_Code_Declaration);
		return pattern.matcher(line).find();

	}
	

	
	private void readFile(Path path) {
		
		Map<String, String> code_msg = null;
		try {
			code_msg = Files.lines(path).filter(line ->
						{
							if(isConstantDeclarationPattern(line)||isErrorCode_EnumPattern(line)) {
								return true;
							} else
								return false;
						}
					).peek(line->System.out.println("DEBUG "+line))
					.map(line -> normalize(line)
						)
					.collect(Collectors.toMap(keyVal -> getKey(keyVal[0].strip()), keyVal -> keyVal[1]));

			
		} catch (IOException e) {

			e.printStackTrace();
		}

		keyValMap.putAll(code_msg);

	}
	
	/**
	 * Read file line and convert it in to key value format and store in global map
	 * e.g. I/P 	public static final String DATA_VALIDATION_CODE = "8000";
	 * 		O/P 	Key:  DATA_VALIDATION_CODE Value :  "8000
	 *	
	 *		I/P 	RULE_50 Value : "BRE005000"
	 * 		O/P 	Key: RULE_51 Value : "BRE005000"
	 * @param line
	 * @return
	 */
	private String[] normalize(String line) {
		String keyVal[]=new String[2];
		if(isConstantDeclarationPattern(line)) {
			keyVal=line.strip().replace(";", "").split("=");
		} else if (isErrorCode_EnumPattern(line)) {
			keyVal=line.strip().replace("(","=").replace("),", "").replace(");", "").split("=");
			System.out.println("POST REPLACE "+line);
		
		}
			return keyVal;
	}
	
	
	private void assessFilesForErrorCodeNErrorMsg() {
		//readFilesFromSourceFolder

		try {
			Files.walk(Paths.get(sourceFolder)).filter(path -> (!path.toFile().isDirectory() && path.toString().endsWith(".java")))
			.parallel()
			.forEach(filePath -> assessFile(filePath));

		} catch (IOException e) {
			System.out.println("Excetion in load RuleError Code map");
			e.printStackTrace();
		}

	}

	//RuleToErrorCodeMapping.RULE_50.getErrorCode(), String.format(ErrorCodeConstants.
//// ErrorLoggingUtility.populateWarningCollection(errorCollection, RuleToErrorCodeMapping.RULE_50.getErrorCode(), 
	//String.format(ErrorCodeConstants.RULE_50_ERROR_MESSAGE, requestProvider.getMasterProviderId()), ErrorLevel.ORANGE, !StringUtils.isEmpty(requestProvider.getMasterProviderId()) ? 
	//Long.valueOf(requestProvider.getMasterProviderId()) : null, null, address,methodName);	
	private void assessFile(Path path) {
		System.out.println("########### File "+path.toFile().getName());
		Map<String, String> code_msg = null;
		try {
			code_msg = Files.lines(path).filter(line ->
						{
							if(line.contains(RuleToErrorCodeMapping_CONST) && line.contains(ErrorCodeConstants_CONST)) {
								return true;
							} else
								return false;
						}
					).map(line -> errorCodeNMsg(line)
						)
					.collect(Collectors.toMap(keyVal -> keyVal[0], 
							keyVal -> keyVal[0],
							(val1,val2) ->{
								System.out.println("DUPLICATE val1 "+val1+" val2 "+val2);
								return val1;
							}
							
							));

			System.out.println("Code VS Messages");
			code_msg.entrySet().forEach(entry -> System.out.println("Key: " + entry.getKey() + " Value : " + entry.getValue()));
			
		
			
		} catch (IOException e) {

			e.printStackTrace();
		}

		
	}
	
	
	private String[] errorCodeNMsg(String line) {
		String rule_key =line.substring(line.indexOf(RuleToErrorCodeMapping_CONST)+RuleToErrorCodeMapping_CONST.length(),line.indexOf(".getErrorCode()"));
		
		String errorCode =keyValMap.get(rule_key);
		String errorMessageKey=rule_key+"_ERROR_MESSAGE";
		System.out.println("Rule KEY "+rule_key +" ErrorCode: "+errorCode+" errorMessageKey: "+errorMessageKey);
		String errorMessage =keyValMap.get(errorMessageKey);
		
		return new String[] {errorCode,errorMessage};
	}
	
}
