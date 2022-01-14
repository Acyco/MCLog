package cn.acyco.mclog.config;

import cn.acyco.mclog.core.MCLogCore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * \
 *
 * @author Acyco
 * @create 2022-01-01 20:41
 * @url https://acyco.cn
 */
public class Config {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(SimpleDateFormat.class, new TypeAdapter<SimpleDateFormat>() {
                @Override
                public void write(JsonWriter out, SimpleDateFormat value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else
                        out.value(value.toPattern());
                }
                @Override
                public SimpleDateFormat read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    } else {
                        return new SimpleDateFormat(in.nextString());
                    }
                }


            })
            .registerTypeAdapter(File.class, new TypeAdapter<File>() {
                @Override
                public void write(JsonWriter out, File value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else
                        out.value(value.getPath());
                }

                @Override
                public File read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    } else {
                        return new File(in.nextString());
                    }

                }
            }).excludeFieldsWithModifiers(Modifier.PROTECTED).setPrettyPrinting().create();

    private ConfigData configData;

    public Config(ConfigData configData) throws IOException {
        this.configData = configData;
        File configFile = MCLogCore.getPathFile("mclog.json");
        //不存在 就创建
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        if (configFile.exists()) {
            //配置文件存在
            //读取配置文件
            try (FileReader reader = new FileReader(configFile)){
                this.configData = gson.fromJson(reader, this.configData.getClass());
            }

        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String day = this.configData.startDay;
            if (day != null && day.equals("")) {
                this.configData.startDay = sdf.format(new Date());
                System.out.println("start : " + this.configData.startDay);
            }
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(this.configData, this.configData.getClass(), writer); //写入
        }

    }

    public static Config loadConfig() throws IOException {
        Config config = new Config(new ConfigData());
        return config;
    }


    public ConfigData getConfigData() {
        return configData;
    }
}
