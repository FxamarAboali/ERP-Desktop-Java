package nfe;

import br.com.samuelweb.nfe.dom.ConfiguracoesNfe;
import br.com.samuelweb.nfe.exception.NfeException;
import br.com.samuelweb.nfe.util.SchemaUtil;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;

class Validar implements ErrorHandler {

    static final String STATUS = "status";
    static final String CONSULTA_XML = "consultaXml";
    static final String CONSULTA_CADASTRO = "br/inf/portalfiscal/nfe/schema/consCad";
    static final String ENVIO = "envio";
    static final String DIST_DFE = "destDfe";
    static final String INUTILIZACAO = "inutilizacao";
    static final String CANCELAR = "cancelar";
    static final String MANIFESTAR = "manifestar";
    static final String CCE = "br/inf/portalfiscal/nfe/schema/cce";
    static final String EPEC = "epec";
    static final String CONSULTA_RECIBO = "consultaRecibo";
    private static String xsd;
    private String listaComErrosDeValidacao;

    /**
     * Construtor privado
     */
    private Validar() {
        this.listaComErrosDeValidacao = "";
    }

    static String validaXml(ConfiguracoesNfe config, String xml, String tipo) throws NfeException {

        String errosValidacao;

        switch (tipo) {
            case STATUS:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.STATUS;
                break;
            case ENVIO:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.ENVIO;
                break;
            case CONSULTA_XML:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.CONSULTA_XML;
                break;
            case CONSULTA_CADASTRO:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.CONSULTA_CADASTRO;
                break;
            case DIST_DFE:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.DIST_DFE;
                break;
            case INUTILIZACAO:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.INUTILIZACAO;
                break;
            case CANCELAR:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.CANCELAR;
                break;
            case EPEC:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.EPEC;
                break;
            case CCE:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.CCE;
                break;
            case MANIFESTAR:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.MANIFESTAR;
                break;
            case CONSULTA_RECIBO:
                xsd = config.getPastaSchemas() + "/" + SchemaUtil.CONSULTA_RECIBO;
                break;
            default:
                break;
        }

        if (!new File(xsd).exists()) {
            throw new NfeException("Schema Nfe n??o Localizado: " + xsd);
        }

        Validar validar = new Validar();

        errosValidacao = validar.validateXml(xml, xsd);

        return errosValidacao;
    }

    private String validateXml(String xml, String xsd) throws NfeException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", xsd);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(this);
        } catch (ParserConfigurationException ex) {
            throw new NfeException(ex.getMessage());
        }

        try {
            builder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception ex) {
            throw new NfeException(ex.toString());
        }

        return this.getListaComErrosDeValidacao();
    }

    public void error(SAXParseException exception) {

        if (isError(exception)) {
            listaComErrosDeValidacao += tratamentoRetorno(exception.getMessage());
        }
    }

    public void fatalError(SAXParseException exception) {

        listaComErrosDeValidacao += tratamentoRetorno(exception.getMessage());
    }

    public void warning(SAXParseException exception) {

        listaComErrosDeValidacao += tratamentoRetorno(exception.getMessage());
    }

    private String tratamentoRetorno(String message) {

        message = message.replaceAll("cvc-type.3.1.3:", "-");
        message = message.replaceAll("cvc-attribute.3:", "-");
        message = message.replaceAll("cvc-complex-type.2.4.a:", "-");
        message = message.replaceAll("cvc-complex-type.2.4.b:", "-");
        message = message.replaceAll("The value", "O valor");
        message = message.replaceAll("of element", "do campo");
        message = message.replaceAll("is not valid", "nao ?? valido");
        message = message.replaceAll("Invalid content was found starting with element", "Encontrado o campo");
        message = message.replaceAll("One of", "Campo(s)");
        message = message.replaceAll("is expected", "?? obrigatorio");
        message = message.replaceAll("\\{", "");
        message = message.replaceAll("\\}", "");
        message = message.replaceAll("\"", "");
        message = message.replaceAll("http://www.portalfiscal.inf.br/nfe:", "");
        return System.getProperty("line.separator") + message.trim();
    }

    private String getListaComErrosDeValidacao() {

        return listaComErrosDeValidacao;
    }

    private boolean isError(SAXParseException exception) {

        return !exception.getMessage().startsWith("cvc-enumeration-valid")
                && !exception.getMessage().startsWith("cvc-pattern-valid")
                && !exception.getMessage().startsWith("cvc-maxLength-valid")
                && !exception.getMessage().startsWith("cvc-datatype");
    }

}
