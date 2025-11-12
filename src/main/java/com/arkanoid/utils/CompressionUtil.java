package com.arkanoid.utils;

import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for LZ4 Frame format compression and decompression.
 */
public class CompressionUtil {
    
    private static final int BUFFER_SIZE = 2048;

    public static byte[] compress(String data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            compressTo(data, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("LZ4 compression failed: " + e.getMessage(), e);
        }
    }

    public static String decompress(byte[] compressedData) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(compressedData)) {
            return decompressFrom(in);
        } catch (IOException e) {
            throw new RuntimeException("LZ4 decompression failed: " + e.getMessage(), e);
        }
    }


    public static void compressToFile(String data, File outputFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            compressTo(data, out);
        }
    }

    public static String decompressFromFile(File inputFile) throws IOException {
        try (FileInputStream in = new FileInputStream(inputFile)) {
            return decompressFrom(in);
        }
    }

    private static void compressTo(String data, OutputStream out) throws IOException {
        try (LZ4FrameOutputStream lz4Out = new LZ4FrameOutputStream(out)) {
            lz4Out.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String decompressFrom(InputStream in) throws IOException {
        try (LZ4FrameInputStream lz4In = new LZ4FrameInputStream(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = lz4In.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
