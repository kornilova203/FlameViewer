package com.github.kornilova_l.flamegraph.proxy;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Classloader that does not have system classloader in chain of parents
 */
class MyClassLoader extends URLClassLoader {
    @SuppressWarnings("FieldCanBeLocal")
    private final Path pathToBin = Paths.get("out", "test", "classes");

    MyClassLoader() {
        super(((URLClassLoader) Thread.currentThread()
                .getContextClassLoader()).getURLs(), null);
    }

    private static byte[] getClassFromJar(URL url, String className) {
        try {
            ZipFile zipFile = new ZipFile(url.getFile());
            JarInputStream jarFile = new JarInputStream(new FileInputStream(url.getFile()));
            ZipEntry jarEntry = jarFile.getNextEntry();

            while (jarEntry != null) {
                if (jarEntry.getName().endsWith(".class") &&
                        jarEntry.getName().contains(className.replace('.', '/'))) {
                    return IOUtils.toByteArray(zipFile.getInputStream(jarEntry));
                }
                jarEntry = jarFile.getNextJarEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * https://habrahabr.ru/post/104229/
     */
    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        if (className.contains("Proxy") || className.contains("StartData")) {
            for (URL url : getURLs()) {
                if (url.toString().contains("proxy.jar")) { // search only in proxy.jar
                    byte[] b = getClassFromJar(url, className);
                    if (b == null) {
                        throw new ClassNotFoundException("Cannot find class " + className + " in proxy.jar");
                    }
                    return defineClass(className, b, 0, b.length);
                }
            }
            throw new ClassNotFoundException("Cannot find proxy.jar");
        }
        try {
            byte b[] = fetchClassFromFS(pathToBin + "/" + className.replace('.', '/') + ".class");
            Class<?> clazz = defineClass(className, b, 0, b.length);
            System.out.println("Load class " + clazz);
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }

    }

    private byte[] fetchClassFromFS(String path) throws IOException {
        InputStream is = new FileInputStream(new File(path));

        // Get the size of the file
        long length = new File(path).length();

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + path);
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;

    }
}
