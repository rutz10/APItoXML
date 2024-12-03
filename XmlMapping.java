public class XmlMapping {
    private String group;
    private String apiFieldName;
    private String apiDataType;
    private String xmlElementName;
    private String xmlDataType;
    private String xmlPath;

    // Constructor
    public XmlMapping(String group, String apiFieldName, String apiDataType, String xmlElementName, String xmlDataType, String xmlPath) {
        this.group = group;
        this.apiFieldName = apiFieldName;
        this.apiDataType = apiDataType;
        this.xmlElementName = xmlElementName;
        this.xmlDataType = xmlDataType;
        this.xmlPath = xmlPath;
    }

    // Getters
    public String getGroup() {
        return group;
    }

    public String getApiFieldName() {
        return apiFieldName;
    }

    public String getApiDataType() {
        return apiDataType;
    }

    public String getXmlElementName() {
        return xmlElementName;
    }

    public String getXmlDataType() {
        return xmlDataType;
    }

    public String getXmlPath() {
        return xmlPath;
    }
}
