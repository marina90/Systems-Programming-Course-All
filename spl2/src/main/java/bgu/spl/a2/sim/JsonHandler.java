package bgu.spl.a2.sim;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import org.json.simple.parser.JSONParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Created by Marina.Izmailov on 12/28/2016.
 */
class JsonHandler {
     JSONObject jsonObject = new JSONObject();
     Warehouse storage = new Warehouse();
     ArrayList<WaveContainer> wavesContainer = new ArrayList<>();

    JsonHandler(String path) throws ParseException,FileNotFoundException {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(path));
            jsonObject = (JSONObject) obj;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }
    int getThreads(){
        return getInt(jsonObject,"threads");
    }
    void setPlans(){
        JSONArray plans = (JSONArray)jsonObject.get("plans");
        JSONObject[] planDetails = getJsonObjects(plans);
        for(JSONObject plan:planDetails) {
            String productName =  (String)plan.get("product");

            JSONArray toolsJson = (JSONArray) plan.get("tools");
            String[] toolsString = JsonArrayToStringArray(toolsJson);

            JSONArray partsJson = (JSONArray) plan.get("parts");
            String[] partsArray = JsonArrayToStringArray(partsJson);

            ManufactoringPlan newPlan = new ManufactoringPlan(productName,partsArray,toolsString);
            storage.addPlan(newPlan);
        }
    }

    private String[] JsonArrayToStringArray(JSONArray jsonArr) {
        String[] testArray = (String[])jsonArr.stream()
                .toArray(String[]::new);
        return testArray;
    }

    void setTools() {
        JSONArray tools = (JSONArray) jsonObject.get("tools");
        JSONObject[] waveDetail = getJsonObjects(tools);
        for (JSONObject toolDetails : waveDetail) {
            String toolName =(String)toolDetails.get("tool");
            int qtyToAdd = getInt(toolDetails,"qty");
            storage.addTool(toolName ,qtyToAdd);
        }
    }

    private int getInt(JSONObject jo,String fieldName) {
        long qty =(Long)jo.get(fieldName);
        return (int)qty;
    }

    void setWaves() {
        JSONArray waves = (JSONArray) jsonObject.get("waves");
        JSONArray[] realWaves = getJsonArrays(waves);
        wavesContainer.ensureCapacity(realWaves.length);
        for (JSONArray wave : realWaves) {
            //Waves is an inner array, lets split it
            JSONObject[] waveDetail = getJsonObjects(wave);
            WaveContainer currentWave = new WaveContainer(waveDetail.length);
            for (JSONObject OrderDetail: waveDetail) {
                String productName =  (String)OrderDetail.get("product");
                int orderSize = getInt(OrderDetail,"qty");
                long startId = (Long)OrderDetail.get("startId");
                currentWave.orders.add(
                        (new OrderContainer(
                        productName,
                        orderSize,
                        startId))
                );
                currentWave.numOrdersFinal+=orderSize;
            }
            wavesContainer.add(currentWave);
        }
    }

    private JSONObject[] getJsonObjects(JSONArray wave) {
        return (JSONObject[])wave.stream().toArray(JSONObject[]::new);
    }

    private JSONArray[] getJsonArrays(JSONArray waves) {
        return (JSONArray[])waves.stream().toArray(JSONArray[]::new);
    }

}
