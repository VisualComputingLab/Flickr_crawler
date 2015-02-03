package gr.iti.vcl.flickrcrawl.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import gr.iti.vcl.flickrcrawl.util.Convert;
import gr.iti.vcl.flickrcrawl.util.GetRequests;
import gr.iti.vcl.flickrcrawl.util.Logger;
import gr.iti.vcl.flickrcrawl.util.RabbitMQHandling;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/*
 *
 * @author  Samaras Dimitris 
 * July 1st, 2014
 * dimitris.samaras@iti.gr
 * 
 */
public class FlickrCrawl {
    // search for image...TAGS AND TEXT
    // https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=e3ce9ef317991cc89d3e2484b1ba2b93&tags=mlan%2Cfashion%2Cmoda%2Cmodel&tag_mode=any&text=milan&min_upload_date=2013-01-01&max_upload_date=2013-12-31&min_taken_date=2013-01-01&max_taken_date=2013-12-31&sort=relevance&lat=38.7166666667&lon=20.65&radius=10&extras=description%2Clicense%2Cdate_upload%2Cdate_taken%2Cowner_name%2Coriginal_format%2Clast_update%2Cgeo%2Ctags%2Curl_m%2Curl_n%2Curl_z%2Curl_c%2Curl_l%2Curl_o%2Cviews%2Cmedia&per_page=10&page=1&format=json&nojsoncallback=1

    // search for image...GEO
    private static final String API_SITE = "https://api.flickr.com/services/rest/?method=";
    private static final String PREFIX_PHOTOS = "flickr.photos.search";
    private static final String PREFIX_COMMENTS = "flickr.photos.comments.getList";
    private static final String API_KEY = "&api_key=";
    //private static final String API_KEY_VAL = "3deabda9457f84512ba274984f8c3a61";
    private static final String PREFIX_TAGS = "&tags=";
    private static final String PREFIX_TAG_MODE = "&tag_mode="; // any (OR) all (AND)
    private static final String PREFIX_TEXT = "&text=";
    private static final String PREFIX_MIN_UPLOAD_DATE = "&min_upload_date=";
    private static final String PREFIX_MAX_UPLOAD_DATE = "&max_upload_date=";
    private static final String PREFIX_MIN_TAKEN_DATE = "&min_taken_date="; // will it always be there?
    private static final String PREFIX_MAX_TAKEN_DATE = "&max_taken_date=";
    private static final String PREFIX_SORT = "&sort="; // date-posted-asc, date-posted-desc, date-taken-asc, date-taken-desc, interestingness-desc, interestingness-asc, and relevance
    private static final String PREFIX_HAS_GEO = "&has_geo=1";
    private static final String PREFIX_LAT = "&lat=";
    private static final String PREFIX_LON = "&lon=";
    private static final String PREFIX_RADIUS = "&radius=";
    private static final String PREFIX_EXTRAS = "&extras=description%2Clicense%2Cdate_upload%2Cdate_taken%2Cowner_name%2Coriginal_format%2Clast_update%2Cgeo%2Ctags%2Curl_m%2Curl_n%2Curl_z%2Curl_c%2Curl_l%2Curl_o%2Cviews%2Cmedia";
    private static final String PREFIX_PAGE = "&page=";
    private static final String PREFIX_MEDIA = "&media=";
    private static final String PREFIX_PER_PAGE = "&per_page="; // not less than max
    private static final String PREFIX_PHOTO = "&photo_id=";
    private static final String PREFIX_FORMAT_JSON = "&format=json&nojsoncallback=1";
    public Connection connection = null;
    public Channel channel = null;

    public FlickrCrawl() {
    }

    @SuppressWarnings("empty-statement")
    //public JSONObject parseOut() throws Exception, IOException {
    public JSONObject parseOut(JSONObject jsonObject) throws Exception, IOException {
        System.out.println("Started crawling flickr");
        // Create the JSONObject to construct the response that will be saved to RabbitMQ
        JSONObject resultObject = new JSONObject();
        JSONObject outObj = null;
        // Create Array to be filled with the id values 
        //HashSet ids = new HashSet();
        HashMap<String, Integer> ids = new HashMap<String, Integer>();        //if date params are not defined
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date_now = new Date();
        String publishedBefore = formatter.format(date_now);
        String publishedAfter = "1900-01-01";


        try {      
            RabbitMQHandling rmqhandling = new RabbitMQHandling();

                // credential for FLickr configuration
                String apiKey_val = jsonObject.getJSONObject("flickr").getString("apiKey");
                String host = jsonObject.getJSONObject("rabbit").getString("host");
                String qName = jsonObject.getJSONObject("rabbit").getString("queue");
                rmqhandling.openRMQ(host, qName);
                String url = API_SITE + PREFIX_PHOTOS + API_KEY + apiKey_val;

                if (jsonObject.getJSONObject("flickr").has("tags")) {
                    String tags = jsonObject.getJSONObject("flickr").getString("tags");

                    if (tags == null || tags.isEmpty()) {
                        Logger.err("No tags given to explore, aborting");
                        resultObject.put("Status", "Error");
                        resultObject.put("Message", "No tags given");
                    }
                    tags = Convert.convertWord(tags);

                    String tag_mode = jsonObject.getJSONObject("flickr").optString("tag_mode", "any");

                    String tag_url = PREFIX_TAGS + tags + PREFIX_TAG_MODE + tag_mode;
                    url = url + tag_url;
                }

                if (jsonObject.getJSONObject("flickr").has("text")) {
                    String text = jsonObject.getJSONObject("flickr").getString("text");

                    if (text == null || text.isEmpty()) {
                        Logger.err("No text given to explore, aborting");
                        resultObject.put("Status", "Error");
                        resultObject.put("Message", "No text given");
                    }

                    text = Convert.convertWord(text);
                    String text_url = PREFIX_TEXT + text;
                    url = url + text_url;
                }

                if (jsonObject.getJSONObject("flickr").has("geo")) {
                    if (!(jsonObject.getJSONObject("flickr").getJSONObject("geo").has("lat")) || !(jsonObject.getJSONObject("flickr").getJSONObject("geo").has("lon"))) {
                        Logger.err("No coordinates given to explore, aborting");
                        resultObject.put("Status", "Error");
                        resultObject.put("Message", "No lat/lon given");
                    }
                    double lat = jsonObject.getJSONObject("flickr").getJSONObject("geo").getDouble("lat");
                    double lon = jsonObject.getJSONObject("flickr").getJSONObject("geo").getDouble("lon");

                    int radius = jsonObject.getJSONObject("flickr").getJSONObject("geo").optInt("radious", 5);

                    String geo_url = PREFIX_HAS_GEO + PREFIX_LAT + lat + PREFIX_LON + lon + PREFIX_RADIUS + radius;
                    url = url + geo_url;
                }

                String min_upload_date = jsonObject.getJSONObject("flickr").optString("min_upload_date", publishedAfter);
                String max_upload_date = jsonObject.getJSONObject("flickr").optString("max_upload_date", publishedBefore);
                String min_taken_date = jsonObject.getJSONObject("flickr").optString("min_taken_date", publishedAfter);
                String max_taken_date = jsonObject.getJSONObject("flickr").optString("max_taken_date", publishedBefore);

                String sort = jsonObject.getJSONObject("flickr").optString("sort", "date-posted-desc");
                String media = jsonObject.getJSONObject("flickr").optString("media", "all");

                int per_page = jsonObject.getJSONObject("flickr").optInt("per_page", 500);

                String rem_url = PREFIX_MIN_UPLOAD_DATE + min_upload_date + PREFIX_MAX_UPLOAD_DATE + max_upload_date + PREFIX_MIN_TAKEN_DATE + min_taken_date + PREFIX_MAX_TAKEN_DATE + max_taken_date + PREFIX_SORT + sort + PREFIX_MEDIA + media + PREFIX_PER_PAGE + per_page + PREFIX_EXTRAS;

                url = url + rem_url;
                int page = jsonObject.getJSONObject("flickr").optInt("page", 1);

                int max_results = jsonObject.optInt("max_results", 4000); // 4000 as defined by the flickr API
                int total = 1;
                do {
                    url = url + PREFIX_PAGE + page + PREFIX_FORMAT_JSON;

                    URL the_url = new URL(url);
                    System.out.println("The request URL: " + the_url.toString());

                    String rsp = GetRequests.callGET(the_url);
                    //System.out.println(rsp);
                    // Create the JSONObject to be parsed
                    JSONObject jobj = new JSONObject(rsp);
                    System.out.println("The object response: " + jobj);

                    //Iterate for next pages of the response
                    int pages = jobj.getJSONObject("photos").getInt("pages");

                    JSONArray photoArr = jobj.getJSONObject("photos").getJSONArray("photo");

                    for (int i = 0; i < photoArr.length(); i++) {
                        // get separate movie from main response
                        JSONObject postResp = new JSONObject(photoArr.getString(i));
                        //System.out.println("Resp: " + total + postResp);
                        String id = postResp.getString("id");
                        if (ids.containsKey(id)) {
                            ids.put(id, ids.get(id) + 1);
                        } else {
                            ids.put(id, 1);
                            //create response object...
                            outObj = new JSONObject();
                            outObj.put("id", id);
                            outObj.put("ownername", postResp.getString("ownername"));
                            outObj.put("owner", postResp.getString("owner"));
                            outObj.put("title", postResp.getString("title"));
                            outObj.put("tags", postResp.getString("tags"));
                            outObj.put("latitude", postResp.getString("latitude"));
                            outObj.put("longitude", postResp.getString("longitude"));
                            if (postResp.has("url_o")) {
                                outObj.put("img_url", postResp.getString("url_o"));
                            } else if (postResp.has("url_l")) {
                                outObj.put("img_url", postResp.getString("url_l"));
                            } else if (postResp.has("url_c")) {
                                outObj.put("img_url", postResp.getString("url_c"));
                            } else if (postResp.has("url_z")) {
                                outObj.put("img_url", postResp.getString("url_z"));
                            } else if (postResp.has("url_n")) {
                                outObj.put("img_url", postResp.getString("url_n"));
                            } else {
                                outObj.put("img_url", postResp.getString("url_m"));
                            }
                            //get photocomments...
                            // https://api.flickr.com/services/rest/?method=flickr.photos.comments.getList&api_key={apikey}&photo_id={photoid}&format=json&nojsoncallback=1
                            URL com_url = new URL(API_SITE + PREFIX_COMMENTS + API_KEY + apiKey_val + PREFIX_PHOTO + id + PREFIX_FORMAT_JSON);
                            String com_rsp = GetRequests.callGET(com_url);
                            JSONObject com_jobj = new JSONObject(com_rsp);
                            if (com_jobj.getJSONObject("comments").has("comment")) {

                                JSONArray comArr = com_jobj.getJSONObject("comments").getJSONArray("comment");
                                outObj.put("comments", comArr);
//                                for (int z = 0; i < comArr.length(); z++) {
//                                    JSONObject comResp = new JSONObject(comArr.getString(z));                                   
//                                }
                            } else {
                                outObj.put("comments", "no comments");
                            }
                            System.out.println("Resp: " + total + outObj.toString());
                            rmqhandling.writeToRMQ(outObj, qName);
                        }
                        total++;
                        if (total > max_results) {
                            break;
                        }
                    }
                    if (page < pages) {
                        page++;
                    } else {
                        break;
                    }
                } while (total < max_results);//max_results<total);
                resultObject.put("Status", "200");
                resultObject.put("Message", "OK, all done");
                rmqhandling.closeRMQ();

        } catch (JSONException e) {
            System.out.println("JSONException" + e);
            Logger.err("JSONException : " + e);
            resultObject.put("Status", "Error");
            resultObject.put("Message", "JSONException : " + e);
        }

        System.out.println("the size " + ids.size() + " the table " + ids);
        return resultObject;

    }
}
