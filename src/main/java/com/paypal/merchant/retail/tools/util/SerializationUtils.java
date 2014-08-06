package com.paypal.merchant.retail.tools.util;

import com.paypal.merchant.retail.tools.exception.SerializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Paolo
 * Created on 4/17/14 1:42 PM
 */
public class SerializationUtils {
    private static Logger logger = Logger.getLogger(SerializationUtils.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private static XStream xstream = new XStream(new DomDriver());
    /**
     * Serialize an object to an XML file
     * @param obj Object of type T
     * @param filePath String
     * @param <T> Generic Type
     */
    public static <T> void serializeToXML(T obj, String filePath) throws SerializationException {
        if(obj == null) {
            logger.info("Object to serialize is null");
            return;
        }
        long start = System.currentTimeMillis();
        try {
            logger.info(String.format("Serializing object (%s) to XML file: %s",obj.getClass().getSimpleName(),filePath));
            String xml = xstream.toXML(obj);
            if(Paths.get(filePath).getParent() != null){
                Files.createDirectories(Paths.get(filePath).getParent());
            }
            Files.write(Paths.get(filePath), xml.getBytes());
        } catch (Exception ex) {
            logger.error("Failed to serialize object to XML file.", ex);
            throw new SerializationException("Failed to serialize object to XML file.", ex);
        } finally {
            logger.debug(String.format("serializeToXML elapsed: %s", System.currentTimeMillis() - start));
        }
    }

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
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            logger.info(String.format("Serializing object (%s) to JSON file: %s",obj.getClass().getSimpleName(),filePath));
            mapper.writeValue(file, obj);
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

    /**
     * Deserialize from an XML file to an object of type T
     * @param filePath String
     * @param <T> Generic Type
     * @return Object of type T
     */
    public static <T> T deserializeFromXML(Path filePath) throws SerializationException {
        try {
            return deserializeFromXML(Files.newInputStream(filePath));
        } catch (IOException ex) {
            logger.error("Failed to get xml string from Path from: " + filePath, ex);
            throw new SerializationException("Failed to deserialize object from XML file: " + filePath, ex);
        }
    }

    /**
     * Deserialize from an XML file to an object of type T
     * @param xmlInputStream InputStream
     * @param <T> Generic Type
     * @return Object of type T
     */
    public static <T> T deserializeFromXML(InputStream xmlInputStream) throws SerializationException {
        long start = System.currentTimeMillis();
        try {
            return (T)xstream.fromXML(xmlInputStream);
        } catch (Exception ex) {
            logger.error("Failed to deserialize object from XML", ex);
            throw new SerializationException("Failed to deserialize object from XML file", ex);
        } finally {
            if(xmlInputStream != null){
                try {
                    xmlInputStream .close();
                } catch (IOException e) {
                    logger.error("Failed to close InputStream");
                }
            }
            logger.debug(String.format("deserializeFromXML elapsed: %s", System.currentTimeMillis() - start));
        }
    }
}
