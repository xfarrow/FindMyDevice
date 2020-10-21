package de.nulide.findmydevice.data.io;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import de.nulide.findmydevice.data.WhiteList;

public class IO {

    public static Context context;
    private static final String whiteListFileName = "whitelist.json";

    public static void writeWhiteList(WhiteList wl) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(context.getFilesDir(), whiteListFileName);
        try {
            String json = mapper.writeValueAsString(wl);
            PrintWriter out = new PrintWriter(file);
            out.write(json);
            out.close();
        } catch (JsonProcessingException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static WhiteList readWhiteList() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(context.getFilesDir(), whiteListFileName);
        WhiteList whiteList = new WhiteList(context);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            try {
                String line;

                while ((line = br.readLine()) != null) {
                    json.append(line);
                    json.append('\n');
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            whiteList = mapper.readValue(json.toString(), WhiteList.class);
            whiteList.setContext(context);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return whiteList;
    }
}
