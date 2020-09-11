package algtestprocess;

import static algtestprocess.JCinfohtml.*;
import static algtestprocess.TPMSupportTable.TPM2_ECC_CURVE_STR;
import static algtestprocess.TPMSupportTable.TPM2_ALG_ID_STR;
import static java.lang.System.out;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.File;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;

public class TPMRunTime {

    public static void beginRunTimeHTML(FileOutputStream file, String title) throws IOException {
        String toFile = "";
        toFile += "<html lang=\"en\">\n";
        toFile += " <head>\n";
        toFile += "\t<meta charset=\"utf-8\">\n" +
                    "\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"+
                    "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n\n";

        toFile += "\t<meta name=\"description\" content=\"The TPMAlgTest is a tool designed for automatic gathering various performance properties of Trusted Platform Modules. \">\n" +
                    "\t<meta name=\"author\" content=\"TPMAlgTest\">\n"  +
                    "\t<title>"+title+"</title>\n";

        toFile += "\t<link href=\"../dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n"
                + "<script type=\"text/javascript\" src=\"../dist/jquery-2.2.3.min.js\"></script>\n"
                + "\t<link href=\"../assets/css/ie10-viewport-bug-workaround.css\" rel=\"stylesheet\">\n"
                + "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"../dist/style.css\">\n";

        toFile += " </head>\n\n";
        toFile += " <body style=\"margin-top:50px; padding:20px\">\n\n";

        toFile += " \t<nav class=\"navbar navbar-inverse navbar-fixed-top\">\n\t\t<div class=\"container\">\n\t\t<script type=\"text/javascript\" src=\"../header-1.js\"></script>\n\t\t</div>\n\t</nav>\n\n";

        file.write(toFile.getBytes());
    }

    public static void endRunTimeHTML(FileOutputStream file) throws IOException {
        String toFile = "";
        toFile += "\t<script type=\"text/javascript\" src=\"../footer.js\"></script>\n"+
                "<a href=\"#\" class=\"back-to-top\"></a>" +
                "\t<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>\n" +
                "\t<script>window.jQuery || document.write('<script src=\"../assets/js/vendor/jquery.min.js\"><\\/script>')</script>\n" +
                "\t<script src=\"../dist/js/bootstrap.min.js\"></script>\n" +
                "\t<script src=\"../assets/js/ie10-viewport-bug-workaround.js\"></script>\n";

        toFile += " </body>\n";
        toFile += "</html>\n";
        file.write(toFile.getBytes());
    }

    private static void addCardInfo(FileOutputStream file, String name) throws IOException{
        StringBuilder toFile = new StringBuilder();
        toFile.append("\t<div class=\"container\">\n\t\t<div class=\"row\">\n");
        toFile.append("\t\t\t<h1>Run time results - "+name+"</h1>\n");
        toFile.append("\t\t\t<p>This file was generated by JCAlgTest tool</p>\n");
        file.write(toFile.toString().getBytes());
    }

    public static void tableGenerator(FileOutputStream output, BufferedReader reader) throws IOException {
        String html = "";
        String line;
        String[] fields;
        String command = "";
        boolean inTable = false;
        boolean createHeader = false;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("TPM2_")) { // new command
                command = line;
                if (inTable) {
                    html += "</table>\n</br>\n";
                }
                inTable = true;
                createHeader = true;
                html += "<h3 id=\"" + line + "\">" + line + "</h3>\n";
                html += "<table cellspacing='0'> <!-- cellspacing='0' is important, must stay -->\n\t<tr>";
            } else {
                fields = line.trim().split(";");
                if (fields.length < 2) continue;

                if (createHeader) {
                    for (int i = 0; i < fields.length; i += 2) {
                        html += "<th style=\"width: 330px;\">"+fields[i]+"</th>";
                    }
                    html += "<th></th><th>Operation average (ms/op)</th><th>Operation minimum (ms/op)</th><th>Operation maximum (ms/op)</th>";
                    html += "<th></th><th class=\"minor\">Iterations</th><th class=\"minor\">Successful</th><th class=\"minor\">Failed</th><th class=\"minor\">Error code</th></tr><!-- Table Header -->\n";
                    createHeader = false;
                }

                if (Arrays.asList(new String[] {
                        "TPM2_Create", "TPM2_RSA_Encrypt", "TPM2_RSA_Decrypt", "TPM2_Sign", "TPM2_VerifySignature"
                        }).contains(command)) {
                    /* Key parameters */
                    if (fields[1].contains("ECC")) {
                        int eccIndex = fields[1].indexOf("0x");
                        int curveId = Integer.parseInt(fields[1].substring(eccIndex + 2), 16);
                        fields[1] = fields[1].substring(0, eccIndex) + TPM2_ECC_CURVE_STR.get(curveId).substring(9); // skip TMP2_ECC_
                    } else if (fields[1].contains("SYMCIPHER") || fields[1].contains("KEYEDHASH")) {
                        int hexIndex = fields[1].indexOf("0x");
                        int algId = Integer.parseInt(fields[1].substring(hexIndex + 2, hexIndex + 6), 16);
                        fields[1] = fields[1].replace(fields[1].substring(hexIndex, hexIndex+6), TPM2_ALG_ID_STR.get(algId).substring(9));
                    }
                    if (!command.equals("TPM2_Create")) {
                        /* Scheme */
                        int algId = Integer.parseInt(fields[3].substring(2), 16);
                        fields[3] = TPM2_ALG_ID_STR.get(algId).substring(9);
                    }
                } else if (command.equals("TPM2_EncryptDecrypt")) {
                    int algId = Integer.parseInt(fields[1].substring(2), 16);
                    fields[1] = TPM2_ALG_ID_STR.get(algId).substring(9);
                    algId = Integer.parseInt(fields[5].substring(2), 16);
                    fields[5] = TPM2_ALG_ID_STR.get(algId).substring(9);
                } else if (command.equals("TPM2_Hash")) {
                    int algId = Integer.parseInt(fields[1].substring(2), 16);
                    fields[1] = TPM2_ALG_ID_STR.get(algId).substring(9);
                }

                html += "\t<tr>";
                for (int i = 1; i < fields.length; i += 2) {
                    html += "<td><b>" + fields[i] + "</b></td>";
                }
                html += "<td></td>";

                line = reader.readLine();
                assert line != null;
                assert line.startsWith("operation stats (ms/op):");
                fields = line.trim().split(";");
                assert fields.length >= 6;
                html += "<td style=\"font-size: 110%; font-weight: bold;\">" + Float.valueOf(fields[2]) + "</td>";
                html += "<td>" + Float.valueOf(fields[4]) + "</td>";
                html += "<td>" + Float.valueOf(fields[6]) + "</td>";

                line = reader.readLine();
                assert line != null;
                assert line.startsWith("operation info:");
                fields = line.trim().split(";");
                assert fields.length >= 6;
                html += "<td class=\"minor\"></td><td class=\"minor\">" + fields[2] + "</td>";
                html += "<td class=\"minor\">" + fields[4] + "</td>";
                html += "<td class=\"minor\">" + fields[6] + "</td>";
                html += "<td class=\"minor\">" + fields[8] + "</td>";
                html += "</tr>\n";
            }
        }
        if (inTable) {
            html += "</table>\n</br>\n";
        }
        output.write(html.getBytes());
    }

    static void writeDetails(FileOutputStream output, BufferedReader reader) throws IOException {
        String line;
        String[] fields;
        String html = "<div class=\"col-md-5 col-xs-5\">\n"
                    + "<h3>Test details</h3>\n";

        HashMap<String, String> infoMap = new HashMap<>();
        while ((line = reader.readLine()) != null && !line.equals("")) {
            fields = line.trim().split(";");
            if (fields.length > 1) {
                infoMap.put(fields[0], fields[1]);
            }
        }
        html += "<p>Execution date/time: <strong>" + infoMap.get("Execution date/time") + "</strong></p>\n";
        html += "<p>Manufacturer: <strong>" + infoMap.get("Manufacturer") + "</strong></p>\n";
        html += "<p>Vendor string: <strong>" + infoMap.get("Vendor string") + "</strong></p>\n";
        html += "<p>Firmware version: <strong>" + infoMap.get("Firmware version") + "</strong></p>\n";
        html += "<p>Algtest version: <strong>" + infoMap.get("Image tag") + "</strong></p>\n";

        html += "</div>";
        output.write(html.getBytes());
    }

    public static String generateRunTimeFile(String filePath) throws FileNotFoundException, IOException {
        File csvFile = new File(filePath);
        String resultsDir = csvFile.getParent() + "/run_time";
        new File(resultsDir).mkdirs();
        String csvFileName = csvFile.getName();
        String tpmName = csvFileName.substring(0, csvFileName.indexOf(".csv"));
        String htmlFileName = tpmName + ".html";

        FileOutputStream output = new FileOutputStream(resultsDir + "/" + htmlFileName);
        beginRunTimeHTML(output, "TPMAlgTest - Run time - " + tpmName);

        addCardInfo(output, tpmName.toString());
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        // TODO quicklinks
        writeDetails(output, reader);
        output.write("\t\t</div>\n\t\t<div class=\"row\">\n".getBytes());
        //topFunction(lines, file, linePosition);
        tableGenerator(output, reader);
        output.write("\t\t</div>\n\t</div>".getBytes());
        endRunTimeHTML(output);

        return tpmName.toString();
    }

    public static List<String> generateRunTimeFolder(String dir) throws IOException {
        List<String> files = listFilesForFolder(new File(dir));
        List<String> names = new ArrayList<>();
        Collections.sort(files);

        for(String filePath : files) {
            names.add(generateRunTimeFile(filePath));
        }

        return names;
    }

    public static void generateRunTimeMain(String dir, List<String> namesOfTpms) throws IOException {
        FileOutputStream page = new FileOutputStream(dir + "/run_time/execution-time.html");
        beginRunTimeHTML(page, "TPMAlgTest - Algortithm execution time");
        StringBuilder toFile = new StringBuilder();
        toFile.append("\t<div class=\"container\">\n");
        toFile.append("\t\t<div class=\"row\">\n");
        toFile.append("\t\t\t<div class=\"col-md-7 col-xs-7\">\n");
        toFile.append("\t\t<h1>Algorithm execution time</h1>\n");
        toFile.append("\t\t<p>HTML page is generated from CSV files for each TPM.</p>\n");
        toFile.append("\t\t<p>The page consists of 5 tables presenting each group of tested commands.</p>\n");
        toFile.append("\t\t<p><strong>Every row of each table contains the name of measured function, time of execution</strong> (average, minimum, maximum) and a number of total, successful and failed test runs. </p><br>\n");
        toFile.append("\t\t\t</div>\n");
        toFile.append("\t\t\t</div>\n");
        toFile.append("\t\t<div>\n");
        toFile.append("\t\t<h3>List of tested TPMs: </h3>\n");
        toFile.append("\t\t<ul class=\"list-group\">\n");
        for(String name : namesOfTpms){
            toFile.append("\t\t\t<li class=\"list-group-item\"><a href=\""+name+".html\">"+name+"</a></li>\n");
        }
        toFile.append("\t\t</ul>\n");

        toFile.append("\t\t</div>\n");
        toFile.append("\t</div>\n");
        page.write(toFile.toString().getBytes());

        endRunTimeHTML(page);
        page.close();
    }

    public static void runRunTime(String dir) throws IOException {
        List<String> names = generateRunTimeFolder(dir);
        generateRunTimeMain(dir, names);
    }
}