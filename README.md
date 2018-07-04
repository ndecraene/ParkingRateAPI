#Parking Rate Calculator API

This is an API which calculates a parking rate for a specified datetime range, given a list of rates.
The API only supports JSON over HTTP.

###Endpoints

####"/rates"

##### PUT

 - The PUT verb takes a request body of that is a list of ParkingRates and stores them to be used to calculate any get requests.
 - This request is idempotent and will overwrite any previous requests to PUT
 
 REQUEST
 
 - The request body must be a ParkingRateListWrapper, or an object with a list of ParkingRates in it
 - Each rate object must never overlap
 - Each rate object has three fields:
    - **"days"** - String, a list of the days of the week, formatted to comma delimited first three or four letters of each day.
    "mon,tues" or "mond,tue" for example
    - **"times"** - String, a range of hours/minutes, formatted "hhmm-hhmm".
    - **"price"** - Integer, the price for that rate, formatted "dollarscents". Example: $15.00 would be 1500
    
    Example of a request body:
    
    ```json
        {
          "rates": [
            {
              "days": "mon,tues,wed,thurs,fri",
              "times": "0600-1800",
              "price": 1500
            },
            {
              "days": "sat,sun",
              "times": "0600-2000",
              "price": 2000
            }
          ]
        }
    ```
 
 
##### GET
 - The GET verb is used to get a rate, given a date time range.
 
 REQUEST
 - This request will always need two parameters "startTime" and "endTime", which are DateTimes in ISO format
 
 RESPONSE
 - The request returns a single integer, which will be the dollars and cents of the rate. 
 For example, if a rate is $15.00, then the return value would be 1500.
 - If there is no rate for the range given, or if the range is not entirely encapsulated by a rate, then the service will return a 503, or service unavailable.