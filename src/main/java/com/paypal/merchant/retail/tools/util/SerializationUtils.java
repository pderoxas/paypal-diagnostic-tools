package com.paypal.merchant.retail.tools.util;

import com.paypal.merchant.retail.tools.exception.SerializationException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.File;

/**
 * Created by Paolo
 * Created on 4/17/14 1:42 PM
 */
public class SerializationUtils {
    private static Logger logger = Logger.getLogger(SerializationUtils.class);
    private static ObjectMapper mapper = new ObjectMapper();



    /**
     * Serialize an object of type T to a JSON file
     * @param obj Object of type T
     * @param filePath String
     * @param <T> Generic Type
     */
    public static <T> void serializeToJSON(T obj, String filePath) throws SerializationException {
        if(obj == null) {
            logger.info("Object to serialize is null");
            return;
        }
        long start = System.currentTimeMillis();
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        try {
            logger.info(String.format("Serializing object (%s) to JSON file: %s",obj.getClass().getSimpleName(),filePath));
            mapper.writeValue(new File(filePath), obj);
        } catch (Exception ex) {
            logger.error("Failed to serialize object to JSON file.", ex);
            throw new SerializationException("Failed to serialize object to JSON file.", ex);
        } finally {
            logger.debug(String.format("serializeToJSON elapsed: %s", System.currentTimeMillis() - start));
        }
    }

    /**
     * Deserialize from a JSON file to an object of type T
     * @param filePath String
     * @param clazz The expected class
     * @param <T> Generic Type
     * @return Object of type T
     */
    public static <T> T deserializeFromJSON(Class<T> clazz, String filePath) throws SerializationException {
        long start = System.currentTimeMillis();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            logger.info("Deserializing object from JSON file: " + filePath);
            return mapper.readValue(new File(filePath), clazz);
        } catch (Exception ex) {
            logger.error("Failed to deserialize object from JSON file: " + filePath, ex);
            throw new SerializationException("Failed to deserialize object from JSON file: " + filePath, ex);
        } finally {
            logger.debug(String.format("deserializeFromJSON elapsed: %s", System.currentTimeMillis() - start));
        }
    }
}
