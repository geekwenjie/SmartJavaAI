package cn.smartjavaai.vision.utils;

import ai.djl.util.JsonUtils;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tensorflow synset utils
 * @author dwj
 */
public class TensorflowSynsetUtils {

    /**
     * 加载synset
     * @param synsetUrl
     * @return
     * @throws IOException
     */
    public static Map<Integer, String> loadSynset(URL synsetUrl) throws IOException {
        return loadSynset(synsetUrl.openStream());
    }


    /**
     * 加载synset
     * @param synsetPath
     * @return
     * @throws IOException
     */
    public static Map<Integer, String> loadSynset(Path synsetPath) throws IOException {
        return loadSynset(Files.newInputStream(synsetPath));
    }


    /**
     * 加载synset
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static Map<Integer, String> loadSynset(InputStream inputStream) throws IOException {
        Map<Integer, String> map = new ConcurrentHashMap<>();
        int maxId = 0;
        try (InputStream is = new BufferedInputStream(inputStream);
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("item ");
            while (scanner.hasNext()) {
                String content = scanner.next();
                content = content.replaceAll("(\"|\\d)\\n\\s", "$1,");
                Item item = JsonUtils.GSON.fromJson(content, Item.class);
                map.put(item.id, item.displayName);
                if (item.id > maxId) {
                    maxId = item.id;
                }
            }
        }
        return map;
    }

    private static final class Item {
        int id;

        @SerializedName("display_name")
        String displayName;
    }
}
