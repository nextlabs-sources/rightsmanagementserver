package com.nextlabs.rms.services.util;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class XMLUtil {

    public static String getXMLStringFromFile(String fileName) {
        StringBuilder response = new StringBuilder("");
//        String rootPath = servlet.getRealPath(resourceName);
        File file = new File(fileName);

        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                response.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }
	
}
