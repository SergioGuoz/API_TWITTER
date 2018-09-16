/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pool.servicios;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pool.dao.Piscina;
import com.pool.modelo.Pool;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.ByteString;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;



/**
 *
 * @author Sergio
 */
@Path("/estado")
public class PoolService {
    
    private static List<Pool> lista = Piscina.getPools();
    Pool pool= new Pool(0,0);
    int notificar=0;
    int contador=2;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEstado(){
        String respuesta="";
        respuesta+=sendOrderArduino("1");
        respuesta+="\n";
        respuesta+=sendOrderArduinoSimple("0");
        try {
            respuesta+="FUNCIONANDO";
                    connectTwitter();
                } catch (TwitterException ex) {
                    respuesta+="ERROR";
                    Logger.getLogger(PoolService.class.getName()).log(Level.SEVERE, null, ex);
                }
        return Response.ok(respuesta).build();
    }
    
    @POST
    @Consumes({"application/json"})
    @Path("set")
    public Response setEstado(String content){
        System.out.println("********---******");
        System.out.println(content);
        System.out.println("------------------");
        JsonParser parser= new JsonParser();
        JsonObject obj=parser.parse(content).getAsJsonObject();
        int estado= obj.get("estado").getAsInt();
        System.out.println("El estado: "+estado);
        System.out.println("JSON; "+obj.toString());
        pool.setEstado(estado);
        if(pool.getEstado()==1){
                notificar=1;
                try {
                    connectTwitter();
                } catch (TwitterException ex) {
                    Logger.getLogger(PoolService.class.getName()).log(Level.SEVERE, null, ex);
                }
        }else{
            notificar=0;
        }
        return Response.ok(pool).build();
    }
    
    @POST
    @Consumes({"application/json"})
    @Path("orden")
    public Response setOrden(String content){
        JsonParser parser= new JsonParser();
        JsonObject obj=parser.parse(content).getAsJsonObject();
        int or= obj.get("orden").getAsInt();
        System.out.println("Orden: "+or);
        System.out.println("JSON; "+obj.toString());
        pool.setOrden(or);
        System.out.println(sendOrderArduino(Integer.toString(or)));
        return Response.ok(pool).build();
    }
        
    
    public static final okhttp3.MediaType JSON= okhttp3.MediaType.get("application/json; charset=utf-8");
            OkHttpClient client=new OkHttpClient();    
    
    public String sendOrderArduinoSimple(String ejecutar){
        String respuesta="Empty";
        try {                      
            String json="{\"orden\":"+ejecutar+"}";
            HttpUrl.Builder urlBuilder=HttpUrl.parse("http://go-api-facebook.appspot.com/var").newBuilder();
            urlBuilder.addQueryParameter("var", json);
                        String url=urlBuilder.build().toString();
            Request req= new Request.Builder()
                    .url(url)
                    .build();
            
            okhttp3.Response response=client.newCall(req).execute();
            respuesta=response.body().string();
            respuesta=req.toString();
        } catch (IOException ex) {
            Logger.getLogger(PoolService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return respuesta;
    }
    
    public String sendOrderArduino(String ejecutar){
        String respuesta="Empty";
        try {
            String json="{\"orden\":"+ejecutar+"}";
            URL url = new URL("http://go-api-facebook.appspot.com/var?var="+ejecutar);
            okhttp3.RequestBody rbody=RequestBody.create(JSON,json);
            Request req= new Request.Builder()
                    .url(url)
                    //.post(rbody)
                    .build();
            okhttp3.Response response=client.newCall(req).execute();
            respuesta=response.body().string();
            respuesta=req.toString();
        } catch (IOException ex) {
            Logger.getLogger(PoolService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return respuesta;
    }
    public void connectTwitter() throws TwitterException{
        ConfigurationBuilder configbuilder= new ConfigurationBuilder();
        configbuilder.setDebugEnabled(true).setOAuthConsumerKey("a0fpuJujorPJNCy6tvlnZdcoo")
                .setOAuthConsumerSecret("e1EFpSSi2LzczoPdOAtHTZkogd7pmQAdPi9pfP0wt0k6EoET8s")
                .setOAuthAccessToken("1035333064833073152-s4TDen2PU2Qzux3Al9qTpVbGElCks2")
                .setOAuthAccessTokenSecret("G5y16OynGFxb4Z7uEV9vZjxO5dOiMBPL7QSXEN5lLa9zX");
        TwitterFactory tf= new TwitterFactory(configbuilder.build());
        twitter4j.Twitter twitter=tf.getInstance();
        String message="PISCINA LISTA... [INGENIERIA_SISTEMAS_USAC] *"+contador+"*";
        Status writeTweet = twitter.updateStatus(message);
        contador++;
    }   
} 