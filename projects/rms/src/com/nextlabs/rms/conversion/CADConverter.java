package com.nextlabs.rms.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;

public class CADConverter implements IFileConverter {

    private static Logger logger = Logger.getLogger(CADConverter.class);

    private static final String LICENSE_KEY = "4DbvBSRbxAU2xEaM0CE6AuMH1DUzATbovUuK2uMY0xnw3CQ5j7DFjE3xBiuT8Vj32DJlCuyj1U7mvbDFCTrv9Ubb2yBz9EJk1EuK0E2OzSZo4xM5xTJ0zReKAEuUDvbc1hZ0Aw7vzQR$9xaG6gjp2if41TaK7Trx2DZu0heY5CUL8EaZBzeM0hJw0eYI9eMPAze2vEUV6gFk0faPCzbz7QY5vRVbDyzm0Cv$5yJ5CyjE7SNn9UA3C7tNj7Fd1CjlgrHFjERdCQLGj7DF3eN47StJj7F4BTVu2rTFjBY10Eq1xBe5vDI17TZp8DI68fi38uJm8UazwARk8TQ3j7F6wjiJwjqRDzq6AuEP8jiLCjb3wwe02zqzwyVx7QVaEfiL3S62AybpvBfpxhM8wBe6vRe1whRlxBa5vQJkwuI4xRfm8hY4xAQzvQQ87Ua6xDQ3xfnkxhY09fblwuZn8DNo9fjo8AU3xeY5xvi18va27TQ79hM58DRkwvi7wQM7xTMzxvezvARk8QY1whM6wANlvQE2vRe8xuY1wRe3wRizxuY6xDQzvQQ38Bm6xuNn9hY2wfiz8DM8xAQ2xeI89fnpwRfkxDI49eZkwfi47TI18hM37QQ3xBjowuZp8xYzvTU8wve87Re0xuE0xRfn7QQ8xTM2xfjl8hU18fa2vQVm9hY5vTRmwDY5wyblxfm8wfboxeNl8BnovBi8xTJnwDZkwBi3j7Fb1RI11ji6vANqAQJtCuY13Va5xVeMByFakbDF3Sn7D95Fj7Fk4TjrxDQ5xTRp5hIQ3yyIwFrr1SeK1SJ3xxQ_9TJq9ji_Bxb2ACeXxCMN1Be5CyQGASr14gvc5ifs3E3pAuQ2Biq3wiFxABb9CTr05iZ2xxVtBFjr9fi2xhY13Vb95CuVAy204iB7vTM5Dxb6xeIN2E7pzRY3wUzp2DRa0faJEij3AEmW5Si29gIW0xV79xq2zUIT1EeZCzjl2gUSCwqI3wRa3iZp8Vf6Bgjm9gUGDSN0AhMVwReR0C3cCQZ4EgAW1Czw4wqSzUF1BBfu2wVr2BjdDwja1SNb4UF$4gNt9UqMDffvvVjoxfmT9fQLxzmT4EByxTRw4SYL8Sb6BjiK3Ba62yBcDyb89Urq5guM3AQX2UI78Ta_4UVoEfe03UyJEeIW1gzoBzj91haXCiE7AviHxgiKxiVu6fnv5xR$EjmQ8xU6wy2W9SUS5wQUATR3BSU1EfQO5SmR0wJozQR93DJ6BBMI5y34EfQNxfQ_DfNw9veNweEZwgFbvEVkxvj2xDJuwfaW3jbvCgIWEhYLCEj23gjwDSe5AyJ2DjiJ5xI2CCUz8fi78wVrzS60xEEQwgvq4vbmj5";

    @Override
    public boolean convertFile(String inputPath, String destinationPath) throws RMSException {
        /* obtaining the file extension from destination path */
        int index = destinationPath.toLowerCase().lastIndexOf(".");
        String fileExtension = destinationPath.substring(index + 1, destinationPath.length());
        return executeHOOPSConverter(inputPath, destinationPath, "--output_" + fileExtension);
    }

    /**
     * Taken from Java 8 java.lang.Process and modified to add process parameter.
     * 
     * @param timeout
     * @param unit
     * @param process
     * @return exitValue of the process or null if it's timeout
     * @throws InterruptedException
     */
    private Integer waitFor(long timeout, TimeUnit unit, Process process) throws InterruptedException {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);
        do {
            try {
                return process.exitValue();
            } catch (IllegalThreadStateException ex) {
                if (rem > 0) {
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
                }
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return null;
    }

    public boolean executeHOOPSConverter(String inputPath, String destinationPath, String command) throws RMSException {
        ProcessBuilder pb = null;
        String binLocation = "";
        boolean conversionresult = false;
        try {
            logger.debug("Input Path" + inputPath + " Output Path " + destinationPath);
            String converterLocation = "";

            if (!GlobalConfigManager.getInstance().isUnix) {
                converterLocation = GlobalConfigManager.getInstance().getWinCadConverter();
            } else {
                binLocation = GlobalConfigManager.getInstance().getCadBinDir();
                converterLocation = GlobalConfigManager.getInstance().getUnixCadConverter();
            }
            File f = new File(converterLocation);
            if (!f.exists()) {
                logger.error("Converter does not exist in the specified path:" + converterLocation);
                throw new RMSException(RMSMessageHandler.getClientString("missingViewerPackageErr", "CAD Viewer",
                        FilenameUtils.getExtension(inputPath).toUpperCase()));
            }
            Process process = null;
            pb = new ProcessBuilder(converterLocation, "--input", inputPath, command, destinationPath, "--license",
                    LICENSE_KEY);
            if (binLocation.length() > 0) {
                File directory = new File(binLocation);
                pb.directory(directory);
            }
            process = pb.start();

            try {
                try (BufferedReader stdOut = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String s = null;
                    while ((s = stdOut.readLine()) != null) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Converter Output : " + s);
                        }
                    }
                }
                try (BufferedReader stdOut = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String s = null;
                    while ((s = stdOut.readLine()) != null) {
                        logger.error("Converter Output : " + s);
                    }
                }
                Integer exitValue = waitFor(7, TimeUnit.MINUTES, process);
                if (exitValue == null) {
                    logger.error(
                            "Terminating the converter since it failed to convert file within 7 minutes. Input path: "
                                    + inputPath);
                    process.destroy();
                    return false;
                }
                if (exitValue == 0) {
                    conversionresult = true;
                }
                logger.debug("Exit Value : " + exitValue + " for input path :" + inputPath);
            } finally {
                closeQuietly(process.getOutputStream());
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error while executing " + pb.command());
        }
        return conversionresult;
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                closeable = null;
            }
        }
    }
}