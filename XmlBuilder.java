import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;

public class XmlBuilder {

    /**
     * Converts the API response object to XML based on the provided mappings.
     *
     * @param mappings    List of XmlMapping defining the field mappings.
     * @param apiResponse The API response object.
     * @return XML as a String.
     * @throws Exception if an error occurs during conversion.
     */
    public static String buildXml(List<ExcelMappingReader.XmlMapping> mappings, Object apiResponse) throws Exception {
        // Create a new XML Document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // Determine the root element from mappings (assume first mapping's XML Path root)
        String rootPath = getRootPath(mappings);
        String rootElementName = getLastPathSegment(rootPath);
        Element rootElement = document.createElement(rootElementName);
        document.appendChild(rootElement);

        // Group mappings by their "Group" column
        Map<String, List<ExcelMappingReader.XmlMapping>> groupMap = groupMappingsByGroup(mappings);

        // Iterate through each group to build XML
        for (Map.Entry<String, List<ExcelMappingReader.XmlMapping>> entry : groupMap.entrySet()) {
            String group = entry.getKey();
            List<ExcelMappingReader.XmlMapping> groupMappings = entry.getValue();

            switch (group) {
                case "Company":
                    handleGroup(document, rootElement, groupMappings, apiResponse);
                    break;
                case "Branch":
                    handleGroup(document, rootElement, groupMappings, apiResponse);
                    break;
                case "Team":
                    handleGroup(document, rootElement, groupMappings, apiResponse);
                    break;
                case "Member":
                    handleGroup(document, rootElement, groupMappings, apiResponse);
                    break;
                case "Technology":
                    handleGroup(document, rootElement, groupMappings, apiResponse);
                    break;
                case "Task":
                    handleGroup(document, rootElement, groupMappings, apiResponse);
                    break;
                case "Campaign":
                    handleGroup(document, rootElement, groupMappings, apiResponse);
                    break;
                default:
                    // Handle unknown groups or skip
                    break;
            }
        }

        // Convert the Document to a String
        return transformDocumentToString(document);
    }

    /**
     * Determines the root path from the first mapping.
     */
    private static String getRootPath(List<ExcelMappingReader.XmlMapping> mappings) {
        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("No mappings provided.");
        }
        return mappings.get(0).getXmlPath().split("/")[0];
    }

    /**
     * Gets the last segment of an XML path.
     */
    private static String getLastPathSegment(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Groups mappings by their "Group" column.
     */
    private static Map<String, List<ExcelMappingReader.XmlMapping>> groupMappingsByGroup(List<ExcelMappingReader.XmlMapping> mappings) {
        Map<String, List<ExcelMappingReader.XmlMapping>> grouped = new HashMap<>();
        for (ExcelMappingReader.XmlMapping mapping : mappings) {
            grouped.computeIfAbsent(mapping.getGroup(), k -> new ArrayList<>()).add(mapping);
        }
        return grouped;
    }

    /**
     * Handles the creation of XML elements for a specific group.
     */
    private static void handleGroup(Document document, Element rootElement, List<ExcelMappingReader.XmlMapping> groupMappings, Object apiResponse) throws Exception {
        for (ExcelMappingReader.XmlMapping mapping : groupMappings) {
            String xmlPath = mapping.getXmlPath();
            String xmlElementName = mapping.getXmlElementName();
            String apiFieldName = mapping.getApiFieldName();
            String apiDataType = mapping.getApiDataType();
            String xmlDataType = mapping.getXmlDataType();

            // Extract the object at the current XML Path
            Object currentObject = extractObject(apiResponse, xmlPath);

            if (currentObject == null) continue;

            // Check if the current path represents a collection
            if (apiDataType.startsWith("List<")) {
                List<?> list = (List<?>) currentObject;
                // Determine the singular form of the collection element
                String singularName = getSingularForm(getLastPathSegment(xmlPath));
                // Create parent container if necessary
                Element parentContainer = getOrCreateParentContainer(document, rootElement, xmlPath);
                for (Object item : list) {
                    Element itemElement = document.createElement(singularName);
                    parentContainer.appendChild(itemElement);
                    // Iterate through mappings within the group to set child elements
                    for (ExcelMappingReader.XmlMapping subMapping : groupMappings) {
                        if (!subMapping.getXmlPath().startsWith(xmlPath)) continue;
                        String subXmlPath = subMapping.getXmlPath();
                        String relativePath = subXmlPath.substring(xmlPath.length());
                        if (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1);
                        }
                        if (relativePath.contains("/")) {
                            // Nested path; handle accordingly (could require recursive logic)
                            // For simplicity, assume single-level nesting within the group
                            continue;
                        }
                        String subApiFieldName = subMapping.getApiFieldName();
                        String subXmlElementName = subMapping.getXmlElementName();
                        String subApiDataType = subMapping.getApiDataType();
                        String subXmlDataType = subMapping.getXmlDataType();

                        Object fieldValue = getFieldValue(item, subApiFieldName);
                        String convertedValue = DataTypeConverter.convert(fieldValue, subApiDataType, subXmlDataType);

                        if (convertedValue != null && !convertedValue.isEmpty()) {
                            Element element = document.createElement(subXmlElementName);
                            element.appendChild(document.createTextNode(convertedValue));
                            itemElement.appendChild(element);
                        }
                    }
                }
            } else {
                // Single object field
                String convertedValue = DataTypeConverter.convert(currentObject, apiDataType, xmlDataType);
                if (convertedValue != null && !convertedValue.isEmpty()) {
                    Element element = document.createElement(xmlElementName);
                    element.appendChild(document.createTextNode(convertedValue));
                    rootElement.appendChild(element);
                }
            }
        }
    }

    /**
     * Extracts the object from the API response based on the XML Path.
     */
    private static Object extractObject(Object apiResponse, String xmlPath) throws Exception {
        String[] pathSegments = xmlPath.split("/");
        Object currentObject = apiResponse;
        for (String segment : pathSegments) {
            if (segment.equals(getLastPathSegment(xmlPath))) continue; // Skip the last segment for field extraction
            currentObject = getFieldValue(currentObject, segment);
            if (currentObject == null) break;
        }
        return currentObject;
    }

    /**
     * Retrieves the value of a field from an object using reflection.
     */
    private static Object getFieldValue(Object obj, String fieldName) throws Exception {
        if (obj == null) return null;
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) {
            throw new NoSuchFieldException("Field '" + fieldName + "' not found in " + obj.getClass().getName());
        }
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * Recursively searches for a field in a class and its superclasses.
     */
    private static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null) return null;
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getField(clazz.getSuperclass(), fieldName);
        }
    }

    /**
     * Determines if a path represents a collection.
     */
    private static boolean isCollectionPath(String path) {
        // Simple heuristic: if the last segment is plural, it's a collection
        String lastSegment = getLastPathSegment(path);
        return lastSegment.endsWith("s");
    }

    /**
     * Converts plural to singular (simple heuristic).
     * For more complex scenarios, consider using a library like Apache Commons Lang's WordUtils.
     */
    private static String getSingularForm(String plural) {
        if (plural.endsWith("ies")) {
            return plural.substring(0, plural.length() - 3) + "y";
        } else if (plural.endsWith("s") && !plural.endsWith("ss")) {
            return plural.substring(0, plural.length() - 1);
        }
        return plural; // Return as-is if not identifiable
    }

    /**
     * Creates or retrieves the parent container element based on the XML Path.
     */
    private static Element getOrCreateParentContainer(Document document, Element rootElement, String xmlPath) throws Exception {
        String[] pathSegments = xmlPath.split("/");
        Element parentContainer = rootElement;
        for (String segment : pathSegments) {
            NodeList nodeList = parentContainer.getElementsByTagName(segment);
            if (nodeList.getLength() == 0) {
                Element newElement = document.createElement(segment);
                parentContainer.appendChild(newElement);
                parentContainer = newElement;
            } else {
                parentContainer = (Element) nodeList.item(0);
            }
        }
        return parentContainer;
    }

    /**
     * Transforms an XML Document to a formatted String.
     */
    private static String transformDocumentToString(Document document) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        // Pretty print the XML
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // Set XML declaration
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.getBuffer().toString();
    }
}
