# Flickr_crawler
Netbeans project of a java web crawler for the Flickr social network

#About this project 

Project name: FlickrCrawler
Architecture: Restfull application
Programming language: java 
Structuring and output format: json
Application server: Apache Tomcat

A java wrapper for the ‘Flickr’ API. Flickr enhances Picture galleries with social networking, chat, groups, and photo ratings.
With FlickCrawler images, urls and metadata are stored for further process.
The process is initiated by posting (POST request) a request to the Tomcat using a rest client (i.e. Advanced Rest Client for Google Chrome browser) followed by the .json file containing the request payload. The result of the request is provided as a server response and is also stored locally.

#Users - REST calls 

POST 
http://localhost:8084/FlickrCrawler/resources/crawl

Content-Type: "application/json"

Payload
{
"flickr": {
	"apiKey": "yourApiKey",
	"tags":"tags_to_search_for",
	"tag_mode":"any/all",
	"text":"text_to_search_for",
	"min_upload_date":"yyyy-MM-dd",//conversion needed... 
	"max_upload_date":"yyyy-MM-dd",
	"min_upload_taken":"yyyy-MM-dd",//conversion needed... 
	"max_upload_taken":"yyyy-MM-dd",
"sort":"date-posted-asc/date-posted-desc/date-taken-asc/ date-taken-desc/interestingness-desc/interestingness-asc/relevance",
	"media":"photos/videos",
	"geo":{
		//lefkas_island
		"lat":"38.7166666667",
		"lon":"20.65",
		"radius":"10"
	},
	"per_page":"500",//use the max as default
		"page":"1" //4000 results max per post...

},
"rabbit": {
	"host": "localhost",
	"queue": "RT_CRAWLER_IN_QUEUE"},
"max_results":"2000" //should be included...
}


•	The url defines where the service runs
•	The content-type defines what type is the request payload we are about to send to the application server

•	flickr object:

//USE NONE, SOME OR ALL OF THE PARAMS


"flickr":{

//full options
//https://www.flickr.com/services/api/flickr.photos.search.html

tags (Optional): {
    A comma-delimited list of tags. Photos with one or more of the tags listed will be returned. You can exclude results that match a term by prepending it with a - character.}

tag_mode (Optional):{
    Either 'any' for an OR combination of tags, or 'all' for an AND combination. Defaults to 'any' if not specified.}

text (Optional):{
    A free text search. Photos who's title, description or tags contain the text will be returned. You can exclude results that match a term by prepending it with a - character.}

min_upload_date & max_upload_date (Optional):{
Minimun & Maximum upload date. Photos with an upload date less than or equal to this value will be returned. The date can be in the form of a unix timestamp or mysql datetime.}

min_taken_date & max_taken_date (Optional):{
    Maximum taken date. Photos with an taken date less than or equal to this value will be returned. The date can be in the form of a mysql datetime or unix timestamp.}

sort (Optional):{
    The order in which to sort returned photos. Deafults to date-posted-desc (unless you are doing a radial geo query, in which case the default sorting is by ascending distance from the point specified). The possible values are: date-posted-asc, date-posted-desc, date-taken-asc, date-taken-desc, interestingness-desc, interestingness-asc, and relevance.}

media (Optional):{
    Filter results by media type. Possible values are all (default), photos or videos}

lat & lon(Optional):{
    A valid latitude, in decimal format, for doing radial geo queries.

    A valid longitude, in decimal format, for doing radial geo queries.

    Geo queries require some sort of limiting agent in order to prevent the database from crying. This is basically like the check against "parameterless searches" for queries without a geo component.

    A tag, for instance, is considered a limiting agent as are user defined min_date_taken and min_date_upload parameters — If no limiting factor is passed we return only photos added in the last 12 hours (though we may extend the limit in the future).}
 
radius (Optional):{
    A valid radius used for geo queries, greater than zero and less than 20 miles (or 32 kilometers), for use with point-based geo queries. The default value is 5 (km).}

extras (Optional-NECESSARY):{
    A comma-delimited list of extra information to fetch for each returned record. Currently supported fields are: description, license, date_upload, date_taken, owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media, path_alias, url_sq, url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o}

// Extras is embeded in the code...
PREFIX_EXTRAS = "&extras=description%2Clicense%2Cdate_upload%2Cdate_taken%2Cowner_name%2Coriginal_format%2Clast_update%2Cgeo%2Ctags%2Curl_m%2Curl_n%2Curl_z%2Curl_c%2Curl_l%2Curl_o%2Cviews%2Cmedia";

per_page (Optional):{
    Number of photos to return per page. If this argument is omitted, it defaults to 100. The maximum allowed value is 500.}

page (Optional):{
    The page of results to return. If this argument is omitted, it defaults to 1.}

 }

The server returns a json response containing the operation output.
	 

#Developers --class explanation

Package: gr.iti.dimsam.flickrcrawl.impl

Contains the java classes that obtain and process the json data Produces the responses and stores images in the local directory.

FlickrCrawl.java methods documentation

The output is a server response in json format containing information about the operation, and if information is processed correctly  RabbitMQ will have messages in the specified queue.

parseOut

Responsible method calling the request and parsing the responses from the GET requests to the FLickr API. Returns a json object that contains the operation result. 

@param jsonObject 	The paylod of the initial POST request that the user provides and. defines the parameters to form the GET request to the Flickr API. 
@return 		The json object containing information about process status.
@throws IOException 	If an input or output exception occurred.
@throws Exception 		If an input or output exception occurred.

Package: gr.iti.dimsam.flickrcrawl.rest

FlickrCrawl_Rest.java methods documentation

The Jersey rest class

@POST
@Consumes("application/json")
@Produces("application/json")

postJson

The rest implementation for the crawler.
@param json 	The json object containing the payload for the Post request provided by the user.
@return json	The json object containing the result.
@throws Exception	if json object not provided to method 


Package: gr.iti.dimsam.complexrtcrawl.util

Package containing utility classes
GetRequest.java methods documentation

The class containing the implementation of the methods for the Get request to the API.
 
callGET 

Responsible for passing the URL for the GET request to the Flickr API. Returns the response back as String so that processing is initiated.

@param url		The url of the request.
@return 		The response of the GET request as String. 


Convert.java methods documentation

convertStreamToString

Responsible for parsing the inputstream created by the GET request to a String 

@param is 		The inputStream.
@return 		The String. 
@throws IOException 	If an input or output exception occurred.

convertWord

Responsible for substituting special characters within a String 
 
@param word		The String.
@return 		The  processed String. 


Logger

log & err

Logging and error messaging methods


#Problems met

3600 requests /hour …1 request/sec

Queries return at max 4000 results as specified by the API. Yet a lot more than 4000 results do come back…. Have to check for duplicates

Duplicate images had to be dealt with HashMap – HashSet  (HashMap was selected as we where looking for the those having been returned more than once)

Uncertain of how parameters in the reques work along (e.g. tags refer to tags, text refers to title, description, tags)

Photos and Videos show up as image urls in the response. So videos are images for Flick. No video url reference.

Original images are user protected and displayed at user’s will. (Also have a different secret than the rest of the set (secret_o)).

Lat/Lon search returns 250 results per request instead of 500 for tag/text requests.


#Future work


Try Catch statements surrounding all object parsing methods to prevent user from malformed and erroneous input…. (Restrict it from the UI).

Parse comments array and get specific objects instead of the whole array.


