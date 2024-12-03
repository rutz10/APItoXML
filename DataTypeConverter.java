public class DataTypeConverter {

    /**
     * Converts a value from API data type to XML data type.
     *
     * @param value        The value to convert.
     * @param apiDataType  The data type of the API field.
     * @param xmlDataType  The desired data type in XML.
     * @return The converted value as a String.
     * @throws IllegalArgumentException if conversion fails.
     */
    public static String convert(Object value, String apiDataType, String xmlDataType) throws IllegalArgumentException {
        if (value == null) return null;

        try {
            // First, ensure the value matches the API data type
            Object apiTypedValue = parseValue(value, apiDataType);

            // Then, convert to the XML data type
            return formatValue(apiTypedValue, xmlDataType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting value: " + value + " from " + apiDataType + " to " + xmlDataType, e);
        }
    }

    /**
     * Parses the value according to the API data type.
     */
    private static Object parseValue(Object value, String apiDataType) {
        switch (apiDataType.toLowerCase()) {
            case "string":
                return value.toString();
            case "integer":
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else {
                    return Integer.parseInt(value.toString());
                }
            case "float":
                if (value instanceof Number) {
                    return ((Number) value).floatValue();
                } else {
                    return Float.parseFloat(value.toString());
                }
            case "double":
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else {
                    return Double.parseDouble(value.toString());
                }
            case "boolean":
                if (value instanceof Boolean) {
                    return value;
                } else {
                    return Boolean.parseBoolean(value.toString());
                }
            default:
                return value.toString();
        }
    }

    /**
     * Formats the value according to the XML data type.
     */
    private static String formatValue(Object value, String xmlDataType) {
        switch (xmlDataType.toLowerCase()) {
            case "string":
                return value.toString();
            case "integer":
                if (value instanceof Number) {
                    return String.valueOf(((Number) value).intValue());
                } else {
                    return String.valueOf(Integer.parseInt(value.toString()));
                }
            case "float":
                if (value instanceof Number) {
                    return String.valueOf(((Number) value).floatValue());
                } else {
                    return String.valueOf(Float.parseFloat(value.toString()));
                }
            case "double":
                if (value instanceof Number) {
                    return String.valueOf(((Number) value).doubleValue());
                } else {
                    return String.valueOf(Double.parseDouble(value.toString()));
                }
            case "boolean":
                if (value instanceof Boolean) {
                    return String.valueOf(value);
                } else {
                    return String.valueOf(Boolean.parseBoolean(value.toString()));
                }
            default:
                return value.toString();
        }
    }
}
