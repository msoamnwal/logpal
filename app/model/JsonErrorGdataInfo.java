package model;

import com.google.api.client.util.Key;

/*
 Class to parse google API error response:
 {
 "error": {
  "errors": [
   {
    "message": "No tokens exist for the specified issue domain"
   }
  ],
  "code": 500,
  "message": "No tokens exist for the specified issue domain"
 }
}
*/
public class JsonErrorGdataInfo {
	@Key public String[] errors;
	@Key public Integer code;
	@Key public String message;
}
