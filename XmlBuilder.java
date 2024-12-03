import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

public class XmlBuilder {

    /**
     * Converts the API response object to XML based on the provided mappings.
     *
     * @param mappings    List of XmlMapping defining the field mappings.
     * @param apiResponse The API response object (e.g., Company).
     * @return XML as a String.
     * @throws Exception if an error occurs during conversion.
     */
    public static String buildXml(List<ExcelMappingReader.XmlMapping> mappings, Object apiResponse) throws Exception {
        // Create a new XML Document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // Determine the root element from mappings
        String rootPath = mappings.get(0).getXmlPath().split("/")[0];
        Element rootElement = document.createElement(rootPath);
        document.appendChild(rootElement);

        // Recursively build the XML
        buildXmlElements(document, rootElement, mappings, apiResponse, rootPath);

        // Convert the Document to a String
        return transformDocumentToString(document);
    }

    /**
     * Recursively builds XML elements based on the mappings and the API response object.
     */
    private static void buildXmlElements(Document document, Element parentElement, List<ExcelMappingReader.XmlMapping> mappings, Object currentObject, String currentPath) throws Exception {
        if (currentObject == null) return;

        Class<?> clazz = currentObject.getClass();

        for (ExcelMappingReader.XmlMapping mapping : mappings) {
            String xmlPath = mapping.getXmlPath();
            if (!xmlPath.startsWith(currentPath)) continue; // Skip mappings not under the current path

            String relativePath = xmlPath.substring(currentPath.length());
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);

            if (relativePath.isEmpty()) continue; // Current object

            String[] pathParts = relativePath.split("/");

            // Traverse or create the necessary elements
            Element currentElement = parentElement;
            for (int i = 0; i < pathParts.length; i++) {
                String part = pathParts[i];
                boolean isLastPart = (i == pathParts.length - 1);

                // Check if the next part corresponds to a collection
                ExcelMappingReader.XmlMapping nextMapping = findMappingByPath(mappings, currentPath + "/" + part);
                boolean isCollection = nextMapping != null && nextMapping.getApiDataType().startsWith("List<");

                if (isCollection && !isLastPart) {
                    // Handle collection container
                    NodeList nodeList = currentElement.getElementsByTagName(part);
                    Element collectionElement;
                    if (nodeList.getLength() == 0) {
                        collectionElement = document.createElement(part);
                        currentElement.appendChild(collectionElement);
                    } else {
                        collectionElement = (Element) nodeList.item(0);
                    }
                    currentElement = collectionElement;
                } else if (isLastPart) {
                    // Leaf element: set the value
                    String apiFieldName = mapping.getApiFieldName();
                    Object value = getFieldValue(currentObject, apiFieldName);
                    String convertedValue = DataTypeConverter.convert(value, mapping.getApiDataType(), mapping.getXmlDataType());

                    if (convertedValue != null && !convertedValue.isEmpty()) {
                        Element element = document.createElement(mapping.getXmlElementName());
                        element.appendChild(document.createTextNode(convertedValue));
                        currentElement.appendChild(element);
                    }
                } else {
                    // Nested object
                    NodeList nodeList = currentElement.getElementsByTagName(part);
                    Element nestedElement;
                    if (nodeList.getLength() == 0) {
                        nestedElement = document.createElement(part);
                        currentElement.appendChild(nestedElement);
                    } else {
                        nestedElement = (Element) nodeList.item(0);
                    }

                    // Get the nested object
                    Object nestedObject = getFieldValue(currentObject, part);
                    if (nestedObject != null) {
                        currentElement = nestedElement;
                        buildXmlElements(document, currentElement, mappings, nestedObject, currentPath + "/" + part);
                    }
                }
            }
        }

        // Handle collections (Lists)
        for (ExcelMappingReader.XmlMapping mapping : mappings) {
            String xmlPath = mapping.getXmlPath();
            if (!xmlPath.startsWith(currentPath)) continue; // Skip mappings not under the current path

            String relativePath = xmlPath.substring(currentPath.length());
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);

            String[] pathParts = relativePath.split("/");
            if (pathParts.length < 2) continue; // Need at least one parent and one child

            // Check if current path is a collection
            String parentPath = String.join("/", java.util.Arrays.copyOf(pathParts, pathParts.length - 1));
            if (xmlPath.equals(parentPath)) continue; // Skip container

            // Check if the currentObject is a collection
            String apiDataType = mapping.getApiDataType();
            if (apiDataType.startsWith("List<")) {
                // Get the list
                Object fieldObject = getFieldValue(currentObject, mapping.getApiFieldName());
                if (fieldObject instanceof List<?>) {
                    List<?> list = (List<?>) fieldObject;
                    for (Object item : list) {
                        // Create a new element for each item
                        String elementName = mapping.getXmlElementName().replaceAll("List<|>", ""); // e.g., "branches" -> "branch"
                        Element itemElement = document.createElement(elementName);
                        parentElement.appendChild(itemElement);
                        buildXmlElements(document, itemElement, mappings, item, xmlPath);
                    }
                }
            }
        }
    }

    /**
     * Finds a mapping by XML path.
     */
    private static ExcelMappingReader.XmlMapping findMappingByPath(List<ExcelMappingReader.XmlMapping> mappings, String path) {
        for (ExcelMappingReader.XmlMapping mapping : mappings) {
            if (mapping.getXmlPath().equals(path)) {
                return mapping;
            }
        }
        return null;
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
