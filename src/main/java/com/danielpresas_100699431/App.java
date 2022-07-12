package com.danielpresas_100699431;

import java.io.*;
import java.net.URL;
import java.nio.charset.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.apache.commons.csv.*;

public class App {
    public static void main(final String[] args) {
        var csvPath = App.class.getResource("airline_safety.csv");
        var csvDirectory = csvPath.getPath().substring(0, csvPath.getPath().lastIndexOf("/"));

        var convertedXmlFile = new File(csvDirectory + "/converted_airline_safety.xml");
        var summaryXmlFile = new File(csvDirectory + "/airline_summary_statistic.xml");

        processTheData(csvPath, convertedXmlFile);
        summarizeTheData(csvPath, summaryXmlFile);
        createAChartUsingJavaFX();
    }

    private static void processTheData(URL inCsvPath, File outXmlFile) {
        try {
            var oldCsvFormatBuilder = CSVFormat.DEFAULT.builder().setHeader();
            var oldCsvParser = CSVParser.parse(inCsvPath, Charset.defaultCharset(), oldCsvFormatBuilder.build());
            var oldRecords = oldCsvParser.getRecords();

            var newCsvHeader = new ArrayList<String>(oldCsvParser.getHeaderNames());
            newCsvHeader.add("total_incidents");
            var newCsvFormatBuilder = CSVFormat.DEFAULT.builder().setHeader(newCsvHeader.toArray(new String[0]));
            var newCsvOutStream = new ByteArrayOutputStream();
            var newCsvPrinter = new CSVPrinter(new OutputStreamWriter(newCsvOutStream), newCsvFormatBuilder.build());

            for (var record : oldRecords) {
                var r = new ArrayList<String>(record.toList());
                r.add(
                    String.valueOf(
                        // Column 2 = incidents 85-99,    Column 5 = incidents 00-14
                        Integer.parseInt(record.get(2)) + Integer.parseInt(record.get(5))
                    )
                );
                newCsvPrinter.printRecord(r.toArray());
            }
            newCsvPrinter.flush();
            newCsvPrinter.close();

            var newCsvInStream = new ByteArrayInputStream(newCsvOutStream.toByteArray());
            var newCsvParser = CSVParser.parse(newCsvInStream, Charset.defaultCharset(), newCsvFormatBuilder.build());
            var newRecords = newCsvParser.getRecords();
            newRecords.remove(0);  // Get rid of the header row

            var xmlTransformer = TransformerFactory.newInstance().newTransformer();
            xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xmlTransformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            var xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            xmlDocument.setXmlStandalone(true);
            var xmlDomSource = new DOMSource(xmlDocument);
            var xmlResult = new StreamResult(outXmlFile);

            var xmlRoot = xmlDocument.createElement("airline_safety");
            xmlDocument.appendChild(xmlRoot);
            for(var record : newRecords) {
                var airline = xmlDocument.createElement(newCsvParser.getHeaderNames().get(0));
                xmlRoot.appendChild(airline);
                var airlineName = xmlDocument.createAttribute("name");
                airline.setAttributeNode(airlineName);
                airlineName.setValue(record.get(0));

                for (int i = 1; i < record.size(); ++i) {
                    var column = xmlDocument.createElement(newCsvParser.getHeaderNames().get(i));
                    column.appendChild(xmlDocument.createTextNode(record.get(i)));
                    airline.appendChild(column);
                }
            }

            xmlDocument.normalize();
            xmlTransformer.transform(xmlDomSource, xmlResult);
        }
        catch(Exception e) {
            System.out.println("-----\n" + e.toString() + "\n-----");
            e.printStackTrace();
        }
    }

    private static void summarizeTheData(URL inCsvPath, File outXmlFile) {
        try {
            var csvFormatBuilder = CSVFormat.DEFAULT.builder().setHeader();
            var csvParser = CSVParser.parse(inCsvPath, Charset.defaultCharset(), csvFormatBuilder.build());
            var records = csvParser.getRecords();

            var xmlTransformer = TransformerFactory.newInstance().newTransformer();
            xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xmlTransformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            var xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            xmlDocument.setXmlStandalone(true);
            var xmlDomSource = new DOMSource(xmlDocument);
            // var xmlResult = new StreamResult(System.out);
            var xmlResult = new StreamResult(outXmlFile);

            var xmlRoot = xmlDocument.createElement("summary");
            xmlDocument.appendChild(xmlRoot);
            for (int i = 1; i < csvParser.getHeaderNames().size(); ++i) {
                var max = Long.MIN_VALUE;
                var min = Long.MAX_VALUE;
                var accumulator = 0;
                for(var r : records) {
                    var val = Long.parseLong(r.get(i));
                    accumulator += val;
                    if(val > max) { max = val; }
                    if(val < min) { min = val; }
                }
                var avg = (float)accumulator / records.size();

                var stat = xmlDocument.createElement("stat");
                xmlRoot.appendChild(stat);
                var statName = xmlDocument.createAttribute("name");
                stat.setAttributeNode(statName);
                statName.setValue(csvParser.getHeaderNames().get(i));

                var minNode = xmlDocument.createElement("min"); stat.appendChild(minNode);
                var maxNode = xmlDocument.createElement("max"); stat.appendChild(maxNode);
                var avgNode = xmlDocument.createElement("avg"); stat.appendChild(avgNode);

                minNode.setTextContent(String.format("%,d", min));
                maxNode.setTextContent(String.format("%,d", max));
                avgNode.setTextContent(String.format("%,.2f", avg));
            }

            {
                var accumulator = 0;
                for(var r : records) {
                    var val = Long.parseLong(r.get(2));  // Column 2 = incidents 85-99
                    accumulator += val;
                }
                var avg = (float)accumulator / records.size();

                var stat = xmlDocument.createElement("stat");
                xmlRoot.appendChild(stat);
                var statName = xmlDocument.createAttribute("name");
                stat.setAttributeNode(statName);
                statName.setValue("avg_incidents_85_99");

                var minNode = xmlDocument.createElement("min"); stat.appendChild(minNode);
                var maxNode = xmlDocument.createElement("max"); stat.appendChild(maxNode);
                var avgNode = xmlDocument.createElement("avg"); stat.appendChild(avgNode);

                minNode.setTextContent("");
                maxNode.setTextContent("");
                avgNode.setTextContent(String.format("%,.2f", avg));
            }

            {
                var accumulator = 0;
                for(var r : records) {
                    var val = Long.parseLong(r.get(5));  // Column 5 = incidents 00-14
                    accumulator += val;
                }
                var avg = (float)accumulator / records.size();

                var stat = xmlDocument.createElement("stat");
                xmlRoot.appendChild(stat);
                var statName = xmlDocument.createAttribute("name");
                stat.setAttributeNode(statName);
                statName.setValue("avg_incidents_00_14");

                var minNode = xmlDocument.createElement("min"); stat.appendChild(minNode);
                var maxNode = xmlDocument.createElement("max"); stat.appendChild(maxNode);
                var avgNode = xmlDocument.createElement("avg"); stat.appendChild(avgNode);

                minNode.setTextContent("");
                maxNode.setTextContent("");
                avgNode.setTextContent(String.format("%,.2f", avg));
            }

            xmlDocument.normalize();
            xmlTransformer.transform(xmlDomSource, xmlResult);
        }
        catch(Exception e) {
            System.out.println("-----\n" + e.toString() + "\n-----");
            e.printStackTrace();
        }
    }

    private static void createAChartUsingJavaFX() {
        JavaFXApp.main();
    }
}
