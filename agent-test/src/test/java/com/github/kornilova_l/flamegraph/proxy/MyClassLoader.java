package com.github.kornilova_l.flamegraph.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classloader that does not have system classloader in chain of parents
 */
class MyClassLoader extends ClassLoader {
    @SuppressWarnings("FieldCanBeLocal")
    private final Path pathToBin = Paths.get("out", "test", "classes");

    MyClassLoader() {
        super(null);
    }


    /**
     * https://habrahabr.ru/post/104229/
     */
    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        if (className.equals("com.github.kornilova_l.flamegraph.proxy.Proxy") ||
                className.equals("com.github.kornilova_l.flamegraph.proxy.StartData")) {
            return ClassLoader.getSystemClassLoader().loadClass(className);
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
